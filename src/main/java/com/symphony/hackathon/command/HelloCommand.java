package com.symphony.hackathon.command;

import clients.SymBotClient;
import com.symphony.hackathon.service.TemplatesService;
import model.InboundMessage;
import model.OutboundMessage;
import org.springframework.stereotype.Service;

@Service
public class HelloCommand implements Command {
    private final SymBotClient bot;
    private final TemplatesService template;

    public HelloCommand(SymBotClient bot, TemplatesService template) {
        this.bot = bot;
        this.template = template;
    }

    @Override
    public void execute(InboundMessage msg) {
        OutboundMessage msgOut = new OutboundMessage(template.load("new-template"));
        this.bot.getMessagesClient().sendMessage(msg.getStream().getStreamId(), msgOut);
    }
}
