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
    UIColor *color;
}

@end

@implementation RecognizedPlateBorderView

- (instancetype)init {
    if (self = [super init]) {
        self.userInteractionEnabled = NO;
        self.opaque = NO;
        self.backgroundColor = [UIColor clearColor];
        color = [UIColor colorWithRed:0 green:40/255. blue:0xff/255. alpha:1.0];
    }
    return self;
}

// Assumes input like "#00FF00" (#RRGGBB).
+ (UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1]; // bypass '#' character
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

- (void)setColorString:(NSString *)colorString {
    _colorString = colorString;
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:colorString];
    if ([colorString rangeOfString:@"#"].location == 0) {
        [scanner setScanLocation:1];
    }
    [scanner scanHexInt:&rgbValue];
    color = [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

- (void)drawRect:(CGRect)rect {
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSaveGState(context);
    
    
    // (1) Draw the bounding box.
    CGContextSetLineWidth(context, 2.0);
    CGContextSetStrokeColorWithColor(context, [color colorWithAlphaComponent:.7].CGColor);
    
    CGContextAddPath(context, path.CGPath);
    CGContextStrokePath(context);
    CGContextAddPath(context, path.CGPath);
    
    CGContextSetFillColorWithColor(context, [color colorWithAlphaComponent:0.1].CGColor);
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
