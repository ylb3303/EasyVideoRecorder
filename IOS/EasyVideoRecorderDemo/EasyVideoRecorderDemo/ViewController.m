//
//  ViewController.m
//  EasyVideoRecorderDemo
//
//  Created by chenshun on 16/3/4.
//  Copyright © 2016年 EasyDarwin. All rights reserved.
//

#import "ViewController.h"
#import "PureLayout.h"
#import "EasyCaptureViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    UIButton *playButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.view addSubview:playButton];
    [playButton autoSetDimensionsToSize:CGSizeMake(75, 75)];
    [playButton autoCenterInSuperview];
    [playButton setTitle:@"sffe" forState:UIControlStateNormal];
    [playButton addTarget:self action:@selector(startPlay:) forControlEvents:UIControlEventTouchUpInside];
    playButton.hidden = NO;
}

- (IBAction)startPlay:(id)sender
{
    EasyCaptureViewController *mainViewController = [[EasyCaptureViewController alloc] init];
    UINavigationController *nav = [[UINavigationController alloc]initWithRootViewController:mainViewController];
    [self presentViewController:nav animated:YES completion:nil];

}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
