package com.symphony.hackathon.listener;

import clients.SymBotClient;
import com.symphony.hackathon.service.TemplatesService;
import listeners.IMListener;
import lombok.extern.slf4j.Slf4j;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IMListenerImpl implements IMListener {
    private final SymBotClient bot;
    private final TemplatesService template;

    public IMListenerImpl(SymBotClient bot, TemplatesService template) {
        this.bot = bot;
        this.template = template;
    }

    public void onIMMessage(InboundMessage msg) {
        this.bot.getMessagesClient()
            .sendMessage(msg.getStream().getStreamId(), new OutboundMessage(template.load("menu")));
    }

    public void onIMCreated(Stream stream) {}
}
