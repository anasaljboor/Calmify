package ui


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 49.6.dp,
    corner: Dp = 14.dp,
    background: Color = Color(0xFF151E3F),
    borderColor: Color = Color(0xFF00FFFF),
    glowColor: Color = Color(0xFF00FFFF),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    textStyle: TextStyle = TextStyle(color = Color.White),
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val shape = RoundedCornerShape(corner)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                // Multiplatform "glow": draw multiple strokes with low alpha
                val r = corner.toPx()
                val w = size.width
                val h = size.height

                // outer glow rings (bigger stroke = further glow)
                val rings = listOf(18f to 0.10f, 12f to 0.16f, 7f to 0.22f)
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
            .border(0.8.dp, borderColor, shape)
            .padding(contentPadding),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = textStyle.copy(color = Color(0xFF6B7280))
                )
            }
            inner()
        }
    )
}
