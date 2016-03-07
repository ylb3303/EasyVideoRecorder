

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "EasyCaptureType.h"

@interface EasyCaptureViewController : UIViewController 

@property (nonatomic, copy)EasyVideoOutputCompletion outputCallback;
@property (nonatomic)CGFloat minDuration;
@property (nonatomic)CGFloat maxDuration;

@end
