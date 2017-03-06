//
//  CameraManager.m
//  RNOpenAlpr
//
//  Created by Evan Rosenfeld on 2/24/17.
//  Copyright © 2017 CarDash. All rights reserved.
//

#import "ALPRCameraManager.h"
#import "ALPRCamera.h"
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>
#import <React/RCTLog.h>
#import <React/UIView+React.h>
#import <AVFoundation/AVFoundation.h>
#import "PlateScanner.h"


#pragma mark OpenCV -


void rot90(cv::Mat &matImage, int rotflag) {
    // 1=CW, 2=CCW, 3=180
    if (rotflag == 1) {
        // transpose+flip(1)=CW
        transpose(matImage, matImage);
        flip(matImage, matImage, 1);
    } else if (rotflag == 2) {
        // transpose+flip(0)=CCW
        transpose(matImage, matImage);
        flip(matImage, matImage, 0);
    } else if (rotflag == 3){
        // flip(-1)=180
        flip(matImage, matImage, -1);
    }
}

#pragma mark Implementation -

@interface ALPRCameraManager () {
    dispatch_queue_t videoDataOutputQueue;
    UIDeviceOrientation deviceOrientation;
}
@property (atomic) BOOL isProcessingFrame;

@end

@implementation ALPRCameraManager

RCT_EXPORT_MODULE(ALPRCameraManager);

- (UIView *)view
{
    self.session = [AVCaptureSession new];
#if !(TARGET_IPHONE_SIMULATOR)
    self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.session];
    self.previewLayer.needsDisplayOnBoundsChange = YES;
#endif
    
    if(!self.camera){
        self.camera = [[ALPRCamera alloc] initWithManager:self bridge:self.bridge];
    }
    return self.camera;
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"Aspect": @{
                     @"stretch": @(ALPRCameraAspectStretch),
                     @"fit": @(ALPRCameraAspectFit),
                     @"fill": @(ALPRCameraAspectFill)
                     },
             @"CaptureQuality": @{
                     @"low": @(ALPRCameraCaptureSessionPresetLow),
                     @"AVCaptureSessionPresetLow": @(ALPRCameraCaptureSessionPresetLow),
                     @"medium": @(ALPRCameraCaptureSessionPresetMedium),
                     @"AVCaptureSessionPresetMedium": @(ALPRCameraCaptureSessionPresetMedium),
                     @"high": @(ALPRCameraCaptureSessionPresetHigh),
                     @"AVCaptureSessionPresetHigh": @(ALPRCameraCaptureSessionPresetHigh),
                     @"photo": @(ALPRCameraCaptureSessionPresetPhoto),
                     @"AVCaptureSessionPresetPhoto": @(ALPRCameraCaptureSessionPresetPhoto),
                     @"480p": @(ALPRCameraCaptureSessionPreset480p),
                     @"AVCaptureSessionPreset640x480": @(ALPRCameraCaptureSessionPreset480p),
                     @"720p": @(ALPRCameraCaptureSessionPreset720p),
                     @"AVCaptureSessionPreset1280x720": @(ALPRCameraCaptureSessionPreset720p),
                     @"1080p": @(ALPRCameraCaptureSessionPreset1080p),
                     @"AVCaptureSessionPreset1920x1080": @(ALPRCameraCaptureSessionPreset1080p)
                     },
             @"TorchMode": @{
                     @"off": @(ALPRCameraTorchModeOff),
                     @"on": @(ALPRCameraTorchModeOn),
                     @"auto": @(ALPRCameraTorchModeAuto)
                     }
             };
}

RCT_EXPORT_VIEW_PROPERTY(showPlateOutline, BOOL);
RCT_EXPORT_VIEW_PROPERTY(plateOutlineColor, NSString);
RCT_EXPORT_VIEW_PROPERTY(touchToFocus, BOOL);

RCT_CUSTOM_VIEW_PROPERTY(country, NSString, ALPRCamera) {
    NSString *strValue = [RCTConvert NSString:json];
    [[PlateScanner sharedInstance] setCountry: strValue];
}

