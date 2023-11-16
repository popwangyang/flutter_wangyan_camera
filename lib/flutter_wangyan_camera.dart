import 'dart:async';

import 'package:flutter/services.dart';

class FlutterWangyanCamera {
  static const MethodChannel _channel = MethodChannel('flutter_wangyan_camera');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> pickFromCamera() async {
    final String? src = await _channel.invokeMethod("pickFromCamera");
    return src;
  }
}
