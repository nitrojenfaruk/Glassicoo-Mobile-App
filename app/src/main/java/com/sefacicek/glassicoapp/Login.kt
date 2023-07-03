package com.sefacicek.glassicoapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sefacicek.glassicoapp.ui.theme.DarkGreyTextColor
import com.sefacicek.glassicoapp.ui.theme.GlassicoAppTheme
import com.sefacicek.glassicoapp.ui.theme.IndigoButtonColor
import com.sefacicek.glassicoapp.ui.theme.PlaygroundColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


val patrickHandFontFamily = FontFamily(
    Font(resId = R.font.patrick_hand, weight = FontWeight.Normal)
)


@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun LoginPage(navController: NavController) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(context.applicationContext as Application)
    )


    var email by remember { mutableStateOf("") }

    var isMailGood by remember { mutableStateOf(false) }
    var isEnoughInfo by remember { mutableStateOf(false) }


    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }

    val TAG = "CameraXApp"
    val REQUIRED_PERMISSIONS =
        mutableListOf(
            android.Manifest.permission.CAMERA,
//            android.Manifest.permission.READ_EXTERNAL_STORAGE,

        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    context,
                    "Kamera izni verilmedi. Kamera iznini ayarlardan değiştirebilirsiniz.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Permission granted, proceed with the desired action
            }
        }
    )


    GlassicoAppTheme {


        isEnoughInfo = email.isNotBlank()
        isMailGood = appViewModel.findUser(email)
            .observeAsState().value != null

        val appPreferences = AppPreferences(context)
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                imageUri?.let {
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap.value = MediaStore.Images
                            .Media.getBitmap(context.contentResolver, it)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, it)
                        bitmap.value = ImageDecoder.decodeBitmap(source)
                    }

                    bitmap.value?.let { btm ->
                        UserAvatar(btm)
//                        Image(
//                            bitmap = btm.asImageBitmap(),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .size(400.dp)
//                                .padding(20.dp)
//                        )
                    }
                }


                Icon(
                    painter = painterResource(id = R.drawable.icon_eyes),
                    contentDescription = "icon_eyes",
                    modifier = Modifier
                        .size(36.dp)
                        .padding(top = 16.dp)
                )
                Text(
                    text = "Hoş Geldiniz",
                    style = TextStyle(
                        fontFamily = patrickHandFontFamily,
                        fontSize = 28.sp,
                        letterSpacing = (0.05).em
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )


                Button(
                    enabled = isEnoughInfo && isMailGood,
                    onClick = {
                            if (!isMailGood) {
                                println("kötü mail")

                                Toast.makeText(
                                    context,
                                    "Bu mail ile kayıtlı bir hesap bulunmaktadır.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (!isEnoughInfo) {
                                println("not enough info")

                                Toast.makeText(
                                        context,
                                        "Lütfen mail giriniz.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            } else {
                                println("basarılı giris")
                                appPreferences.user_mail = email

                                navController.navigate("page_main") {
                                    popUpTo("page_login") { inclusive = true }
                                }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoButtonColor),
                ) {
                    Text(
                        text = "Giriş Yap",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isEnoughInfo && isMailGood) Color.White else DarkGreyTextColor
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Bir hesabınız yok mu?",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGreyTextColor,
                    )
                    TextButton(onClick = {
                        navController.navigate("page_signup") {
                          //  popUpTo("page_login") { inclusive = true }
                        }
                    }) {
                        Text(
                            text = "Kayıt Ol",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displaySmall,
                            textDecoration = TextDecoration.Underline,
                            color = DarkGreyTextColor,
                        )
                    }
                }
            }
        }
    }
}
