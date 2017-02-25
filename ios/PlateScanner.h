//
//  PlateScanner.h
//  AlprSample
//
//  Created by Alex on 04/11/15.
//  Copyright © 2015 alpr. All rights reserved.
//

#import <Foundation/Foundation.h>

#ifdef __cplusplus
#include "opencv2/highgui/highgui.hpp"
#import <opencv2/videoio/cap_ios.h>
using namespace cv;
#endif

@interface PlateScanner : NSObject

typedef void(^onPlateScanSuccess)(NSArray *);
typedef void(^onPlateScanFailure)(NSError *);

+ (instancetype)sharedInstance;
- (void) scanImage:(cv::Mat&)colorImage
         onSuccess:(onPlateScanSuccess)success
         onFailure:(onPlateScanFailure)failure;
@end
