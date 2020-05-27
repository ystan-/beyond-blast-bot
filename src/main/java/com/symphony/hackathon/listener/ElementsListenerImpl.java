package com.symphony.hackathon.listener;

import clients.SymBotClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.hackathon.command.ElementsResponse;
import com.symphony.hackathon.command.DynamicBlastHandler;
import com.symphony.hackathon.command.PreparedBlastFormHandler;
import com.symphony.hackathon.service.TemplatesService;
import listeners.ElementsListener;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ElementsListenerImpl implements ElementsListener {
    private final SymBotClient bot;
    private final Map<String, ElementsResponse> responses;
    private final List<String> menuForms;
    private final ObjectMapper mapper;
    private final TemplatesService template;

    public ElementsListenerImpl(SymBotClient bot,
                                ObjectMapper mapper,
                                TemplatesService template,
                                DynamicBlastHandler dynamicBlast,
                                PreparedBlastFormHandler preparedBlastFormHandler) {
        this.bot = bot;
        this.mapper = mapper;
        this.template = template;
        this.responses = Map.of(
            "dynamic-blast", dynamicBlast,
            "prepared-blast-form", preparedBlastFormHandler
        );
        this.menuForms = List.of(
            "dynamic-blast-form"
        );
    }

    public void onElementsAction(User user, SymphonyElementsAction action) {
        String payload = null;
        try {
            payload = mapper.writeValueAsString(action.getFormValues());
        } catch (JsonProcessingException ignore) {}
        log.info("Received submission: {}\n{}", action.getFormId(), payload);
        if (responses.containsKey(action.getFormId())) {
            responses.get(action.getFormId()).execute(user, action);
        } else if (menuForms.contains(action.getFormId())) {
            this.bot.getMessagesClient().sendMessage(
                action.getStreamId(),
                new OutboundMessage(template.load(action.getFormId()))
            );
        } else {
            this.bot.getMessagesClient()
                .sendMessage(action.getStreamId(), new OutboundMessage("Sorry, I don't understand"));
        }
    }
}
