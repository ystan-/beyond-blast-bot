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
public class PreparedBlastFormHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final TemplatesService template;
    private final DistributionListRepository distributionListRepository;
    private final TemplateRepository templateRepository;

    public PreparedBlastFormHandler(SymBotClient bot,
                                    TemplatesService template,
                                    DistributionListRepository distributionListRepository,
                                    TemplateRepository templateRepository) {
        this.bot = bot;
        this.template = template;
        this.distributionListRepository = distributionListRepository;
        this.templateRepository = templateRepository;
    }

    public void execute(User user, SymphonyElementsAction action) {
        List<DistributionList> distributionLists = distributionListRepository.findAllByOwner(user.getUserId());
        List<Template> templates = templateRepository.findAllByOwner(user.getUserId());

        if (distributionLists.isEmpty()) {
            String error = "Please setup at least one distribution list first";
            this.bot.getMessagesClient().sendMessage(action.getStreamId(), new OutboundMessage(error));
            return;
        }

        if (templates.isEmpty()) {
            String error = "Please setup at least one template first";
            this.bot.getMessagesClient().sendMessage(action.getStreamId(), new OutboundMessage(error));
            return;
        }

        Map<String, Object> data = Map.of(
            "distributionLists", distributionLists,
            "templates", templates
        );

        String message = template.compile("prepared-blast-form", data);
        this.bot.getMessagesClient().sendMessage(
            action.getStreamId(), new OutboundMessage(message)
        );
    }
}
