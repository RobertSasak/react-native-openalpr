package com.cardash.openalpr;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.facebook.react.uimanager.ThemedReactContext;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class ALPRCameraView extends JavaCameraView implements ICameraView {

    private static final String TAG = ALPRCameraView.class.getSimpleName();
    private ALPR.ResultsCallback callback;
    private int quality = Quality.MEDIUM;
    private Size highResolution;
    private Size mediumResolution;
    private Size lowResolution;
    private List<Point> coordinates;
    private int[] plateBorderRgb = new int[]{0, 0, 255};
    private boolean plateBorderEnabled;
    private String country = "us";
    private boolean tapToFocusEnabled;
    private boolean torchEnabled = false;
    private int rotation;

    public ALPRCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public ALPRCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public interface Quality {
        int LOW = 0;
        int MEDIUM = 1;
        int HIGH = 2;
    }

    private CvCameraViewListener2 createCvCameraViewListener() {
        return new CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                initResolutions();
                setFlashMode(torchEnabled);
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
                Mat rgba = inputFrame.rgba();

                if (callback != null) {
                    ALPR.getInstance().process(rgba, country, rotation, new ALPR.ResultsCallback() {
                        @Override
                        public void onResults(String plate, String confidence, String processingTimeMs, List<android.graphics.Point> coordinates) {
                            if (getContext() == null) return;
                            ALPRCameraView.this.coordinates = getOpenCVPoints(coordinates);
                            callback.onResults(plate, confidence, processingTimeMs, coordinates);
                        }

                        @Override
                        public void onFail() {
                            if (getContext() == null) return;
                            callback.onFail();
                        }
                    }, new WeakReference<>(getContext()));
                }

                return rgba;
            }
        };
    }

    public static Activity scanForActivity(Context viewContext) {
        if (viewContext == null)
            return null;
        else if (viewContext instanceof Activity)
            return (Activity) viewContext;
        else if (viewContext instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) viewContext).getBaseContext());
        else if (viewContext instanceof ThemedReactContext)
            return ((ThemedReactContext) viewContext).getCurrentActivity();
        return null;
    }

    private List<Point> getOpenCVPoints(List<android.graphics.Point> coordinates) {
        android.graphics.Point tl = coordinates.get(0);
        android.graphics.Point tr = coordinates.get(1);
        android.graphics.Point br = coordinates.get(2);
        android.graphics.Point bl = coordinates.get(3);

        final Point tlP = new Point(tl.x, tl.y);
        final Point trP = new Point(tr.x, tr.y);
        final Point brP = new Point(br.x, br.y);
        final Point blP = new Point(bl.x, bl.y);

        return new ArrayList<Point>() {{
            add(tlP);
            add(trP);
            add(brP);
            add(blP);
        }};
    }

    @Override
    protected int[] getPlateBorderRgb() {
        return plateBorderRgb;
    }

    @Override
    protected Point[] getCoordinates() {
        if (coordinates == null || !plateBorderEnabled) {
            return null;
        }
        Point tl = coordinates.get(0);
        Point br = coordinates.get(2);
        Point tlP = new Point(widthOffset + tl.x * widthRatio, heightOffset + tl.y * heightRatio);
        Point brP = new Point(widthOffset + br.x * widthRatio, heightOffset + br.y * heightRatio);

        return new Point[]{tlP, brP};
    }

    private void initResolutions() {
        List<Size> resolutionList = mCamera.getParameters().getSupportedPreviewSizes();
        highResolution = mCamera.getParameters().getPreviewSize();
        mediumResolution = highResolution;
        lowResolution = mediumResolution;

        ListIterator<Size> resolutionItr = resolutionList.listIterator();
        while (resolutionItr.hasNext()) {
            Size s = resolutionItr.next();
            if (s.width < highResolution.width && s.height < highResolution.height && mediumResolution.equals(highResolution)) {
                mediumResolution = s;
            } else if (s.width < mediumResolution.width && s.height < mediumResolution.height) {
                lowResolution = s;
            }
        }
        if (lowResolution.equals(highResolution)) {
            lowResolution = mediumResolution;
        }
        applyQuality(quality);
    }

    private void setResolution(Size resolution) {
        if (resolution == null) return;
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public void setQuality(int captureQuality) {
        switch (captureQuality) {
            case ALPRCameraManager.ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetLow:
                this.quality = ALPRCameraView.Quality.LOW;
                this.setQuality = 0;
                break;
            case ALPRCameraManager.ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetMedium:
                this.quality = ALPRCameraView.Quality.MEDIUM;
                this.setQuality = 1;
                break;
            case ALPRCameraManager.ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetHigh:
            case ALPRCameraManager.ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetPhoto:
            case ALPRCameraManager.ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset480p:
            case ALPRCameraManager.ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset720p:
            case ALPRCameraManager.ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset1080p:
                this.quality = ALPRCameraView.Quality.HIGH;
                this.setQuality = 2;
                break;

        }
        applyQuality(quality);
    }

    @Override
    public void setAspect(int aspect) {
        disableView();
        switch (aspect) {
            case ALPRCameraManager.ALPRCameraAspect.ALPRCameraAspectFill:
                this.aspect = JavaCameraView.ALPRCameraAspect.ALPRCameraAspectFill;
                break;
            case ALPRCameraManager.ALPRCameraAspect.ALPRCameraAspectFit:
                this.aspect = JavaCameraView.ALPRCameraAspect.ALPRCameraAspectFit;
                break;
            case ALPRCameraManager.ALPRCameraAspect.ALPRCameraAspectStretch:
                this.aspect = JavaCameraView.ALPRCameraAspect.ALPRCameraAspectStretch;
                break;
        }
        onResumeALPR();
    }

    private void applyQuality(int quality) {
        switch (quality) {
            case Quality.LOW:
                setResolution(lowResolution);
                break;
            case Quality.MEDIUM:
                setResolution(mediumResolution);
                break;
            case Quality.HIGH:
                setResolution(highResolution);
                break;
        }
    }

    @Override
    public void setResultsCallback(ALPR.ResultsCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onResumeALPR() {
        if (getContext() == null) return;
        BaseLoaderCallback loaderCallback = new BaseLoaderCallback(getContext()) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i(TAG, "OpenCV loaded successfully");
                        if (getContext() != null) {
                            setCvCameraViewListener(createCvCameraViewListener());
                            ALPRCameraView.this.enableView();
                        }
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getContext(), loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (tapToFocusEnabled && mCamera != null) {
            Camera camera = mCamera;
            camera.cancelAutoFocus();
            Rect focusRect = new Rect(-1000, -1000, 1000, 0);

            Parameters parameters = camera.getParameters();
            if (parameters.getFocusMode().equals(Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            }

            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Area> mylist = new ArrayList<Area>();
                mylist.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(mylist);
            }

            try {
                camera.cancelAutoFocus();
                camera.setParameters(parameters);
                camera.startPreview();
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (camera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            Parameters parameters = camera.getParameters();
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                            if (parameters.getMaxNumFocusAreas() > 0) {
                                parameters.setFocusAreas(null);
                            }
                            camera.setParameters(parameters);
                            camera.startPreview();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "onTouchEvent", e);
            }
        }
        return true;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    @Override
    public void setTapToFocus(boolean enabled) {
        tapToFocusEnabled = enabled;
    }

    @Override
    public void setPlateBorderEnabled(boolean enabled) {
        plateBorderEnabled = enabled;
    }

    @Override
    public void setPlateBorderColorHex(String colorStr) {
        colorStr = colorStr.replace("#", "");
        int color = Integer.parseInt(colorStr, 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        plateBorderRgb = new int[]{r, g, b};
    }

    private void setFlashMode(boolean torchEnabled) {
        if (mCamera == null) {
            return;
        }
        Parameters params = mCamera.getParameters();
        List<String> FlashModes = params.getSupportedFlashModes();
        if (torchEnabled) {
            if (FlashModes != null && FlashModes.contains(Parameters.FLASH_MODE_TORCH)) {
                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
            } else {
                Log.e(TAG, "Torch Mode not supported");
            }
        } else {
            if (FlashModes != null && FlashModes.contains(Parameters.FLASH_MODE_OFF)) {
                params.setFlashMode(Parameters.FLASH_MODE_OFF);
            }
        }
        mCamera.setParameters(params);
    }

    @Override
    public void setRotateMode(boolean isLandscape) {
        Context context = getContext();
        if (context == null) return;
        Activity activity = scanForActivity(context);
        if (activity == null) return;
        activity.setRequestedOrientation(isLandscape
                ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void setTorchMode(boolean enabled) {
        this.torchEnabled = enabled;
        setFlashMode(enabled);
    }

    @Override
    public void disableView() {
        removeCvCameraViewListener();
        super.disableView();
        ALPR.getInstance().finish();
    }
}
