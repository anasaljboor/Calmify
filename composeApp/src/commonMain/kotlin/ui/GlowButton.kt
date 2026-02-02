package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.log_out
import org.jetbrains.compose.resources.painterResource

@Composable
fun GlowButton(
    text: String,
    icon: String? = null,
    containerColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(containerColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                Image(
                    painter = painterResource(Res.drawable.log_out),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                style = TextStyle(fontSize = 16.sp, color = Color.White)
            )
        }
    }
}
