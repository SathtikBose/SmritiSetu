package com.example.smritisetu.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
    }
}
