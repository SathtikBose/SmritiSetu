package com.example.smritisetu.data

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.max

enum class UserRole(val displayName: String) {
    PATIENT("Patient"),
    CAREGIVER("Caregiver")
}

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Glass"),
    DARK("Dark Glass"),
    HIGH_CONTRAST("High Contrast (Elder-friendly)")
}

data class UserProfile(
    val id: String = "user_001",
    val name: String = "Dr. Ananya Sharma",
    val email: String = "ananya.sharma@example.com",
    val phone: String = "+91 98765 43210",
    val gender: String = "Female",
    val age: Int = 68,
    val avatarUri: String? = null,
    val preferredLanguage: String = "English",
    val isGoogleLinked: Boolean = false,
    val totalXp: Int = 1450,
    val coins: Int = 1000,
    val streakDays: Int = 12,
    val leagueTier: String = "Silver Division"
)

data class CognitiveGameLog(
    val gameName: String,
    val level: Int,
    val tries: Int,
    val totalCards: Int,
    val timeElapsedMs: Long,
    val hintsUsed: Int,
    val difficulty: String = "NORMAL",
    val timestamp: Long = System.currentTimeMillis()
)

object AuthManager {
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(UserProfile())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Dynamic Font Scaling for Elder Accessibility (0.85f to 1.35f)
    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    // In-App Multilingual Selection - Default is English
    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    // Game Level Progression: Levels 1 to 5 are unlocked by default
    private val _highestUnlockedLevel = MutableStateFlow(5)
    val highestUnlockedLevel: StateFlow<Int> = _highestUnlockedLevel.asStateFlow()

    // Cognitive Telemetry Logs
    private val _telemetryLogs = MutableStateFlow<List<CognitiveGameLog>>(emptyList())
    val telemetryLogs: StateFlow<List<CognitiveGameLog>> = _telemetryLogs.asStateFlow()

    fun isLevelUnlocked(level: Int): Boolean {
        return level <= _highestUnlockedLevel.value
    }

    fun unlockNextLevel(completedLevel: Int) {
        if (completedLevel >= _highestUnlockedLevel.value) {
            _highestUnlockedLevel.value = completedLevel + 1
        }
    }

    fun login(email: String, pass: String): Result<UserProfile> {
        val user = UserProfile(
            name = if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User",
            email = email,
            preferredLanguage = _selectedLanguage.value.displayName
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        return Result.success(user)
    }

    fun signup(name: String, email: String, pass: String, role: UserRole = UserRole.CAREGIVER): Result<UserProfile> {
        val user = UserProfile(
            name = name.ifBlank { "User" },
            email = email,
            preferredLanguage = _selectedLanguage.value.displayName
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
            preferredLanguage = _selectedLanguage.value.displayName
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

    fun changePassword(currentPass: String, newPass: String): Result<Boolean> {
        if (currentPass.isBlank() || newPass.isBlank()) {
            return Result.failure(IllegalArgumentException("Please fill in both password fields"))
        }
        if (newPass.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters"))
        }
        return Result.success(true)
    }

    fun updateProfile(name: String, phone: String, gender: String, age: Int, avatarUri: String?) {
        _currentUser.update { current ->
            current?.copy(
                name = name,
                phone = phone,
                gender = gender,
                age = age,
                avatarUri = avatarUri
            )
        }
    }

    fun addRewards(xp: Int, coins: Int) {
        _currentUser.update { current ->
            current?.copy(
                totalXp = (current.totalXp + xp),
                coins = (current.coins + coins)
            )
        }
    }

    fun recordGameTelemetry(log: CognitiveGameLog) {
        _telemetryLogs.update { it + log }
        try {
            Log.i(
                "SmritiSetuAnalytics",
                "CognitiveGameLog: game=${log.gameName}, level=${log.level}, tries=${log.tries}, totalCards=${log.totalCards}, timeMs=${log.timeElapsedMs}, hints=${log.hintsUsed}, diff=${log.difficulty}"
            )
        } catch (_: Exception) {
            // Unit test environment fallback
            println("[SmritiSetuAnalytics] CognitiveGameLog: $log")
        }
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        _currentUser.update { it?.copy(preferredLanguage = language.displayName) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(0.85f, 1.35f)
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
        _highestUnlockedLevel.value = 5
    }
}
