package com.example.smritisetu.ui.games

import androidx.compose.foundation.background
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

data class GameItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val regionalTheme: String,
    val difficulty: String,
    val icon: ImageVector,
    val xpReward: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    modifier: Modifier = Modifier
) {
    val categories = listOf("All Games", "Memory", "Attention", "Routine Recall")
    var selectedCategory by remember { mutableStateOf("All Games") }

    val games = remember {
        listOf(
            GameItem(
                id = "mem_match",
                title = "NER Cultural Memory Match",
                category = "Memory",
                description = "Pair culturally familiar North East symbols like Bihu Dhol, Rhinoceros, and Japi hats.",
                regionalTheme = "Assam & NER Heritage",
                difficulty = "Adaptive",
                icon = Icons.Default.Extension,
                xpReward = 50
            ),
            GameItem(
                id = "seq_recall",
                title = "Tea Garden Sequence Recall",
                category = "Attention",
                description = "Follow and repeat sound and visual patterns across tea estate markers.",
                regionalTheme = "Audio-Visual Focus",
                difficulty = "Level 2",
                icon = Icons.Default.Audiotrack,
                xpReward = 40
            ),
            GameItem(
                id = "daily_routine",
                title = "Daily Routine Reasoning",
                category = "Routine Recall",
                description = "Organize daily morning, meal, prayer, and rest activities in logical order.",
                regionalTheme = "Everyday Living",
                difficulty = "Level 1",
                icon = Icons.Default.WbSunny,
                xpReward = 35
            ),
            GameItem(
                id = "obj_recog",
                title = "Festivals & Crafts Recognition",
                category = "Memory",
                description = "Identify handloom motifs, musical instruments, and traditional attire.",
                regionalTheme = "NER Folk Crafts",
                difficulty = "Adaptive",
                icon = Icons.Default.Palette,
                xpReward = 45
            )
        )
    }

    val filteredGames = remember(selectedCategory) {
        if (selectedCategory == "All Games") games
        else games.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cognitive Games",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Game Cards
            items(filteredGames, key = { it.id }) { game ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
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
                                    text = game.regionalTheme,
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

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(game.difficulty) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                                Text(
                                    text = "+${game.xpReward} XP",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Button(
                                onClick = { /* Game launcher */ },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play")
                            }
                        }
                    }
                }
            }
        }
    }
}
