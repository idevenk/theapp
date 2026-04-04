
package com.example.theapp

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.math.*

data class UserProfile(
    val username: String,
    val isProfessional: Boolean = false
)

val CursiveFont = FontFamily(Font(R.font.cursive))
val PixelFont = FontFamily(Font(R.font.pixel))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B071E)) {
                    LuminousScreen()
                }
            }
        }
    }
}

@Composable
fun LuminousScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("luminous_prefs", Context.MODE_PRIVATE) }
    val quotes = listOf("Let us brighten your day!", "Keep living", "Don't give up")
    val randomQuote = remember { quotes.random() }
    var rotationAngle by remember { mutableStateOf(0f) }
    var secondsActive by remember { mutableLongStateOf(prefs.getLong("saved_seconds", 0L)) }
    val percentage = ((rotationAngle / 360f) * 100).toInt()

    LaunchedEffect(secondsActive) {
        prefs.edit().putLong("saved_seconds", secondsActive).apply()
    }

    LaunchedEffect(rotationAngle) {
        while (rotationAngle > 10f) {
            kotlinx.coroutines.delay(1000L)
            secondsActive++
        }
    }

    val timeDisplay = "${secondsActive / 3600} H ${(secondsActive % 3600) / 60} M"

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("LuminousWear", color = Color.White, fontSize = 18.sp, fontFamily = PixelFont)
            Text(timeDisplay, color = Color.White, fontSize = 18.sp, fontFamily = PixelFont,
                modifier = Modifier.clickable { showPurpleCalendar(context) })
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = randomQuote,
                color = Color.White,
                style = TextStyle(fontStyle = FontStyle.Italic, fontSize = 22.sp)
            )

        Spacer(modifier = Modifier.height(30.dp))

        Box(contentAlignment = Alignment.Center) {
            CustomSunSlider(rotationAngle) { rotationAngle = it }
            Text("$percentage%", color = Color(0xFFBC9CFF), fontSize = 30.sp, fontFamily = PixelFont)
        }

        Spacer(modifier = Modifier.weight(1f))


        UserChatRow(UserProfile("LumRep", true), "I'm a profesional nigga")
        UserChatRow(UserProfile("NewUser123", false), "Just got my first jacket!")

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { },
            modifier = Modifier.width(220.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("CONNECT TO DEVICE", color = Color.Black, fontFamily = PixelFont, fontSize = 12.sp)
        }
    }
}

@Composable
fun UserChatRow(user: UserProfile, message: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(30.dp).background(if(user.isProfessional) Color(0xFFBC9CFF) else Color.Gray, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(user.username, color = Color.White, fontFamily = PixelFont, fontSize = 12.sp)
        if (user.isProfessional) {
            Text(" [PRO]", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(": $message", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun CustomSunSlider(angle: Float, onAngleChange: (Float) -> Unit) {
    val sunImage = painterResource(id = R.drawable.ic_sun)

    Box(
        modifier = Modifier.size(320.dp).pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = change.position.x - center.x
                val dy = change.position.y - center.y
                var a = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (a < 0) a += 360f
                onAngleChange(a)
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2.3f
            drawCircle(
                color = Color(0xFFBC9CFF),
                radius = radius,
                style = Stroke(width = 2.dp.toPx()),
                alpha = 0.3f
            )
        }

        Box(modifier = Modifier.graphicsLayer {
            val orbit = 140.dp.toPx()
            translationX = orbit * cos(Math.toRadians(angle.toDouble())).toFloat()
            translationY = orbit * sin(Math.toRadians(angle.toDouble())).toFloat()
            rotationZ = angle
        }) {
            Box(modifier = Modifier.size(150.dp).background(
                Brush.radialGradient(listOf(Color(0xFFBC9CFF).copy(0.4f), Color.Transparent)), CircleShape)
            )
            Image(painter = sunImage, contentDescription = null, modifier = Modifier.size(110.dp))
        }
    }
}

fun showPurpleCalendar(context: Context) {
    val c = Calendar.getInstance()
    DatePickerDialog(context, 0, { _, _, _, _ -> },
        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
}