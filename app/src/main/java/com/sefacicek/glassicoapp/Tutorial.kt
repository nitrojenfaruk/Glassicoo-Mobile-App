package com.sefacicek.glassicoapp

import android.app.Application
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sefacicek.glassicoapp.ui.theme.GlassicoAppTheme
import com.sefacicek.glassicoapp.ui.theme.IndigoButtonColor
import com.sefacicek.glassicoapp.ui.theme.PlaygroundColor

var showTutorial: Boolean = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialPage(navController: NavController) {

    val checkedState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appViewModel : AppViewModel = viewModel(
        factory = AppViewModelFactory(context.applicationContext as Application)
    )

    GlassicoAppTheme {

        val appPreferences = AppPreferences(context)

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
                Text(
                    modifier = Modifier
                        .padding(bottom = 24.dp),
                    text = "Uygulama",
                    style = MaterialTheme.typography.titleLarge,
                )

                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(PlaygroundColor)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 12.dp, top = 16.dp, end = 12.dp)
                                .clip(shape = RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth,
                            painter = painterResource(R.drawable.tutorial_image),
                            contentDescription = "Tutorial Image",
                        )
                        Icon(
                            painter = painterResource(R.drawable.icon_camera),
                            contentDescription = "icon_camera",
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                Instructions()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = checkedState.value,
                        onCheckedChange = {
                            checkedState.value = it
                            showTutorial = (!checkedState.value)
                            appPreferences.user_tutorial =  (!checkedState.value)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFF5F5F5),
                            uncheckedColor = IndigoButtonColor,
                            checkmarkColor = IndigoButtonColor
                        )
                    )

                    Text(
                        text = "Bir Daha Gösterme",
                        style = MaterialTheme.typography.displaySmall,
                        color = IndigoButtonColor
                    )
                }

                Button(
                    onClick = {
                        navController.navigate("page_camera") {
                            popUpTo("page_tutorial") { inclusive = true }
                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoButtonColor),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "İlerle",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.icon_arrow),
                            contentDescription = "icon_arrow",
                            tint = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Tutorial Screen")
@Composable
fun PreviewTutorialPage() {
    val navController = rememberNavController()
    GlassicoAppTheme {
        TutorialPage(navController)
    }
}

@Composable
fun Instructions() {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 36.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_instruction),
            contentDescription = "icon_instruction_1",
            modifier = Modifier.padding(end = 16.dp)
        )
        val text = buildAnnotatedString {
            append("Optik camı, ")
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("kırmızı kareler")
            }
            append(" ile belirlenen alanın içerisine yerleştiriniz.")
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )

    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_instruction),
            contentDescription = "icon_instruction_2",
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            style = MaterialTheme.typography.labelLarge,
            text = "Uygun yerleşimi yaptıktan sonra kamera butonu " +
                    "ile ürünün fotoğrafını çekebilirsiniz, " +
                    "galeriden fotoğraf ekleyebilirsiniz ya da video " +
                    "kaydedebilirsiniz."
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_instruction),
            contentDescription = "icon_instruction_3",
            modifier = Modifier.padding(end = 16.dp)
        )
        val text = buildAnnotatedString {
            append("Ürünün tespiti için ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                append("4 fotoğraf")
            }
            append(" gerekmektedir.")
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )

    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_instruction),
            contentDescription = "icon_instruction_4",
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            style = MaterialTheme.typography.labelLarge,
            text = "Elde edilen sonucu " +
                    "sonradan görüntüleyebilir, " +
                    "düzenleyebilir ve paylaşabilirsiniz."
        )
    }

}