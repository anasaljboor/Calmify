package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    background: Color = Color(0xFF151E3F),
    borderColor: Color = Color(0xFF00FFFF),
    glowColor: Color = Color(0xFF00FFFF),
    borderWidth: Dp = 0.8.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(corner)

    Box(
        modifier = modifier
            .drawBehind {
                val r = corner.toPx()
                val w = size.width
                val h = size.height
                val rings = listOf(22f to 0.10f, 14f to 0.16f, 8f to 0.22f)
                rings.forEach { (stroke, alpha) ->
                    drawRoundRect(
                        color = glowColor.copy(alpha = alpha),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(r, r),
                        style = Stroke(width = stroke)
                    )
                }
            }
            .background(background, shape)
            .border(borderWidth, borderColor, shape)
            .padding(padding)
    ) { content() }
}