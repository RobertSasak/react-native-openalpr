package com.cardash.openalpr;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;


public class ALPRCameraManager extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String TAG = ReactContextBaseJavaModule.class.getSimpleName();

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {

    }

    public interface ALPRCameraAspect {
        int ALPRCameraAspectFill = 0;
        int ALPRCameraAspectFit = 1;
        int ALPRCameraAspectStretch = 2;
    };

    public interface ALPRCameraCaptureSessionPreset {
        int ALPRCameraCaptureSessionPresetLow = 0;
        int ALPRCameraCaptureSessionPresetMedium = 1;
        int ALPRCameraCaptureSessionPresetHigh = 2;
        int ALPRCameraCaptureSessionPresetPhoto = 3;
        int ALPRCameraCaptureSessionPreset480p = 4;
        int ALPRCameraCaptureSessionPreset720p = 5;
        int ALPRCameraCaptureSessionPreset1080p = 6;
    };

    public interface ALPRCameraRotateMode {
        int ALPRCameraRotateModeOff = 0;
        int ALPRCameraRotateModeOn = 1;
    };

    public interface ALPRCameraTorchMode {
        int ALPRCameraTorchModeOff = 0;
        int ALPRCameraTorchModeOn = 1;
        int ALPRCameraTorchModeAuto = 2;
    };

    public ALPRCameraManager(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        WritableMap aspectMap = Arguments.createMap();
        aspectMap.putInt("stretch", ALPRCameraAspect.ALPRCameraAspectStretch);
        aspectMap.putInt("fit", ALPRCameraAspect.ALPRCameraAspectFit);
        aspectMap.putInt("fill", ALPRCameraAspect.ALPRCameraAspectFill);

        WritableMap captureQualityMap = Arguments.createMap();
        captureQualityMap.putInt("low", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetLow);
        captureQualityMap.putInt("AVCaptureSessionPresetLow", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetLow);
        captureQualityMap.putInt("medium", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetMedium);
        captureQualityMap.putInt("AVCaptureSessionPresetMedium", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetMedium);
        captureQualityMap.putInt("high", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetHigh);
        captureQualityMap.putInt("AVCaptureSessionPresetHigh", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetHigh);
        captureQualityMap.putInt("photo", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetPhoto);
        captureQualityMap.putInt("AVCaptureSessionPresetPhoto", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPresetPhoto);
        captureQualityMap.putInt("480p", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset480p);
        captureQualityMap.putInt("AVCaptureSessionPreset640x480", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset480p);
        captureQualityMap.putInt("720p", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset720p);
        captureQualityMap.putInt("AVCaptureSessionPreset1280x720", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset720p);
        captureQualityMap.putInt("1080p", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset1080p);
        captureQualityMap.putInt("AVCaptureSessionPreset1920x1080", ALPRCameraCaptureSessionPreset.ALPRCameraCaptureSessionPreset1080p);

        WritableMap rotateModeMap = Arguments.createMap();
        rotateModeMap.putInt("off", ALPRCameraRotateMode.ALPRCameraRotateModeOff);
        rotateModeMap.putInt("on", ALPRCameraRotateMode.ALPRCameraRotateModeOn);

        WritableMap torchModeMap = Arguments.createMap();
        torchModeMap.putInt("off", ALPRCameraTorchMode.ALPRCameraTorchModeOff);
        torchModeMap.putInt("on", ALPRCameraTorchMode.ALPRCameraTorchModeOn);
        torchModeMap.putInt("auto", ALPRCameraTorchMode.ALPRCameraTorchModeAuto);

        constants.put("Aspect", aspectMap);
        constants.put("CaptureQuality", captureQualityMap);
        constants.put("RotateMode", rotateModeMap);
        constants.put("TorchMode", torchModeMap);
        return constants;
    }

    @Override
    public String getName() {
        return "ALPRCameraManager";
    }
}
