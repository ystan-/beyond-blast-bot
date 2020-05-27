package com.symphony.hackathon.command;

import model.InboundMessage;
import org.springframework.stereotype.Service;

public interface Command {
    void execute(InboundMessage msg);
}
