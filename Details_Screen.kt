package com.example.social_rede_mobile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.social_rede_mobile.data.UserViewModel

@Composable
fun DetailScreen(navController: NavController) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                UserViewModel(context.applicationContext as android.app.Application)
            }
        }
    )

    var bio by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedImageId by remember { mutableStateOf<Int?>(null) }

    val imageOptions = listOf(
        R.drawable.profile_image_1,
        R.drawable.profile_image_2,
        R.drawable.profile_image_3,
        R.drawable.profile_image_4,
        R.drawable.profile_image_5,
        R.drawable.profile_image_6,
        R.drawable.profile_image_7,
        R.drawable.profile_image_8
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Complete Your Profile", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Your Bio") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Date of Birth (dd/mm/yyyy)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Select Profile Picture", style = MaterialTheme.typography.titleMedium)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(imageOptions) { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .border(
                            width = if (selectedImageId == resId) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable { selectedImageId = resId }
                )
            }
        }

        Button(onClick = {
            if (bio.isNotBlank() && dob.isNotBlank() && selectedImageId != null) {
                val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
                val username = prefs.getString("username", null)

                if (username != null) {
                    userViewModel.updateUserDetails(
                        username = username,
                        bio = bio,
                        dob = dob,
                        profileImageResId = selectedImageId!!,
                        onComplete = {
                            Toast.makeText(context, "Details saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        }
                    )
                } else {
                    Toast.makeText(context, "User session not found!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Continue")
        }
    }
}
