package com.symphony.hackathon.command;

import clients.SymBotClient;
import com.symphony.hackathon.model.Template;
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
public class ManageTemplatesFormHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final TemplatesService template;
    private final TemplateRepository templateRepository;

    public ManageTemplatesFormHandler(SymBotClient bot,
                                      TemplatesService template,
                                      TemplateRepository templateRepository) {
        this.bot = bot;
        this.template = template;
        this.templateRepository = templateRepository;
    }

    public void execute(User user, SymphonyElementsAction action) {
        List<Template> templates = templateRepository.findAllByOwner(user.getUserId());

        Map<String, Object> data = Map.of(
            "templates", templates
        );

        String message = template.compile("manage-templates-form", data);
        this.bot.getMessagesClient().sendMessage(
            action.getStreamId(), new OutboundMessage(message)
        );
    }
}
