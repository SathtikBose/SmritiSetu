package com.example.smritisetu

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.smritisetu.ui.auth.ForgotPasswordScreen
import com.example.smritisetu.ui.auth.LoginScreen
import com.example.smritisetu.ui.auth.SignupScreen
import com.example.smritisetu.ui.games.MatchCardGameScreen
import com.example.smritisetu.ui.games.MatchCardLevelSelectScreen
import com.example.smritisetu.ui.main.MainContainerScreen
import com.example.smritisetu.ui.settings.AppAppearanceScreen
import com.example.smritisetu.ui.settings.EditProfileScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(MainRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<LoginRoute> {
                    LoginScreen(
                        onNavigateToSignup = { backStack.add(SignupRoute) },
                        onNavigateToForgotPassword = { backStack.add(ForgotPasswordRoute) },
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(MainRoute)
                        }
                    )
                }
                entry<SignupRoute> {
                    SignupScreen(
                        onNavigateToLogin = { backStack.removeLastOrNull() },
                        onSignupSuccess = {
                            backStack.clear()
                            backStack.add(MainRoute)
                        }
                    )
                }
                entry<ForgotPasswordRoute> {
                    ForgotPasswordScreen(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onPasswordResetSuccess = {
                            backStack.clear()
                            backStack.add(LoginRoute)
                        }
                    )
                }
                entry<MainRoute> {
                    MainContainerScreen(
                        onNavigateToEditProfile = { backStack.add(EditProfileRoute) },
                        onNavigateToAppearance = { backStack.add(AppAppearanceRoute) },
                        onNavigateToLevelSelect = { backStack.add(MatchCardLevelSelectRoute) },
                        onLogout = {
                            backStack.clear()
                            backStack.add(LoginRoute)
                        }
                    )
                }
                entry<EditProfileRoute> {
                    EditProfileScreen(
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<AppAppearanceRoute> {
                    AppAppearanceScreen(
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<MatchCardLevelSelectRoute> {
                    MatchCardLevelSelectScreen(
                        onSelectLevel = { level ->
                            backStack.add(MatchCardGameRoute(initialLevel = level))
                        },
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<MatchCardGameRoute> { route ->
                    MatchCardGameScreen(
                        initialLevel = route.initialLevel,
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
            },
        modifier = Modifier.fillMaxSize()
    )
}
