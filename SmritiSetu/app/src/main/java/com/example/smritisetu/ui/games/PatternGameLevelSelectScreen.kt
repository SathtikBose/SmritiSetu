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

data class PatternLevelInfo(
    val levelNumber: Int,
    val patternLength: Int,
    val timeLimitSeconds: Int,
    val difficulty: String,
    val isUnlocked: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternGameLevelSelectScreen(
    onSelectLevel: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by AuthManager.themeMode.collectAsState()
    val currentUser by AuthManager.currentUser.collectAsState()
    val highestUnlockedPatternLevel by AuthManager.highestUnlockedPatternLevel.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current

    // Dynamically generated endless procedural levels
    var displayedMaxLevel by remember(highestUnlockedPatternLevel) {
        mutableIntStateOf((highestUnlockedPatternLevel + 5).coerceAtLeast(10))
    }

    val levelsList = remember(highestUnlockedPatternLevel, displayedMaxLevel) {
        (1..displayedMaxLevel).map { level ->
            val patternLength = when {
                level <= 3 -> 4
                level <= 6 -> 5
                level <= 10 -> 6
                level <= 15 -> 7
                else -> 8
            }
            val timeLimit = when {
                level <= 5 -> 60 // Easy 60s
                level <= 10 -> 45 // Normal 45s
                else -> 30 // Hard 30s
            }
            val difficulty = when {
                level <= 5 -> strings.difficultyEasy
                level <= 10 -> strings.difficultyNormal
                else -> strings.difficultyHard
            }
            PatternLevelInfo(
                levelNumber = level,
                patternLength = patternLength,
                timeLimitSeconds = timeLimit,
                difficulty = difficulty,
                isUnlocked = level <= highestUnlockedPatternLevel
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Pattern Match • Levels",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        // Top-Right Coins Balance Pill
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
                // Level Tiles
                items(levelsList, key = { it.levelNumber }) { item ->
                    val isUnlocked = item.isUnlocked
                    val isCurrent = item.levelNumber == highestUnlockedPatternLevel

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(enabled = isUnlocked) {
                                onSelectLevel(item.levelNumber)
                            },
                        shape = RoundedCornerShape(22.dp),
                        darkTheme = darkTheme
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Level Badge / Lock Icon
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isUnlocked -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    Text(
                                        text = "${item.levelNumber}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Level Title
                            Text(
                                text = "${strings.level} ${item.levelNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Difficulty & Pattern length tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (item.difficulty) {
                                    strings.difficultyEasy -> Color(0xFF4CAF50).copy(alpha = if (isUnlocked) 0.2f else 0.08f)
                                    strings.difficultyNormal -> Color(0xFFFF9800).copy(alpha = if (isUnlocked) 0.2f else 0.08f)
                                    else -> Color(0xFFF44336).copy(alpha = if (isUnlocked) 0.2f else 0.08f)
                                }
                            ) {
                                Text(
                                    text = "${item.difficulty} • ${item.patternLength} steps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (item.difficulty) {
                                        strings.difficultyEasy -> if (isUnlocked) Color(0xFF2E7D32) else Color.Gray
                                        strings.difficultyNormal -> if (isUnlocked) Color(0xFFE65100) else Color.Gray
                                        else -> if (isUnlocked) Color(0xFFC62828) else Color.Gray
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Timer pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${item.timeLimitSeconds}s",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            }

                            // Star rating for cleared levels
                            if (item.levelNumber < highestUnlockedPatternLevel) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Cleared Star",
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Explore Next 5 Levels Expander Button
                item(span = { GridItemSpan(2) }) {
                    Button(
                        onClick = {
                            displayedMaxLevel += 5
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explore Next 5 Levels (${displayedMaxLevel + 1}–${displayedMaxLevel + 5})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
