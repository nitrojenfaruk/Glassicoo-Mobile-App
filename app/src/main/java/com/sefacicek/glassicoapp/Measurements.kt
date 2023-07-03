package com.sefacicek.glassicoapp

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sefacicek.glassicoapp.ui.theme.DarkGreyTextColor
import com.sefacicek.glassicoapp.ui.theme.GlassicoAppTheme
import com.sefacicek.glassicoapp.ui.theme.PlaygroundColor
import com.sefacicek.glassicoapp.ui.theme.RedButtonColor

@Composable
fun MeasurementsPage(navController: NavController) {

    val context = LocalContext.current
    val appPreferences = AppPreferences(context)
    val prefMail = appPreferences.user_mail!!

    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(context.applicationContext as Application)
    )

    val glasses = appViewModel.findGlass(prefMail).observeAsState().value

    GlassicoAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5) //MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 24.dp),
                ) {
                    Text(
                        text = "Ölçümler  ",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Icon(
                        painter = painterResource(R.drawable.icon_ruler),
                        contentDescription = "icon_ruler",
                        tint = DarkGreyTextColor
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier.padding(start = 38.dp, end = 68.dp),
                        text = "İsim",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = "Cam",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 16.dp,
                        end = 8.dp,
                        bottom = 16.dp
                    )
                )

                LazyColumn {
                    items(glasses?.size ?: 0) { index ->
                        val glass = glasses!![index]
                        val encodeByte: ByteArray = Base64.decode(glass.glassImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
                        AllMeasurementsItem(
                            glass,
                            bitmap,
                            navController
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AllMeasurementsItem(
    glass: Glass,
    bitmap: Bitmap,
    navController: NavController
) {

    val context = LocalContext.current
    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(context.applicationContext as Application)
    )

    var showDialog_Delete by remember { mutableStateOf(false) }
    var showDialog_Info by remember { mutableStateOf(false) }
    var showDialog_Edit by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(size = 8.dp),
        shadowElevation = 1.dp
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
                text = glass.glassName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Glass Image",
                modifier = Modifier
                    .size(56.dp)
                    .weight(1f)
            )

            IconButton(
                onClick = { showDialog_Delete = true },
                modifier = Modifier
                    .weight(0.5f)
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.icon_delete
                    ),
                    contentDescription = "icon_delete",
                    tint = RedButtonColor
                )
            }

            if (showDialog_Delete) {
                AlertDialog(
                    onDismissRequest = { showDialog_Delete = false },
                    title = { Text(text = "Ölçümü Sil") },
                    text = {
                        Text(
                            text = "Ölçümü silmek istediğinize emin misiniz?",
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                appViewModel.deleteGlass(glass)
                                showDialog_Delete = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedButtonColor),
                        ) {
                            Text(text = "Sil", color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showDialog_Delete = false },
                        ) {
                            Text(text = "İptal Et")
                        }
                    },
                )
            }

            IconButton(
                onClick = { showDialog_Info = true },
                modifier = Modifier
                    .weight(0.5f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_info/*iconInfo*/),
                    contentDescription = "icon_info",
                    tint = DarkGreyTextColor
                )
            }

            if (showDialog_Info) {
                AlertDialog(
                    onDismissRequest = { showDialog_Info = false },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Optik Cam Bilgisi")
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Genişlik",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = glass.glassWidth,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Yükseklik",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = glass.glassHeight,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Geometrik Merkez",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = glass.glassGeoCenter,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Montaj Hattı",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "Uzunluk: ${glass.glassMontageLineLength}\nMerkez: ${glass.glassMontageLineCenter}\nBaşlangıç: ${glass.glassMontageLineTopLeft}\nBitiş: ${glass.glassMontageLineBottomRight}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Montaj Noktası",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "Merkez: ${glass.glassMontagePointCenter}\nYarıçap: ${glass.glassMontagePointRadius}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tarih",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = glass.glassDate,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { showDialog_Info = false },
                            ) {
                                Text(text = "Tamam", color = Color.White)
                            }
                        }
                    }
                )
            }

            IconButton(
                onClick = { showDialog_Edit = true },
                modifier = Modifier
                    .weight(0.5f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_pencil/*iconPencil*/),
                    contentDescription = "icon_pencil",
                    tint = DarkGreyTextColor
                )
            }

            if (showDialog_Edit) {
                val text = buildAnnotatedString {
                    append("Ölçümü düzenlemek istediğinize emin misiniz? Bu durumda, ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                        append("mevcut ölçüm silinecektir.")
                    }
                }

                AlertDialog(
                    onDismissRequest = { showDialog_Edit = false },
                    title = { Text(text = "Ölçüm Düzenlemesi") },

                    text = {
                        Text(text = text)
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                appViewModel.deleteGlass(glass)
                                navController.navigate("page_camera") {
                                    popUpTo("page_measurements") { inclusive = false }
                                }
                                showDialog_Edit = false

                            },
                        ) {
                            Text(text = "Düzenle", color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showDialog_Edit = false },
                        ) {
                            Text(text = "İptal Et")
                        }
                    },
                )
            }
        }
    }
}