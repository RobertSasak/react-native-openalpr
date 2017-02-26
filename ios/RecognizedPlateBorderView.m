//
//  RecognizedPlateBorderView.m
//  RNOpenAlpr
//
//  Created by Evan Rosenfeld on 2/25/17.
//  Copyright © 2017 CarDash. All rights reserved.
//

#import "RecognizedPlateBorderView.h"

@interface RecognizedPlateBorderView() {
    UIBezierPath *path;
}

@end

@implementation RecognizedPlateBorderView

- (instancetype)init {
    if (self = [super init]) {
        self.userInteractionEnabled = NO;
        self.opaque = NO;
        self.backgroundColor = [UIColor clearColor];
    }
    return self;
}

- (void)drawRect:(CGRect)rect {
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSaveGState(context);
    
    
    UIColor *color = [UIColor colorWithRed:0 green:40/255. blue:0xff/255. alpha:0.25];
    // (1) Draw the bounding box.
    CGContextSetLineWidth(context, 2.0);
    CGContextSetStrokeColorWithColor(context, [color colorWithAlphaComponent:.7].CGColor);
    
    CGContextAddPath(context, path.CGPath);
    CGContextStrokePath(context);
    CGContextAddPath(context, path.CGPath);
    
    CGContextSetFillColorWithColor(context, [UIColor colorWithRed:0 green:40/255. blue:0xff/255. alpha:0.1].CGColor);
    CGContextFillPath(context);
    
    CGContextRestoreGState(context);
}

- (void)updateCorners:(NSArray *)corners {
    path = [UIBezierPath bezierPath];
    
    
    [path moveToPoint:[[corners objectAtIndex:0] CGPointValue]];
    for (int i = 1; i < 4; i++) {
        [path addLineToPoint:[[corners objectAtIndex:i] CGPointValue]];
    }
    
    [path closePath];
    [self setNeedsDisplay];
}

@end
