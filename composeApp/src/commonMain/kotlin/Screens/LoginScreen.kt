package Screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B172A))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Image(
                // If you use Res.* then replace this painterResource call accordingly
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Calmify logo",
                modifier = Modifier.size(128.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Calmify",
                style = TextStyle(
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8A4FFF),
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = "Welcome Back",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFD1D5DC),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(18.dp))

            // Email label
            Text(
                text = "Email Address",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFD1D5DC)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )

            GlowTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.White)
            )

            Spacer(Modifier.height(10.dp))

            // Password label row (with optional "Forgot Password?" on right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFD1D5DC)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Forgot Password?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF00FFFF)
                    ),
                    modifier = Modifier.clickable { onForgotPassword() }
                )
            }

            GlowTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter your password",
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(10.dp))

            // Login button (purple, rounded)
            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A4FFF),
                    contentColor = Color.White
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
            ) {
                Text("Log In", fontSize = 16.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Bottom sign-up line
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF99A1AF)
                    )
                )
                Text(
                    text = "Sign Up",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF00FFFF),
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { onSignUp() }
                )
            }
        }
    }
}
