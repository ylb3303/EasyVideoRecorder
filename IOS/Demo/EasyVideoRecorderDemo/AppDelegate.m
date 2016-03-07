//
//  AppDelegate.m
//  VideoFilter
//
//  Created by Pandara on 14-8-12.
//  Copyright (c) 2014年 Pandara. All rights reserved.
//



#import "AppDelegate.h"
#import "AAPLPlayerViewController.h"

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];

    self.mainViewController = [[EasyCaptureViewController alloc] init];
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:self.mainViewController];
    self.window.rootViewController = nav;

    self.window.backgroundColor = [UIColor whiteColor];
    [self.window makeKeyAndVisible];

    __weak AppDelegate *weakSelf = self;
    self.mainViewController.outputCallback = ^(NSString *filePath, NSInteger error){
        
        if (error == 0)
        {
            [weakSelf previewVideo:filePath];
        }
        else if (error == kErrorExceedMaxSegments)
        {
            UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"提示"
                                                                message:@"视频片段个数超过限制，最多位12个" delegate:nil
                                                      cancelButtonTitle:nil otherButtonTitles:@"确定", nil];
            [alertView show];
        }
    };
    
    return YES;
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
