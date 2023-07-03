package com.sefacicek.glassicoapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sefacicek.glassicoapp.ui.theme.GlassicoAppTheme
import com.sefacicek.glassicoapp.ui.theme.IndigoButtonColor
import com.sefacicek.glassicoapp.ui.theme.PlaygroundColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime


val bitmapArr = ArrayList<Bitmap?>()

var maxPhotoSize = -1

var cropWidth: Double = 250.0
var cropHeight: Double = 250.0

var rotateAngle by mutableStateOf(0f)

var remainPhotoNum by mutableStateOf(4)
val bitmapRes by mutableStateOf(
    Bitmap.createBitmap(
        cropWidth.toInt(),
        cropHeight.toInt(),
        Bitmap.Config.ARGB_8888
    )
)

val arrList = ArrayList<String?>()
val glassInfo: MutableList<String?> = arrList.toMutableList()


sealed class CameraUIAction {
    object OnVideoStart : CameraUIAction()
    object OnVideoStop : CameraUIAction()
    object OnCameraClick : CameraUIAction()
    object OnGalleryViewClick : CameraUIAction()
    object OnSwitchCameraClick : CameraUIAction()
}

var isVideoRunning by mutableStateOf(false)

@OptIn(ExperimentalTime::class)
@Composable
fun formatDuration(duration: kotlin.time.Duration): String {

    val hours = duration.toDouble(DurationUnit.HOURS).toInt()
    val minutes = (duration.toDouble(DurationUnit.MINUTES) % 60).toInt()
    val seconds = (duration.toDouble(DurationUnit.SECONDS) % 60).toInt()
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

var isVideoVisible by mutableStateOf(true)

// TODO CameraView Start
@Composable
fun CameraControls(cameraUIAction: (CameraUIAction) -> Unit) {

    var elapsedTime by remember { mutableStateOf(0.seconds) }

    LaunchedEffect(isVideoRunning) {
        if (isVideoRunning) {
            while (true) {
                delay(1000)
                elapsedTime += 1.seconds
            }
        }
    }


    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {

        Text(
            text = if (!isVideoVisible) "$remainPhotoNum Fotoğraf Çekiniz" else formatDuration(
                elapsedTime
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedButton(
            onClick = {
                elapsedTime = 0.seconds
                remainPhotoNum = 4
                isVideoVisible = !isVideoVisible
            },
            border = BorderStroke(1.dp, Color.White),
        ) {
            Text(
                text =
                if (isVideoVisible)
                    "FOTOĞRAF" else "VİDEO",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isVideoVisible) {
                if (!isVideoRunning) {
                    CameraControl(
                        painterResource(id = R.drawable.icon_video_start),
                        R.string.icn_camera_view_video_start_content_description,
                        modifier = Modifier
                            .size(56.dp),
                    ) { cameraUIAction(CameraUIAction.OnVideoStart) }
                } else {
                    CameraControl(
                        painterResource(id = R.drawable.icon_video_stop),
                        R.string.icn_camera_view_video_stop_content_description,
                        modifier = Modifier
                            .size(56.dp)
                    ) { cameraUIAction(CameraUIAction.OnVideoStop) }
                }
            } else {
                CameraControl(
                    painterResource(id = R.drawable.icon_gallery),
                    R.string.icn_camera_view_view_gallery_content_description,
                ) { cameraUIAction(CameraUIAction.OnGalleryViewClick) }

                CameraControl(
                    //            Icons.Sharp.Lens,
                    painterResource(id = R.drawable.icon_shoot),
                    R.string.icn_camera_view_camera_shutter_content_description,
                    modifier = Modifier
                        .size(56.dp)
                ) { cameraUIAction(CameraUIAction.OnCameraClick) }

                CameraControl(
                    painterResource(id = R.drawable.icon_flip_camera),
                    R.string.icn_camera_view_switch_camera_content_description,
                ) { cameraUIAction(CameraUIAction.OnSwitchCameraClick) }
            }
        }

    }
}


@Composable
fun CameraControl(
    imageVector: Painter,
    contentDescId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector,
            contentDescription = stringResource(id = contentDescId),
            modifier = modifier,
            tint = Color.White
        )
    }

}
// TODO CameraView End


// TODO CameraExtensions Start
suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val PHOTO_EXTENSION = ".jpg"


fun ImageCapture.takePicture(
    context: Context,
    lensFacing: Int,
    onImageCaptured: (Uri, Boolean) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val outputDirectory = context.getOutputDirectory()
    // Create output file to hold the image
    val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
    val outputFileOptions = getOutputFileOptions(lensFacing, photoFile)

    this.takePicture(
        outputFileOptions,
        Executors.newSingleThreadExecutor(),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                // If the folder selected is an external media directory, this is
                // unnecessary but otherwise other apps will not be able to access our
                // images unless we scan them using [MediaScannerConnection]
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(savedUri.toFile().extension)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(savedUri.toFile().absolutePath),
                    arrayOf(mimeType)
                ) { _, uri ->

                }
                onImageCaptured(savedUri, false)

                print("\nphoto saved\n")

            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        })
}


