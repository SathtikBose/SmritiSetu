package com.example.smritisetu.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

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

enum class PerkType(val costCoins: Int, val displayName: String) {
    HINT(1000, "Extra Hint"),
    SKIP_LEVEL(2000, "Skip Level")
}

data class CaregiverReminder(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "Medicine", // Medicine, Hydration, Activity, Meal
    val time: String = "08:00 AM",
    val message: String = "Morning memory medication",
    val isActive: Boolean = true
)

data class UserProfile(
    val id: String = "user_001",
    val name: String = "Dr. Ananya Sharma",
    val email: String = "ananya.sharma@example.com",
    val phone: String = "+91 98765 43210",
    val gender: String = "Female",
    val age: Int = 68,
    val avatarUri: String? = null,
    val role: UserRole = UserRole.PATIENT,
    val patientLinkCode: String = "SM-8492", // 6-digit unique linking code for patients
    val linkedPatientCode: String? = null, // Linked patient code for caregivers
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
    val hintsUsed: Int, // Inactivity idle hints
    val perkHintsUsed: Int = 0, // Manual perk hints
    val difficulty: String = "NORMAL",
    val timestamp: Long = System.currentTimeMillis()
)

object AuthManager {
    private var sharedPreferences: SharedPreferences? = null

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(UserProfile())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Active View Mode: allows instant switching between Patient Mode and Caregiver Dashboard for testing
    private val _activeRoleView = MutableStateFlow(UserRole.PATIENT)
    val activeRoleView: StateFlow<UserRole> = _activeRoleView.asStateFlow()

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

    // In-Game Perks Inventory (Default: 0)
    private val _hintsCount = MutableStateFlow(0)
    val hintsCount: StateFlow<Int> = _hintsCount.asStateFlow()

    private val _skipLevelCount = MutableStateFlow(0)
    val skipLevelCount: StateFlow<Int> = _skipLevelCount.asStateFlow()

    // Caregiver Daily Reminders
    private val _reminders = MutableStateFlow<List<CaregiverReminder>>(
        listOf(
            CaregiverReminder(id = "rem_1", type = "Medicine", time = "08:00 AM", message = "Morning Memory Medication", isActive = true),
            CaregiverReminder(id = "rem_2", type = "Hydration", time = "02:00 PM", message = "Drink fresh water / Lemon water", isActive = true),
            CaregiverReminder(id = "rem_3", type = "Activity", time = "05:30 PM", message = "Gentle evening stroll in garden", isActive = false)
        )
    )
    val reminders: StateFlow<List<CaregiverReminder>> = _reminders.asStateFlow()

    // Cognitive Telemetry Logs (preloaded with sample historical session logs for rich dashboard display)
    private val _telemetryLogs = MutableStateFlow<List<CognitiveGameLog>>(
        listOf(
            CognitiveGameLog(gameName = "MatchTheCard", level = 1, tries = 3, totalCards = 4, timeElapsedMs = 28000L, hintsUsed = 0, perkHintsUsed = 0, difficulty = "EASY"),
            CognitiveGameLog(gameName = "MatchTheCard", level = 2, tries = 4, totalCards = 6, timeElapsedMs = 34000L, hintsUsed = 0, perkHintsUsed = 0, difficulty = "EASY"),
            CognitiveGameLog(gameName = "MatchTheCard", level = 3, tries = 5, totalCards = 8, timeElapsedMs = 42000L, hintsUsed = 1, perkHintsUsed = 0, difficulty = "EASY"),
            CognitiveGameLog(gameName = "MatchTheCard", level = 4, tries = 6, totalCards = 10, timeElapsedMs = 49000L, hintsUsed = 1, perkHintsUsed = 0, difficulty = "EASY"),
            CognitiveGameLog(gameName = "MatchTheCard", level = 5, tries = 7, totalCards = 12, timeElapsedMs = 58000L, hintsUsed = 2, perkHintsUsed = 1, difficulty = "EASY")
        )
    )
    val telemetryLogs: StateFlow<List<CognitiveGameLog>> = _telemetryLogs.asStateFlow()

