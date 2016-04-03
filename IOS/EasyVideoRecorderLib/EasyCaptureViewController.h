

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

#define ErrorRecordVideoSuccess     0
#define ErrorRecordNoDiskSpace     -5   // 磁盘空间不足

typedef void(^CapturedVideoOutputCallback)(NSURL *exportUrl, NSInteger error);

@interface EasyCaptureViewController : UIViewController 

@property (nonatomic, copy)CapturedVideoOutputCallback outputCallback;
@property (nonatomic)CGFloat minDuration;
@property (nonatomic)CGFloat maxDuration;

@end

