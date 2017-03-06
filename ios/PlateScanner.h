//
//  PlateScanner.h
//  RNOpenAlpr
//
//  Created by Evan Rosenfeld on 2/24/17.
//  Copyright © 2017 CarDash. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PlateResult.h"
#ifdef __cplusplus
#include "opencv2/highgui/highgui.hpp"
#import <opencv2/videoio/cap_ios.h>
using namespace cv;
#endif

@interface PlateScanner : NSObject

typedef void(^onPlateScanSuccess)(PlateResult *);
typedef void(^onPlateScanFailure)(NSError *);

+ (instancetype)sharedInstance;
- (void) setCountry:(NSString *)country;
- (void) scanImage:(cv::Mat&)colorImage
         onSuccess:(onPlateScanSuccess)success
         onFailure:(onPlateScanFailure)failure;
@end
