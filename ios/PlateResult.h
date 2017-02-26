//
//  PlateResult.h
//  RNOpenAlpr
//
//  Created by Evan Rosenfeld on 2/25/17.
//  Copyright © 2017 CarDash. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PlateResult : NSObject

@property (nonatomic, strong) NSString *plate;
@property (nonatomic, strong) NSArray *points;
@property (nonatomic) float confidence;
@property (nonatomic) int cols;
@property (nonatomic) int rows;

@end
