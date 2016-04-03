//
//  EasyVideoSdk.h
//  EasyVideoRecorder
//
//  Created by chenshun on 16/3/16.
//  Copyright © 2016年 EasyDarwin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreFoundation/CoreFoundation.h>
#import <UIKit/UIKit.h>

@interface EasyVideoSdk : NSObject

+ (id)startupWithKey:(NSString *)key error:(NSInteger *)error;

@end
