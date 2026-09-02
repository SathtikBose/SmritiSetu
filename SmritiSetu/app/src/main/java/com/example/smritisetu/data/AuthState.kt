package com.example.smritisetu.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class UserRole(val displayName: String) {
    PATIENT("Patient"),
    CAREGIVER("Caregiver")
}

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    HIGH_CONTRAST("High Contrast (Elder-friendly)")
}

data class UserProfile(
    val id: String = "user_001",
    val name: String = "Dr. Ananya Sharma",
    val email: String = "ananya.sharma@example.com",
    val phone: String = "+91 98765 43210",
    val role: UserRole = UserRole.CAREGIVER,
    val preferredLanguage: String = "Assamese",
    val isGoogleLinked: Boolean = false,
    val totalXp: Int = 1450,
    val streakDays: Int = 12,
    val leagueTier: String = "Silver Division"
)

object AuthManager {
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(UserProfile())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun login(email: String, pass: String): Result<UserProfile> {
        val user = UserProfile(
            name = if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User",
            email = email,
            role = UserRole.CAREGIVER
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        return Result.success(user)
    }

    fun signup(name: String, email: String, pass: String, role: UserRole): Result<UserProfile> {
        val user = UserProfile(
            name = name.ifBlank { "User" },
            email = email,
            role = role
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        return Result.success(user)
    }

    fun loginWithGoogle(): Result<UserProfile> {
        val user = UserProfile(
            name = "Google User",
            email = "user.google@gmail.com",
            isGoogleLinked = true,
            role = UserRole.PATIENT
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        return Result.success(user)
    }

    fun linkGoogleAccount(): Boolean {
        _currentUser.update { it?.copy(isGoogleLinked = true) }
        return true
    }

    fun unlinkGoogleAccount(): Boolean {
        _currentUser.update { it?.copy(isGoogleLinked = false) }
        return true
    }

    fun sendOtp(email: String): Result<String> {
        return Result.success("123456")
    }

    fun verifyOtpAndResetPassword(email: String, otp: String, newPass: String): Result<Boolean> {
        if (otp == "123456" || otp.length == 6) {
            return Result.success(true)
        }
        return Result.failure(IllegalArgumentException("Invalid OTP code"))
    }

    fun updateProfile(name: String, phone: String, language: String, role: UserRole) {
        _currentUser.update { current ->
            current?.copy(
                name = name,
                phone = phone,
                preferredLanguage = language,
                role = role
            )
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
    }
}
