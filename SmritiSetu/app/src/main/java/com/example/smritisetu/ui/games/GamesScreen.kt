package com.example.smritisetu.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.theme.GlassCard
import com.example.smritisetu.theme.getGlassGradientBrush
import com.example.smritisetu.theme.isAppInDarkTheme

data class FiveGameItem(
    val title: String,
    val description: String,
    val tag: String,
    val icon: ImageVector,
    val xpReward: Int
)

@Composable
fun GamesScreen(
    modifier: Modifier = Modifier
) {
    val themeMode by AuthManager.themeMode.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current

    val games = remember(strings) {
        listOf(
            FiveGameItem(
                title = strings.game1Title,
                description = strings.game1Desc,
                tag = strings.game1Tag,
                icon = Icons.Default.Extension,
                xpReward = 50
            ),
            FiveGameItem(
                title = strings.game2Title,
                description = strings.game2Desc,
                tag = strings.game2Tag,
                icon = Icons.Default.Audiotrack,
                xpReward = 40
            ),
            FiveGameItem(
                title = strings.game3Title,
                description = strings.game3Desc,
                tag = strings.game3Tag,
                icon = Icons.Default.WbSunny,
                xpReward = 35
            ),
            FiveGameItem(
                title = strings.game4Title,
                description = strings.game4Desc,
                tag = strings.game4Tag,
                icon = Icons.Default.Palette,
                xpReward = 45
            ),
            FiveGameItem(
                title = strings.game5Title,
                description = strings.game5Desc,
                tag = strings.game5Tag,
                icon = Icons.Default.Spa,
                xpReward = 30
            )
        )
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
                        text = strings.gamesTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.gamesSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5 Game Cards
            items(games) { game ->
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
                                onClick = { /* Game launcher */ },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.play, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
