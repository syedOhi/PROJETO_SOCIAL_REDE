package com.example.social_rede_mobile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.social_rede_mobile.R

data class Story(val name: String, val imageResId: Int)

@Composable
fun StorySection() {
    val stories = listOf(
        Story("Your Story", R.drawable.stories1),
        Story("João", R.drawable.strories2),
        Story("Ana", R.drawable.story3),
        Story("Carlos", R.drawable.story4),
        Story("Beatriz", R.drawable.story5),
        Story("Beatriz", R.drawable.story6)

    )

    var selectedStory by remember { mutableStateOf<Story?>(null) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { story ->
            StoryItem(story = story) { selectedStory = it }
        }
    }


    // ✅ Dialog with only name and message, no story image
    selectedStory?.let { story ->
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
        ) {
            Dialog(onDismissRequest = { selectedStory = null }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${story.name}'s Story",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Conteúdo da STORY em breve...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { selectedStory = null }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StoryItem(story: Story, onClick: (Story) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clickable { onClick(story) }
        ) {
            // Outer gradient ring
            Canvas(modifier = Modifier.size(76.dp)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color.Magenta, Color.Yellow, Color.Red, Color.Magenta)
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )
            }

            // Inner blurred circle with image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .blur(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = story.imageResId),
                    contentDescription = "Story",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
