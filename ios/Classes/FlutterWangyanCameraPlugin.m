#import "FlutterWangyanCameraPlugin.h"
#if __has_include(<flutter_wangyan_camera/flutter_wangyan_camera-Swift.h>)
#import <flutter_wangyan_camera/flutter_wangyan_camera-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_wangyan_camera-Swift.h"
#endif

@implementation FlutterWangyanCameraPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterWangyanCameraPlugin registerWithRegistrar:registrar];
}
@end
