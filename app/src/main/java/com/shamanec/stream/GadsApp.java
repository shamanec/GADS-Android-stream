package com.shamanec.stream;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import static com.shamanec.stream.IntentActionConstants.ORIENTATION_CHANGED_ACTION;

public class GadsApp extends Application {

    private static final String TAG = "GadsApp";
    private int lastKnownOrientation = Configuration.ORIENTATION_UNDEFINED;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Configuration changed: " + newConfig.toString());

        // Check if the orientation has actually changed
        if (newConfig.orientation == lastKnownOrientation) {
            Log.d(TAG, "Orientation did not change, skipping broadcast.");
            return;
        }

        // Update the last known orientation
        lastKnownOrientation = newConfig.orientation;

        // Send the broadcast
        Intent intent = new Intent(ORIENTATION_CHANGED_ACTION);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: " + ORIENTATION_CHANGED_ACTION);
    }
}