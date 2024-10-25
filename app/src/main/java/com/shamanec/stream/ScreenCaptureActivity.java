package com.shamanec.stream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

public class ScreenCaptureActivity extends Activity {
    private static final int REQUEST_CODE = 100;

    //The onCreate method is an Android activity lifecycle method that gets called when the activity is created.
    // In this case, it sets the layout for the activity and calls startProjection().
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startProjection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if the Activity REQUEST_CODE and result are as expected
        // And start the ScreenCaptureService
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startService(ScreenCaptureService.getStartIntent(this, resultCode, data));
            }
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    // The startProjection method gets called when the user wants to start screen capture.
    // It gets the MediaProjectionManager from the system, creates a screen capture intent with createScreenCaptureIntent(),
    // and starts an activity for result using startActivityForResult.
    private void startProjection() {
        MediaProjectionManager mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    // The stopProjection method gets called when the user wants to stop screen capture.
    // It starts a ScreenCaptureService with an intent that tells the service to stop.
    // TODO: Currently unused, don't really want to give option to stop screen capture service, will look into possibly using it
    private void stopProjection() {
        startService(ScreenCaptureService.getStopIntent(this));
    }
}
