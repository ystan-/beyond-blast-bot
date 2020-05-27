package com.symphony.hackathon.command;

import authentication.SymOBORSAAuth;
import authentication.SymOBOUserRSAAuth;
import clients.SymBotClient;
import clients.SymOBOClient;
import com.github.jknack.handlebars.Handlebars;
import com.symphony.hackathon.model.DistributionList;
import com.symphony.hackathon.repository.DistributionListRepository;
import com.symphony.hackathon.service.TemplatesService;
import configuration.SymConfig;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.UserInfo;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import javax.ws.rs.core.NoContentException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DynamicBlastHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final SymOBORSAAuth oboAuth;
    private final SymConfig botConfig;
    private final TemplatesService templatesService;
    private final DistributionListRepository distributionListRepository;

    public DynamicBlastHandler(SymBotClient bot,
                               SymOBORSAAuth oboAuth,
                               SymConfig botConfig,
                               TemplatesService templatesService,
                               DistributionListRepository distributionListRepository) {
        this.bot = bot;
        this.oboAuth = oboAuth;
        this.botConfig = botConfig;
        this.templatesService = templatesService;
        this.distributionListRepository = distributionListRepository;
    }

    @SuppressWarnings("unchecked")
    public void execute(User user, SymphonyElementsAction elementsAction) {
        String action = elementsAction.getFormValues().get("action").toString();
        Map<String, Object> formValues = elementsAction.getFormValues();

        if (action.equals("home")) {
            this.bot.getMessagesClient().sendMessage(
                elementsAction.getStreamId(),
                new OutboundMessage(templatesService.load("menu"))
            );
        } else if (action.equals("preview-blast")) {
            if (formValues.get("template").toString().trim().isEmpty()) {
                this.bot.getMessagesClient().sendMessage(
                    elementsAction.getStreamId(),
                    new OutboundMessage("Error: No message to send")
                );
                return;
            }

            List<UserInfo> recipients = new ArrayList<>();
            if (!formValues.get("distributionList").toString().equals("none")) {
                long distributionListId = Long.parseLong(formValues.get("distributionList").toString());
                DistributionList distributionList = distributionListRepository.findById(distributionListId).orElse(null);
                if (distributionList != null) {
                    recipients.addAll(distributionList.getUsers());
                }
            }
            List<Long> additionalRecipientIds = (List<Long>) formValues.get("recipients");
            try {
                recipients.addAll(bot.getUsersClient().getUsersFromIdList(additionalRecipientIds, false));
            } catch (NoContentException ignore) {}

            if (recipients.isEmpty()) {
                this.bot.getMessagesClient().sendMessage(
                    elementsAction.getStreamId(),
                    new OutboundMessage("Error: No recipients to send to")
                );
                return;
            }

            String sampleMessage = formValues.get("template").toString();
            if (sampleMessage.contains("{{mention}}")) {
                String mentionML = "<mention uid=\"" + recipients.get(0).getId() + "\" />";
                sampleMessage = templatesService.compileInline(
                    sampleMessage,
                    Map.of("mention", new Handlebars.SafeString(mentionML))
                );
            }

            Map<String, Object> data = Map.of(
                "message", new Handlebars.SafeString(sampleMessage),
                "recipients", recipients,
                "recipientsCount", recipients.size()
            );
            String message = templatesService.compile("preview-blast", data);
            bot.getMessagesClient().sendMessage(elementsAction.getStreamId(), new OutboundMessage(message));
        } else {
            bot.getMessagesClient().sendMessage(elementsAction.getStreamId(), new OutboundMessage("Sending blast.."));

            String userTemplate = formValues.get("template").toString();
            List<Long> recipients = new ArrayList<>();

            if (!formValues.get("distributionList").toString().equals("none")) {
                long distributionListId = Long.parseLong(formValues.get("distributionList").toString());
                DistributionList distributionList = distributionListRepository.findById(distributionListId).orElse(null);
                if (distributionList != null) {
                    recipients.addAll(distributionList.getUsers().stream().map(UserInfo::getId).collect(Collectors.toList()));
                }
            }

            recipients.addAll((List<Long>) formValues.get("recipients"));

            SymOBOUserRSAAuth userAuth = oboAuth.getUserAuth(user.getUserId());
            SymOBOClient oboClient = SymOBOClient.initOBOClient(botConfig, userAuth);

            boolean postProcessMention = userTemplate.contains("{{mention}}");

            for (long recipient : recipients) {
                String thisMessage = userTemplate;

                Map<String, Object> postData = new HashMap<>();
                if (postProcessMention) {
                    String mentionML = "<mention uid=\"" + recipient + "\" />";
                    postData.put("mention", new Handlebars.SafeString(mentionML));
                }

                if (!postData.isEmpty()) {
                    thisMessage = templatesService.compileInline(userTemplate, postData);
                }

                log.info("Obtaining streamId for: {}", recipient);
                String streamId = oboClient.getStreamsClient().getUserIMStreamId(recipient);
                log.info("Sending message to stream: {}", streamId);
                oboClient.getMessagesClient().sendMessage(streamId, new OutboundMessage(thisMessage));
                log.info("Blast successful for: {}", recipient);
            }

            bot.getMessagesClient().sendMessage(elementsAction.getStreamId(), new OutboundMessage("Blast complete!"));
        }
    }
}
