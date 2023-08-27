package com.shamanec.stream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionMessage {
    private String action;
    private String actionID;
    private int x;
    private int y;
    private int endX;
    private int endY;
    private String typeText;
    private String shellCommand;

    public ActionMessage() {
    }
}