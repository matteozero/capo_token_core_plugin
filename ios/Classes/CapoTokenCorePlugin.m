#import "CapoTokenCorePlugin.h"
#if __has_include(<capo_token_core_plugin/capo_token_core_plugin-Swift.h>)
#import <capo_token_core_plugin/capo_token_core_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "capo_token_core_plugin-Swift.h"
#endif

@implementation CapoTokenCorePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCapoTokenCorePlugin registerWithRegistrar:registrar];
}
@end
