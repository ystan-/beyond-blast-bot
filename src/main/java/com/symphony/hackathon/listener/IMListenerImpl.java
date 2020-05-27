package com.symphony.hackathon.listener;

import clients.SymBotClient;
import com.symphony.hackathon.service.TemplatesService;
import listeners.IMListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class IMListenerImpl implements IMListener {
    private final SymBotClient botClient;
    private final TemplatesService template;

    public IMListenerImpl(SymBotClient botClient, TemplatesService template) {
        this.botClient = botClient;
        this.template = template;
    }

    public void onIMMessage(InboundMessage msg) {
        Map<String, String> data = Map.of("name", msg.getUser().getFirstName());
        OutboundMessage msgOut = new OutboundMessage(template.compile("hello", data));
        this.botClient.getMessagesClient().sendMessage(msg.getStream().getStreamId(), msgOut);
    }

    public void onIMCreated(Stream stream) {}
}
