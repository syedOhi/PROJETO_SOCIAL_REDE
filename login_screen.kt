package com.example.social_rede_mobile

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.social_rede_mobile.data.UserViewModel

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                UserViewModel(context.applicationContext as Application)
            }
        }
    )

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val textFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
        disabledPlaceholderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.bz_logo),
            contentDescription = "Buzz Connect Logo",
            modifier = Modifier
                .size(160.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(320.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Nome de utilizador") },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Palavra-passe") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = "Toggle Password Visibility")
                        }
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        userViewModel.login(
                            username = username,
                            password = password,
                            onSuccess = {
                                navController.navigate("home")
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Enter all fields", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Login")
                }


                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Não tem conta? Register",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }
        }
    }
}
