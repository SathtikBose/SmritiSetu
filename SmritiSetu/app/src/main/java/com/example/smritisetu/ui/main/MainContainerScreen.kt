package com.example.smritisetu.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.theme.*
import com.example.smritisetu.ui.games.GamesScreen
import com.example.smritisetu.ui.home.HomeScreen
import com.example.smritisetu.ui.league.LeagueScreen
import com.example.smritisetu.ui.settings.SettingsScreen

enum class BottomNavTab(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(Icons.Filled.Home, Icons.Outlined.Home),
    GAMES(Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    LEAGUE(Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    SETTINGS(Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainContainerScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLevelSelect: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.HOME) }
    val themeMode by AuthManager.themeMode.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current
    val gradientBrush = getGlassGradientBrush(darkTheme)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        // Active Screen content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            when (selectedTab) {
                BottomNavTab.HOME -> HomeScreen()
                BottomNavTab.GAMES -> GamesScreen(
                    onPlayMatchCardGame = onNavigateToLevelSelect
                )
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
            val navBgColor = if (darkTheme) Color(0xEB162421) else Color(0xEBFFFFFF)
            val navBorderColor = if (darkTheme) Color(0x33FFFFFF) else Color(0x66FFFFFF)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black.copy(alpha = if (darkTheme) 0.35f else 0.08f),
                        spotColor = Color.Black.copy(alpha = if (darkTheme) 0.55f else 0.15f)
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
                        val label = when (tab) {
                            BottomNavTab.HOME -> strings.navHome
                            BottomNavTab.GAMES -> strings.navGames
                            BottomNavTab.LEAGUE -> strings.navLeague
                            BottomNavTab.SETTINGS -> strings.navSettings
                        }

                        val activeContainerColor = if (darkTheme) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
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
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
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
