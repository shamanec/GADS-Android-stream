package com.shamanec.stream;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    InstrumentationWebSocketServer server = null;
    @Test
    public void useAppContext() {
        server = new InstrumentationWebSocketServer(1992);
        server.start();
        while (true) {
        }
    }

    @After
    public void release() {
        try {
            server.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}