RCT_CUSTOM_VIEW_PROPERTY(captureQuality, NSInteger, ALPRCamera) {
    NSInteger quality = [RCTConvert NSInteger:json];
    NSString *qualityString;
    switch (quality) {
        default:
        case ALPRCameraCaptureSessionPresetHigh:
            qualityString = AVCaptureSessionPresetHigh;
            break;
        case ALPRCameraCaptureSessionPresetMedium:
            qualityString = AVCaptureSessionPresetMedium;
            break;
        case ALPRCameraCaptureSessionPresetLow:
            qualityString = AVCaptureSessionPresetLow;
            break;
        case ALPRCameraCaptureSessionPresetPhoto:
            qualityString = AVCaptureSessionPresetPhoto;
            break;
        case ALPRCameraCaptureSessionPreset1080p:
            qualityString = AVCaptureSessionPreset1920x1080;
            break;
        case ALPRCameraCaptureSessionPreset720p:
            qualityString = AVCaptureSessionPreset1280x720;
            break;
        case ALPRCameraCaptureSessionPreset480p:
            qualityString = AVCaptureSessionPreset640x480;
            break;
    }
    
    [self setCaptureQuality:qualityString];
}

RCT_CUSTOM_VIEW_PROPERTY(aspect, NSInteger, ALPRCamera) {
    NSInteger aspect = [RCTConvert NSInteger:json];
    NSString *aspectString;
    switch (aspect) {
        default:
        case ALPRCameraAspectFill:
            aspectString = AVLayerVideoGravityResizeAspectFill;
            break;
        case ALPRCameraAspectFit:
            aspectString = AVLayerVideoGravityResizeAspect;
            break;
        case ALPRCameraAspectStretch:
            aspectString = AVLayerVideoGravityResize;
            break;
    }
    
    self.previewLayer.videoGravity = aspectString;
}


RCT_CUSTOM_VIEW_PROPERTY(torchMode, NSInteger, ALPRCamera) {
    dispatch_async(self.sessionQueue, ^{
        NSInteger torchMode = [RCTConvert NSInteger:json];
        AVCaptureDevice *device = [self.videoCaptureDeviceInput device];
        NSError *error = nil;
        
        if (![device hasTorch]) return;
        if (![device lockForConfiguration:&error]) {
            NSLog(@"%@", error);
            return;
        }
        [device setTorchMode: (AVCaptureTorchMode)torchMode];
        [device unlockForConfiguration];
    });
}

RCT_EXPORT_VIEW_PROPERTY(onPlateRecognized, RCTBubblingEventBlock)

- (id)init {
    if ((self = [super init])) {
        self.sessionQueue = dispatch_queue_create("cameraManagerQueue", DISPATCH_QUEUE_SERIAL);
    }
    return self;
}


