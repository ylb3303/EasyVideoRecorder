//
//  AppDelegate.m
//  VideoFilter
//
//  Created by Pandara on 14-8-12.
//  Copyright (c) 2014年 Pandara. All rights reserved.
//



#import "AppDelegate.h"
#import "AAPLPlayerViewController.h"
#import "CustomVideoEditorController.h"
#import "EasyVideoSdk.h"

@interface AppDelegate() <CustomVideoEditorDelegate>
{
    UINavigationController *mainNav;
}
@end

@implementation AppDelegate

- (NSString *)genSeed
{
    return @"";
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    
    [EasyVideoSdk startupWithKey:@"Jp8cd7XVRdlY4pn6zj8yNTGjoZ75BMMyY/wwh3H8SRWHUixBCRlgx1bZBV3O+GHj" error:nil];
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
//    
    self.mainViewController = [[EasyCaptureViewController alloc] init];
    mainNav = [[UINavigationController alloc] initWithRootViewController:self.mainViewController];
    self.window.rootViewController = mainNav;
 
    self.window.backgroundColor = [UIColor whiteColor];
    [self.window makeKeyAndVisible];
////
    __weak AppDelegate *weakSelf = self;
    self.mainViewController.outputCallback = ^(NSURL *url, NSInteger error){
        
        if (error == 0)
        {
            [weakSelf showVideoEditor:url];
        }
    };

//    NSString *path = [[NSBundle mainBundle] pathForResource:@"IMG_2777.mp4" ofType:nil];
//    
//    CustomVideoEditorController *playController = [[CustomVideoEditorController alloc] initWithVideoFileURL:[NSURL fileURLWithPath:path] delegate:self];
//    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:playController];
//    self.window.rootViewController = nav;
//    playController.outputCallback = ^(NSString *exportPath, NSInteger error){
//        
//        if (error == 0)
//        {
//            [weakSelf previewVideo:exportPath];
//        }
//    };
//    
//    [playController reloadEffects:[self localTestMusic]];
//    [playController reloadEffects:[self localTestFxMov]];
//    [playController reloadEffects:[self localTestPicFrame]];
    return YES;
}

- (void)showVideoEditor:(NSURL *)url
{
    __weak AppDelegate *weakSelf = self;
    CustomVideoEditorController *playController = [[CustomVideoEditorController alloc] initWithVideoFileURL:url delegate:self];
    [mainNav pushViewController:playController animated:YES];
    playController.showProgress = NO;
    playController.outputCallback = ^(NSString *exportPath, NSInteger error){
        
        if (error == 0)
        {
            [weakSelf previewVideo:exportPath];
        }
    };

    [playController reloadEffects:[self localTestMusic]];
    [playController reloadEffects:[self localTestFxMov]];
    [playController reloadEffects:[self localTestPicFrame]];
}

#pragma mark CustomVideoEditorDelegate

- (void)willShowMoreMusicView:(EasyVideoEditorController *)editor
{
    
}

- (void)willShowMoreFxMovView:(EasyVideoEditorController *)editor
{
    
}

- (void)willShowMorePicFrameView:(EasyVideoEditorController *)editor
{
    
}

#pragma mark testData
- (NSArray *)localTestMusic
{
    NSMutableArray *localCacheMusics = [[NSMutableArray alloc] init];
    NSString *path = [[NSBundle mainBundle] pathForResource:@"mp3.bundle" ofType:nil];
    NSArray *array = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:nil];
    for (int i=0; i<[array count]; i++)
    {
        NSString *fileName = [array objectAtIndex:i];
        MusicEffect *music = [[MusicEffect alloc] init];
        music.name = [fileName stringByDeletingPathExtension];
        music.localPath = [path stringByAppendingPathComponent:fileName];
        [localCacheMusics addObject:music];
    }
    
    path = [[NSBundle mainBundle] pathForResource:@"resource" ofType:nil];
    path = [path stringByAppendingPathComponent:@"testMov"];

    MusicEffect *music = [[MusicEffect alloc] init];
    music.name = @"sun";
    music.localPath = [path stringByAppendingPathComponent:@"2/sun.mp4"];;
    [localCacheMusics addObject:music];
    
    path = [[NSBundle mainBundle] pathForResource:@"resource" ofType:nil];
    path = [path stringByAppendingPathComponent:@"testMov"];
    
    MusicEffect *music2 = [[MusicEffect alloc] init];
    music2.name = @"sun2";
    music2.localPath = [path stringByAppendingPathComponent:@"2/sun2.mp4"];;
    [localCacheMusics addObject:music2];
    return localCacheMusics;
}

