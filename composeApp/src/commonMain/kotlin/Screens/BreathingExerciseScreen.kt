package Screens

import Wearable_Hub.AppGraph
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun BreathingExerciseScreen(
    graph: AppGraph,
    totalSeconds: Int = 5 * 60,
    onBack: () -> Unit = {},
    onEndSession: () -> Unit = {},
) {
    val bg = Color(0xFF0B172A)
    val cyan = Color(0xFF00FFFF)
    val cyanGlowStrong = Color(0x9900FFFF)
    val cyanGlowSoft = Color(0x4D00FFFF)

    // ---- Timer state ----
    var remainingSec by remember { mutableStateOf(totalSeconds) }
    var running by remember { mutableStateOf(true) }

    // ---- Breathing pattern (4-7-8) ----
    // 0..3 inhale, 4..10 hold, 11..18 exhale
    fun instructionFor(secondInCycle: Int): String = when {
        secondInCycle < 4 -> "Inhale…"
        secondInCycle < 11 -> "Hold…"
        else -> "Exhale…"
    }

    // cycle length = 19 seconds (4 + 7 + 8)
    val cycleLen = 19
    val secInCycle = (totalSeconds - remainingSec) % cycleLen
    val instruction = instructionFor(secInCycle)

    // ---- Countdown effect ----
    LaunchedEffect(running) {
        while (running && remainingSec > 0) {
            delay(1000)
            remainingSec -= 1
        }
        if (remainingSec <= 0) {
            running = false
        }
    }

    // ---- Pulse animation (scale) ----
    val targetScale = when (instruction) {
        "Inhale…" -> 1.08f
        "Hold…" -> 1.04f
        else -> 0.96f // Exhale…
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "breathScale"
    )

    fun fmt(sec: Int): String {
        val s = max(0, sec)
        val m = s / 60
        val r = s % 60
        return m.toString() + ":" + r.toString().padStart(2, '0')
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        // Top bar (Back)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable { onBack() }
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", style = TextStyle(fontSize = 18.sp, color = Color(0x99FFFFFF)))
            Spacer(Modifier.width(10.dp))
            Text("Back", style = TextStyle(fontSize = 16.sp, color = Color(0x99FFFFFF)))
        }

        // Main content centered
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing circle + pulse
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(scale)
                    .shadow(
                        elevation = 40.dp,
                        spotColor = cyanGlowStrong,
                        ambientColor = cyanGlowStrong,
                        shape = CircleShape
                    )
                    .border(width = 4.dp, color = cyan, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = instruction,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            // Timer chip
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 20.dp,
                        spotColor = cyanGlowSoft,
                        ambientColor = cyanGlowSoft,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(width = 1.6.dp, color = cyan, shape = RoundedCornerShape(16.dp))
                    .background(color = bg, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fmt(remainingSec),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = cyan,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Optional: pause/resume
            Text(
                text = if (running) "Tap to Pause" else "Tap to Resume",
                color = Color(0xFF99A1AF),
                modifier = Modifier.clickable { running = !running }
            )
        }

        // Bottom button (End Session)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF8A4FFF))
                .clickable { onEndSession() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "End Session",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                    color = Color.White
                )
            )
        }
    }
}
