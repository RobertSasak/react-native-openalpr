package com.cardash.openalpr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class ReactCameraManager extends SimpleViewManager<FrameLayout> implements LifecycleEventListener {
    public static final String TAG = ReactContextBaseJavaModule.class.getSimpleName();
    public static final String REACT_CLASS = "ALPRCamera";
    WeakReference<ViewGroup> layoutRef;
    private BroadcastReceiver receiver;
    private static final String BCAST_CONFIGCHANGED = "android.intent.action.CONFIGURATION_CHANGED";

    public ReactCameraManager(ReactApplicationContext reactContext) {
        super();
        receiver = createOrientationReceiver();
        reactContext.addLifecycleEventListener(this);

    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public FrameLayout createViewInstance(final ThemedReactContext context) {

        ALPRCamera.createInstance();

        LayoutInflater inflater = LayoutInflater.from(context);
        final FrameLayout preview = (FrameLayout) inflater.inflate(R.layout.camera_layout, null);
        layoutRef = new WeakReference<ViewGroup>(preview);
        ICameraView camera = (ICameraView) preview.findViewById(R.id.camera_view);
        camera.setResultsCallback(new ALPR.ResultsCallback() {
            @Override
            public void onResults(String plate, String confidence, String processingTimeMs, List<Point> coordinates) {
                Log.i(ReactCameraManager.class.getSimpleName(), plate);
                WritableMap event = Arguments.createMap();
                event.putString("plate", plate);
                event.putString("confidence", confidence);
                event.putString("processingTimeMs", processingTimeMs);
                ReactContext reactContext = context;
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        preview.getId(),
                        "resultReady",
                        event);
            }

            @Override
            public void onFail() {

            }
        });

        return preview;
    }

    @ReactProp(name = "mounted")
    public void setMounted(FrameLayout view, @Nullable boolean mounted) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        if (mounted) {
            registerReceiver(view.getContext());
            camera.onResumeALPR();
        } else {
            camera.disableView();
        }
    }

    @ReactProp(name = "country")
    public void setCountry(FrameLayout view, @Nullable String country) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setCountry(country);
    }

    @ReactProp(name = "captureTarget")
    public void setCaptureTarget(FrameLayout view, @Nullable int captureTarget) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setTarget(captureTarget);
    }

    @ReactProp(name = "captureQuality")
    public void setCaptureQuality(FrameLayout view, @Nullable int captureQuality) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setQuality(captureQuality);
    }

    @ReactProp(name = "aspect")
    public void setAspect(FrameLayout view, @Nullable int aspect) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setAspect(aspect);
    }

    @ReactProp(name = "type")
    public void setType(FrameLayout view, @Nullable int type) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setType(type);
    }

    @ReactProp(name = "plateOutlineColor")
    public void setPlateOutlineColor(FrameLayout view, @Nullable String plateOutlineColor) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setPlateBorderColorHex(plateOutlineColor);
    }

    @ReactProp(name = "showPlateOutline")
    public void setShowPlateOutline(FrameLayout view, @Nullable Boolean showPlateOutline) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setPlateBorderEnabled(showPlateOutline);
    }

    @ReactProp(name = "rotateMode")
    public void setRotateMode(FrameLayout view, @Nullable int rotateMode) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setRotateMode(rotateMode == ALPRCameraManager.ALPRCameraRotateMode.ALPRCameraRotateModeOn);
    }

    @ReactProp(name = "torchMode")
    public void setTorchMode(FrameLayout view, @Nullable int torchMode) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setTorchMode(torchMode == ALPRCameraManager.ALPRCameraTorchMode.ALPRCameraTorchModeOn);
    }

    @ReactProp(name = "touchToFocus")
    public void setTouchToFocus(FrameLayout view, @Nullable Boolean touchToFocus) {
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.setTapToFocus(touchToFocus);
    }

    @Nullable
    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "resultReady",
                MapBuilder.of("registrationName", "onPlateRecognized")
        );
    }

    @Override
    public void onDropViewInstance(FrameLayout view) {
        super.onDropViewInstance(view);
        ICameraView camera = (ICameraView) view.findViewById(R.id.camera_view);
        camera.disableView();
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume");
        if (layoutRef == null) return;
        ViewGroup layout = layoutRef.get();
        if (layout == null) return;
        registerReceiver(layout.getContext());
        ICameraView camera = (ICameraView) layout.findViewById(R.id.camera_view);
        camera.onResumeALPR();
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause");
        if (layoutRef == null) return;
        ViewGroup layout = layoutRef.get();
        if (layout == null) return;
        unregisterReceiver(layout.getContext());
        ICameraView camera = (ICameraView) layout.findViewById(R.id.camera_view);
        camera.disableView();
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostDestroy");
        if (layoutRef == null) return;
        ViewGroup layout = layoutRef.get();
        if (layout == null) return;
        unregisterReceiver(layout.getContext());
    }

    private BroadcastReceiver createOrientationReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equals(BCAST_CONFIGCHANGED)) return;
                Log.d(TAG, "onReceive() CONFIGURATION_CHANGED");
                if (layoutRef == null) return;
                ViewGroup layout = layoutRef.get();
                if (layout == null) return;
                ICameraView camera = (ICameraView) layout.findViewById(R.id.camera_view);
                camera.disableView();
                camera.onResumeALPR();
            }
        };
    }

    private void registerReceiver(Context context) {
        Log.d(TAG, "registerReceiver()");
        final Activity activity = ALPRCameraView.scanForActivity(context);
        if (activity != null) {
            activity.registerReceiver(receiver, new IntentFilter(BCAST_CONFIGCHANGED));
        }
    }

    private void unregisterReceiver(Context context) {
        final Activity activity = ALPRCameraView.scanForActivity(context);
        if (activity == null) return;
        try {
            activity.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "receiver already unregistered");
        }
    }
}