- (NSArray *)localTestPicFrame
{
    NSMutableArray *overlayArray = [[NSMutableArray alloc] init];
    
    PicFrameEffect *pic = [[PicFrameEffect alloc] init];
    pic.name = @"原画质";
    pic.effectID = 0;
    [overlayArray addObject:pic];
    
    PicFrameEffect *pic1 = [[PicFrameEffect alloc] init];
    pic1.name = @"相框1";
    pic1.effectID = 1;
    pic1.thumbnailPath = [[NSBundle mainBundle] pathForResource:@"frame_1.png" ofType:nil];
    NSString *path1 = [[NSBundle mainBundle] pathForResource:@"frame_1.png" ofType:nil];
    NSString *path2 = [[NSBundle mainBundle] pathForResource:@"frame_2.png" ofType:nil];
    NSString *path3 = [[NSBundle mainBundle] pathForResource:@"frame_3.png" ofType:nil];
    
    pic1.seqFramePaths = [NSArray arrayWithObjects:path1, path2, path3, nil];
    pic1.animateDuration = 0.3;
    pic1.loopCount = 0;
    [overlayArray addObject:pic1];
    return overlayArray;
}

- (NSArray *)localTestFxMov
{
    NSDictionary *testDic = @{
                          @"1":@{@"name":@"时光", @"video":@"ssss.mp4", @"audio":@"dream.mp4"} ,
                          @"2":@{@"name":@"阳光", @"video":@"sun-480-480.mp4", @"audio":@"sun.mp4"} ,
                          @"3":@{@"name":@"地球", @"video":@"png05.mp4", @"audio":@"sun.mp4"} ,
                          @"9":@{@"name":@"电影", @"video":@"old-movie-2-480-480.mp4", @"audio":@"old-movie.mp4"} ,
                          @"10":@{@"name":@"雨天", @"video":@"rain-480-480.mp4", @"audio":@"rain.mp4"}
                          };
    NSMutableArray *localCacheMV = [[NSMutableArray alloc] init];
    NSString *path = [[NSBundle mainBundle] pathForResource:@"resource" ofType:nil];
    path = [path stringByAppendingPathComponent:@"testMov"];
    NSArray *array = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:nil];
    
    FxMovEffect *fxMov = [[FxMovEffect alloc] init];
    fxMov.name = @"原画质";
    fxMov.effectID = 0;
    [localCacheMV addObject:fxMov];
    
    {
        // 加测试序列帧，注意下面几个参数的设置
        PicFrameEffect *pic1 = [[PicFrameEffect alloc] init];
        pic1.name = @"序列帧";
        pic1.effectID = 4;
        pic1.thumbnailPath = [[NSBundle mainBundle] pathForResource:@"frame_1.png" ofType:nil];
        NSString *path1 = [[NSBundle mainBundle] pathForResource:@"frame_1.png" ofType:nil];
        NSString *path2 = [[NSBundle mainBundle] pathForResource:@"frame_2.png" ofType:nil];
        NSString *path3 = [[NSBundle mainBundle] pathForResource:@"frame_3.png" ofType:nil];
        pic1.seqFramePaths = [NSArray arrayWithObjects:path1, path2, path3, nil];
        pic1.animateDuration = 0.3;
        pic1.loopCount = 0;
        pic1.audioTrackPath = [path stringByAppendingPathComponent:@"2/sun.mp4"];
        // 序列帧时候一定要设置为EffectTypeFxMv
        pic1.type = EffectTypeFxMv;
        [localCacheMV addObject:pic1];
    }
    
    // 加载本地其他的mp4
    for (int i=0; i<[array count]; i++)
    {
        NSString *dirName = [array objectAtIndex:i];
        NSString *dirPath = [path stringByAppendingPathComponent:dirName];
        
        NSDictionary *contentDic = [testDic objectForKey:dirName];
        FxMovEffect *fxMov = [[FxMovEffect alloc] init];
        fxMov.name = [contentDic objectForKey:@"name"];
        fxMov.effectID = [dirName intValue];
        if ([dirName isEqualToString:@"3"])
        {
            // 不透明效果
            fxMov.blendMode = Blend_ColorKey;
        }
        else
        {
            fxMov.blendMode = Blend_Sceen;
        }
        
        // mv
        fxMov.videoPath = [dirPath stringByAppendingPathComponent:[contentDic objectForKey:@"video"]];
        fxMov.audioTrackPath = [dirPath stringByAppendingPathComponent:[contentDic objectForKey:@"audio"]];
        
        fxMov.thumbnailPath = [dirPath stringByAppendingPathComponent:@"icon.png"];
        
        [localCacheMV addObject:fxMov];
    }
    
    return localCacheMV;
}

//
- (void)previewVideo:(NSString *)path
{
    UINavigationController *navController = (UINavigationController *)self.window.rootViewController;
    AAPLPlayerViewController *captureViewCon = [[AAPLPlayerViewController alloc] initWithNibName:@"AAPLPlayerViewController" bundle:nil];
    captureViewCon.asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:path]];
    [navController pushViewController:captureViewCon animated:YES];
}

@end
