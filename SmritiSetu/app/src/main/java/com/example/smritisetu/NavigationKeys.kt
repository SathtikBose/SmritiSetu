package com.example.smritisetu

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute : NavKey

@Serializable
data object SignupRoute : NavKey

@Serializable
data object ForgotPasswordRoute : NavKey

@Serializable
data object MainRoute : NavKey

@Serializable
data object EditProfileRoute : NavKey

@Serializable
data object AppAppearanceRoute : NavKey
