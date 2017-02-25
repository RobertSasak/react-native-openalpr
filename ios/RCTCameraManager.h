//
//  CameraManager.h
//  RNOpenAlpr
//
//  Created by Evan Rosenfeld on 2/24/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <React/RCTViewManager.h>
#import <AVFoundation/AVFoundation.h>

#ifdef __cplusplus
#include "opencv2/highgui/highgui.hpp"
#import <opencv2/videoio/cap_ios.h>
using namespace cv;
#endif

@class RCTCamera;

typedef NS_ENUM(NSInteger, RCTCameraAspect) {
    RCTCameraAspectFill = 0,
    RCTCameraAspectFit = 1,
    RCTCameraAspectStretch = 2
};

typedef NS_ENUM(NSInteger, RCTCameraCaptureSessionPreset) {
    RCTCameraCaptureSessionPresetLow = 0,
    RCTCameraCaptureSessionPresetMedium = 1,
    RCTCameraCaptureSessionPresetHigh = 2,
    RCTCameraCaptureSessionPresetPhoto = 3,
    RCTCameraCaptureSessionPreset480p = 4,
    RCTCameraCaptureSessionPreset720p = 5,
    RCTCameraCaptureSessionPreset1080p = 6
};

typedef NS_ENUM(NSInteger, RCTCameraTorchMode) {
    RCTCameraTorchModeOff = AVCaptureTorchModeOff,
    RCTCameraTorchModeOn = AVCaptureTorchModeOn,
    RCTCameraTorchModeAuto = AVCaptureTorchModeAuto
};

@interface RCTCameraManager : RCTViewManager<AVCaptureVideoDataOutputSampleBufferDelegate>

@property (nonatomic, strong) dispatch_queue_t sessionQueue;
@property (nonatomic, strong) AVCaptureSession *session;
@property (nonatomic, strong) AVCaptureDeviceInput *videoCaptureDeviceInput;
@property (nonatomic, strong) id runtimeErrorHandlingObserver;
@property (nonatomic, assign) NSInteger presetCamera;
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, strong) RCTCamera *camera;


- (void)initializeCaptureSessionInput:(NSString*)type;
- (void)startSession;
- (void)stopSession;
- (void)focusAtThePoint:(CGPoint) atPoint;

@end
