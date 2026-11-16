package com.bignerdranch.android.praktika__6.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

@Composable
fun SignInScreen(onSignInClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E12))
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WorldCinema",
                color = Color(0xFF3399CC),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "E-mail", color = Color(0xFF3399CC), fontSize = 10.sp)
            BasicTextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(Color(0xFF3399CC)),
                decorationBox = { innerTextField ->
                    Column {
                        Box(modifier = Modifier.padding(vertical = 2.dp)) {
                            if (email.isEmpty()) {
                                Text("you@example.com", color = Color.Gray, fontSize = 12.sp)
                            }
                            innerTextField()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF3399CC))
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Пароль", color = Color(0xFF3399CC), fontSize = 10.sp)
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(Color(0xFF3399CC)),
                decorationBox = { innerTextField ->
                    Column {
                        Box(modifier = Modifier.padding(vertical = 2.dp)) {
                            if (password.isEmpty()) {
                                Text("Ваш пароль", color = Color.Gray, fontSize = 12.sp)
                            }
                            innerTextField()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF3399CC))
                        )
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFF3399CC))
                .clickable { onSignInClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Войти",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}