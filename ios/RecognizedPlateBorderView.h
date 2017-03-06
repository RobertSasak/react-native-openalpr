//
//  RecognizedPlateBorderView.h
//  RNOpenAlpr
//
//  Created by Evan Rosenfeld on 2/25/17.
//  Copyright © 2017 CarDash. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface RecognizedPlateBorderView : UIView

- (void)updateCorners:(NSArray *)corners;

@property (nonatomic, strong) NSString *colorString;
@end
