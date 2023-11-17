package com.example.flutter_wangyan_camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var isBackCamera: Boolean = true;
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var flashImage: ImageButton;
    private var scaleDetector: ScaleGestureDetector? = null
    private var resolution: String? = null;

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        flashImage = findViewById<ImageButton>(R.id.flash_btn)
        flashImage.setOnClickListener { cameraFlashChange() }
        findViewById<Button>(R.id.image_capture_button).setOnClickListener { takePhoto() }
        findViewById<ImageButton>(R.id.camera_change).setOnClickListener { cameraChange() }
        findViewById<ImageButton>(R.id.back).setOnClickListener { back() }
        findViewById<View>(R.id.root_box).setOnTouchListener { _, event ->
            focusCamera(event)
            false
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        resolution = intent.getStringExtra("resolution")
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    private fun back() {
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun focusCamera(event: MotionEvent) {
        val action = FocusMeteringAction.Builder(
            findViewById<PreviewView>(R.id.viewFinder).meteringPointFactory
                .createPoint(event.x, event.y)
        ).build();
            showTapView(event.x.toInt(), event.y.toInt())
        camera?.cameraControl?.startFocusAndMetering(action)
    }

     @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
     fun zoom(delta: Float) {
        val zoomState = camera?.cameraInfo?.zoomState
        zoomState?.value?.let {
            val currentZoomRatio = it.zoomRatio
            camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
        }
    }


    private fun showTapView(x: Int, y: Int) {
        val popupWindow =  PopupWindow(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.ic_focus_view)
        popupWindow.contentView = imageView
        popupWindow.showAsDropDown( findViewById<PreviewView>(R.id.viewFinder), x-20, y-20)
        findViewById<PreviewView>(R.id.viewFinder).postDelayed({ popupWindow.dismiss() }, 600)
        findViewById<PreviewView>(R.id.viewFinder).playSoundEffect(SoundEffectConstants.CLICK)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun cameraChange() {
        val cameraSelector = if (isBackCamera){
            CameraSelector.DEFAULT_FRONT_CAMERA;
        }else{
            CameraSelector.DEFAULT_BACK_CAMERA;
        }
        if (bindPreview(cameraSelector)){
            isBackCamera = !isBackCamera
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun cameraFlashChange() {
        if (imageCapture != null) {
            when(imageCapture!!.flashMode){
                ImageCapture.FLASH_MODE_AUTO -> setCameraFlash(ImageCapture.FLASH_MODE_ON)
                ImageCapture.FLASH_MODE_ON -> setCameraFlash(ImageCapture.FLASH_MODE_OFF)
                ImageCapture.FLASH_MODE_OFF -> setCameraFlash(ImageCapture.FLASH_MODE_AUTO)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setCameraFlash(flash: Int) {
        if (imageCapture != null){
            when(flash) {
                ImageCapture.FLASH_MODE_AUTO -> {
                    imageCapture!!.flashMode = flash
                    flashImage.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.flash_auto)))
                }
                ImageCapture.FLASH_MODE_ON -> {
                    imageCapture!!.flashMode = flash
                    flashImage.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.flash_on)))
                }
                ImageCapture.FLASH_MODE_OFF -> {
                    imageCapture!!.flashMode = flash
                    flashImage.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.flash_off)))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bindPreview(
        cameraSelector : CameraSelector
    ): Boolean {
        if (cameraProvider != null && preview != null && imageCapture != null) {
            return try {
                // Unbind use cases before rebinding
                cameraProvider!!.unbindAll()
                // 设置闪光灯为自动模式
                setCameraFlash(ImageCapture.FLASH_MODE_AUTO)
                // Bind use cases to camera
                camera = cameraProvider!!.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                true
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                false
            }
        }else{
            return false
        }
    }

    private fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension
        )

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.cacheDir?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        val photoFile = createFile(getOutputDirectory(this), FILENAME, PHOTO_EXTENSION)

        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = !isBackCamera
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    back(output.savedUri?.path)
                }
            }
        )
    }

    private fun back(path: String?) {
        if(path != null) {
            val intent = Intent()
            intent.putExtra("path", path)
            Log.d(TAG, "$path")
            setResult(RESULT_RESPONSE_CODE, intent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
                .setJpegQuality(50)
                .setTargetRotation(Surface.ROTATION_0)
                .setTargetResolution(Size.parseSize(if (resolution != null && resolution != "max"){
                    resolution
                }else{
                    "1280x720"
                }))
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            bindPreview(cameraSelector)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun hasBackCamera(cameraProvider: CameraProvider): Boolean {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
        }
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun hasFrontCamera(cameraProvider: CameraProvider): Boolean {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
        }
    }
}