RCT_EXPORT_METHOD(checkVideoAuthorizationStatus:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
    __block NSString *mediaType = AVMediaTypeVideo;
    
    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:^(BOOL granted) {
        resolve(@(granted));
    }];
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput
didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
       fromConnection:(AVCaptureConnection *)connection {
    @autoreleasepool {
        if (self.isProcessingFrame) {
            return;
        }
        self.isProcessingFrame = YES;
        
        CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
        CVPixelBufferLockBaseAddress(imageBuffer, 0);
        
        // Y_PLANE
        int plane = 0;
        char *planeBaseAddress = (char *)CVPixelBufferGetBaseAddressOfPlane(imageBuffer, plane);
        
        size_t width = CVPixelBufferGetWidthOfPlane(imageBuffer, plane);
        size_t height = CVPixelBufferGetHeightOfPlane(imageBuffer, plane);
        size_t bytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(imageBuffer, plane);
        
        int numChannels = 1;
        
        cv::Mat src = cv::Mat(cvSize((int)width, (int)height), CV_8UC(numChannels), planeBaseAddress, (int)bytesPerRow);
        int rotate = 0;
        if (deviceOrientation == UIDeviceOrientationPortrait) {
            rotate = 1;
        } else if (deviceOrientation == UIDeviceOrientationLandscapeRight) {
            rotate = 3;
        } else if (deviceOrientation == UIDeviceOrientationPortraitUpsideDown) {
            rotate = 2;
        }
        rot90(src, rotate);
        
//        NSDate *date = [NSDate date];
        
        [[PlateScanner sharedInstance] scanImage:src onSuccess:^(PlateResult *result) {
            if (result && self.camera.onPlateRecognized) {
                self.camera.onPlateRecognized(@{
                    @"confidence": @(result.confidence),
                    @"plate": result.plate
                });
            }
            
            CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
//            NSLog(@"Time: %f", -[date timeIntervalSinceNow]);
            self.isProcessingFrame = NO;
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.camera updatePlateBorder:result orientation:deviceOrientation];
            });
            
        } onFailure:^(NSError *err) {
//            NSLog(@"Error: %@", err);
            CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
            self.isProcessingFrame = NO;
        }];
    }
}

- (void)startSession {
#if TARGET_IPHONE_SIMULATOR
    return;
#endif
    dispatch_async(self.sessionQueue, ^{
        if (self.presetCamera == AVCaptureDevicePositionUnspecified) {
            self.presetCamera = AVCaptureDevicePositionBack;
        }
        
        AVCaptureVideoDataOutput *videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
        
        // The algorithm is going to convert to grayscale anyways, so let's use a format that makes it
        // easy to extract
        NSDictionary *videoOutputSettings = @{
            (NSString*)kCVPixelBufferPixelFormatTypeKey: @(kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange)
        };
        [videoDataOutput setVideoSettings:videoOutputSettings];
        videoDataOutput.alwaysDiscardsLateVideoFrames = YES;
        videoDataOutputQueue = dispatch_queue_create("OpenALPR-video-queue", NULL);
        [videoDataOutput setSampleBufferDelegate:self queue:videoDataOutputQueue];
        
        
        if ([self.session canAddOutput:videoDataOutput]) {
            [self.session addOutput:videoDataOutput];
        }
        
        __weak ALPRCameraManager *weakSelf = self;
        [self setRuntimeErrorHandlingObserver:[NSNotificationCenter.defaultCenter addObserverForName:AVCaptureSessionRuntimeErrorNotification object:self.session queue:nil usingBlock:^(NSNotification *note) {
            ALPRCameraManager *strongSelf = weakSelf;
            dispatch_async(strongSelf.sessionQueue, ^{
                // Manually restarting the session since it must have been stopped due to an error.
                [strongSelf.session startRunning];
            });
        }]];
        
        [self.session startRunning];
        [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(deviceDidRotate:) name:UIDeviceOrientationDidChangeNotification object:nil];
        deviceOrientation = [[UIDevice currentDevice] orientation];
    });
}

- (void)deviceDidRotate:(NSNotification *)notification
{
    UIDeviceOrientation currentOrientation = [[UIDevice currentDevice] orientation];
    
    // Ignore changes in device orientation if unknown, face up, or face down.
    if (!UIDeviceOrientationIsValidInterfaceOrientation(currentOrientation)) {
        return;
    }
    deviceOrientation = currentOrientation;
}

- (void)stopSession {
#if TARGET_IPHONE_SIMULATOR
    self.camera = nil;
    return;
#endif
    dispatch_async(self.sessionQueue, ^{
        self.camera = nil;
        [self.previewLayer removeFromSuperlayer];
        [self.session commitConfiguration];
        [self.session stopRunning];
        for(AVCaptureInput *input in self.session.inputs) {
            [self.session removeInput:input];
        }
        
        for(AVCaptureOutput *output in self.session.outputs) {
            [self.session removeOutput:output];
        }
        
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        if ([[UIDevice currentDevice] isGeneratingDeviceOrientationNotifications]) {
            [[UIDevice currentDevice] endGeneratingDeviceOrientationNotifications];
        }
    });
}