    fun initStorage(context: Context) {
        try {
            sharedPreferences = context.getSharedPreferences("smritisetu_prefs", Context.MODE_PRIVATE)
            loadFromStorage()
        } catch (e: Exception) {
            Log.e("AuthManager", "Failed to init SharedPreferences", e)
        }
    }

    private fun loadFromStorage() {
        val prefs = sharedPreferences ?: return
        val savedCoins = prefs.getInt("user_coins", 1000)
        val savedXp = prefs.getInt("user_xp", 1450)
        val savedHighestLevel = prefs.getInt("highest_unlocked_level", 5)
        val savedHints = prefs.getInt("hints_count", 0)
        val savedSkips = prefs.getInt("skips_count", 0)
        val savedRoleName = prefs.getString("user_role", UserRole.PATIENT.name) ?: UserRole.PATIENT.name
        val savedLinkCode = prefs.getString("patient_link_code", "SM-8492") ?: "SM-8492"
        val savedLinkedCode = prefs.getString("linked_patient_code", null)
        val savedLangName = prefs.getString("selected_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name

        _highestUnlockedLevel.value = savedHighestLevel.coerceAtLeast(5)
        _hintsCount.value = savedHints
        _skipLevelCount.value = savedSkips
        _selectedLanguage.value = AppLanguage.entries.find { it.name == savedLangName } ?: AppLanguage.ENGLISH

        val userRole = UserRole.entries.find { it.name == savedRoleName } ?: UserRole.PATIENT
        _activeRoleView.value = userRole

        _currentUser.update { current ->
            current?.copy(
                coins = savedCoins,
                totalXp = savedXp,
                role = userRole,
                patientLinkCode = savedLinkCode,
                linkedPatientCode = savedLinkedCode
            )
        }
    }

    private fun persistToStorage() {
        val prefs = sharedPreferences ?: return
        prefs.edit().apply {
            putInt("user_coins", _currentUser.value?.coins ?: 1000)
            putInt("user_xp", _currentUser.value?.totalXp ?: 1450)
            putInt("highest_unlocked_level", _highestUnlockedLevel.value)
            putInt("hints_count", _hintsCount.value)
            putInt("skips_count", _skipLevelCount.value)
            putString("user_role", _currentUser.value?.role?.name ?: UserRole.PATIENT.name)
            putString("patient_link_code", _currentUser.value?.patientLinkCode ?: "SM-8492")
            putString("linked_patient_code", _currentUser.value?.linkedPatientCode)
            putString("selected_language", _selectedLanguage.value.name)
            apply()
        }
    }

    fun isLevelUnlocked(level: Int): Boolean {
        return level <= _highestUnlockedLevel.value
    }

    fun unlockNextLevel(completedLevel: Int) {
        if (completedLevel >= _highestUnlockedLevel.value) {
            _highestUnlockedLevel.value = completedLevel + 1
            persistToStorage()
        }
    }

    fun toggleActiveRoleView() {
        val newRole = if (_activeRoleView.value == UserRole.PATIENT) UserRole.CAREGIVER else UserRole.PATIENT
        _activeRoleView.value = newRole
    }

    fun setActiveRoleView(role: UserRole) {
        _activeRoleView.value = role
    }

    fun linkPatientByCode(linkCode: String): Result<Boolean> {
        val cleaned = linkCode.trim().uppercase()
        if (cleaned.isBlank() || cleaned.length < 4) {
            return Result.failure(IllegalArgumentException("Please enter a valid Patient Linking Code (e.g. SM-8492)"))
        }

        _currentUser.update { current ->
            current?.copy(linkedPatientCode = cleaned)
        }
        persistToStorage()
        return Result.success(true)
    }

    fun addReminder(type: String, time: String, message: String) {
        val newReminder = CaregiverReminder(
            type = type,
            time = time,
            message = message,
            isActive = true
        )
        _reminders.update { it + newReminder }
    }

    fun toggleReminder(id: String) {
        _reminders.update { list ->
            list.map { if (it.id == id) it.copy(isActive = !it.isActive) else it }
        }
    }

    fun deleteReminder(id: String) {
        _reminders.update { list ->
            list.filter { it.id != id }
        }
    }

    fun buyPerk(perkType: PerkType): Result<Boolean> {
        val currentCoins = _currentUser.value?.coins ?: 0
        if (currentCoins < perkType.costCoins) {
            return Result.failure(IllegalStateException("Not enough coins! Earn more by completing levels."))
        }

        // Deduct coins
        _currentUser.update { it?.copy(coins = currentCoins - perkType.costCoins) }

        // Increment perk count
        when (perkType) {
            PerkType.HINT -> _hintsCount.update { it + 1 }
            PerkType.SKIP_LEVEL -> _skipLevelCount.update { it + 1 }
        }

        persistToStorage()
        return Result.success(true)
    }

    fun useHint(): Boolean {
        if (_hintsCount.value > 0) {
            _hintsCount.update { it - 1 }
            persistToStorage()
            return true
        }
        return false
    }

    fun useSkipLevel(): Boolean {
        if (_skipLevelCount.value > 0) {
            _skipLevelCount.update { it - 1 }
            persistToStorage()
            return true
        }
        return false
    }

    fun addRewards(xp: Int, coins: Int) {
        _currentUser.update { current ->
            current?.copy(
                totalXp = (current.totalXp + xp),
                coins = (current.coins + coins)
            )
        }
        persistToStorage()
    }

    fun login(email: String, pass: String): Result<UserProfile> {
        val user = UserProfile(
            name = if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User",
            email = email,
            role = UserRole.PATIENT,
            preferredLanguage = _selectedLanguage.value.displayName
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _activeRoleView.value = UserRole.PATIENT
        persistToStorage()
        return Result.success(user)
    }

    fun signup(
        name: String,
        email: String,
        pass: String,
        role: UserRole = UserRole.PATIENT,
        patientCodeToLink: String? = null
    ): Result<UserProfile> {
        val generatedPatientCode = "SM-" + (1000..9999).random()
        val user = UserProfile(
            name = name.ifBlank { if (role == UserRole.CAREGIVER) "Caregiver" else "Patient" },
            email = email,
            role = role,
            patientLinkCode = generatedPatientCode,
            linkedPatientCode = if (role == UserRole.CAREGIVER) patientCodeToLink?.trim()?.uppercase() ?: "SM-8492" else null,
            preferredLanguage = _selectedLanguage.value.displayName
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _activeRoleView.value = role
        persistToStorage()
        return Result.success(user)
    }

    fun loginWithGoogle(): Result<UserProfile> {
        val user = UserProfile(
            name = "Google User",
            email = "user.google@gmail.com",
            isGoogleLinked = true,
            role = UserRole.PATIENT,
            preferredLanguage = _selectedLanguage.value.displayName
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _activeRoleView.value = UserRole.PATIENT
        persistToStorage()
        return Result.success(user)
    }

    fun linkGoogleAccount(): Boolean {
        _currentUser.update { it?.copy(isGoogleLinked = true) }
        persistToStorage()
        return true
    }

    fun unlinkGoogleAccount(): Boolean {
        _currentUser.update { it?.copy(isGoogleLinked = false) }
        persistToStorage()
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
        persistToStorage()
    }

    fun recordGameTelemetry(log: CognitiveGameLog) {
        _telemetryLogs.update { it + log }
        try {
            Log.i(
                "SmritiSetuAnalytics",
                "CognitiveGameLog: game=${log.gameName}, level=${log.level}, tries=${log.tries}, totalCards=${log.totalCards}, timeMs=${log.timeElapsedMs}, idleHints=${log.hintsUsed}, perkHints=${log.perkHintsUsed}, diff=${log.difficulty}"
            )
        } catch (_: Exception) {
            println("[SmritiSetuAnalytics] CognitiveGameLog: $log")
        }
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        _currentUser.update { it?.copy(preferredLanguage = language.displayName) }
        persistToStorage()
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
        _hintsCount.value = 0
        _skipLevelCount.value = 0
        _activeRoleView.value = UserRole.PATIENT
        persistToStorage()
    }
}
