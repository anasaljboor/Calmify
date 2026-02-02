package Screens

import Wearable_Hub.AppGraph
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import ui.GlowCard

data class Tip(val number: String, val title: String, val desc: String)

@Composable
fun HomeScreen(
    graph: AppGraph,
    userName: String,
    onOpenMenu: () -> Unit,
    onOpenBreathing: () -> Unit,
    onStartSimulation: () -> Unit
) {
    val stress by graph.stressStore.state.collectAsState()
    val latestSim by graph.latestSim.collectAsState()
    val running by graph.stressStore.isRunning.collectAsState()

    // ✅ charts from API (StateFlow)
    val dailyStress by graph.dailyChart.collectAsState()
    val weeklyStress by graph.weeklyChart.collectAsState()

    // ✅ refresh charts when screen appears (and user is logged in)
    LaunchedEffect(Unit) {
        graph.refreshChartsIfLoggedIn()
    }

    val avgStress = latestSim?.summary?.get("avgStress").asIntOrNull()
    val maxStress = latestSim?.summary?.get("maxStress").asIntOrNull()
    val avgHr = latestSim?.summary?.get("avgHr").asIntOrNull()

    val bg = Color(0xFF0B172A)
    val accent = Color(0xFF00FFFF)
    val purple = Color(0xFF8A4FFF)
    val textMuted = Color(0xFFD1D5DC)

    val tips = listOf(
        Tip("1", "4-7-8 Breathing", "Inhale for 4 seconds, hold for 7, exhale for 8"),
        Tip("2", "Box Breathing", "Breathe in, hold, out, hold - each for 4 counts"),
        Tip("3", "Deep Belly Breaths", "Focus on expanding your diaphragm fully")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back,",
                        style = TextStyle(
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = userName,
                        style = TextStyle(
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }

                val initial = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable { onOpenMenu() }
                        .background(Color(0xFF151E3F))
                        .padding(2.dp)
                        .background(accent, CircleShape)
                        .padding(2.dp)
                        .background(Color(0xFF151E3F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        item {
            GlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                corner = 16.dp,
                padding = PaddingValues(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    tips.forEach { tip ->
                        TipRow(
                            number = tip.number,
                            title = tip.title,
                            desc = tip.desc,
                            accent = accent,
                            textMuted = textMuted
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Heart Rate",
                    value = "${stress.hrBpm ?: "--"} BPM",
                    subtitle = avgHr?.let { "Last sim avg: $it" } ?: "Live",
                    accent = accent
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "HRV\nRMSSD",
                    value = stress.rmssd?.let { "${it.toInt()} ms" } ?: "--",
                    subtitle = "Live",
                    accent = accent
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Stress\nLevel",
                    value = "${stress.stress0to100}/100",
                    subtitle = when {
                        maxStress != null -> "Last sim max: $maxStress"
                        else -> if (stress.stress0to100 >= 75) "High" else "OK"
                    },
                    accent = accent
                )
            }
        }

        item {
            Text(
                text = "Daily Report",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }

        item {
            GlowCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 16.dp,
                padding = PaddingValues(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Today's Progress",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )

                    StressBarChart(
                        values = dailyStress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFF0F1A35), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        yMax = 100f,
                        labelEvery = 4,
                        accent = accent,
                        secondary = purple
                    )

                    LegendRow(accent = accent, purple = purple)

                    // ✅ FIXED NoteBox (single text so it won't go vertical)
                    NoteBoxSingle(
                        prefix = "Your stress peaked at ",
                        highlight = "4PM",
                        suffix = " but you're doing great!",
                        accent = accent
                    )
                }
            }
        }

        item {
            Text(
                text = "Weekly Report",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }

        item {
            GlowCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 16.dp,
                padding = PaddingValues(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Calm vs. Stress",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )

                    StressLineChart(
                        values = weeklyStress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFF0F1A35), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        yMax = 100f,
                        lineColor = accent
                    )

                    LegendRow(accent = accent, purple = purple)

                    // ✅ FIXED NoteBox
                    NoteBoxSingle(
                        prefix = "Your average stress level is down ",
                        highlight = "12%",
                        suffix = " this week.",
                        accent = accent
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            Button(
                onClick = {
                    onStartSimulation()
                    // after starting / posting simulation, refresh charts
                    graph.refreshChartsIfLoggedIn()
                },
                enabled = !running,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A4FFF),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF8A4FFF).copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (running) "Simulation Running" else "Start Simulation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Button(
                onClick = onOpenBreathing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF151E3F),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Open Breathing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TipRow(
    number: String,
    title: String,
    desc: String,
    accent: Color,
    textMuted: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color(0xFF0B172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Start with $title :",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.White
                )
            )
            Text(
                text = desc,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = textMuted
                )
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String,
    accent: Color
) {
    GlowCard(
        modifier = modifier.height(134.dp),
        corner = 16.dp,
        padding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)
            Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF99A1AF), fontSize = 12.sp)
        }
    }
}

@Composable
private fun LegendRow(accent: Color, purple: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        LegendDot(label = "Calm", color = accent)
        Spacer(Modifier.width(22.dp))
        LegendDot(label = "Stress", color = purple)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color(0xFFD1D5DC), fontSize = 12.sp)
    }
}

