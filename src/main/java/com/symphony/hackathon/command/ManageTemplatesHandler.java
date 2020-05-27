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
import java.util.Map;

@Slf4j
@Service
public class ManageTemplatesHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final TemplatesService templatesService;
    private final TemplateRepository templateRepository;
    private final ManageTemplatesFormHandler manageTemplatesFormHandler;

    public ManageTemplatesHandler(SymBotClient bot,
                                  TemplatesService templatesService,
                                  TemplateRepository templateRepository,
                                  ManageTemplatesFormHandler manageTemplatesFormHandler) {
        this.bot = bot;
        this.templatesService = templatesService;
        this.templateRepository = templateRepository;
        this.manageTemplatesFormHandler = manageTemplatesFormHandler;
    }

    public void execute(User user, SymphonyElementsAction elementsAction) {
        Map<String, Object> formValues = elementsAction.getFormValues();
        String action = elementsAction.getFormValues().get("action").toString();
        Template template = null;

        if (action.equals("add-template")) {
            template = Template.builder().build();
        } else if (action.equals("cancel-template")) {
            manageTemplatesFormHandler.execute(user, elementsAction);
            return;
        } else if (action.equals("save-template")) {
            long id;
            if (formValues.get("id").toString().equals("0")) {
                id = templateRepository.count() + 1;
            } else {
                id = Long.parseLong(formValues.get("id").toString());
            }
            template = Template.builder()
                .id(id)
                .name(formValues.get("name").toString())
                .template(formValues.get("template").toString())
                .owner(user.getUserId())
                .build();
            templateRepository.save(template);
            this.bot.getMessagesClient().sendMessage(
                elementsAction.getStreamId(),
                new OutboundMessage("Template saved")
            );
            manageTemplatesFormHandler.execute(user, elementsAction);
            return;
        } else if (action.startsWith("manage-template-")) {
            long id = Long.parseLong(action.substring(16));
            template = templateRepository.findById(id).orElse(null);
            if (template == null) {
                this.bot.getMessagesClient().sendMessage(
                    elementsAction.getStreamId(),
                    new OutboundMessage("Error: no such template")
                );
                return;
            }
        }
        this.bot.getMessagesClient().sendMessage(
            elementsAction.getStreamId(),
            new OutboundMessage(templatesService.compile("add-edit-template-form", template))
        );
    }
}
