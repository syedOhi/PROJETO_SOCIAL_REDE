package com.example.social_rede_mobile

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(userViewModel: UserViewModel, navController: NavController) {

    val query by userViewModel.searchQuery.collectAsState()
    val searchResults by userViewModel.searchResults.collectAsState()

    val isLoading = remember(query, searchResults) { query.isNotEmpty() && searchResults.isEmpty() }

    BuzzBackground {

        BuzzCard {

            // Header row (Back)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search input (dark + readable)
            OutlinedTextField(
                value = query,
                onValueChange = { userViewModel.updateSearchQuery(it) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color(0xFFC7D2FE)) },
                label = { Text("Search users...", color = Color(0xFF9CA3AF)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x0FFFFFFF),
                    focusedContainerColor = Color(0x12FFFFFF),
                    unfocusedBorderColor = Color(0x22FFFFFF),
                    focusedBorderColor = Color(0x806366F1),
                    focusedLabelColor = Color(0xFFC7D2FE),
                    cursorColor = Color(0xFF6366F1),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Section title
            Text(
                text = if (query.isEmpty()) "Suggested users" else "Resultados para \"$query\"",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Content area
            when {
                isLoading -> {
                    ShimmerListPlaceholderDark()
                }

                searchResults.isNotEmpty() -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(searchResults) { user ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("search_details/${user.username}") },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF)),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0x1FFFFFFF)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    val profilePainter = when {
                                        !user.profileImageUri.isNullOrBlank() -> {
                                            val imageUri = remember(user.profileImageUri) { Uri.parse(user.profileImageUri) }
                                            rememberAsyncImagePainter(imageUri)
                                        }
                                        user.profileImageResId != null -> painterResource(id = user.profileImageResId)
                                        else -> painterResource(id = R.drawable.default_pfp)
                                    }

                                    Image(
                                        painter = profilePainter,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0F172A))
                                            .border(2.dp, Color(0x336366F1), CircleShape),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "@${user.username}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = user.fullName ?: "BuzzConnect user",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }

                                    // small arrow feel
                                    Text("›", color = Color(0xFFC7D2FE), fontSize = 22.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                query.isNotEmpty() && !isLoading -> {
                    Text(
                        text = "User Not Found ❌",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* ---------- Dark shimmer that matches your theme ---------- */

@Composable
fun ShimmerListPlaceholderDark() {
    val shimmerBrush = rememberShimmerBrushDark()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

@Composable
fun rememberShimmerBrushDark(): Brush {
    val shimmerColors = listOf(
        Color(0x1FFFFFFF),
        Color(0x0FFFFFFF),
        Color(0x1FFFFFFF)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 240f, translateAnim + 240f)
    )
}
@Composable
fun BuzzBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF0B1220))
                )
            )
            .padding(18.dp)
    ) {
        content()
    }
}

@Composable
fun BuzzCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}