package com.shamanec.stream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.core.util.Pair;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

public class ScreenCaptureService extends Service {
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;

    LocalWebsocketServer server;

    public ScreenCaptureService() throws IOException {
        server = new LocalWebsocketServer(1991);
        server.start();
    }

    public static Intent getStartIntent(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, ScreenCaptureService.class);
        intent.putExtra("ACTION", "START");
        intent.putExtra("RESULT_CODE", resultCode);
        intent.putExtra("DATA", data);
        return intent;
    }

    public static Intent getStopIntent(Context context) {
        Intent intent = new Intent(context, ScreenCaptureService.class);
        intent.putExtra("ACTION", "STOP");
        return intent;
    }

    private static boolean isStartCommand(Intent intent) {
        return intent.hasExtra("RESULT_CODE") && intent.hasExtra("DATA")
                && intent.hasExtra("ACTION") && Objects.equals(intent.getStringExtra("ACTION"), "START");
    }

    private static boolean isStopCommand(Intent intent) {
        return intent.hasExtra("ACTION") && Objects.equals(intent.getStringExtra("ACTION"), "STOP");
    }

    private static int getVirtualDisplayFlags() {
        return DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    }

    private Bitmap imageToBitmap(Image image) {
        // Retrieve the image planes from the image object
        Image.Plane[] planes = image.getPlanes();
        // Extract the pixel data from the first plane by calling getBuffer() on the first element of the planes array
        // As far as I've observed the images we receive have only 1 plane
        ByteBuffer buffer = planes[0].getBuffer();
        // Get distance between adjacent pixels in the same row
        int pixelStride = planes[0].getPixelStride();
        // distance between adjacent rows in bytes
        int rowStride = planes[0].getRowStride();
        // Calculate the padding between rows, which is the difference between rowStride and pixelStride multiplied by the image width
        int rowPadding = rowStride - pixelStride * mWidth;

        // Calculate the initial bitmap width to create a valid image
        int newWidth = mWidth + (rowPadding / pixelStride);

        // Create the initial bitmap object
        // And copy into it the pixels from the buffer declared above
        Bitmap bitmap = Bitmap.createBitmap(newWidth, mHeight, Bitmap.Config.ARGB_4444);
        bitmap.copyPixelsFromBuffer(buffer);

        // Close the Image to free up memory
        image.close();

        // We reinitialize the bitmap object using the original bitmap
        // but with the display width and height
        // essentially cropping it properly
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight);

        return bitmap;
    }

    BlockingQueue<Bitmap> imageQueue = new LinkedBlockingDeque<>(3);

    private class ImageConsumer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // Take the next image from the queue (this will block if the queue is empty)
                    Bitmap bitmap = imageQueue.take();

                    // Compress the image
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, ScreenCaptureActivity.jpegQuality, outputStream);

                    bitmap.recycle();
                    byte[] compressedImage = outputStream.toByteArray();

                    // Send the compressed image over the WebSocket
                    server.broadcast(compressedImage);

                } catch (InterruptedException e) {
                    // Handle interruption or exit the loop
                    break;
                }
            }
        }
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Bitmap bitmap = null;
            // Get the latest image from the image reader
            try (Image image = mImageReader.acquireLatestImage()) {
                if (image != null) {
                    bitmap = imageToBitmap(image);
                    imageQueue.put(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Callback to reset the virtual display when orientation changes
    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // When orientation changes get the display rotation
            final int rotation = mDisplay.getRotation();
            // If the current rotation is different than the previous rotation
            // Re-assign it
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // Clean up the previous virtual display if it exists
                    if (mVirtualDisplay != null) mVirtualDisplay.release();

                    // Start the onImageAvailableListener on the image reader
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // The MediaProjection.Callback class is a callback interface for receiving updates about changes to a media projection.
    // this code ensures that resources used by the media projection are properly released when the media projection stops, which helps to avoid memory leaks and other issues.
    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        // onStop is called when the MediaProjection stops
        @Override
        public void onStop() {
            mHandler.post(() -> {
                // Release virtual display, image reader and orientation change callback
                if (mVirtualDisplay != null) mVirtualDisplay.release();
                if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                // Unregister the MediaProjectionStopCallback object (this) from the media projection.
                // This ensures that the onStop() method is not called again if the media projection is stopped again in the future.
                mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    // The onStartCommand() method is called by the Android system when the service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isStartCommand(intent)) {
            // Create notification
            Pair<Integer, Notification> notification = NotificationUtils.getNotification(this);
            // Start it in the foreground
            startForeground(notification.first, notification.second);
            // Start projection
            int resultCode = intent.getIntExtra("RESULT_CODE", Activity.RESULT_CANCELED);
            Intent data = intent.getParcelableExtra("DATA");
            startProjection(resultCode, data);
        } else if (isStopCommand(intent)) {
            stopProjection();
            stopSelf();
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    // Set up a media projection by obtaining a MediaProjection object and creating a virtual display
    private void startProjection(int resultCode, Intent data) {
        // Get a reference to the MediaProjectionManager
        MediaProjectionManager mpManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // If mMediaProjection is null we obtain it from the MediaProjectionManager
        if (mMediaProjection == null) {
            mMediaProjection = mpManager.getMediaProjection(resultCode, data);
            if (mMediaProjection != null) {
                // If MediaProjection object is successfully obtained we create a virtual display
                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                // Get the actual display
                mDisplay = windowManager.getDefaultDisplay();
                createVirtualDisplay();

                // Register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // Register media projection stop callback
                mMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
            }
        }
    }

    private void stopProjection() {
        if (mHandler != null) {
            mHandler.post(() -> {
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                }
            });
        }
    }

    @SuppressLint("WrongConstant")
    private void createVirtualDisplay() {
        // We get the real display metrics on the display object
        final DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getRealMetrics(metrics);
        // Set up the width and height for the image reader to be half of the real display metrics
        // This significantly increases the FPS even with JPEG quality of 100
        // Instead of rescaling bitmaps which reduces quality even further
        int metricsWidth = metrics.widthPixels;
        int metricsHeight = metrics.heightPixels;
        if (metricsWidth > metricsHeight) {
            if (metricsWidth > 1280 && metricsHeight > 720) {
                mWidth = metricsWidth / 2;
                mHeight = metricsHeight / 2;
            }
        } else if (metricsWidth > 720 && metricsHeight > 1280) {
            mWidth = metricsWidth / 2;
            mHeight = metricsHeight / 2;
        } else {
            mWidth = metricsWidth;
            mHeight = metricsHeight;
        }

        mDensity = metrics.densityDpi;

        // Create an ImageReader object with the proper display dimensions and PixelFormat
        // Couldn't get it to work with any other PixelFormat than RGBA
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        // Create the VirtualDisplay object with all properties we have obtained until now
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screencapture", mWidth, mHeight,
                mDensity, getVirtualDisplayFlags(), mImageReader.getSurface(), null, mHandler);

        Thread imageConsumerThread = new Thread(new ImageConsumer());
        imageConsumerThread.start();

        // Set an ImageAvailableListener on the ImageReader object
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }
}