package com.example.flutter_wangyan_camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

class WyCameraX(@NonNull val binding: ActivityPluginBinding, val channel: MethodChannel): FlutterPluginsDelegate, PluginRegistry.ActivityResultListener {

    private val context: Context = binding.activity.applicationContext
    private var activity: Activity = binding.activity
    private var resultHandle: MethodChannel.Result? = null

    init {
        binding.addActivityResultListener(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "pickFromCamera") {
            var resolution = call.argument<String?>("resolution")
            if (resolution == null) {
                resolution = "1280x720"
            }
            resultHandle = result
            val intent = Intent(activity, CameraActivity::class.java)
            intent.putExtra("resolution", resolution)
            activity.startActivityForResult(intent, RESULT_REQUEST_CODE)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == RESULT_REQUEST_CODE && resultCode == RESULT_RESPONSE_CODE) {
            val path = data?.getStringExtra("path")
            resultHandle?.success(path)
        }
        return true
    }

}