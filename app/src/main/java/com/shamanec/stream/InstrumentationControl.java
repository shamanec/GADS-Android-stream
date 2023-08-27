package com.shamanec.stream;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import org.java_websocket.WebSocket;

import java.io.IOException;

public class InstrumentationControl {
    private UiDevice uiDevice = null;

    public InstrumentationControl(UiDevice uiDevice) {
        this.uiDevice = uiDevice;
    }

    private void lock(WebSocket conn, String actionID) {
        try {
            conn.send(String.format("%sLocking device"));
            uiDevice.sleep();
        } catch (Exception e) {
            conn.send(String.format("%sCould not lock device - %s", e));
        }
    }

    private void unlock(WebSocket conn, String actionID) {
        try {
            conn.send(String.format("%sUnlocking device"));
            uiDevice.wakeUp();
        } catch (Exception e) {
            conn.send(String.format("%sCould not unlock device - %s", e));
        }
    }

    private void performSwipe(WebSocket conn, String actionID, Object json) {
        try {
            int startX = JsonPath.read(json, "$.startX");
            int startY = JsonPath.read(json, "$.startY");
            int endX = JsonPath.read(json, "$.endX");
            int endY = JsonPath.read(json, "$.endY");
            int steps = JsonPath.read(json, "$.steps");

            conn.send(String.format("%sSwiping - startX: %s, startY: %s, endX: %s, endY: %s, steps: %s", actionID, startX, startY, endX, endY, steps));
            boolean swiped = uiDevice.swipe(startX, startY, endX, endY, steps);
            if (!swiped) {
                conn.send(String.format("%sInvalid coordinates or unsuccessful swipe action", actionID));
            }
        } catch (Exception e) {
            conn.send(String.format("%sInvalid swipe fields provided - %s", actionID, e));
            return;
        }
    }

    private void performTap(WebSocket conn, String actionID, Object json) {
        try {
            int x = JsonPath.read(json, "$.x");
            int y = JsonPath.read(json, "$.y");

            conn.send(String.format("%sTapping X:%s, Y:%s", actionID, x, y));
            boolean tapped = uiDevice.click(x, y);
            if (!tapped) {
                conn.send(String.format("%sInvalid coordinates or unsuccessful tap action", actionID));
            }
        } catch (Exception e) {
            conn.send(String.format("%sInvalid tap action JSON format - %s", actionID, e));
            return;
        }
    }

    private void typeText(WebSocket conn, String actionID, Object json) {
        String textToType = "";
        try {
            textToType = JsonPath.read(json, "$.text");
        } catch (Exception e) {
            conn.send(String.format("%sInvalid typeText JSON format - %s", actionID, e));
            return;
        }

        UiObject focusedObject = uiDevice.findObject(new UiSelector().focused(true));
        try {
            focusedObject.clearTextField();
        } catch (Exception e) {
            conn.send(String.format("%sCould not clear text before typing - %s", actionID, e));
            return;
        }
        try {
            focusedObject.setText(textToType);
        } catch (Exception e) {
            conn.send(String.format("%sCould not set text - %s", actionID, e));
        }
    }

    private void executeShellCommand(WebSocket conn, String actionID, Object json) {
        String shellCommand = "";
        try {
            shellCommand = JsonPath.read(json, "$.shellCommand");
        } catch (Exception e) {
            conn.send(String.format("%sInvalid shell command JSON format - %s", actionID, e));
            return;
        }

        try {
            uiDevice.executeShellCommand(shellCommand);
        } catch (Exception e) {
            conn.send(String.format("%sCould not execute shell command - %s", actionID, e));
        }
    }

    public void performAction(WebSocket conn, String messageJSON) {
        Object jsonDoc;
        try {
            jsonDoc = Configuration.defaultConfiguration().jsonProvider().parse(messageJSON);
        } catch (Exception e) {
            conn.send(String.format("Could not parse JSON message - %s", e));
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ActionMessage actionMessage = objectMapper.readValue(messageJSON, ActionMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String action = "";
        try {
            action = JsonPath.read(jsonDoc, "$.action");
        } catch (Exception e) {
            conn.send("Invalid JSON format, `action` field is missing");
            return;
        }

        String actionID = "";
        try {
            actionID = JsonPath.read(jsonDoc, "$.actionID").toString();
            actionID = String.format("[%s]", actionID);
        } catch (Exception e) {
            actionID = "";
        }

        if (action.equals("typeText")) {
            typeText(conn, actionID, jsonDoc);
        } else if (action.equals("tap")) {
            performTap(conn, actionID, jsonDoc);
        } else if (action.equals("swipe")) {
            performSwipe(conn, actionID, jsonDoc);
        } else if (action.equals("executeShellCommand")) {
            executeShellCommand(conn, actionID, jsonDoc);
        } else if (action.equals("lock")) {
            lock(conn, actionID);
        } else if (action.equals("unlock")) {
            unlock(conn, actionID);
        } else {
            conn.send(String.format("%s`%s` is not a valid action", actionID, action));
            return;
        }
    }
}
