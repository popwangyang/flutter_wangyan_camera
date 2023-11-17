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
    String resolutionStr = "320x240";

    switch (resolutionPreset) {
      case WyResolutionPreset.low:
        resolutionStr = "320x240";
        break;
      case WyResolutionPreset.medium:
        resolutionStr = "720x480";
        break;
      case WyResolutionPreset.high:
        resolutionStr = "1280x720";
        break;
      case WyResolutionPreset.veryHigh:
        resolutionStr = "1920x1080";
        break;
      case WyResolutionPreset.ultraHigh:
        resolutionStr = "3840x2160";
        break;
      case WyResolutionPreset.max:
        resolutionStr = "max";
        break;
      default:
        resolutionStr = "1280x720";
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
