package com.example.sampleuiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sampleuiapp.ui.theme.SampleUIAppTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleUIAppTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }

                // AuthenticationScreenPreview()
                LoginScreenPreview()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SampleUIAppTheme {
        Greeting("Android")
    }
}


@Composable
fun AuthenticationScreen() {
    // Main background with gradient visible at edges
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF002569)
                    )
                )
            )
    ) {
        // Full screen white background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Content column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF000000)
                    )
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_add),
                        contentDescription = "Plus Icon",
                        tint = Color(0xFF00A0E9),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Authenticate your device to begin using AI Assistant",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 6-digit code boxes
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..6) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$i",
                                fontSize = 24.sp,
                                color = Color(0xFF5A7591)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Provide this 6 digit code to your admin to start the app",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF5A7591)
                )

                Spacer(modifier = Modifier.weight(1f))
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthenticationScreenPreview() {
    AuthenticationScreen()
}

@Composable
fun LoginScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF002569)
                    )
                )
            )
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF000000)
                    )
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_add),
                        contentDescription = "Plus Icon",
                        tint = Color(0xFF00A0E9),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Continue using you AI Assistant",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF333333)
                )

                Text(
                    text = "Choose a method to log in",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login options section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // QR Code section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Login with QR Code",
                            fontSize = 14.sp,
                            color = Color(0xFF5A7591),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // QR Code image placeholder
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.Black)
                                .padding(8.dp)
                        ) {
                            // In a real app, replace with your actual QR code
                            // For preview purposes, we'll use a simple black box
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Scan this with your mobile device\nto log in instantly",
                            fontSize = 12.sp,
                            color = Color(0xFF5A7591),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Vertical divider with OR text
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(150.dp)
                    ) {
                        // Vertical divider
                        Divider(
                            color = Color(0xFFD0D0D0),
                            modifier = Modifier
                                .width(1.dp)
                                .height(150.dp)
                        )

                        // OR text with background
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5))
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "OR",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5A7591)
                            )
                        }
                    }

                    // Google login section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                    ) {
                        Button(
                            onClick = { /* Google login action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4285F4)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                // Google icon placeholder
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.White, RoundedCornerShape(50))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        color = Color(0xFF4285F4),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Login with Google",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // App dock at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 1..6) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // This would be replaced with actual app icons
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
