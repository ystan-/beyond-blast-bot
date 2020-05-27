package com.symphony.hackathon.listener;

import clients.SymBotClient;
import com.symphony.hackathon.command.Command;
import com.symphony.hackathon.command.HelloCommand;
import listeners.IMListener;
import lombok.extern.slf4j.Slf4j;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class IMListenerImpl implements IMListener {
    private final SymBotClient bot;
    private final Map<String, Command> commands;

    public IMListenerImpl(SymBotClient bot, HelloCommand hello) {
        this.bot = bot;
        commands = Map.of(
            "hello", hello
        );
    }

    public void onIMMessage(InboundMessage msg) {
        String msgText = msg.getMessageText().trim();
        log.info("Received IM: {}", msgText);
        if (!msgText.startsWith("/")) {
            return;
        }
        msgText = msgText.substring(1);
        String command = msgText.contains(" ") ? msgText.substring(0, msgText.indexOf(' ')) : msgText;
        if (commands.containsKey(command)) {
            commands.get(command).execute(msg);
        } else {
            this.bot.getMessagesClient()
                .sendMessage(msg.getStream().getStreamId(), new OutboundMessage("Sorry, I don't understand"));
        }
    }

    public void onIMCreated(Stream stream) {}
}
