//
//  MainViewController.m
//  VideoFilter
//
//  Created by Pandara on 14-8-12.
//  Copyright (c) 2014年 Pandara. All rights reserved.
//



#import "MainViewController.h"

#import "EasyCaptureViewController.h"

@interface MainViewController ()
{
    NSMutableArray *array;
    NSMutableArray *selectAssets;
    
    CMTime nextPTS;
    CMTime frameDuration;

}
@end

@implementation MainViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"main_bg.png"]];
    
    array = [[NSMutableArray alloc] init];
    selectAssets = [[NSMutableArray alloc] init];
    
    [[UINavigationBar appearance] setBackgroundImage:[UIImage imageNamed:@"navigation_bar_bk.png"] forBarMetrics:UIBarMetricsDefault];
    [[UINavigationBar appearance] setTintColor:[UIColor whiteColor]];
    [[UINavigationBar appearance] setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
}

- (IBAction)startCamera:(id)sender
{    
    EasyCaptureViewController *captureViewCon = [[EasyCaptureViewController alloc]init];
    [self.navigationController pushViewController:captureViewCon animated:YES];
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)prefersStatusBarHidden
{
    return NO;
}

@end
