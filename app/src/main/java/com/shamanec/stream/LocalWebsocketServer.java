package com.shamanec.stream;

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

    public LocalWebsocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send(getDeviceInfo());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message == "info") {
            conn.send(getDeviceInfo());
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

    private byte[] getDeviceInfo() {
        ByteBuffer infoMessage = ByteBuffer.allocate(8);
        infoMessage.putInt(ScreenCaptureService.metricsWidth);
        infoMessage.putInt(ScreenCaptureService.metricsHeight);

        // Message type `1` for information
        byte[] messageTypeBytes = ByteBuffer.allocate(4).putInt(1).array();
        byte[] infoBytes = infoMessage.array();

        byte[] combinedBytes = new byte[messageTypeBytes.length + infoBytes.length];

        System.arraycopy(messageTypeBytes, 0, combinedBytes, 0, messageTypeBytes.length);
        System.arraycopy(infoBytes, 0, combinedBytes, messageTypeBytes.length, infoBytes.length);

        return combinedBytes;
    }

}
