package com.example.theapp

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// fonts and class
data class UserProfile(val username: String, val isProfessional: Boolean = false)
val CursiveFont = FontFamily(Font(R.font.cursive))
val PixelFont = FontFamily(Font(R.font.pixel))

//background
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF000000)) {
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

    // Date formatting
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())
    //chat open/close
    var isChatOpen by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    // Load today's specific usage
    var todaySeconds by remember { mutableLongStateOf(prefs.getLong(today, 0L)) }
    // random quotes
    val quotes = listOf("Let us brighten your day!", "Keep living", "Don't give up", "Keep Yourself Safe <3")
    val randomQuote = remember { quotes.random() }
    val percentage = ((rotationAngle / 360f) * 100).toInt()

    // Save today's usage
    LaunchedEffect(todaySeconds) {
        prefs.edit().putLong(today, todaySeconds).apply()
    }

    // Timer logic
    LaunchedEffect(rotationAngle) {
        while (rotationAngle > 10f) {
            delay(1000L)
            todaySeconds++
        }
    }

    val timeDisplay = "${todaySeconds / 3600} H ${(todaySeconds % 3600) / 60} M"
//chat
    if (isChatOpen) {
        FullChatView(onBack = { isChatOpen = false })
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("LuminousWear", color = Color.White, fontSize = 18.sp, fontFamily = PixelFont)

                // History Calendar
                Text(timeDisplay, color = Color.White, fontSize = 18.sp, fontFamily = PixelFont,
                    modifier = Modifier.clickable {
                        showHistoryCalendar(context, prefs)
                    })
            }
                //show quote
            Text(text = randomQuote, color = Color.White, textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = CursiveFont, fontSize = 48.sp))

            Spacer(modifier = Modifier.height(15.dp))

            // SLIDER text in
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(340.dp)) {
                CustomSunSlider(angle = rotationAngle, onAngleChange = { rotationAngle = it })
                Text("$percentage%", color = Color(0xFBC9CFFF), fontSize = 32.sp, fontFamily = PixelFont)
            }

            Spacer(modifier = Modifier.weight(1f))

            // COMMUNITY
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("COMMUNITY", color = Color.White, fontFamily = PixelFont, fontSize = 14.sp)
                IconButton(onClick = { isChatOpen = true }) {
                    Icon(painter = painterResource(id = R.drawable.ic_chat), contentDescription = null, tint = Color(
                        0xFFFFFFFF
                    ), modifier = Modifier.size(26.dp))
                }
            }
            UserChatRow(UserProfile("NewUser", false), "Hi!")
            UserChatRow(UserProfile("LumRep", true), "How can I help you?")

            Spacer(modifier = Modifier.height(5.dp))

            Button(
                onClick = { /* Bluetooth Logic */ },
                modifier = Modifier.width(240.dp).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CONNECT TO DEVICE", color = Color.Black, fontFamily = PixelFont, fontSize = 13.sp)
            }
        }
    }
}

// HISTORY CALENDAR function
fun showHistoryCalendar(context: Context, prefs: android.content.SharedPreferences) {
    val c = Calendar.getInstance()
    val dialog = DatePickerDialog(context, R.style.Theme_Theapp, { _, year, month, day ->
        // Create the key for the selected date
        val selectedDate = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
        val savedSeconds = prefs.getLong(selectedDate, 0L)

        val h = savedSeconds / 3600
        val m = (savedSeconds % 3600) / 60

        // Show a popup with the history
        Toast.makeText(context, "Usage on $selectedDate: $h Hours, $m Mins", Toast.LENGTH_LONG).show()

    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))

    dialog.setOnShowListener {
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#BC9CFF"))
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.parseColor("#BC9CFF"))
    }
    dialog.show()
}
   // sun slider
@Composable
   // sun image
fun CustomSunSlider(angle: Float, onAngleChange: (Float) -> Unit) {
    val sunImage = painterResource(id = R.drawable.ic_sun)
    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectDragGestures { change, _ ->
            val center = Offset(size.width / 2f, size.height / 2f)
            val dx = change.position.x - center.x
            val dy = change.position.y - center.y
            var a = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            if (a < 0) a += 360f
            onAngleChange(a)
        }
        //sun glow
    }, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2.3f
            drawIntoCanvas { canvas ->
                val glowPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#BC9CFF")
                    style = Paint.Style.STROKE
                    strokeWidth = 10.dp.toPx()
                    maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawCircle(center.x, center.y, radius, glowPaint)
            }
            drawCircle(color = Color(0xFFBC9CFF), radius = radius, style = Stroke(width = 2.dp.toPx()), alpha = 0.5f)
        }
        //
        Box(modifier = Modifier.graphicsLayer {
            val orbit = 145.dp.toPx()
            translationX = orbit * cos(Math.toRadians(angle.toDouble())).toFloat()
            translationY = orbit * sin(Math.toRadians(angle.toDouble())).toFloat()
            rotationZ = angle
        }, contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(140.dp).background(Brush.radialGradient(listOf(Color(0xFFBC9CFF).copy(0.6f), Color.Transparent)), CircleShape))
            Image(painter = sunImage, contentDescription = null, modifier = Modifier.size(110.dp))
        }
    }
}

@Composable
fun UserChatRow(user: UserProfile, message: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(30.dp).background(if (user.isProfessional) Color(0xFFBC9CFF) else Color.Gray, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(user.username, color = Color.White, fontFamily = PixelFont, fontSize = 12.sp)
        if (user.isProfessional) Text(" [PRO]", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(": $message", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
    }
}
//chat function window
@Composable
fun FullChatView(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0B071E)).padding(24.dp)) {
        Text("← BACK", color = Color.White, fontFamily = PixelFont, modifier = Modifier.clickable { onBack() })
        Spacer(modifier = Modifier.height(20.dp))
        Text("GLOBAL CHAT", color = Color.White, fontFamily = PixelFont, fontSize = 24.sp)
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color.White.copy(0.1f), RoundedCornerShape(8.dp)).padding(12.dp)) {
            Text("Type something...", color = Color.Gray, fontFamily = PixelFont)
        }
    }
}