package com.shamanec.stream;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GadsInstrumentationTest {
    InstrumentationWebSocketServer server = null;
    @Test
    public void endlessTest() {
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