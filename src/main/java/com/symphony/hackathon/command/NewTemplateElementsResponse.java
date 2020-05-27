package com.symphony.hackathon.command;

import clients.SymBotClient;
import com.symphony.hackathon.service.TemplatesService;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewTemplateElementsResponse implements ElementsResponse {
    private final SymBotClient bot;
    private final TemplatesService template;

    public NewTemplateElementsResponse(SymBotClient bot, TemplatesService template) {
        this.bot = bot;
        this.template = template;
    }

    @SuppressWarnings("unchecked")
    public void execute(User user, SymphonyElementsAction action) {
        Map<String, Object> formValues = action.getFormValues();
        String title = formValues.get("title").toString();
        String userTemplate = formValues.get("template").toString();
        List<Long> recipients = (List<Long>) formValues.get("recipients");
        String recipientsList = recipients.stream()
            .map(Object::toString).collect(Collectors.joining(", "));

        String message = "Title: " + title + "<br/>Recipients: "
            + recipientsList + "<br/>Template: " + userTemplate;
        bot.getMessagesClient().sendMessage(action.getStreamId(), new OutboundMessage(message));
    }
}
