package com.symphony.hackathon.command;

import model.User;
import model.events.SymphonyElementsAction;

public interface ElementsResponse {
    void execute(User user, SymphonyElementsAction action);
}
