package com.symphony.hackathon.command;

import model.InboundMessage;

public interface Command {
    void execute(InboundMessage msg);
}
