package com.symphony.hackathon.command;

import clients.SymBotClient;
import com.symphony.hackathon.service.TemplatesService;
import model.InboundMessage;
import model.OutboundMessage;
import org.springframework.stereotype.Service;
import java.util.Map;

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
        Map<String, String> data = Map.of("name", msg.getUser().getFirstName());
        OutboundMessage msgOut = new OutboundMessage(template.compile("hello", data));
        this.bot.getMessagesClient().sendMessage(msg.getStream().getStreamId(), msgOut);
    }
}