- (void)initializeCaptureSessionInput:(NSString *)type {
    dispatch_async(self.sessionQueue, ^{
        [self.session beginConfiguration];
        
        NSError *error = nil;
        AVCaptureDevice *captureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
        
        if (captureDevice == nil) {
            return;
        }
        
        AVCaptureDeviceInput *captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:captureDevice error:&error];
        
        if (error || captureDeviceInput == nil) {
            NSLog(@"%@", error);
            return;
        }
        
        if (type == AVMediaTypeVideo) {
            [self.session removeInput:self.videoCaptureDeviceInput];
        }
        
        if ([self.session canAddInput:captureDeviceInput]) {
            [self.session addInput:captureDeviceInput];
            if (type == AVMediaTypeVideo) {
                self.videoCaptureDeviceInput = captureDeviceInput;
            }
        }
        
        [self.session commitConfiguration];
    });
}

- (void)subjectAreaDidChange:(NSNotification *)notification
{
    CGPoint devicePoint = CGPointMake(.5, .5);
    [self focusWithMode:AVCaptureFocusModeContinuousAutoFocus exposeWithMode:AVCaptureExposureModeContinuousAutoExposure atDevicePoint:devicePoint monitorSubjectAreaChange:NO];
}

- (void)focusWithMode:(AVCaptureFocusMode)focusMode exposeWithMode:(AVCaptureExposureMode)exposureMode atDevicePoint:(CGPoint)point monitorSubjectAreaChange:(BOOL)monitorSubjectAreaChange
{
    dispatch_async([self sessionQueue], ^{
        AVCaptureDevice *device = [[self videoCaptureDeviceInput] device];
        NSError *error = nil;
        if ([device lockForConfiguration:&error])
        {
            if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:focusMode])
            {
                [device setFocusMode:focusMode];
                [device setFocusPointOfInterest:point];
            }
            if ([device isExposurePointOfInterestSupported] && [device isExposureModeSupported:exposureMode])
            {
                [device setExposureMode:exposureMode];
                [device setExposurePointOfInterest:point];
            }
            [device setSubjectAreaChangeMonitoringEnabled:monitorSubjectAreaChange];
            [device unlockForConfiguration];
        }
        else
        {
            NSLog(@"%@", error);
        }
    });
}

- (void)focusAtThePoint:(CGPoint) atPoint;
{
    Class captureDeviceClass = NSClassFromString(@"AVCaptureDevice");
    if (captureDeviceClass != nil) {
        dispatch_async([self sessionQueue], ^{
            AVCaptureDevice *device = [[self videoCaptureDeviceInput] device];
            if([device isFocusPointOfInterestSupported] &&
               [device isFocusModeSupported:AVCaptureFocusModeAutoFocus]) {
                CGRect screenRect = [[UIScreen mainScreen] bounds];
                double screenWidth = screenRect.size.width;
                double screenHeight = screenRect.size.height;
                double focus_x = atPoint.x/screenWidth;
                double focus_y = atPoint.y/screenHeight;
                if([device lockForConfiguration:nil]) {
                    [device setFocusPointOfInterest:CGPointMake(focus_x,focus_y)];
                    [device setFocusMode:AVCaptureFocusModeAutoFocus];
                    if ([device isExposureModeSupported:AVCaptureExposureModeAutoExpose]){
                        [device setExposureMode:AVCaptureExposureModeAutoExpose];
                    }
                    [device unlockForConfiguration];
                }
            }
        });
    }
}

- (void)setCaptureQuality:(NSString *)quality
{
#if !(TARGET_IPHONE_SIMULATOR)
    if (quality) {
        [self.session beginConfiguration];
        if ([self.session canSetSessionPreset:quality]) {
            self.session.sessionPreset = quality;
        }
        [self.session commitConfiguration];
    }
#endif
}


@end
