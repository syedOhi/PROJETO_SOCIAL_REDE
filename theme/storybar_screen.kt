package com.example.social_rede_mobile.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.social_rede_mobile.StoryUser

@Composable
fun StoryBar(storyUsers: List<StoryUser>, navController: NavController) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(storyUsers) { user ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate("story/${user.username}")
                }
            ) {
                // Circle with colorful ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(68.dp)
                ) {
                    // Outer ring
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Brush.linearGradient( // colorful ring
                            colors = listOf(
                                Color(0xFFE91E63), // pink
                                Color(0xFFFFC107), // amber
                                Color(0xFF2196F3)  // blue
                            )
                        )),
                        modifier = Modifier.size(60.dp),
                        color = Color.Transparent
                    ) {}

                    // Inner profile circle
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp),
                        color = Color.Gray // replace with profile picture or color
                    ) {
                        // Placeholder, can be replaced with an image
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}
