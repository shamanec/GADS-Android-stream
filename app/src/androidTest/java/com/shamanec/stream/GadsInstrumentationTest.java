package com.shamanec.stream;

import android.app.Instrumentation;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

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
    // adb shell am instrument -w -e debug false com.shamanec.stream.test/androidx.test.runner.AndroidJUnitRunner
    InstrumentationWebSocketServer server = null;
    @Test
    public void endlessTest() {
        logToConsole("Started endless test");
        server = new InstrumentationWebSocketServer(1992);
        logToConsole("Starting server on port 1992");
        server.start();
        logToConsole("Server started");
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

    public void logToConsole(String string) {
        Bundle bundle = new Bundle();
        bundle.putString(Instrumentation.REPORT_KEY_STREAMRESULT, string + "\n");
        InstrumentationRegistry.getInstrumentation().sendStatus(0, bundle);
    }
}