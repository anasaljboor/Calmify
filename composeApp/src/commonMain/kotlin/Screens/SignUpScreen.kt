package Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import ui.GlowTextField

@Composable
fun SignUpScreen(
    onSignUp: (email: String, password: String, username: String) -> Unit = { _, _, _ -> },
    onBackToLogin: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val canSubmit =
        username.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                passwordsMatch

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B172A))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // ✅ correct place
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {

            item {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "Calmify logo",
                    modifier = Modifier.size(128.dp)
                )
            }

            item {
                Text(
                    text = "Calmify",
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8A4FFF),
                        textAlign = TextAlign.Center
                    )
                )
            }

            item {
                Text(
                    text = "Create your account",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFD1D5DC),
                        textAlign = TextAlign.Center
                    )
                )
            }

            item {
                Text(
                    text = "Username",
                    fontSize = 14.sp,
                    color = Color(0xFFD1D5DC),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                GlowTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "Choose a username",
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.White)
                )
            }

            item {
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    color = Color(0xFFD1D5DC),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                GlowTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Enter your email",
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.White)
                )
            }

            item {
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    color = Color(0xFFD1D5DC),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                GlowTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Create a password",
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            item {
                Text(
                    text = "Confirm Password",
                    fontSize = 14.sp,
                    color = Color(0xFFD1D5DC),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                GlowTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Re-enter your password",
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                item {
                    Text(
                        text = "Passwords do not match",
                        fontSize = 13.sp,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Button(
                    onClick = { onSignUp(email.trim(), password, username.trim()) },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8A4FFF),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF8A4FFF).copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                ) {
                    Text("Sign Up", fontSize = 16.sp)
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 14.sp,
                        color = Color(0xFF99A1AF)
                    )
                    Text(
                        text = "Log In",
                        fontSize = 14.sp,
                        color = Color(0xFF00FFFF),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onBackToLogin() }
                    )
                }
            }
        }
    }
}
