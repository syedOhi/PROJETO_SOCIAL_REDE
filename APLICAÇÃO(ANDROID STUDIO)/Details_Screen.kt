package com.example.social_rede_mobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.rememberAsyncImagePainter
import androidx.navigation.NavController
import com.example.social_rede_mobile.data.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory {
            initializer { UserViewModel(context.applicationContext as android.app.Application) }
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

    // ---- UI ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0B1220)
                    )
                )
            )
            .padding(18.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Complete o seu perfil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Adicione bio, data de nascimento e uma foto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                }

                // Avatar preview (big + nice)
                Box(contentAlignment = Alignment.BottomEnd) {
                    val painter =
                        if (selectedImageUri.value != null) rememberAsyncImagePainter(selectedImageUri.value)
                        else rememberAsyncImagePainter(null)

                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(
                                width = 2.dp,
                                color = Color(0x336366F1),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri.value != null) {
                            Image(
                                painter = painter,
                                contentDescription = "Selected Profile Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "🙂",
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    }
                }

                // Pick image button
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x196366F1),
                        contentColor = Color(0xFFC7D2FE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Selecionar imagem da galeria", fontWeight = FontWeight.Bold)
                }

                // Fields
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("A sua biografia") },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0x0FFFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedBorderColor = Color(0x22FFFFFF),
                        focusedBorderColor = Color(0x806366F1),
                        focusedLabelColor = Color(0xFFC7D2FE),
                        cursorColor = Color(0xFF6366F1),
                        focusedTextColor = Color(0xFFE5E7EB),
                        unfocusedTextColor = Color(0xFFE5E7EB)
                    )
                )

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Data de nascimento (dd/mm/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFE5E7EB),   // 👈 FIX
                        unfocusedTextColor = Color(0xFFE5E7EB),
                        unfocusedContainerColor = Color(0x0FFFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedBorderColor = Color(0x22FFFFFF),
                        focusedBorderColor = Color(0x806366F1),
                        focusedLabelColor = Color(0xFFC7D2FE),
                        cursorColor = Color(0xFF6366F1)
                    )
                )

                // Save
                Button(
                    onClick = {
                        if (bio.isNotBlank() && dob.isNotBlank() && selectedImageUri.value != null) {
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)
                }

                // Tiny hint
                Text(
                    text = "Pode alterar isto depois no Perfil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}