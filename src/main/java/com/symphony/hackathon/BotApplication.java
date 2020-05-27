package com.symphony.hackathon;

import com.symphony.hackathon.listener.ElementsListenerImpl;
import com.symphony.hackathon.listener.IMListenerImpl;
import com.symphony.hackathon.service.BotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class BotApplication {
    public BotApplication(BotService bot,
                          IMListenerImpl imListener,
                          ElementsListenerImpl elementsListener) {
        bot.init(imListener, elementsListener);
    }

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