fun getOutputFileOptions(
    lensFacing: Int,
    photoFile: File
): ImageCapture.OutputFileOptions {

    // Setup image capture metadata
    val metadata = ImageCapture.Metadata().apply {
        // Mirror image when using the front camera
        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
    }
    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
        .setMetadata(metadata)
        .build()

    return outputOptions
}

fun createFile(baseFolder: File, format: String, extension: String) =
    File(
        baseFolder, SimpleDateFormat(format, Locale.US)
            .format(System.currentTimeMillis()) + extension
    )


fun Context.getOutputDirectory(): File {
    val mediaDir = this.externalMediaDirs.firstOrNull()?.let {
        File(it, this.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else this.filesDir
}
// TODO CameraExtensions End


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (OpenCVLoader.initDebug()) {
            Log.d("LOADED", "success")
        } else {
            Log.d("LOADED", "error")
        }

        setContent {

            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()

            val appPreferences = AppPreferences(LocalContext.current)
            val prefMail = appPreferences.user_mail!!

            println("prefmail: $prefMail")

            GlassicoAppTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F5) //MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if (prefMail == "") "page_login" else "page_main"
                    ) {
                        composable("page_main") { MyScreen(navController) }
                        composable("page_signup") { SignUpPage(navController) }
                        composable("page_login") {
                            LoginPage(
                                navController,
                            )
                        }
                        composable("page_tutorial") { TutorialPage(navController) }
                        composable("page_measurements") { MeasurementsPage(navController) }
                        composable("page_result") { ResultPage() }
                        composable("page_camera") { CameraPage(navController) }
                    }
                }
            }
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    suspend fun extractFramesFromVideo(
        context: Context,
        videoUri: Uri,
        interval: Long
    ): List<Bitmap> {
        return withContext(Dispatchers.Default) {
            val frames = mutableListOf<Bitmap>()

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)

            val durationString =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationString?.toLong() ?: 0L

            var currentTime = 0L
            while (currentTime < duration) {
                val frameBitmap =
                    retriever.getFrameAtTime(currentTime * 1000)
                if (frameBitmap != null) {
                    val rotatedBitmap = rotateBitmap(
                        frameBitmap,
                        270f
                    )
                    frames.add(rotatedBitmap)
                }
                currentTime += interval
            }

            retriever.release()

            frames
        }
    }


    @Composable
    fun CameraPage(navController: NavController) {

        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        CameraView(

            onVideoSaved = { uri ->

                glassInfo.clear()
                bitmapArr.clear()

                lineWidth = -1

                rotateAngle = 0f
                Log.d(TAG, "video uri: $uri")

                coroutineScope.launch {

                    val frames = extractFramesFromVideo(context, uri, 1000L)

                    var maxSize = -1

                    // Call algorithm for the extracted frames
                    for (frame in frames) {

                        lineWidth = -1
                        val mat: Mat? = detectGlass(frame, false)

                        if (mat != null) {
                            val size = mat.width() * mat.height()
                            if (size > maxSize) {
                                bitmapArr.add(frame)
                                maxSize = size
                            }
                        }
                    }

                    if (bitmapArr.size > 0) {   // max size'a sahip result bulundu

                        val tempResBitmap =
                            bitmapArr.get(bitmapArr.size - 1)

                        val flagMat: Mat? = detectGlass(
                            tempResBitmap!!,
                            true
                        )

                        Imgproc.resize(flagMat, flagMat, Size(cropWidth, cropHeight))
                        Utils.matToBitmap(flagMat, bitmapRes)

                        bitmapArr.clear()
                        lineWidth = -1

                        navController.navigate("page_result") {
                            popUpTo("page_camera") { inclusive = true }
                        }
                    } else {

                        remainPhotoNum = 4
                        lineWidth = -1

                        Toast.makeText(
                            baseContext,
                            "Gözlük bulunamadı. Lütfen tekrar çekim yapınız.",
                            Toast.LENGTH_SHORT
                        ).show()
                        coroutineScope.launch {
                            navController.navigate("page_camera") {
                                // popUpTo("page_camera") { inclusive = true }
                            }
                        }
                    }
                }
            },


            onImageCaptured = { uri, fromGallery ->

                glassInfo.clear()
                lineWidth = -1

                remainPhotoNum--
                rotateAngle = 90f

                Log.d(TAG, "remainPhotoNum: $remainPhotoNum")
                Log.d(TAG, "Image Uri Captured from Camera View")
                Log.d(TAG, "uri: $uri")
                Log.d(TAG, "from gallery: $fromGallery")

                val bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    uri
                )

                coroutineScope.launch {

                    val mat: Mat? = detectGlass(bitmap, false)

                    if (mat != null) {
                        val size = mat.width() * mat.height()
                        if (size > maxPhotoSize) {
                            bitmapArr.add(bitmap)
                            maxPhotoSize = size
                        }
                    }

                    if (remainPhotoNum == 0) {

                        if (bitmapArr.size > 0) {

                            coroutineScope.launch {

                                val tempResBitmap =
                                    bitmapArr.get(bitmapArr.size - 1)


                                val flagMat: Mat? = detectGlass(
                                    tempResBitmap!!,
                                    true
                                )

                                Imgproc.resize(flagMat, flagMat, Size(cropWidth, cropHeight))
                                Utils.matToBitmap(flagMat, bitmapRes)

                                bitmapArr.clear()
                                remainPhotoNum = 4
                                maxPhotoSize = -1

                                coroutineScope.launch {
                                    navController.navigate("page_result") {
                                        popUpTo("page_camera") { inclusive = true }
                                    }
                                }
                            }
                        } else {

                            bitmapArr.clear()

                            remainPhotoNum = 4
                            maxPhotoSize = -1
                            lineWidth = -1
                            isVideoVisible = false

                            Toast.makeText(
                                baseContext,
                                "Gözlük bulunamadı. Lütfen tekrar çekim yapınız.",
                                Toast.LENGTH_SHORT
                            ).show()
                            coroutineScope.launch {
                                navController.navigate("page_camera") {
                                    // popUpTo("page_camera") { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }, onError = { imageCaptureException ->
                print("Take Picture Error\n")
            })
    }

    val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    this,
                    "Kamera izni verilmedi. Kamera iznini ayarlardan değiştirebilirsiniz.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                //
            }
        }

    companion object {
        private const val TAG = "CameraXApp"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }


    @Composable
    fun CameraView(
        onVideoSaved: (Uri) -> Unit,
        onImageCaptured: (Uri, Boolean) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {

        val context = LocalContext.current
        var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
        val imageCapture: ImageCapture = remember {
            ImageCapture.Builder().build()
        }
        val galleryLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) onImageCaptured(uri, true)
        }

        var recording: Recording? by remember { mutableStateOf(null) } // Store the recording object

        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HIGHEST,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
            )
            .build()

        val videoCapture = remember {
            VideoCapture.withOutput(recorder)
        }


        CameraPreviewView(
            videoCapture,
            imageCapture,
            lensFacing
        ) { cameraUIAction ->
            when (cameraUIAction) {
                is CameraUIAction.OnVideoStart -> {

                    isVideoRunning = true

                    // create and start a new recording session
                    val name = SimpleDateFormat(FILENAME, Locale.US)
                        .format(System.currentTimeMillis())
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
                        }
                    }

                    val mediaStoreOutputOptions = MediaStoreOutputOptions
                        .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                        .setContentValues(contentValues)
                        .build()


                    recording = videoCapture.output
                        .prepareRecording(this, mediaStoreOutputOptions)
                        .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                            when (recordEvent) {
                                is VideoRecordEvent.Start -> {
                                    val msg = "Video Başladı"
                                    Log.d(TAG, msg)
                                }

                                is VideoRecordEvent.Finalize -> {
                                    if (!recordEvent.hasError()) {

                                        val msg = "Video Çekildi"// +
                                        Log.d(TAG, msg)
                                        onVideoSaved(recordEvent.outputResults.outputUri)
                                    } else {
                                        val msg = "Video hata ile bitti: " +
                                                "${recordEvent.error}"
                                        Log.d(TAG, msg)

                                    }

                                }
                            }
                        }

                }

                is CameraUIAction.OnVideoStop -> {
                    isVideoRunning = false
                    recording?.stop()
                }

                is CameraUIAction.OnCameraClick -> {
                    imageCapture.takePicture(context, lensFacing, onImageCaptured, onError)
                }

                is CameraUIAction.OnSwitchCameraClick -> {
                    lensFacing =
                        if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
                        else
                            CameraSelector.LENS_FACING_BACK
                }

                is CameraUIAction.OnGalleryViewClick -> {
                    galleryLauncher.launch("image/*")
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun CameraPreviewView(
        videoCapture: VideoCapture<Recorder>,
        imageCapture: ImageCapture,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        cameraUIAction: (CameraUIAction) -> Unit
    ) {


        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val preview = androidx.camera.core.Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()


        val previewView = remember { PreviewView(context) }
        LaunchedEffect(lensFacing) {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize()) {

            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.Bottom
            ) {
                CameraControls(cameraUIAction)
            }

        }
    }


    @Preview(showBackground = true, name = "Home Screen", uiMode = Configuration.UI_MODE_NIGHT_NO)
    @Composable
    fun MyImagePreview() {
        val navController = rememberNavController()
        GlassicoAppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF5F5F5) //MaterialTheme.colorScheme.background
            ) {
                MyScreen(navController)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyScreen(navController: NavController) {

        val context = LocalContext.current
        val appPreferences = AppPreferences(context)
        val prefMail = appPreferences.user_mail!!

        val appViewModel: AppViewModel = viewModel(
            factory = AppViewModelFactory(context.applicationContext as Application)
        )

        val bitmap = remember { mutableStateOf<Bitmap?>(null) }

        var userAvatar: Bitmap? = null
        var userName: String = ""
        var userMail: String = ""
        val users = appViewModel.readAllData.observeAsState(listOf()).value

        val glasses = appViewModel.findGlass(prefMail).observeAsState().value
        val lastMeasurements: List<Glass> = glasses?.take(3) ?: emptyList()

        val user = appViewModel.findUser(prefMail).observeAsState().value
        user?.let {
            userName = it.userName
            userMail = it.userMail
        }

        GlassicoAppTheme {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 4.dp, end = 16.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paddingFromBaseline(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppBar(navController)
                    Spacer(modifier = Modifier.height(16.dp))
//                    UserAvatar(painterResource(R.drawable.glass))
//
//                            bitmap.value?.let { btm ->
//                                UserAvatar(btm)
//                            }
//                    if (userAvatar != null)
//                        UserAvatar(userAvatar!!)
                    Spacer(modifier = Modifier.height(8.dp))
                    UserName(name = userName)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Son Ölçümler",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.icon_clock),
                            contentDescription = "icon_clock",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                LastMeasurements()

                LazyColumn {
                    items(lastMeasurements.size) { index ->
                        val glass = lastMeasurements[index]
                        val encodeByte: ByteArray = Base64.decode(glass.glassImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
                        MeasurementItem(
                            date = glass.glassDate.substring(0, 10),
                            glassImage = bitmap,
                            width = glass.glassWidth,
                            height = glass.glassHeight
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        navController.navigate("page_measurements") {
                            popUpTo("page_main") { inclusive = false }
                        }
                    },
                    border = BorderStroke(1.dp, IndigoButtonColor),
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 6.dp)
                        .align(Alignment.End)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ölçümler",
                            style = MaterialTheme.typography.displaySmall,
                            color = IndigoButtonColor
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.icon_ruler),
                            contentDescription = "icon_ruler",
                            tint = IndigoButtonColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (!MainActivity.hasPermissions(baseContext)) {
                            // Request camera-related permissions
                            activityResultLauncher.launch(MainActivity.REQUIRED_PERMISSIONS)
                        }
                        if (appPreferences.user_tutorial) {
                            navController.navigate("page_tutorial")
                            {
//                            popUpTo("page_main") { inclusive = false }
                            }
                        } else {
                            navController.navigate("page_camera")
                            {
//                            popUpTo("page_main") { inclusive = false }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoButtonColor),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Yeni Ölçüm",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.icon_new_measurement),
                            contentDescription = "icon_new_measurement",
                            tint = Color.White,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBar(navController: NavController) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Glassic",
                style = TextStyle(
                    fontFamily = patrickHandFontFamily,
                    fontSize = 28.sp,
                    letterSpacing = (0.05).em
                )
            )
            Icon(
                painter = painterResource(id = R.drawable.icon_eyes),
                contentDescription = "icon_eyes",
                modifier = Modifier.padding(start = 2.dp, top = 6.dp)
            )
        }
        IconButton(
            onClick = {
                navController.navigate("page_login") {
                    popUpTo("page_main") { inclusive = true }
                }
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_logout),
                contentDescription = "icon_logout",
            )
        }

    }
}


@Composable
fun UserName(name: String?) {
    Text(name!!, style = MaterialTheme.typography.displayMedium)
}

@Composable
fun LastMeasurements() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Tarih",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                modifier = Modifier.padding(start = 40.dp, end = 28.dp),
                text = "Cam",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "Genişlik",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = " Yükseklik",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Divider(
            thickness = 2.dp,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, end = 8.dp, bottom = 16.dp),
        )

    }

}

@Composable
fun MeasurementItem(date: String, glassImage: Bitmap, width: String, height: String) {

    Surface(
        modifier = Modifier
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(size = 8.dp), shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(size = 8.dp))
                .background(PlaygroundColor)
                .border(
                    width = (0.05).dp,
                    color = Color.White,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = date,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Image(
                bitmap = glassImage.asImageBitmap(),
                contentDescription = "Glass Image",
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(8.dp))
                    .size(56.dp)
                    .weight(1f)
            )
            Text(
                text = width,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = " $height",
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)


            )
        }
    }
}


@Composable
fun UserAvatar(bitmap: Bitmap) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text("Avatar", fontWeight = FontWeight.Bold)
        Image(
            bitmap = bitmap.asImageBitmap(),
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            contentDescription = "Glass Image",
        )
    }
}

