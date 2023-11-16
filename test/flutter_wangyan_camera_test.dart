import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_wangyan_camera/flutter_wangyan_camera.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_wangyan_camera');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterWangyanCamera.platformVersion, '42');
  });
}
