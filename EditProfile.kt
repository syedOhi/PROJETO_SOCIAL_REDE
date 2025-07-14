package com.example.social_rede_mobile

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", android.content.Context.MODE_PRIVATE)
    val loggedInUsername = prefs.getString("username", "") ?: ""

    val userViewModel: UserViewModel = viewModel(factory = viewModelFactory {
        initializer { UserViewModel(context.applicationContext as Application) }
    })

    val userLiveData = userViewModel.getUserByUsername(loggedInUsername)
    val currentUser by userLiveData.observeAsState()

    // Editable fields
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    // Fill initial values
    LaunchedEffect(currentUser) {
        currentUser?.let {
            fullName = it.fullName
            password = it.password
            bio = it.bio
            dob = it.dob
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 👤 Profile Image
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(top = 8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                val profilePainter = when {
                    !currentUser?.profileImageUri.isNullOrEmpty() -> {
                        rememberAsyncImagePainter(Uri.parse(currentUser!!.profileImageUri))
                    }
                    currentUser?.profileImageResId != null -> {
                        painterResource(id = currentUser!!.profileImageResId!!)
                    }
                    else -> {
                        painterResource(id = R.drawable.profile_icon)
                    }
                }

                Image(
                    painter = profilePainter,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .shadow(8.dp, CircleShape)
                        .clickable { /* TODO: Handle image picker */ }
                )

                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit Picture",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .offset(x = (-6).dp, y = (-6).dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 📝 Editable Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("DD/MM/YYYY") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 💾 Save Button
            Button(
                onClick = {
                    currentUser?.let {
                        val updatedUser = it.copy(
                            fullName = fullName,
                            password = password,
                            bio = bio,
                            dob = dob
                        )
                        userViewModel.updateUser(updatedUser)
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar alterações", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}