/**
 * ✅ Fix for your vertical wrapping bug:
 * Use ONE Text with an AnnotatedString instead of 3 Texts in a Row.
 */
@Composable
private fun NoteBoxSingle(
    prefix: String,
    highlight: String,
    suffix: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1A35), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append(prefix)
                withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
                    append(highlight)
                }
                append(suffix)
            },
            color = Color(0xFFD1D5DC),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun StressBarChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
    yMax: Float = 100f,
    labelEvery: Int = 4,
    accent: Color,
    secondary: Color
) {
    val safe = if (values.isEmpty()) List(24) { 0 } else values

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val paddingTop = 10f
        val paddingBottom = 16f
        val chartH = h - paddingTop - paddingBottom

        val gridLines = 4
        repeat(gridLines + 1) { i ->
            val y = paddingTop + chartH * (i / gridLines.toFloat())
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        val n = safe.size
        val gap = max(6f, w * 0.012f)
        val barW = (w - gap * (n + 1)) / n

        for (i in 0 until n) {
            val v = safe[i].coerceIn(0, yMax.toInt()).toFloat()
            val barH = (v / yMax) * chartH
            val left = gap + i * (barW + gap)
            val top = paddingTop + (chartH - barH)

            val color = if (v >= 70f) secondary else accent

            drawRoundRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(left, top),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(10f, 10f)
            )

            if (labelEvery > 0 && i % labelEvery == 0) {
                val x = left + barW / 2f
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(x, h - 10f),
                    end = Offset(x, h - 2f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun StressLineChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
    yMax: Float = 100f,
    lineColor: Color
) {
    val safe = if (values.isEmpty()) List(7) { 0 } else values

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val paddingTop = 10f
        val paddingBottom = 10f
        val chartH = h - paddingTop - paddingBottom

        val gridLines = 4
        repeat(gridLines + 1) { i ->
            val y = paddingTop + chartH * (i / gridLines.toFloat())
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        val n = safe.size
        if (n < 2) return@Canvas

        val stepX = w / (n - 1).toFloat()

        fun point(i: Int): Offset {
            val v = safe[i].coerceIn(0, yMax.toInt()).toFloat()
            val y = paddingTop + (chartH - (v / yMax) * chartH)
            val x = i * stepX
            return Offset(x, y)
        }

        for (i in 0 until n - 1) {
            val p1 = point(i)
            val p2 = point(i + 1)
            drawLine(
                color = lineColor.copy(alpha = 0.9f),
                start = p1,
                end = p2,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        for (i in 0 until n) {
            val p = point(i)
            drawCircle(color = Color.White.copy(alpha = 0.95f), radius = 6f, center = p)
            drawCircle(color = lineColor, radius = 4f, center = p)
        }
    }
}

private fun Any?.asIntOrNull(): Int? = when (this) {
    is Int -> this
    is Long -> this.toInt()
    is Double -> this.toInt()
    is Float -> this.toInt()
    is Number -> this.toInt()
    is String -> this.trim().toIntOrNull()
    else -> null
}
