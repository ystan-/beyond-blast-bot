package com.symphony.hackathon.command;

import clients.SymBotClient;
import com.github.jknack.handlebars.Handlebars;
import com.symphony.hackathon.repository.DistributionListRepository;
import com.symphony.hackathon.repository.TemplateRepository;
import com.symphony.hackathon.service.TemplatesService;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PreparedBlastFormHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final TemplatesService template;
    private final DistributionListRepository distributionListRepository;
    private final TemplateRepository templateRepository;
    public static final String OPTION_TEMPLATE = "<option value=\"%s\">%s</option>";

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
        String distributionLists = distributionListRepository.findAllByOwner(user.getUserId())
            .stream()
            .map(d -> String.format(OPTION_TEMPLATE, d.getId(), d.getName()))
            .collect(Collectors.joining("\n"));

        String templates = templateRepository.findAllByOwner(user.getUserId())
            .stream()
            .map(t -> String.format(OPTION_TEMPLATE, t.getId(), t.getName()))
            .collect(Collectors.joining("\n"));

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
            "distributionLists", new Handlebars.SafeString(distributionLists),
            "templates", new Handlebars.SafeString(templates)
        );

        String message = template.compile("prepared-blast-form", data);
        this.bot.getMessagesClient().sendMessage(
            action.getStreamId(), new OutboundMessage(message)
        );
    }
}
