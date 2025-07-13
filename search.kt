package com.example.social_rede_mobile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.social_rede_mobile.data.UserViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.ArrowBack
import coil.compose.rememberAsyncImagePainter
import android.net.Uri

@Composable
fun SearchScreen(userViewModel: UserViewModel, navController: NavController)
 {
    val query by userViewModel.searchQuery.collectAsState()
    val searchResults by userViewModel.searchResults.collectAsState()

    val isLoading = remember(query) { query.isNotEmpty() && searchResults.isEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Back",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { userViewModel.updateSearchQuery(it) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            label = { Text("Search users...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (query.isEmpty()) "Suggested users" else "resultados para \"$query\"",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                ShimmerListPlaceholder()
            }

            searchResults.isNotEmpty() -> {
                // Show results normally...
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("search_details/${user.username}")
                                },
                            elevation = CardDefaults.cardElevation(6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val profilePainter = when {
                                    !user.profileImageUri.isNullOrBlank() -> {
                                        val imageUri = remember(user.profileImageUri) { Uri.parse(user.profileImageUri) }
                                        rememberAsyncImagePainter(imageUri)
                                    }
                                    user.profileImageResId != null -> {
                                        painterResource(id = user.profileImageResId)
                                    }
                                    else -> {
                                        painterResource(id = R.drawable.default_pfp)
                                    }
                                }

                                Image(
                                    painter = profilePainter,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "@${user.username}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            query.isNotEmpty() && !isLoading -> {
                // 👈 Now shows even if 2+ letters and no user match
                Text(
                    text = "User Not Found ❌",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

    }
}



@Composable
fun ShimmerListPlaceholder() {
    val shimmerBrush = rememberShimmerBrush()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.Gray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        )
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )
}
