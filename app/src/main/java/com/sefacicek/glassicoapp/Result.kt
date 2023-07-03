package com.sefacicek.glassicoapp

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sefacicek.glassicoapp.ui.theme.DarkGreyTextColor
import com.sefacicek.glassicoapp.ui.theme.GlassicoAppTheme
import com.sefacicek.glassicoapp.ui.theme.IndigoButtonColor
import com.sefacicek.glassicoapp.ui.theme.PlaygroundColor
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun encodeImage(bm: Bitmap): String? {
    val baos = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val b = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultPage() {

    val context = LocalContext.current
    val appPreferences = AppPreferences(context)
    val prefMail = appPreferences.user_mail!!

    var showDialogGlass by remember { mutableStateOf(true) }
    var press by remember { mutableStateOf(false) }
    var textGlass by remember { mutableStateOf(TextFieldValue("")) }

    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(context.applicationContext as Application)
    )

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    GlassicoAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5) //MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (showDialogGlass) {
                    AlertDialog(
                        onDismissRequest = { showDialogGlass = true },
//                        title = { Text(text = "Cam") },
                        text = {
                            TextField(
                                value = textGlass,
                                onValueChange = {
                                    textGlass = it
                                },
                                label = { Text(text = "Cam İsmi") },
//                                placeholder = { Text(text = "Cam ismini giriniz" },
                            )
                        },
                        confirmButton = {
                            Button(
                                enabled = textGlass.text != "",
                                onClick = {
                                    showDialogGlass = false
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoButtonColor),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tamam",
                                        color = if (textGlass.text != "") Color.White else DarkGreyTextColor
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_arrow),
                                        contentDescription = "icon_arrow",
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        },

                        )
                } else {


                    Text(
                        text = textGlass.text,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .clip(shape = RoundedCornerShape(8.dp))
                            .background(PlaygroundColor)
                    ) {

                        Image(
                            bitmap = bitmapRes.asImageBitmap(),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(48.dp)
                                .clip(shape = RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth,
                            contentDescription = "Result Image",
                        )
                    }

                    val glassGeoCenter = glassInfo[0]
                    val glassMontageLine =
                        "${glassInfo[1]}\n${glassInfo[2]}\n${glassInfo[3]}\n${glassInfo[4]}"
                    val glassMontageLineLength = glassInfo[1]
                    val glassMontageLineCenter = glassInfo[2]
                    val glassMontageLineTopLeft = glassInfo[3]
                    val glassMontageLineBottomRight = glassInfo[4]
                    val glassMontagePoint = "${glassInfo[5]}\n${glassInfo[6]}"
                    val glassMontagePointCenter = glassInfo[5]
                    val glassMontagePointRadius = glassInfo[6]
                    val glassWidthInfo = glassInfo[7]
                    val glassHeightInfo = glassInfo[8]

                    ResultItem("Genişlik", glassWidthInfo!!)
                    ResultItem("Yükseklik", glassHeightInfo!!)
                    ResultItem("Geometrik Merkez", glassGeoCenter!!)
                    ResultItem("Montaj Hattı Uzunluğu", glassMontageLineLength!!)
                    ResultItem("Montaj Hattı Merkezi", glassMontageLineCenter!!)
                    ResultItem("Montaj Hattı Başlangıç", glassMontageLineTopLeft!!)
                    ResultItem("Montaj Hattı Bitiş", glassMontageLineBottomRight!!)
                    ResultItem("Montaj Noktası Merkezi", glassMontagePointCenter!!)
                    ResultItem("Montaj Noktası Yarıçapı", glassMontagePointRadius!!)

                    Button(
                        enabled = !press,
                        onClick = {
                            val msg = "Kaydedildi"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d("CameraXApp", msg)
                            press = true
                            val image = encodeImage(bitmapRes)

                            val glassDate = LocalDateTime.now().format(formatter)


                            val glassGeoCenter = glassInfo[0]
                            val glassMontageLineLength = glassInfo[1]
                            val glassMontageLineCenter = glassInfo[2]
                            val glassMontageLineTopLeft = glassInfo[3]
                            val glassMontageLineBottomRight = glassInfo[4]
                            val glassMontagePointCenter = glassInfo[5]
                            val glassMontagePointRadius = glassInfo[6]
                            val glassWidthInfo = glassInfo[7]
                            val glassHeightInfo = glassInfo[8]

                            println("glassGeoCenter: $glassGeoCenter")
                            println("glassMontageLineLength: $glassMontageLineLength")
                            println("glassMontageLineCenter: $glassMontageLineCenter")
                            println("glassMontageLineTopLeft: $glassMontageLineTopLeft")
                            println("glassMontageLineBottomRight: $glassMontageLineBottomRight")
                            println("glassMontagePointCenter: $glassMontagePointCenter")
                            println("glassMontagePointRadius: $glassMontagePointRadius")
                            println("glassWidthInfo: $glassWidthInfo")
                            println("glassHeightInfo: $glassHeightInfo")

                            appViewModel.addGlass(
                                Glass(
                                    null,
                                    prefMail,
                                    textGlass.text,
                                    image!!,
                                    glassDate,
                                    glassGeoCenter!!,
                                    glassMontageLineLength!!,
                                    glassMontageLineCenter!!,
                                    glassMontageLineTopLeft!!,
                                    glassMontageLineBottomRight!!,
                                    glassMontagePointCenter,
                                    glassMontagePointRadius,
                                    glassWidthInfo!!,
                                    glassHeightInfo!!
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoButtonColor)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Kaydet",
                                style = MaterialTheme.typography.displaySmall,
                                color = if (!press) Color.White else DarkGreyTextColor

                            )
                            Icon(
                                painter = painterResource(id = R.drawable.icon_save),
                                contentDescription = "icon_save",
                                tint = if (!press) Color.White else DarkGreyTextColor,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Result Screen")
@Composable
fun PreviewResultPage() {
    GlassicoAppTheme {
        ResultPage()
    }
}


@Composable
fun ResultItem(key: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .weight(0.8f)
        )
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(4.dp))
                .background(PlaygroundColor)
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .padding(8.dp)
            )

        }

    }
}