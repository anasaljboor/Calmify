package ui



import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.borderGlowLite(
    borderColor: Color,
    corner: Dp = 16.dp
): Modifier = this.border(0.8.dp, borderColor, RoundedCornerShape(corner))
