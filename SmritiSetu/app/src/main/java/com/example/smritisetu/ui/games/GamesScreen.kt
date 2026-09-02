package com.example.smritisetu.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smritisetu.theme.GlassCard
import com.example.smritisetu.theme.getGlassGradientBrush

data class CleanGameItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val tag: String,
    val icon: ImageVector,
    val xpReward: Int
)

@Composable
fun GamesScreen(
    modifier: Modifier = Modifier
) {
    val darkTheme = isSystemInDarkTheme()
    val categories = listOf("All Activities", "Memory", "Attention", "Daily Living")
    var selectedCategory by remember { mutableStateOf("All Activities") }

    val games = remember {
        listOf(
            CleanGameItem(
                id = "mem_match",
                title = "Cultural Memory Match",
                category = "Memory",
                description = "Gently match familiar pairs of North Eastern cultural symbols and instruments.",
                tag = "Assam Heritage",
                icon = Icons.Default.Extension,
                xpReward = 50
            ),
            CleanGameItem(
                id = "seq_recall",
                title = "Tea Garden Sound Recall",
                category = "Attention",
                description = "Listen and follow calm audio and visual sequence patterns.",
                tag = "Audio-Visual Focus",
                icon = Icons.Default.Audiotrack,
                xpReward = 40
            ),
            CleanGameItem(
                id = "daily_routine",
                title = "Daily Routine Recall",
                category = "Daily Living",
                description = "Arrange familiar morning and evening daily activities in natural order.",
                tag = "Peace of Mind",
                icon = Icons.Default.WbSunny,
                xpReward = 35
            ),
            CleanGameItem(
                id = "art_recog",
                title = "Folk Crafts & Motifs",
                category = "Memory",
                description = "Recognize classic handloom weaving motifs and regional crafts.",
                tag = "Visual Recall",
                icon = Icons.Default.Palette,
                xpReward = 45
            )
        )
    }

    val filteredGames = remember(selectedCategory) {
        if (selectedCategory == "All Activities") games
        else games.filter { it.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getGlassGradientBrush(darkTheme))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 24.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Cognitive Games",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adaptive, non-stressful exercises for memory stimulation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Category Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Game Cards
            items(filteredGames, key = { it.id }) { game ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    darkTheme = darkTheme
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = game.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = game.tag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = game.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "+${game.xpReward} XP",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Button(
                                onClick = { /* Game trigger */ },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
