package com.symphony.hackathon.command;

import clients.SymBotClient;
import com.symphony.hackathon.model.DistributionList;
import com.symphony.hackathon.repository.DistributionListRepository;
import com.symphony.hackathon.service.TemplatesService;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.UserInfo;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import javax.ws.rs.core.NoContentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ManageDistributionListsHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final TemplatesService templatesService;
    private final DistributionListRepository distributionListRepository;
    private final ManageDistributionListsFormHandler manageDistributionListsFormHandler;

    public ManageDistributionListsHandler(SymBotClient bot,
                                          TemplatesService templatesService,
                                          DistributionListRepository distributionListRepository,
                                          ManageDistributionListsFormHandler manageDistributionListsFormHandler) {
        this.bot = bot;
        this.templatesService = templatesService;
        this.distributionListRepository = distributionListRepository;
        this.manageDistributionListsFormHandler = manageDistributionListsFormHandler;
    }

    @SuppressWarnings("unchecked")
    public void execute(User user, SymphonyElementsAction elementsAction) {
        Map<String, Object> formValues = elementsAction.getFormValues();
        String action = elementsAction.getFormValues().get("action").toString();
        DistributionList distributionList = null;

        if (action.equals("add-distribution-list")) {
            distributionList = DistributionList.builder().build();
        } else if (action.equals("cancel-distribution-list")) {
            manageDistributionListsFormHandler.execute(user, elementsAction);
            return;
        } else if (action.equals("save-distribution-list")) {
            long id;
            if (formValues.get("id").toString().equals("0")) {
                id = distributionListRepository.count() + 1;
            } else {
                id = Long.parseLong(formValues.get("id").toString());
                distributionList = distributionListRepository.findById(id).orElse(null);
            }

            List<UserInfo> newUsers = new ArrayList<>();
            List<Long> userIdList = new ArrayList<>();
            if (formValues.containsKey("newMembers")) {
                userIdList = (List<Long>) formValues.get("newMembers");
                try {
                    newUsers = bot.getUsersClient().getUsersFromIdList(userIdList, false);
                } catch (NoContentException ignore) {
                    this.bot.getMessagesClient().sendMessage(
                        elementsAction.getStreamId(),
                        new OutboundMessage("Error: unable to find users")
                    );
                    return;
                }
            }

            if (distributionList == null) {
                if (newUsers.isEmpty()) {
                    this.bot.getMessagesClient().sendMessage(
                        elementsAction.getStreamId(),
                        new OutboundMessage("Please ensure you add users to the list")
                    );
                    return;
                }
                distributionList = DistributionList.builder()
                    .id(id)
                    .name(formValues.get("name").toString())
                    .users(newUsers)
                    .owner(user.getUserId())
                    .build();
            } else {
                List<Long> idsToRemove = new ArrayList<>();
                if (formValues.containsKey("remove")) {
                    try {
                        idsToRemove.add(Long.parseLong(formValues.get("remove").toString()));
                    } catch (NumberFormatException e) {
                        idsToRemove.addAll((List<Long>) formValues.get("remove"));
                    }
                }

                List<UserInfo> users = distributionList.getUsers()
                    .stream().filter(u -> !idsToRemove.contains(u.getId()))
                    .collect(Collectors.toList());
                users.addAll(newUsers);

                distributionList.setUsers(users);
            }
            distributionListRepository.save(distributionList);
            this.bot.getMessagesClient().sendMessage(
                elementsAction.getStreamId(),
                new OutboundMessage("Distribution list saved")
            );
            manageDistributionListsFormHandler.execute(user, elementsAction);
            return;
        } else if (action.startsWith("manage-distribution-list-")) {
            long id = Long.parseLong(action.substring(25));
            distributionList = distributionListRepository.findById(id).orElse(null);
            if (distributionList == null) {
                this.bot.getMessagesClient().sendMessage(
                    elementsAction.getStreamId(),
                    new OutboundMessage("Error: no such distribution list")
                );
                return;
            }
        }
        this.bot.getMessagesClient().sendMessage(
            elementsAction.getStreamId(),
            new OutboundMessage(templatesService.compile("add-edit-distribution-list-form", distributionList))
        );
    }
}
