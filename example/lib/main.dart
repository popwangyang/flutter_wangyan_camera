import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_wangyan_camera/flutter_wangyan_camera.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  File? _file;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              ElevatedButton(
                  onPressed: () async {
                    String? path = await FlutterWangyanCamera.pickFromCamera(
                        resolutionPreset: WyResolutionPreset.veryHigh);
                    if (path != null) {
                      _file = File(path);
                    }
                    setState(() {});
                  },
                  child: const Text("按钮")),
              _file != null ? Image.file(_file!) : Container()
            ],
          ),
        ),
      ),
    );
  }
}
