package com.symphony.hackathon.listener;

import clients.SymBotClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.hackathon.command.Command;
import com.symphony.hackathon.command.ElementsResponse;
import com.symphony.hackathon.command.NewTemplateElementsResponse;
import listeners.ElementsListener;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class ElementsListenerImpl implements ElementsListener {
    private final SymBotClient bot;
    private final Map<String, ElementsResponse> responses;
    private final ObjectMapper mapper;

    public ElementsListenerImpl(SymBotClient bot,
                                ObjectMapper mapper,
                                NewTemplateElementsResponse newTemplate) {
        this.bot = bot;
        this.mapper = mapper;
        responses = Map.of(
            "new-template", newTemplate
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
        } else {
            this.bot.getMessagesClient()
                .sendMessage(action.getStreamId(), new OutboundMessage("Sorry, I don't understand"));
        }
    }
}
