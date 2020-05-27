package com.symphony.hackathon.command;

import clients.SymBotClient;
import com.symphony.hackathon.model.DistributionList;
import com.symphony.hackathon.model.Template;
import com.symphony.hackathon.repository.DistributionListRepository;
import com.symphony.hackathon.repository.TemplateRepository;
import com.symphony.hackathon.service.TemplatesService;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DynamicBlastFormHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final TemplatesService template;
    private final DistributionListRepository distributionListRepository;

    public DynamicBlastFormHandler(SymBotClient bot,
                                   TemplatesService template,
                                   DistributionListRepository distributionListRepository) {
        this.bot = bot;
        this.template = template;
        this.distributionListRepository = distributionListRepository;
    }

    public void execute(User user, SymphonyElementsAction action) {
        List<DistributionList> distributionLists = distributionListRepository.findAllByOwner(user.getUserId());

        Map<String, Object> data = Map.of(
            "distributionLists", distributionLists
        );

        String message = template.compile("dynamic-blast-form", data);
        this.bot.getMessagesClient().sendMessage(
            action.getStreamId(), new OutboundMessage(message)
        );
    }
}
