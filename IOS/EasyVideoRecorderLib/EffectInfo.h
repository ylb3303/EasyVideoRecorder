//
//  EffectInfo.h
//  EasyVideoRecorder
//
//  Created by tsinglink on 16/3/22.
//  Copyright © 2016年 EasyDarwin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

typedef enum
{
    EffectTypeFilter,
    EffectTypeFxMv,
    EffectTypePicFrame,
    EffectTypeMusic,
}IEffectType;

@interface EffectInfo : NSObject

@property (nonatomic) IEffectType type;
@property (nonatomic)NSInteger effectID; // 0表示原画质 无效果
@property (nonatomic, copy)NSString *name;
@property (nonatomic, strong)NSString *thumbnailPath;

@end

@interface FxMovEffect : EffectInfo
@property (nonatomic, copy)NSString *videoPath;
@property (nonatomic, copy)NSString *audioTrackPath;
@end

@interface PicFrameEffect : EffectInfo
@property (nonatomic, copy)NSString *overlayPath;
@end

@interface FilterEffect : EffectInfo

@end

@interface MusicEffect : EffectInfo
@property (nonatomic, copy)NSString *localPath;
@end