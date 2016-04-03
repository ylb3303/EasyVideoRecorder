//
//  CustomVideoEditorController.h
//  EasyVideoRecorder
//
//  Created by tsinglink on 16/3/21.
//  Copyright © 2016年 EasyDarwin. All rights reserved.
//

#import "EasyVideoEditorController.h"

@protocol CustomVideoEditorDelegate;

@interface CustomVideoEditorController : EasyVideoEditorController

- (id)initWithVideoFileURL:(NSURL *)videoFileURL delegate:(id<CustomVideoEditorDelegate>)delegate;

//刷新数据源,可以刷新音乐，mv特效, 相框
- (NSInteger)reloadEffects:(NSArray <__kindof EffectInfo *> *)effcts;

@end

@protocol CustomVideoEditorDelegate<NSObject>

@optional

// 点击更多产生的回调，可以在回调中显示下载页面。再调用reloadEffects刷新数据源。
- (void)willShowMoreMusicView:(EasyVideoEditorController *)editor;
- (void)willShowMoreFxMovView:(EasyVideoEditorController *)editor;
- (void)willShowMorePicFrameView:(EasyVideoEditorController *)editor;
@end
