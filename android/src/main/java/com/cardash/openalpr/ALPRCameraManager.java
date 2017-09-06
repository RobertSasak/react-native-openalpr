package com.cardash.openalpr;

import android.hardware.Camera;
import android.os.Environment;
import android.media.MediaScannerConnection;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class ALPRCameraManager extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String TAG = ReactContextBaseJavaModule.class.getSimpleName();

    private static ReactApplicationContext _reactContext;
    private static MediaActionSound sound = new MediaActionSound();
    public static final int ALPRCameraTypeBack = 2;
    public static final int ALPRCameraFlashModeOff = 0;
    public static final int ALPRCameraFlashModeOn = 1;
    public static final int ALPRCameraFlashModeAuto = 2;
    public static final int ALPRCameraTorchModeOff = 0;
    public static final int ALPRCameraTorchModeOn = 1;
    public static final int ALPRCameraTorchModeAuto = 2;
    public static final int ALPRCameraCaptureTargetMemory = 0;
    public static final int ALPRCameraCaptureTargetDisk = 1;
    public static final int ALPRCameraCaptureTargetCameraRoll = 2;
    public static final int ALPRCameraCaptureTargetTemp = 3;
    public static final int ALPRCameraCaptureQualityPreview = 3;
    public static final int ALPRCameraCaptureQualityHigh = 2;
    public static final int ALPRCameraCaptureQualityMedium = 1;
    public static final int ALPRCameraCaptureQualityLow = 0;
    public static final int ALPRCameraCaptureQuality1080P = 6;
    public static final int ALPRCameraCaptureQuality720P = 5;
    public static final int ALPRCameraCaptureQuality480P = 4;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private Boolean mSafeToCapture = true;

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }

    public ALPRCameraManager(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;
        _reactContext.addLifecycleEventListener(this);
        sound.load(MediaActionSound.SHUTTER_CLICK);
    }

    public static ReactApplicationContext getReactContextSingleton() {
      return _reactContext;
    }


    public interface ALPRCameraAspect {
        int ALPRCameraAspectFill = 0;
        int ALPRCameraAspectFit = 1;
        int ALPRCameraAspectStretch = 2;
    };

    public interface ALPRCameraCaptureTarget {
      int ALPRCameraCaptureTargetMemory = 0;
      int ALPRCameraCaptureTargetDisk = 1;
      int ALPRCameraCaptureTargetCameraRoll = 2;
      int ALPRCameraCaptureTargetTemp = 3;
    }

    public interface ALPRCameraCaptureSessionPreset {
        int ALPRCameraCaptureSessionPresetLow = 0;
        int ALPRCameraCaptureSessionPresetMedium = 1;
        int ALPRCameraCaptureSessionPresetHigh = 2;
        int ALPRCameraCaptureSessionPresetPhoto = 3;
        int ALPRCameraCaptureSessionPreset480p = 4;
        int ALPRCameraCaptureSessionPreset720p = 5;
        int ALPRCameraCaptureSessionPreset1080p = 6;
    };

    public interface ALPRCameraType {
        int ALPRCameraTypeFront = 1;
        int ALPRCameraTypeBack = 2;
    }

    public interface ALPRCameraRotateMode {
        int ALPRCameraRotateModeOff = 0;
        int ALPRCameraRotateModeOn = 1;
    };

    public interface ALPRCameraTorchMode {
        int ALPRCameraTorchModeOff = 0;
        int ALPRCameraTorchModeOn = 1;
        int ALPRCameraTorchModeAuto = 2;
    };

    public interface ALPRCameraFlashMode {
        int ALPRCameraFlashModeOff = 0;
        int ALPRCameraFlashModeOn = 1;
        int ALPRCameraFlashModeAuto = 2;
    };

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        WritableMap aspectMap = Arguments.createMap();
        aspectMap.putInt("stretch", ALPRCameraAspect.ALPRCameraAspectStretch);
        aspectMap.putInt("fit", ALPRCameraAspect.ALPRCameraAspectFit);
        aspectMap.putInt("fill", ALPRCameraAspect.ALPRCameraAspectFill);

        WritableMap captureTargetMap = Arguments.createMap();
        captureTargetMap.putInt("memory", ALPRCameraCaptureTarget.ALPRCameraCaptureTargetMemory);
        captureTargetMap.putInt("disk", ALPRCameraCaptureTarget.ALPRCameraCaptureTargetDisk);
        captureTargetMap.putInt("cameraRoll", ALPRCameraCaptureTarget.ALPRCameraCaptureTargetCameraRoll);
        captureTargetMap.putInt("temp", ALPRCameraCaptureTarget.ALPRCameraCaptureTargetTemp);

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

        WritableMap flashModeMap = Arguments.createMap();
        flashModeMap.putInt("off", ALPRCameraFlashMode.ALPRCameraFlashModeOff);
        flashModeMap.putInt("on", ALPRCameraFlashMode.ALPRCameraFlashModeOn);
        flashModeMap.putInt("auto", ALPRCameraFlashMode.ALPRCameraFlashModeAuto);

        WritableMap cameraTypeMap = Arguments.createMap();
        cameraTypeMap.putInt("front", ALPRCameraType.ALPRCameraTypeFront);
        cameraTypeMap.putInt("back", ALPRCameraType.ALPRCameraTypeBack);

        constants.put("Aspect", aspectMap);
        constants.put("CaptureTarget", captureTargetMap);
        constants.put("CaptureQuality", captureQualityMap);
        constants.put("Type", cameraTypeMap);
        constants.put("RotateMode", rotateModeMap);
        constants.put("TorchMode", torchModeMap);
        return constants;
    }


    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise) {
        Camera camera = ALPRCamera.getInstance().acquireCameraInstance();
        if (null == camera) {
            promise.reject("No camera found. " + options.getInt("type"));
            return;
        }

        ALPRCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getInt("captureQuality"));

        if (options.hasKey("playSoundOnCapture") && options.getBoolean("playSoundOnCapture")) {
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }

        if (options.hasKey("captureQuality")) {
            ALPRCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getInt("captureQuality"));
        }

        Camera.PictureCallback captureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        processImage(new MutableImage(data), options, promise);
                    }
                });

                mSafeToCapture = true;
            }
        };

        if(mSafeToCapture) {
          try {
            camera.takePicture(null, null, captureCallback);
            mSafeToCapture = false;
          } catch(RuntimeException ex) {
              Log.e(TAG, "Couldn't capture photo.", ex);
          }
        }
    }


    /**
     * synchronized in order to prevent the user crashing the app by taking many photos and them all being processed
     * concurrently which would blow the memory (esp on smaller devices), and slow things down.
     */
    private synchronized void processImage(MutableImage mutableImage, ReadableMap options, Promise promise) {
        try {
            mutableImage.fixOrientation();
        } catch (MutableImage.ImageMutationFailedException e) {
            promise.reject("Error mirroring image", e);
        }

        int jpegQualityPercent = 80;
        if(options.hasKey("jpegQuality")) {
            jpegQualityPercent = options.getInt("jpegQuality");
        }

        switch (options.getInt("target")) {
            case ALPRCameraCaptureTargetMemory:
                String encoded = mutableImage.toBase64(jpegQualityPercent);
                WritableMap response = new WritableNativeMap();
                response.putString("data", encoded);
                promise.resolve(response);
                break;
            case ALPRCameraCaptureTargetCameraRoll: {
                File cameraRollFile = getOutputCameraRollFile(MEDIA_TYPE_IMAGE);
                if (cameraRollFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(cameraRollFile, options, jpegQualityPercent);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                addToMediaStore(cameraRollFile.getAbsolutePath());

                resolve(cameraRollFile, promise);

                break;
            }
            case ALPRCameraCaptureTargetDisk: {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(pictureFile, options, 85);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                resolve(pictureFile, promise);

                break;
            }
            case ALPRCameraCaptureTargetTemp: {
                File tempFile = getTempMediaFile(MEDIA_TYPE_IMAGE);
                if (tempFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(tempFile, options, 85);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                resolve(tempFile, promise);

                break;
            }
        }
    }

    private File getTempMediaFile(int type) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputDir = _reactContext.getCacheDir();
            File outputFile;

            if (type == MEDIA_TYPE_IMAGE) {
                outputFile = File.createTempFile("IMG_" + timeStamp, ".jpg", outputDir);
            } else {
                Log.e(TAG, "Unsupported media type:" + type);
                return null;
            }
            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private void addToMediaStore(String path) {
        MediaScannerConnection.scanFile(_reactContext, new String[] { path }, null, null);
    }

    private File getOutputFile(int type, File storageDir) {
        // Create the storage directory if it does not exist
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory:" + storageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
        String fileName = String.format("%s", new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        if (type == MEDIA_TYPE_IMAGE) {
            fileName = String.format("IMG_%s.jpg", fileName);
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return new File(String.format("%s%s%s", storageDir.getPath(), File.separator, fileName));
    }

    @ReactMethod
    public void hasFlash(ReadableMap options, final Promise promise) {
        Camera camera = ALPRCamera.getInstance().acquireCameraInstance();
        if (null == camera) {
            promise.reject("No camera found.");
            return;
        }
        List<String> flashModes = camera.getParameters().getSupportedFlashModes();
        promise.resolve(null != flashModes && !flashModes.isEmpty());
    }

    private File getOutputMediaFile(int type) {
        // Get environment directory type id from requested media type.
        String environmentDirectoryType;
        if (type == MEDIA_TYPE_IMAGE) {
            environmentDirectoryType = Environment.DIRECTORY_PICTURES;
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(environmentDirectoryType)
        );
    }

    private File getOutputCameraRollFile(int type) {
        return getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        );
    }

    private void resolve(final File imageFile, final Promise promise) {
        final WritableMap response = new WritableNativeMap();
        response.putString("path", Uri.fromFile(imageFile).toString());

        // borrowed from react-native CameraRollManager, it finds and returns the 'internal'
        // representation of the image uri that was just saved.
        // e.g. content://media/external/images/media/123
        MediaScannerConnection.scanFile(
                _reactContext,
                new String[]{imageFile.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        if (uri != null) {
                            response.putString("mediaUri", uri.toString());
                        }

                        promise.resolve(response);
                    }
                });
    }

    @Override
    public String getName() {
        return "ALPRCameraManager";
    }
}
