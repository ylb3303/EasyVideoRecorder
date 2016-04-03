//
#import <UIKit/UIKit.h>
#import "EffectInfo.h"

#define ErrorCodeNotSupport  -2
#define ErrorCodeInvalidPara  -1
#define ErrorCodeSuccess     0

typedef void(^EffectVideoOutputCallback)(NSString *exportPath, NSInteger error);

@interface EasyVideoEditorController : UIViewController
{
    UIView *preview;
    NSURL *curApplyMusicUrl;
}

@property (nonatomic, copy)EffectVideoOutputCallback outputCallback;

@property (nonatomic, copy, readonly)NSURL *curApplyMusicUrl;
@property (strong, nonatomic, readonly) NSURL *videoFileURL;

- (id)initWithVideoFileURL:(NSURL *)videoFileURL;

- (void)stopAll;
- (void)restore;
@end
