package com.symphony.hackathon;

import clients.SymBotClient;
import com.symphony.hackathon.listeners.IMListenerImpl;
import com.symphony.hackathon.listeners.RoomListenerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class BotApplication {
    public BotApplication() {
        try {
            SymBotClient botClient = SymBotClient.initBotRsa("config.json");

            botClient.getDatafeedEventsService().addListeners(
                new IMListenerImpl(botClient),
                new RoomListenerImpl(botClient)
            );
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
