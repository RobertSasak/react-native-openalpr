package com.cardash.openalpr;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.openalpr.OpenALPR;
import org.openalpr.model.Coordinate;
import org.openalpr.model.Result;
import org.openalpr.model.Results;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ALPR {
    private static final String TAG = ALPR.class.getSimpleName();
    private static ALPR instance;
    private Handler mHandler = null;
    private AtomicBoolean isProcessing = new AtomicBoolean(false);

    /**
     * constructor for initialization message handling on separate thread
     */
    private ALPR() {
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG, "handleMessage");
                HashMap<String, Object> data = (HashMap<String, Object>) msg.obj;
                if ((Boolean) data.get("finish")) {
                    // finishAlpr();
                } else {
                    executeRecognition(
                            (Mat) data.get("m"),
                            (String) data.get("country"),
                            (ResultsCallback) data.get("callback"),
                            (WeakReference<Context>) data.get("context"),
                            (int) data.get("rotation")
                    );
                }

            }
        };
    }

    /**
     * singleton implementation, is used from main thread only
     */
    static ALPR getInstance() {
        if (instance == null) {
            instance = new ALPR();
        }
        return instance;
    }

    interface ResultsCallback {
        void onResults(String plate, String confidence, String processingTimeMs, List<Point> coordinates);

        void onFail();
    }

    /**
     * prepare data and recognize the license plate
     */
    private void executeRecognition(final Mat m, final String country, final ResultsCallback callback, final WeakReference<Context> context, int rotation) {
        Log.i(TAG, "executeRecognition");
        Context ctx = context.get();
        if (ctx == null) {
            finishExecution(ctx, callback);
            return;
        }
        if (m.cols() == 0 || m.rows() == 0) {
            finishExecution(ctx, callback);
            return;
        }

        // image is need to have proper orientation before delivering to openalpr for recognition
        applyOrientation(m, true, rotation);

        // path to openalpr config file in android environment
        final String androidDataDir = ctx.getApplicationInfo().dataDir;
        final String openAlprConfFile = androidDataDir + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";

        // create file in internal memory for passing to openalpr
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 10;
        Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bm);
        m.release();
        File file = saveToInternalStorage(bm, ctx);
        if (file == null) {
            finishExecution(ctx, callback);
            return;
        }

        // synchronous call for license plate recognition to openalpr
        String result = OpenALPR.Factory.create(ctx, androidDataDir).recognizeWithCountryRegionNConfig(country, "", file.getAbsolutePath(), openAlprConfFile, 10);

        // deliver results to main thread
        Handler handler = new Handler(ctx.getMainLooper());
        try {
            final Results results = new Gson().fromJson(result, Results.class);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (results == null || results.getResults() == null || results.getResults().size() == 0) {
                        Log.d(TAG, "It was not possible to detect the licence plate.");
                        callback.onFail();
                    } else {
                        Result res0 = results.getResults().get(0);
                        callback.onResults(
                                res0.getPlate(),
                                String.format("%.2f", res0.getConfidence()),
                                String.format("%.2f", ((results.getProcessingTimeMs() / 1000.0) % 60)),
                                getAndroidPoints(res0.getCoordinates()));
                    }
                    if (isProcessing != null) isProcessing.set(false);
                }
            });

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "AlprException", e);
            finishExecution(ctx, callback);
        }
    }

    /**
     * finishes processing if something went wrong
     */
    private void finishExecution(Context ctx, final ResultsCallback callback) {
        if (ctx == null) {
            if (isProcessing != null) isProcessing.set(false);
            return;
        }
        Handler handler = new Handler(ctx.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) callback.onFail();
                if (isProcessing != null) isProcessing.set(false);
            }
        });
    }

    /**
     * applies proper orientation for an image
     */
    private static void applyOrientation(Mat rgba, boolean clockwise, int rotation) {
        if (rotation == Surface.ROTATION_0) {
            // Rotate clockwise / counter clockwise 90 degrees
            Mat rgbaT = rgba.t();
            Core.flip(rgbaT, rgba, clockwise ? 1 : -1);
            rgbaT.release();
        } else if (rotation == Surface.ROTATION_270) {
            // Rotate clockwise / counter clockwise 180 degrees
            Mat rgbaT = rgba.t();
            Core.flip(rgba.t(), rgba, clockwise ? 1 : -1);
            rgbaT.release();
            Mat rgbaT2 = rgba.t();
            Core.flip(rgba.t(), rgba, clockwise ? 1 : -1);
            rgbaT2.release();
        }
    }

    /**
     * entry point for frame processing request
     */
    void process(final Mat m, final String country, int rotation, final ResultsCallback callback, final WeakReference<Context> context) {
        // post message to separate thread with image and related data
        if (!mHandler.hasMessages(10) && isProcessing.compareAndSet(false, true)) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("m", m.clone());
            map.put("country", country);
            map.put("callback", callback);
            map.put("context", context);
            map.put("rotation", rotation);
            map.put("finish", false);
            Message mes = mHandler.obtainMessage(10, 0, 0, map);
            mHandler.sendMessage(mes);
        }
    }

    void finish() {
        // for next version
    }

    /**
     * returns android native coordinates from openalpr object
     */
    private static List<Point> getAndroidPoints(List<Coordinate> coordinates) {
        Coordinate tl = coordinates.get(0);
        Coordinate tr = coordinates.get(1);
        Coordinate br = coordinates.get(2);
        Coordinate bl = coordinates.get(3);

        final Point tlP = new Point(tl.getX(), tl.getY());
        final Point trP = new Point(tr.getX(), tr.getY());
        final Point brP = new Point(br.getX(), br.getY());
        final Point blP = new Point(bl.getX(), bl.getY());

        return new ArrayList<Point>() {{
            add(tlP);
            add(trP);
            add(brP);
            add(blP);
        }};
    }

    /**
     * returns File ref. to internally saved image
     */
    private static File saveToInternalStorage(Bitmap bitmapImage, Context context) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/com.awesomeproject/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, "frame.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath;
    }
}
