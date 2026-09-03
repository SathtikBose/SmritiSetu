package com.example.smritisetu.ui.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.theme.GlassCard
import com.example.smritisetu.theme.getGlassGradientBrush
import com.example.smritisetu.theme.isAppInDarkTheme

data class LevelInfo(
    val levelNumber: Int,
    val pairsCount: Int,
    val timeLimitSeconds: Int,
    val difficulty: String,
    val isUnlocked: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCardLevelSelectScreen(
    onSelectLevel: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by AuthManager.themeMode.collectAsState()
    val currentUser by AuthManager.currentUser.collectAsState()
    val highestUnlockedLevel by AuthManager.highestUnlockedLevel.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current

    // Dynamically generated endless procedural levels (Candy Crush style)
    var displayedMaxLevel by remember(highestUnlockedLevel) {
        mutableIntStateOf((highestUnlockedLevel + 5).coerceAtLeast(10))
    }

    val levelsList = remember(highestUnlockedLevel, displayedMaxLevel) {
        (1..displayedMaxLevel).map { level ->
            val pairs = when (level) {
                1 -> 2
                2 -> 3
                3 -> 4
                4 -> 5
                5 -> 6
                6 -> 7
                else -> 8 // Capped at 8 pairs (16 cards) for elder-friendly touch targets
            }
            val timeLimit = when {
                level <= 5 -> 150 // Easy 150s
                level <= 10 -> 100 // Normal 100s
                else -> 50 // Hard 50s
            }
            val difficulty = when {
                level <= 5 -> strings.difficultyEasy
                level <= 10 -> strings.difficultyNormal
                else -> strings.difficultyHard
            }
            LevelInfo(
                levelNumber = level,
                pairsCount = pairs,
                timeLimitSeconds = timeLimit,
                difficulty = difficulty,
                isUnlocked = level <= highestUnlockedLevel
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        Text(
                            text = strings.selectLevel,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        // Top-Right Coins Balance Counter
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${currentUser?.coins ?: 1000}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(getGlassGradientBrush(darkTheme))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // Header Banner
                item(span = { GridItemSpan(2) }) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        darkTheme = darkTheme
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AllInclusive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Endless Cognitive Levels",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.chooseLevelSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Level Tiles
                items(levelsList, key = { it.levelNumber }) { item ->
                    val isUnlocked = item.isUnlocked
                    val isCurrentTarget = item.levelNumber == highestUnlockedLevel
                    val totalCards = item.pairsCount * 2

                    val borderColor = when {
                        isCurrentTarget -> MaterialTheme.colorScheme.primary
                        isUnlocked -> if (darkTheme) Color(0x44FFFFFF) else Color(0x66FFFFFF)
                        else -> if (darkTheme) Color(0x1AFFFFFF) else Color(0x33000000)
                    }

                    val cardBgColor = when {
                        isCurrentTarget -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        isUnlocked -> if (darkTheme) Color(0xCC1A2B27) else Color(0xEBFFFFFF)
                        else -> if (darkTheme) Color(0x66121C1A) else Color(0x99F1F5F9)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.95f)
                            .clickable(enabled = isUnlocked) {
                                onSelectLevel(item.levelNumber)
                            },
                        shape = RoundedCornerShape(22.dp),
                        color = cardBgColor,
                        border = BorderStroke(if (isCurrentTarget) 2.dp else 1.dp, borderColor),
                        shadowElevation = if (isCurrentTarget) 6.dp else if (isUnlocked) 3.dp else 0.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                            // Top Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isUnlocked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = item.difficulty,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp
                                    )
                                }

                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (item.levelNumber < highestUnlockedLevel) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Current Target",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Center Content
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrentTarget) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.levelNumber}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${strings.level} ${item.levelNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                if (isUnlocked) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$totalCards Cards",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${item.timeLimitSeconds}s",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Finish L${item.levelNumber - 1} to unlock",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Load More Endless Levels Button
                item(span = { GridItemSpan(2) }) {
                    OutlinedButton(
                        onClick = { displayedMaxLevel += 5 },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explore Next 5 Levels (Procedural Endless)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
