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

@interface ALPRCameraManager () <AVCapturePhotoCaptureDelegate>  {
    dispatch_queue_t videoDataOutputQueue;
    UIDeviceOrientation deviceOrientation;
}
@property (atomic) BOOL isProcessingFrame;
@property(nonatomic, strong) AVCapturePhotoOutput *avCaptureOutput;
@property(nonatomic, strong) NSHashTable *takePictureParams;
@property(nonatomic, strong) NSDictionary *takePictureOptions;
@property(nonatomic, strong) RCTPromiseResolveBlock takePictureResolve;
@property(nonatomic, strong) RCTPromiseRejectBlock takePictureReject;

@end

@implementation ALPRCameraManager

RCT_EXPORT_MODULE(ALPRCameraManager);

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

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
                     },
             @"RotateMode": @{
                     @"off": @false,
                     @"on": @true
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

RCT_EXPORT_METHOD(takePicture:(NSDictionary *)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    self.takePictureOptions = options;
    self.takePictureResolve = resolve;
    self.takePictureReject = reject;
    
    AVCapturePhotoSettings *settings = [AVCapturePhotoSettings photoSettings];
    [self.avCaptureOutput capturePhotoWithSettings:settings delegate:self];
}

- (void)captureOutput:(AVCapturePhotoOutput *)output didFinishProcessingPhoto:(AVCapturePhoto *)photo error:(nullable NSError *)error
{
    if (!error) {
        NSData *imageData = [photo fileDataRepresentation];
        NSData* compressedImage = [ALPRCameraManager imageWithImage:imageData options:self.takePictureOptions];
        NSString *path = [ALPRCameraManager generatePathInDirectory:[[ALPRCameraManager cacheDirectoryPath] stringByAppendingPathComponent:@"Camera"] withExtension:@".jpg"];
        NSString *uri = [ALPRCameraManager writeImage:compressedImage toPath:path];
        self.takePictureResolve(uri);
    } else {
        self.takePictureReject(@"E_IMAGE_CAPTURE_FAILED", @"Image could not be captured", error);
    }
}

+ (NSData *)imageWithImage:(NSData *)imageData options:(NSDictionary *)options {
    UIImage *image = [UIImage imageWithData:imageData];
    
    // Calculate the image size.
    int width = image.size.width, height = image.size.height;
    float quality, scale;
    
    if([options valueForKey:@"width"] != nil) {
        width = [options[@"width"] intValue];
    }
    if([options valueForKey:@"height"] != nil) {
        height = [options[@"height"] intValue];
    }
    
    float widthScale = image.size.width / width;
    float heightScale = image.size.height / height;
    
    if(widthScale > heightScale) {
        scale = heightScale;
    } else {
        scale = widthScale;
    }
    
    if([options valueForKey:@"quality"] != nil) {
        quality = [options[@"quality"] floatValue];
    } else {
        quality = 1.0; // Default quality
    }
    
    UIImage *destImage = [UIImage imageWithCGImage:[image CGImage] scale:scale orientation:UIImageOrientationUp];
    NSData *destData = UIImageJPEGRepresentation(destImage, quality);
    return destData;
}

+ (NSString *)generatePathInDirectory:(NSString *)directory withExtension:(NSString *)extension
{
    NSString *fileName = [[[NSUUID UUID] UUIDString] stringByAppendingString:extension];
    [ALPRCameraManager ensureDirExistsWithPath:directory];
    return [directory stringByAppendingPathComponent:fileName];
}

+ (BOOL)ensureDirExistsWithPath:(NSString *)path
{
    BOOL isDir = NO;
    NSError *error;
    BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir];
    if (!(exists && isDir)) {
        [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:&error];
        if (error) {
            return NO;
        }
    }
    return YES;
}

+ (NSString *)writeImage:(NSData *)image toPath:(NSString *)path
{
    [image writeToFile:path atomically:YES];
    NSURL *fileURL = [NSURL fileURLWithPath:path];
    return [fileURL absoluteString];
}

+ (NSString *)cacheDirectoryPath
{
    NSArray *array = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    return [array objectAtIndex:0];
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
        
        self.avCaptureOutput = [[AVCapturePhotoOutput alloc] init];
        if([self.session canAddOutput:self.avCaptureOutput]) {
            [self.session addOutput:self.avCaptureOutput];
        }
        
        __weak ALPRCameraManager *weakSelf = self;
        [self setRuntimeErrorHandlingObserver:[NSNotificationCenter.defaultCenter addObserverForName:AVCaptureSessionRuntimeErrorNotification object:self.session queue:nil usingBlock:^(NSNotification *note) {
            ALPRCameraManager *strongSelf = weakSelf;
            dispatch_async(strongSelf.sessionQueue, ^{
                // Manually restarting the session since it must have been stopped due to an error.
                [strongSelf.session startRunning];
            });
        }]];
        
        [[NSOperationQueue mainQueue] addOperationWithBlock:^ {
            [self.session startRunning];
        }];
        
        [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(deviceDidRotate:) name:UIDeviceOrientationDidChangeNotification object:nil];
        deviceOrientation = [[UIDevice currentDevice] orientation];
        [self updatePreviewLayerOrientation];
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
    [self updatePreviewLayerOrientation];
}

// Function to rotate the previewLayer according to the device's orientation.
- (void)updatePreviewLayerOrientation {
    //Get Preview Layer connection
    AVCaptureConnection *previewLayerConnection = self.previewLayer.connection;
    if ([previewLayerConnection isVideoOrientationSupported]) {
        switch(deviceOrientation) {
            case UIDeviceOrientationPortrait:
                [previewLayerConnection setVideoOrientation:AVCaptureVideoOrientationPortrait];
                break;
            case UIDeviceOrientationPortraitUpsideDown:
                [previewLayerConnection setVideoOrientation:AVCaptureVideoOrientationPortraitUpsideDown];
                break;
            case UIDeviceOrientationLandscapeLeft:
                // Not sure why I need to invert left and right, but this is what is needed for
                // it to function properly. Otherwise it reverses the image.
                [previewLayerConnection setVideoOrientation:AVCaptureVideoOrientationLandscapeRight];
                break;
            case UIDeviceOrientationLandscapeRight:
                [previewLayerConnection setVideoOrientation:AVCaptureVideoOrientationLandscapeLeft];
                break;
        }
    }
}

- (void)stopSession {
#if TARGET_IPHONE_SIMULATOR
    self.camera = nil;
    return;
#endif
    // Make sure that we are on the main thread when we are 
    // ending the session, otherwise we may get an exception:
    // Fatal Exception: NSGenericException
    // *** Collection <CALayerArray: 0x282781230> was mutated while being enumerated.
    // -[ALPRCamera removeFromSuperview]
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
