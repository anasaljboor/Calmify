package Screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.about_icon
import myapplication.composeapp.generated.resources.edit_profile_icon
import myapplication.composeapp.generated.resources.exit_icon
import myapplication.composeapp.generated.resources.notification_icon
import org.jetbrains.compose.resources.painterResource
import ui.GlowButton
import ui.SideMenuItem
import ui.borderGlowLite

@Composable
@Preview

fun SideMenu(
    name: String,
    email: String,
    onClose: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onAbout: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val bg = Color(0xFF0B172A)
    val card = Color(0xFF151E3F)
    val border = Color(0x3301FFFF) // from your figma: 0x33 alpha
    val accent = Color(0xFF00FFFF)
    val purple = Color(0xFF8A4FFF)
    val textMuted = Color(0xFF99A1AF)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(bg)
            .padding(24.dp)
    ) {
        // Close button
        Image(
            painter = painterResource(Res.drawable.exit_icon),
            contentDescription = "Close",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopStart)
                .clickable { onClose() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

                // Profile block (bordered container)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(276.8.dp)
                        .background(card, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                        .borderGlowLite(borderColor = border, corner = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with cyan ring
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(accent)
                                .padding(2.dp)
                                .background(card, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            )


                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = name,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = email,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = textMuted
                            )
                        )
                    }
                }

                // Menu items
                SideMenuItem(
                    icon = Res.drawable.edit_profile_icon,
                    title = "Edit Profile",
                    onClick = onEditProfile
                )
                SideMenuItem(
                    icon = Res.drawable.notification_icon,
                    title = "Notifications",
                    onClick = onNotifications
                )
                SideMenuItem(
                    icon = Res.drawable.about_icon,
                    title = "About the App",
                    onClick = onAbout
                )
            }

            // Logout button
            GlowButton(
                text = "Log Out",
                icon = "drawable/ic_logout.png",
                containerColor = purple,
                onClick = onLogout
            )
        }
    }
}
