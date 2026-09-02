package com.example.smritisetu.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smritisetu.theme.*
import com.example.smritisetu.ui.games.GamesScreen
import com.example.smritisetu.ui.home.HomeScreen
import com.example.smritisetu.ui.league.LeagueScreen
import com.example.smritisetu.ui.settings.SettingsScreen

enum class BottomNavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    GAMES("Games", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    LEAGUE("League", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainContainerScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.HOME) }
    val darkTheme = isSystemInDarkTheme()
    val gradientBrush = getGlassGradientBrush(darkTheme)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        // Active Screen content with padding at bottom for floating nav
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 84.dp)
        ) {
            when (selectedTab) {
                BottomNavTab.HOME -> HomeScreen(onNavigateToGames = { selectedTab = BottomNavTab.GAMES })
                BottomNavTab.GAMES -> GamesScreen()
                BottomNavTab.LEAGUE -> LeagueScreen()
                BottomNavTab.SETTINGS -> SettingsScreen(
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToAppearance = onNavigateToAppearance,
                    onLogout = onLogout
                )
            }
        }

        // Floating Glassmorphic Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            val navBgColor = if (darkTheme) GlassBottomNavDark else GlassBottomNavLight
            val navBorderColor = if (darkTheme) GlassBorderDark else GlassBorderLight

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.18f)
                    ),
                shape = RoundedCornerShape(32.dp),
                color = navBgColor,
                border = BorderStroke(1.2.dp, navBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val activeContainerColor = if (darkTheme) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        }

                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) activeContainerColor else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
