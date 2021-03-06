package com.symphony.hackathon.service;

import authentication.SymExtensionAppRSAAuth;
import authentication.SymOBORSAAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BotService {
    private SymBotClient botClient;
    private SymOBORSAAuth oboAuth;

    public BotService() {
        try {
            this.botClient = SymBotClient.initBotRsa("config.json");
            this.oboAuth = new SymOBORSAAuth(this.botClient.getConfig());
            this.oboAuth.authenticate();
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    public void init(listeners.DatafeedListener... listeners) {
        botClient.getDatafeedEventsService().addListeners(listeners);
    }

    @Bean
    public SymConfig getConfig() {
        return botClient.getConfig();
    }

    @Bean
    public SymBotClient getBot() {
        return botClient;
    }

    @Bean
    public SymOBORSAAuth getOboAuth() {
        return oboAuth;
    }
}
