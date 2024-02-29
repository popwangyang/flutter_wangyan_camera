import 'dart:async';

import 'package:flutter/services.dart';

class FlutterWangyanCamera {
  static const MethodChannel _channel = MethodChannel('flutter_wangyan_camera');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> pickFromCamera(
      {WyResolutionPreset? resolutionPreset = WyResolutionPreset.high}) async {
    String resolutionStr = "240x320";

    switch (resolutionPreset) {
      case WyResolutionPreset.low:
        resolutionStr = "240x320";
        break;
      case WyResolutionPreset.medium:
        resolutionStr = "480x720";
        break;
      case WyResolutionPreset.high:
        resolutionStr = "720x1280";
        break;
      case WyResolutionPreset.veryHigh:
        resolutionStr = "1080x1920";
        break;
      case WyResolutionPreset.ultraHigh:
        resolutionStr = "2160x3840";
        break;
      case WyResolutionPreset.max:
        resolutionStr = "2160x3840";
        break;
      default:
        resolutionStr = "720x1280";
    }

    final String? src = await _channel
        .invokeMethod("pickFromCamera", {"resolution": resolutionStr});
    return src;
  }
}

enum WyResolutionPreset {
  /// 352x288 on iOS, 240p (320x240) on Android and Web
  low,

  /// 480p (640x480 on iOS, 720x480 on Android and Web)
  medium,

  /// 720p (1280x720)
  high,

  /// 1080p (1920x1080)
  veryHigh,

  /// 2160p (3840x2160 on Android and iOS, 4096x2160 on Web)
  ultraHigh,

  /// The highest resolution available.
  max,
}
