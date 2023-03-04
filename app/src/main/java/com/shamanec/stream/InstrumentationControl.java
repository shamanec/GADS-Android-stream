package com.shamanec.stream;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;

public class InstrumentationControl {
    private UiDevice uiDevice = null;

    public InstrumentationControl(UiDevice uiDevice) {
        this.uiDevice = uiDevice;
    }

    public void performAction(String messageJSON) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(messageJSON);

        String action = JsonPath.read(document, "$.action");
        if (action.equals("typeText")) {
            String textToType = JsonPath.read(document, "$.text");

            UiObject focusedObject = uiDevice.findObject(new UiSelector().focused(true));
            try {
                focusedObject.clearTextField();
                focusedObject.setText(textToType);
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        } else if (action.equals("tap")) {
            int x = JsonPath.read(document, "$.x");
            int y = JsonPath.read(document, "$.y");
            uiDevice.click(x, y);
        } else if (action.equals("swipe")) {
            int startX = JsonPath.read(document, "$.startX");
            int startY = JsonPath.read(document, "$.startY");
            int endX = JsonPath.read(document, "$.endX");
            int endY = JsonPath.read(document, "$.endY");
            int steps = JsonPath.read(document, "$.steps");

            steps = steps == 0 ? 50 : steps;

            uiDevice.swipe(startX, startY, endX, endY, steps);
        } else if (action.equals("executeShellCommand")) {
            String shellCommand = JsonPath.read(document, "$.shellCommand");
            try {
                uiDevice.executeShellCommand(shellCommand);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
