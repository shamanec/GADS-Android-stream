package com.shamanec.stream;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class LocalWebsocketServer extends org.java_websocket.server.WebSocketServer {
    private final ScreenCaptureService screenCaptureService;

    public LocalWebsocketServer(int port, ScreenCaptureService service) {
        super(new InetSocketAddress(port));
        this.screenCaptureService = service;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String[] splitMsg = message.split(":");

        for (int i=0;i < splitMsg.length; i++) {
            String currentMsgPart = splitMsg[i];

            // Check if the message contains "targetFPS="
            if (currentMsgPart.startsWith("targetFPS=")) {
                try {
                    // Parse the FPS value from the message
                    int fps = Integer.parseInt(currentMsgPart.split("=")[1]);
                    // Update the targetFPS in ScreenCaptureService
                    screenCaptureService.setTargetFPS(fps);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e("LocalWebsocketServer", "Invalid FPS value received: " + currentMsgPart);
                }
            }
            if (currentMsgPart.startsWith("jpegQuality=")) {
                try {
                    // Parse the jpegQuality value from the message
                    int jpegQuality = Integer.parseInt(currentMsgPart.split("=")[1]);
                    // Update the jpegQuality in ScreenCaptureService
                    screenCaptureService.setJpegQuality(jpegQuality);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e("LocalWebsocketServer", "Invalid JPEG quality value received: " + currentMsgPart);
                }
            }
            if (currentMsgPart.startsWith("scalingFactor=")) {
                try {
                    // Parse the scalingFactor value from the message
                    int scalingFactor = Integer.parseInt(currentMsgPart.split("=")[1]);
                    // Update the scalingFactor in ScreenCaptureService
                    screenCaptureService.setScalingFactor(scalingFactor);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e("LocalWebsocketServer", "Invalid scaling factor value received: " + currentMsgPart);
                }
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // TODO: Handle something here, like port binding failed or whatever
        }
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
