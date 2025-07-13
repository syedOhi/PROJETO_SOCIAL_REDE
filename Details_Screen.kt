package com.example.social_rede_mobile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.social_rede_mobile.data.UserViewModel
import androidx.compose.ui.graphics.Color
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import android.content.Intent
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

    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri.value = it
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Complete o seu perfil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("A sua biografia") },
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Data de nascimento (dd/mm/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Escolha uma foto de perfil:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(onClick = {
                imagePickerLauncher.launch("image/*")
            }) {
                Text("Selecionar imagem da galeria")
            }

            selectedImageUri.value?.let { uri ->

            Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Profile Image",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(top = 12.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (bio.isNotBlank() && dob.isNotBlank() && selectedImageUri != null) {
                    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
                    val username = prefs.getString("username", null)

                    if (username != null) {
                        userViewModel.updateUserDetailsWithUri(
                            username = username,
                            bio = bio,
                            dob = dob,
                            profileImageUri = selectedImageUri.value.toString(),

                                    onComplete = {
                                Toast.makeText(context, "Detalhes guardados!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            }
                        )
                    } else {
                        Toast.makeText(context, "Sessão não encontrada!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}

