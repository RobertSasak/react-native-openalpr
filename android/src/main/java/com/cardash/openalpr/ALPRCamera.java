package com.cardash.openalpr;

import android.hardware.Camera;
import android.media.CamcorderProfile;

import org.opencv.android.JavaCameraView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;

public class ALPRCamera {

    private static ALPRCamera ourInstance;
    private final HashMap<Integer, CameraInfoWrapper> _cameraInfos;
    private static Camera mCamera = null;
    private static final Resolution RESOLUTION_480P = new Resolution(853, 480); // 480p shoots for a 16:9 HD aspect ratio, but can otherwise fall back/down to any other supported camera sizes, such as 800x480 or 720x480, if (any) present. See getSupportedPictureSizes/getSupportedVideoSizes below.
    private static final Resolution RESOLUTION_720P = new Resolution(1280, 720);
    private static final Resolution RESOLUTION_1080P = new Resolution(1920, 1080);

    public static ALPRCamera getInstance() {
        return ourInstance;
    }

    public static void createInstance() {
        ourInstance = new ALPRCamera();
    }

    public static void setCamera() {
        mCamera = JavaCameraView.getInstance();
    }

    public synchronized Camera acquireCameraInstance() {
        return mCamera;
    }

    public void releaseCameraInstance(int type) {
        Camera releasingCamera = mCamera;
        if (null != releasingCamera) {
            releasingCamera.release();
        }
    }

    public int getPreviewWidth(int type) {
        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        if (null == cameraInfo) {
            return 0;
        }
        return cameraInfo.previewWidth;
    }

    public int getPreviewHeight(int type) {
        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        if (null == cameraInfo) {
            return 0;
        }
        return cameraInfo.previewHeight;
    }

    public Camera.Size getBestSize(List<Camera.Size> supportedSizes, int maxWidth, int maxHeight) {
        Camera.Size bestSize = null;
        for (Camera.Size size : supportedSizes) {
            if (size.width > maxWidth || size.height > maxHeight) {
                continue;
            }

            if (bestSize == null) {
                bestSize = size;
                continue;
            }

            int resultArea = bestSize.width * bestSize.height;
            int newArea = size.width * size.height;

            if (newArea > resultArea) {
                bestSize = size;
            }
        }

        return bestSize;
    }

    private Camera.Size getSmallestSize(List<Camera.Size> supportedSizes) {
        Camera.Size smallestSize = null;
        for (Camera.Size size : supportedSizes) {
            if (smallestSize == null) {
                smallestSize = size;
                continue;
            }

            int resultArea = smallestSize.width * smallestSize.height;
            int newArea = size.width * size.height;

            if (newArea < resultArea) {
                smallestSize = size;
            }
        }

        return smallestSize;
    }

    private Camera.Size getClosestSize(List<Camera.Size> supportedSizes, int matchWidth, int matchHeight) {
      Camera.Size closestSize = null;
      for (Camera.Size size : supportedSizes) {
          if (closestSize == null) {
              closestSize = size;
              continue;
          }

          int currentDelta = Math.abs(closestSize.width - matchWidth) * Math.abs(closestSize.height - matchHeight);
          int newDelta = Math.abs(size.width - matchWidth) * Math.abs(size.height - matchHeight);

          if (newDelta < currentDelta) {
              closestSize = size;
          }
      }
      return closestSize;
    }

    public void setCaptureQuality(int cameraType, int captureQuality) {
        if (mCamera == null) {
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size pictureSize = null;
        List<Camera.Size> supportedSizes = parameters.getSupportedPictureSizes();

        switch (captureQuality) {
            case ALPRCameraManager.ALPRCameraCaptureQualityLow:
                pictureSize = getSmallestSize(supportedSizes);
                break;
            case ALPRCameraManager.ALPRCameraCaptureQualityMedium:
                pictureSize = supportedSizes.get(supportedSizes.size() / 2);
                break;
            case ALPRCameraManager.ALPRCameraCaptureQualityHigh:
                pictureSize = getBestSize(parameters.getSupportedPictureSizes(), Integer.MAX_VALUE, Integer.MAX_VALUE);
                break;
            case ALPRCameraManager.ALPRCameraCaptureQuality480P:
                pictureSize = getBestSize(supportedSizes, RESOLUTION_480P.width, RESOLUTION_480P.height);
                break;
            case ALPRCameraManager.ALPRCameraCaptureQuality720P:
                pictureSize = getBestSize(supportedSizes, RESOLUTION_720P.width, RESOLUTION_720P.height);
                break;
            case ALPRCameraManager.ALPRCameraCaptureQuality1080P:
                pictureSize = getBestSize(supportedSizes, RESOLUTION_1080P.width, RESOLUTION_1080P.height);
                break;
        }

        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            mCamera.setParameters(parameters);
        }
    }

    public void setTorchMode(int torchMode) {
        if (null == mCamera) {
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        String value = parameters.getFlashMode();
        switch (torchMode) {
            case ALPRCameraManager.ALPRCameraTorchModeOn:
                value = Camera.Parameters.FLASH_MODE_TORCH;
                break;
            case ALPRCameraManager.ALPRCameraTorchModeOff:
                value = Camera.Parameters.FLASH_MODE_OFF;
                break;
            case ALPRCameraManager.ALPRCameraTorchModeAuto:
                value = Camera.Parameters.FLASH_MODE_AUTO;
                break;
        }

        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(value)) {
            parameters.setFlashMode(value);
            mCamera.setParameters(parameters);
        }
    }

    public void setFlashMode(int flashMode) {
        if (null == mCamera) {
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        String value = parameters.getFlashMode();
        switch (flashMode) {
            case ALPRCameraManager.ALPRCameraFlashModeAuto:
                value = Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case ALPRCameraManager.ALPRCameraFlashModeOn:
                value = Camera.Parameters.FLASH_MODE_ON;
                break;
            case ALPRCameraManager.ALPRCameraFlashModeOff:
                value = Camera.Parameters.FLASH_MODE_OFF;
                break;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(value)) {
            parameters.setFlashMode(value);
            mCamera.setParameters(parameters);
        }
    }

    public void adjustPreviewLayout(int type) {
        if (null == mCamera) {
            return;
        }

        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size optimalPreviewSize = getBestSize(parameters.getSupportedPreviewSizes(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        int width = optimalPreviewSize.width;
        int height = optimalPreviewSize.height;

        parameters.setPreviewSize(width, height);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cameraInfo.rotation == 0 || cameraInfo.rotation == 180) {
            cameraInfo.previewWidth = width;
            cameraInfo.previewHeight = height;
        } else {
            cameraInfo.previewWidth = height;
            cameraInfo.previewHeight = width;
        }
    }

    private ALPRCamera() {
        _cameraInfos = new HashMap<>();

        // map camera types to camera indexes and collect cameras properties
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && _cameraInfos.get(ALPRCameraManager.ALPRCameraTypeBack) == null) {
                _cameraInfos.put(ALPRCameraManager.ALPRCameraTypeBack, new CameraInfoWrapper(info));
            }
        }
    }

    private class CameraInfoWrapper {
        public final Camera.CameraInfo info;
        public int rotation = 0;
        public int previewWidth = -1;
        public int previewHeight = -1;

        public CameraInfoWrapper(Camera.CameraInfo info) {
            this.info = info;
        }
    }

    private static class Resolution {
        public int width;
        public int height;

        public Resolution(final int width, final int height) {
            this.width = width;
            this.height = height;
        }
    }
}
