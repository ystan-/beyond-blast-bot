package com.symphony.hackathon.service;

import clients.SymBotClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BotService {
    private SymBotClient botClient;

    public BotService() {
        try {
            this.botClient = SymBotClient.initBotRsa("config.json");
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    public void init(listeners.DatafeedListener... listeners) {
        botClient.getDatafeedEventsService().addListeners(listeners);
    }

    @Bean
    public SymBotClient getBot() {
        return botClient;
    }
}
