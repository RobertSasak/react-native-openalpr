//
//  RCTCamera.m
//  RNOpenAlpr
//
//  Created by Evan Rosenfeld on 2/24/17.
//  Copyright © 2017 CarDash. All rights reserved.
//

#import "ALPRCamera.h"
#import "ALPRCameraManager.h"
#import <React/RCTLog.h>
#import <React/RCTUtils.h>

#import <React/UIView+React.h>

#import "CameraTouchFocusView.h"

@interface ALPRCamera ()


@property (nonatomic, weak) ALPRCameraManager *manager;
@property (nonatomic, weak) RCTBridge *bridge;
@property (nonatomic, strong) CameraTouchFocusView *camFocus;

@end

@implementation ALPRCamera
{
    BOOL _multipleTouches;
    BOOL _touchToFocus;
}

- (void)setTouchToFocus:(BOOL)enabled
{
    if (_touchToFocus != enabled) {
        _touchToFocus = enabled;
    }
}

- (id)initWithManager:(ALPRCameraManager*)manager bridge:(RCTBridge *)bridge
{
    
    if ((self = [super init])) {
        self.manager = manager;
        self.bridge = bridge;
        UIPinchGestureRecognizer *pinchGesture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinchToZoomRecognizer:)];
        [self addGestureRecognizer:pinchGesture];
        [self.manager initializeCaptureSessionInput:AVMediaTypeVideo];
        [self.manager startSession];
        _multipleTouches = NO;
        _touchToFocus = YES;
    }
    return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    self.manager.previewLayer.frame = self.bounds;
    [self setBackgroundColor:[UIColor blackColor]];
    [self.layer insertSublayer:self.manager.previewLayer atIndex:0];
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    [self insertSubview:view atIndex:atIndex + 1];
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    [subview removeFromSuperview];
    return;
}

- (void)removeFromSuperview
{
    [self.manager stopSession];
    [super removeFromSuperview];
}


- (void) touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    // Update the touch state.
    if ([[event touchesForView:self] count] > 1) {
        _multipleTouches = YES;
    }
    
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    if (!_touchToFocus) return;
    
    BOOL allTouchesEnded = ([touches count] == [[event touchesForView:self] count]);
    
    // Do not conflict with zooming and etc.
    if (allTouchesEnded && !_multipleTouches) {
        UITouch *touch = [[event allTouches] anyObject];
        CGPoint touchPoint = [touch locationInView:touch.view];
        // Focus camera on this point
        [self.manager focusAtThePoint:touchPoint];
        
        if (self.camFocus)
        {
            [self.camFocus removeFromSuperview];
        }

        // Show animated rectangle on the touched area
        if (_touchToFocus) {
            self.camFocus = [[CameraTouchFocusView alloc]initWithFrame:CGRectMake(touchPoint.x-40, touchPoint.y-40, 80, 80)];
            [self.camFocus setBackgroundColor:[UIColor clearColor]];
            [self addSubview:self.camFocus];
            [self.camFocus setNeedsDisplay];
            
            [UIView beginAnimations:nil context:NULL];
            [UIView setAnimationDuration:1.0];
            [self.camFocus setAlpha:0.0];
            [UIView commitAnimations];
        }
    }
    
    if (allTouchesEnded) {
        _multipleTouches = NO;
    }
    
}


@end
