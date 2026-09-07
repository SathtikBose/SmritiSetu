package com.example.smritisetu.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.smritisetu.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    SHOW_AGAIN(800, "Peek"),
    SKIP_LEVEL(2000, "Skip Level");

    companion object {
        val PEEK = SHOW_AGAIN
    }
}

enum class LeagueTier(
    val tierName: String,
    val tierNameShort: String,
    val minXp: Int,
    val maxXp: Int,
    val minLevels: Int,
    val colorHex: Long,
    val iconEmoji: String,
    val description: String,
    val imageResId: Int = com.example.smritisetu.R.drawable.bronze_league
) {
    BRONZE(
        tierName = "Bronze Division",
        tierNameShort = "Bronze",
        minXp = 0,
        maxXp = 224,
        minLevels = 0,
        colorHex = 0xFFCD7F32,
        iconEmoji = "🥉",
        description = "Early Steps • Levels 1 - 14",
        imageResId = com.example.smritisetu.R.drawable.bronze_league
    ),
    SILVER(
        tierName = "Silver Division",
        tierNameShort = "Silver",
        minXp = 225, // 15 levels * 15 XP
        maxXp = 449,
        minLevels = 15,
        colorHex = 0xFFC0C0C0,
        iconEmoji = "🥈",
        description = "Growing Focus • Levels 15 - 29",
        imageResId = com.example.smritisetu.R.drawable.silver_league
    ),
    GOLD(
        tierName = "Gold Division",
        tierNameShort = "Gold",
        minXp = 450, // 30 levels * 15 XP
        maxXp = 674,
        minLevels = 30,
        colorHex = 0xFFFFD700,
        iconEmoji = "🥇",
        description = "Sharp Recall • Levels 30 - 44",
        imageResId = com.example.smritisetu.R.drawable.gold_league
    ),
    PLATINUM(
        tierName = "Platinum Division",
        tierNameShort = "Platinum",
        minXp = 675, // 45 levels * 15 XP
        maxXp = 899,
        minLevels = 45,
        colorHex = 0xFF00CED1,
        iconEmoji = "💎",
        description = "Master Memory • Levels 45 - 59",
        imageResId = com.example.smritisetu.R.drawable.platinum_league
    ),
    DIAMOND(
        tierName = "Diamond Division",
        tierNameShort = "Diamond",
        minXp = 900, // 60+ levels * 15 XP
        maxXp = Int.MAX_VALUE,
        minLevels = 60,
        colorHex = 0xFF9932CC,
        iconEmoji = "👑",
        description = "Grand Master • Levels 60+",
        imageResId = com.example.smritisetu.R.drawable.diamond_league
    );

    fun getNextTier(): LeagueTier? {
        val all = LeagueTier.entries
        val nextIdx = ordinal + 1
        return if (nextIdx < all.size) all[nextIdx] else null
    }

    companion object {
        const val XP_PER_LEVEL = 15
        const val LEVELS_PER_TIER = 15 // 15 levels required to advance to next tier
        const val XP_PER_TIER = 225 // 15 * 15 = 225 XP

        fun fromXp(monthlyXp: Int): LeagueTier {
            return when {
                monthlyXp < 225 -> BRONZE
                monthlyXp < 450 -> SILVER
                monthlyXp < 675 -> GOLD
                monthlyXp < 900 -> PLATINUM
                else -> DIAMOND
            }
        }
    }
}

data class CaregiverReminder(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "Medicine", // Medicine, Hydration, Activity, Meal
    val time: String = "08:00 AM",
    val message: String = "Morning memory medication",
    val isActive: Boolean = true
)

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val age: Int = 0,
    val avatarUri: String? = null,
    val role: UserRole = UserRole.PATIENT,
    val patientLinkCode: String = "SM-" + (1000..9999).random(), // 6-digit unique linking code for patients
    val linkedPatientCode: String? = null, // Linked patient code for caregivers
    val preferredLanguage: String = "English",
    val isGoogleLinked: Boolean = false,
    val totalXp: Int = 0,
    val monthlyLeagueXp: Int = 0,
    val coins: Int = 0,
    val streakDays: Int = 0,
    val lastActiveDate: String? = null,
    val leagueTier: String = LeagueTier.BRONZE.tierName
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

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Dynamic Daily Streak Tracking
    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _lastActiveDate = MutableStateFlow<String?>(null)
    val lastActiveDate: StateFlow<String?> = _lastActiveDate.asStateFlow()

    // Active View Mode
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

    // Game Level Progression: Level 1 unlocked by default
    private val _highestUnlockedLevel = MutableStateFlow(1)
    val highestUnlockedLevel: StateFlow<Int> = _highestUnlockedLevel.asStateFlow()

    // Game 2 (Pattern Matching) Level Progression: Level 1 unlocked by default
    private val _highestUnlockedPatternLevel = MutableStateFlow(1)
    val highestUnlockedPatternLevel: StateFlow<Int> = _highestUnlockedPatternLevel.asStateFlow()

    // In-Game Perks Inventory (Default: 0)
    private val _hintsCount = MutableStateFlow(0)
    val hintsCount: StateFlow<Int> = _hintsCount.asStateFlow()

    private val _showAgainCount = MutableStateFlow(0)
    val showAgainCount: StateFlow<Int> = _showAgainCount.asStateFlow()
    val peekCount: StateFlow<Int> = _showAgainCount.asStateFlow()

    private val _skipLevelCount = MutableStateFlow(0)
    val skipLevelCount: StateFlow<Int> = _skipLevelCount.asStateFlow()

    // Monthly League Season XP & Reset Tracking
    private val _monthlyLeagueXp = MutableStateFlow(0)
    val monthlyLeagueXp: StateFlow<Int> = _monthlyLeagueXp.asStateFlow()

    private val _lastSeasonResetMonth = MutableStateFlow(getCurrentYearMonthKey())
    val lastSeasonResetMonth: StateFlow<String> = _lastSeasonResetMonth.asStateFlow()

    // Caregiver Daily Reminders (Starts empty, dynamic from backend)
    private val _reminders = MutableStateFlow<List<CaregiverReminder>>(emptyList())
    val reminders: StateFlow<List<CaregiverReminder>> = _reminders.asStateFlow()

    // Cognitive Telemetry Logs
    private val _telemetryLogs = MutableStateFlow<List<CognitiveGameLog>>(emptyList())
    val telemetryLogs: StateFlow<List<CognitiveGameLog>> = _telemetryLogs.asStateFlow()

    fun getCurrentYearMonthKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun getTodayDateKey(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun calculateDaysBetween(startDateStr: String, endDateStr: String): Long? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDate = sdf.parse(startDateStr) ?: return null
            val endDate = sdf.parse(endDateStr) ?: return null

            val cal1 = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val cal2 = Calendar.getInstance().apply {
                time = endDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diffMs = cal2.timeInMillis - cal1.timeInMillis
            diffMs / (1000 * 60 * 60 * 24)
        } catch (_: Exception) {
            null
        }
    }

    fun getEffectiveStreakDays(todayKey: String = getTodayDateKey()): Int {
        val lastDate = _lastActiveDate.value ?: return 0
        val diff = calculateDaysBetween(lastDate, todayKey)
        return if (diff != null && diff <= 1L) {
            _streakDays.value
        } else {
            0
        }
    }

    fun recordDailyActivity(todayKey: String = getTodayDateKey()): Int {
        val lastDate = _lastActiveDate.value
        val currentStreak = _streakDays.value

        val newStreak = when {
            lastDate == null -> {
                // First session
                1
            }
            else -> {
                val diff = calculateDaysBetween(lastDate, todayKey)
                when {
                    diff == 0L -> {
                        // Already played today: maintain current streak (ensure at least 1)
                        currentStreak.coerceAtLeast(1)
                    }
                    diff == 1L -> {
                        // Played consecutive day: increment streak
                        (currentStreak.coerceAtLeast(0)) + 1
                    }
                    diff != null && diff > 1L -> {
                        // Missed one or more days: start new streak
                        1
                    }
                    else -> {
                        // Clock adjustment fallback
                        currentStreak.coerceAtLeast(1)
                    }
                }
            }
        }

        _streakDays.value = newStreak
        _lastActiveDate.value = todayKey

        _currentUser.update { current ->
            current?.copy(
                streakDays = newStreak,
                lastActiveDate = todayKey
            )
        }
        persistToStorage()
        return newStreak
    }

    fun setStreakForTesting(days: Int, lastDate: String?) {
        _streakDays.value = days
        _lastActiveDate.value = lastDate
        _currentUser.update { current ->
            current?.copy(
                streakDays = days,
                lastActiveDate = lastDate
            )
        }
    }

    fun getDaysUntilNextMonthReset(): Int {
        val cal = Calendar.getInstance()
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        return (maxDay - currentDay + 1).coerceAtLeast(1)
    }

    fun getCurrentSeasonName(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun getCurrentLeagueTier(): LeagueTier = LeagueTier.fromXp(_monthlyLeagueXp.value)

    fun checkAndPerformMonthlyLeagueReset(): Boolean {
        val currentKey = getCurrentYearMonthKey()
        if (_lastSeasonResetMonth.value != currentKey) {
            _monthlyLeagueXp.value = 0
            _lastSeasonResetMonth.value = currentKey
            _currentUser.update { current ->
                current?.copy(
                    monthlyLeagueXp = 0,
                    leagueTier = LeagueTier.BRONZE.tierName
                )
            }
            persistToStorage()
            try {
                Log.d("SmritiSetu", "Monthly League Reset applied on Date 1 for season: $currentKey. Reset to Bronze.")
            } catch (_: Exception) {}
            return true
        }
        return false
    }

    fun setMonthlyLeagueXpForTesting(xp: Int, seasonMonthKey: String? = null) {
        _monthlyLeagueXp.value = xp
        if (seasonMonthKey != null) {
            _lastSeasonResetMonth.value = seasonMonthKey
        }
        val tier = LeagueTier.fromXp(xp)
        _currentUser.update { current ->
            current?.copy(
                monthlyLeagueXp = xp,
                leagueTier = tier.tierName
            )
        }
    }

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initStorage(context: Context) {
        try {
            sharedPreferences = context.getSharedPreferences("smritisetu_prefs", Context.MODE_PRIVATE)
            loadFromStorage()
            // Fetch live profile in background if token exists
            val token = sharedPreferences?.getString("auth_token", null)
            if (!token.isNullOrBlank()) {
                ApiClient.authToken = token
                appScope.launch { refreshProfileFromBackend() }
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Failed to init SharedPreferences", e)
        }
    }

    suspend fun refreshProfileFromBackend() {
        try {
            val response = ApiClient.userApi.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                withContext(Dispatchers.Main) {
                    dto.name?.let { n -> if (n.isNotBlank()) _currentUser.update { it?.copy(name = n) } }
                    dto.phone?.let { p -> _currentUser.update { it?.copy(phone = p) } }
                    dto.gender?.let { g -> _currentUser.update { it?.copy(gender = g) } }
                    dto.age?.let { a -> _currentUser.update { it?.copy(age = a) } }
                    dto.avatarUri?.let { av -> _currentUser.update { it?.copy(avatarUri = av) } }
                    dto.patientLinkCode?.let { code -> _currentUser.update { it?.copy(patientLinkCode = code) } }
                    dto.linkedPatientCode?.let { lcode -> _currentUser.update { it?.copy(linkedPatientCode = lcode) } }
                    dto.coins?.let { c -> _currentUser.update { it?.copy(coins = c) } }
                    dto.totalXp?.let { xp -> _currentUser.update { it?.copy(totalXp = xp) } }
                    dto.monthlyLeagueXp?.let { mxp ->
                        _monthlyLeagueXp.value = mxp
                        val tier = LeagueTier.fromXp(mxp)
                        _currentUser.update { it?.copy(monthlyLeagueXp = mxp, leagueTier = tier.tierName) }
                    }
                    dto.hintsCount?.let { _hintsCount.value = it }
                    dto.showAgainCount?.let { _showAgainCount.value = it }
                    dto.skipLevelCount?.let { _skipLevelCount.value = it }
                    dto.highestUnlockedLevel?.let { _highestUnlockedLevel.value = it.coerceAtLeast(1) }
                    dto.highestUnlockedPatternLevel?.let { _highestUnlockedPatternLevel.value = it.coerceAtLeast(1) }
                    dto.streakDays?.let { _streakDays.value = it }
                    dto.lastActiveDate?.let { _lastActiveDate.value = it }
                    persistToStorage()
                }
            }

            // Also refresh reminders dynamically from backend
            val userRole = _currentUser.value?.role ?: UserRole.PATIENT
            if (userRole == UserRole.PATIENT) {
                val remindersRes = ApiClient.userApi.getOwnReminders()
                if (remindersRes.isSuccessful && remindersRes.body() != null) {
                    val list = remindersRes.body()!!.map { r ->
                        CaregiverReminder(
                            id = r.id,
                            type = r.type,
                            time = r.scheduledTime,
                            message = r.message,
                            isActive = r.active ?: true
                        )
                    }
                    withContext(Dispatchers.Main) {
                        _reminders.value = list
                    }
                }
            } else if (userRole == UserRole.CAREGIVER) {
                val summaryRes = ApiClient.caregiverApi.getPatientSummary()
                if (summaryRes.isSuccessful && summaryRes.body() != null) {
                    val summary = summaryRes.body()!!
                    summary.patientId?.let { pid ->
                        val pRemindersRes = ApiClient.caregiverApi.getPatientReminders(pid)
                        if (pRemindersRes.isSuccessful && pRemindersRes.body() != null) {
                            val list = pRemindersRes.body()!!.map { r ->
                                CaregiverReminder(
                                    id = r.id,
                                    type = r.type,
                                    time = r.scheduledTime,
                                    message = r.message,
                                    isActive = r.active ?: true
                                )
                            }
                            withContext(Dispatchers.Main) {
                                _reminders.value = list
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Could not refresh profile from backend: ${e.message}")
        }
    }

    private fun loadFromStorage() {
        val prefs = sharedPreferences ?: return
        val savedToken = prefs.getString("auth_token", null)
        val savedEmail = prefs.getString("user_email", null)

        if (savedToken.isNullOrBlank() && savedEmail.isNullOrBlank()) {
            _isLoggedIn.value = false
            _currentUser.value = null
            _reminders.value = emptyList()
            _telemetryLogs.value = emptyList()
            return
        }

        ApiClient.authToken = savedToken

        val savedCoins = prefs.getInt("user_coins", 0)
        val savedXp = prefs.getInt("user_xp", 0)
        val savedMonthlyXp = prefs.getInt("monthly_league_xp", 0)
        val savedResetMonth = prefs.getString("last_season_reset_month", getCurrentYearMonthKey()) ?: getCurrentYearMonthKey()
        val savedHighestLevel = prefs.getInt("highest_unlocked_level", 1)
        val savedHighestPatternLevel = prefs.getInt("highest_unlocked_pattern_level", 1)
        val savedHints = prefs.getInt("hints_count", 0)
        val savedShowAgain = prefs.getInt("show_again_count", 0)
        val savedSkips = prefs.getInt("skips_count", 0)
        val savedStreak = prefs.getInt("streak_days", 0)
        val savedLastActiveDate = prefs.getString("last_active_date", null)
        val savedRoleName = prefs.getString("user_role", UserRole.PATIENT.name) ?: UserRole.PATIENT.name
        val savedLinkCode = prefs.getString("patient_link_code", "SM-" + (1000..9999).random()) ?: "SM-8492"
        val savedLinkedCode = prefs.getString("linked_patient_code", null)
        val savedLangName = prefs.getString("selected_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        val savedName = prefs.getString("user_name", "") ?: ""
        val savedPhone = prefs.getString("user_phone", "") ?: ""
        val savedGender = prefs.getString("user_gender", "") ?: ""
        val savedAge = prefs.getInt("user_age", 0)
        val savedAvatarUri = prefs.getString("user_avatar_uri", null)
        val savedIsGoogle = prefs.getBoolean("is_google_linked", false)

        _highestUnlockedLevel.value = savedHighestLevel.coerceAtLeast(1)
        _highestUnlockedPatternLevel.value = savedHighestPatternLevel.coerceAtLeast(1)
        _hintsCount.value = savedHints
        _showAgainCount.value = savedShowAgain
        _skipLevelCount.value = savedSkips
        _monthlyLeagueXp.value = savedMonthlyXp
        _lastSeasonResetMonth.value = savedResetMonth
        _lastActiveDate.value = savedLastActiveDate
        _selectedLanguage.value = AppLanguage.entries.find { it.name == savedLangName } ?: AppLanguage.ENGLISH

        // Compute effective active streak based on last active date
        val effectiveStreak = if (savedLastActiveDate != null) {
            val diff = calculateDaysBetween(savedLastActiveDate, getTodayDateKey())
            if (diff != null && diff <= 1L) savedStreak else 0
        } else {
            0
        }
        _streakDays.value = effectiveStreak

        val userRole = UserRole.entries.find { it.name == savedRoleName } ?: UserRole.PATIENT
        _activeRoleView.value = userRole

        checkAndPerformMonthlyLeagueReset()
        val tier = LeagueTier.fromXp(_monthlyLeagueXp.value)

        _currentUser.value = UserProfile(
            name = savedName,
            email = savedEmail ?: "",
            phone = savedPhone,
            gender = savedGender,
            age = savedAge,
            avatarUri = savedAvatarUri,
            role = userRole,
            patientLinkCode = savedLinkCode,
            linkedPatientCode = savedLinkedCode,
            preferredLanguage = _selectedLanguage.value.displayName,
            isGoogleLinked = savedIsGoogle,
            totalXp = savedXp,
            monthlyLeagueXp = _monthlyLeagueXp.value,
            coins = savedCoins,
            streakDays = effectiveStreak,
            lastActiveDate = savedLastActiveDate,
            leagueTier = tier.tierName
        )
        _isLoggedIn.value = true
    }

    private fun persistToStorage() {
        val prefs = sharedPreferences ?: return
        val user = _currentUser.value
        prefs.edit().apply {
            putString("auth_token", ApiClient.authToken)
            putString("user_email", user?.email)
            putString("user_name", user?.name)
            putString("user_phone", user?.phone)
            putString("user_gender", user?.gender)
            putInt("user_age", user?.age ?: 0)
            putString("user_avatar_uri", user?.avatarUri)
            putBoolean("is_google_linked", user?.isGoogleLinked ?: false)
            putInt("user_coins", user?.coins ?: 0)
            putInt("user_xp", user?.totalXp ?: 0)
            putInt("monthly_league_xp", _monthlyLeagueXp.value)
            putString("last_season_reset_month", _lastSeasonResetMonth.value)
            putInt("highest_unlocked_level", _highestUnlockedLevel.value)
            putInt("highest_unlocked_pattern_level", _highestUnlockedPatternLevel.value)
            putInt("hints_count", _hintsCount.value)
            putInt("show_again_count", _showAgainCount.value)
            putInt("skips_count", _skipLevelCount.value)
            putInt("streak_days", _streakDays.value)
            putString("last_active_date", _lastActiveDate.value)
            putString("user_role", user?.role?.name ?: UserRole.PATIENT.name)
            putString("patient_link_code", user?.patientLinkCode ?: "SM-8492")
            putString("linked_patient_code", user?.linkedPatientCode)
            putString("selected_language", _selectedLanguage.value.name)
            apply()
        }
    }

    fun isLevelUnlocked(level: Int): Boolean {
        return level <= _highestUnlockedLevel.value
    }

    fun isPatternLevelUnlocked(level: Int): Boolean {
        return level <= _highestUnlockedPatternLevel.value
    }

    fun unlockNextLevel(completedLevel: Int) {
        if (completedLevel >= _highestUnlockedLevel.value) {
            _highestUnlockedLevel.value = completedLevel + 1
            persistToStorage()
        }
    }

    fun unlockNextPatternLevel(completedLevel: Int) {
        if (completedLevel >= _highestUnlockedPatternLevel.value) {
            _highestUnlockedPatternLevel.value = completedLevel + 1
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

        // Sync with live backend
        appScope.launch {
            try {
                ApiClient.caregiverApi.linkPatientByCode(LinkByCodeRequestDto(linkCode = cleaned))
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to link patient on backend: ${e.message}")
            }
        }

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

        // Sync with backend if linked patient exists
        val patientId = _currentUser.value?.id
        if (patientId != null) {
            appScope.launch {
                try {
                    ApiClient.caregiverApi.createReminder(
                        patientId = patientId,
                        request = ReminderRequestDto(type = type, scheduledTime = time, message = message, active = true)
                    )
                } catch (e: Exception) {
                    Log.w("AuthManager", "Failed to sync reminder creation: ${e.message}")
                }
            }
        }
    }

    fun toggleReminder(id: String) {
        _reminders.update { list ->
            list.map { if (it.id == id) it.copy(isActive = !it.isActive) else it }
        }
        val patientId = _currentUser.value?.id
        if (patientId != null) {
            appScope.launch {
                try {
                    ApiClient.caregiverApi.toggleReminder(patientId = patientId, reminderId = id)
                } catch (e: Exception) {
                    Log.w("AuthManager", "Failed to sync reminder toggle: ${e.message}")
                }
            }
        }
    }

    fun deleteReminder(id: String) {
        _reminders.update { list ->
            list.filter { it.id != id }
        }
        val patientId = _currentUser.value?.id
        if (patientId != null) {
            appScope.launch {
                try {
                    ApiClient.caregiverApi.deleteReminder(patientId = patientId, reminderId = id)
                } catch (e: Exception) {
                    Log.w("AuthManager", "Failed to sync reminder deletion: ${e.message}")
                }
            }
        }
    }

    fun buyPerk(perkType: PerkType): Result<Boolean> {
        val currentCoins = _currentUser.value?.coins ?: 0
        if (currentCoins < perkType.costCoins) {
            return Result.failure(IllegalStateException("Not enough coins! Earn more by completing levels."))
        }

        // Deduct coins locally
        _currentUser.update { it?.copy(coins = currentCoins - perkType.costCoins) }

        // Increment perk count locally
        when (perkType) {
            PerkType.HINT -> _hintsCount.update { it + 1 }
            PerkType.SHOW_AGAIN -> _showAgainCount.update { it + 1 }
            PerkType.SKIP_LEVEL -> _skipLevelCount.update { it + 1 }
        }

        persistToStorage()

        // Sync with backend shop
        appScope.launch {
            try {
                ApiClient.shopApi.buyPerk(BuyPerkRequestDto(perkType = perkType.name))
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to sync buy perk: ${e.message}")
            }
        }

        return Result.success(true)
    }

    fun useHint(): Boolean {
        if (_hintsCount.value > 0) {
            _hintsCount.update { it - 1 }
            persistToStorage()
            appScope.launch {
                try {
                    ApiClient.userApi.usePerk(UsePerkRequestDto("HINT"))
                } catch (e: Exception) {
                    Log.w("AuthManager", "Failed to sync perk use: ${e.message}")
                }
            }
            return true
        }
        return false
    }

    fun useShowAgain(): Boolean {
        if (_showAgainCount.value > 0) {
            _showAgainCount.update { it - 1 }
            persistToStorage()
            appScope.launch {
                try {
                    ApiClient.userApi.usePerk(UsePerkRequestDto("PEEK"))
                } catch (e: Exception) {
                    Log.w("AuthManager", "Failed to sync perk use: ${e.message}")
                }
            }
            return true
        }
        return false
    }

    fun usePeek(): Boolean = useShowAgain()

    fun useSkipLevel(): Boolean {
        if (_skipLevelCount.value > 0) {
            _skipLevelCount.update { it - 1 }
            persistToStorage()
            appScope.launch {
                try {
                    ApiClient.userApi.usePerk(UsePerkRequestDto("SKIP"))
                } catch (e: Exception) {
                    Log.w("AuthManager", "Failed to sync perk use: ${e.message}")
                }
            }
            return true
        }
        return false
    }

    fun addRewards(xp: Int, coins: Int) {
        checkAndPerformMonthlyLeagueReset()
        val updatedStreak = recordDailyActivity()
        _monthlyLeagueXp.update { it + xp }
        val updatedTier = LeagueTier.fromXp(_monthlyLeagueXp.value)

        _currentUser.update { current ->
            current?.copy(
                totalXp = (current.totalXp + xp),
                monthlyLeagueXp = _monthlyLeagueXp.value,
                leagueTier = updatedTier.tierName,
                coins = (current.coins + coins),
                streakDays = updatedStreak,
                lastActiveDate = _lastActiveDate.value
            )
        }
        persistToStorage()
    }

    fun login(email: String, pass: String): Result<UserProfile> {
        val tier = LeagueTier.fromXp(_monthlyLeagueXp.value)
        val user = UserProfile(
            name = if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User",
            email = email,
            role = UserRole.PATIENT,
            preferredLanguage = _selectedLanguage.value.displayName,
            monthlyLeagueXp = _monthlyLeagueXp.value,
            leagueTier = tier.tierName,
            streakDays = _streakDays.value,
            lastActiveDate = _lastActiveDate.value
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _activeRoleView.value = UserRole.PATIENT
        persistToStorage()

        // Asynchronously authenticate against live backend
        appScope.launch {
            try {
                val response = ApiClient.authApi.login(LoginRequest(email, pass))
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    dto.token?.let { token ->
                        ApiClient.authToken = token
                        persistToStorage()
                    }
                    refreshProfileFromBackend()
                }
            } catch (e: Exception) {
                Log.w("AuthManager", "Live login connection failed (offline mode active): ${e.message}")
            }
        }

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
        val tier = LeagueTier.fromXp(_monthlyLeagueXp.value)
        val user = UserProfile(
            name = name.ifBlank { if (role == UserRole.CAREGIVER) "Caregiver" else "Patient" },
            email = email,
            role = role,
            patientLinkCode = generatedPatientCode,
            linkedPatientCode = if (role == UserRole.CAREGIVER) patientCodeToLink?.trim()?.uppercase()?.takeIf { it.isNotBlank() } else null,
            preferredLanguage = _selectedLanguage.value.displayName,
            monthlyLeagueXp = _monthlyLeagueXp.value,
            leagueTier = tier.tierName,
            streakDays = _streakDays.value,
            lastActiveDate = _lastActiveDate.value
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _activeRoleView.value = role
        persistToStorage()

        // Asynchronously register with live backend
        appScope.launch {
            try {
                val response = ApiClient.authApi.register(
                    RegisterRequest(
                        username = email,
                        password = pass,
                        name = user.name,
                        role = role.name,
                        preferredLanguage = _selectedLanguage.value.name
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    dto.token?.let { token ->
                        ApiClient.authToken = token
                        persistToStorage()
                    }
                    if (role == UserRole.CAREGIVER && patientCodeToLink != null) {
                        ApiClient.caregiverApi.linkPatientByCode(LinkByCodeRequestDto(linkCode = patientCodeToLink))
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthManager", "Live registration connection failed: ${e.message}")
            }
        }

        return Result.success(user)
    }

    fun loginWithGoogleAccount(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        role: UserRole = UserRole.PATIENT
    ): Result<UserProfile> {
        val googleName = account.displayName ?: account.givenName ?: if (role == UserRole.CAREGIVER) "Caregiver" else "Patient"
        val googleEmail = account.email ?: "google.user@example.com"
        val photoUri = account.photoUrl?.toString()
        val idToken = account.idToken
        val generatedPatientCode = "SM-" + (1000..9999).random()

        val user = UserProfile(
            id = account.id ?: UUID.randomUUID().toString(),
            name = googleName,
            email = googleEmail,
            phone = "",
            gender = "",
            age = 0,
            avatarUri = photoUri,
            role = role,
            patientLinkCode = generatedPatientCode,
            linkedPatientCode = null,
            preferredLanguage = _selectedLanguage.value.displayName,
            isGoogleLinked = true,
            totalXp = 0,
            monthlyLeagueXp = 0,
            coins = 0,
            streakDays = 0,
            lastActiveDate = null,
            leagueTier = LeagueTier.BRONZE.tierName
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _activeRoleView.value = role
        persistToStorage()

        if (!idToken.isNullOrBlank()) {
            appScope.launch {
                try {
                    val response = ApiClient.authApi.googleLogin(
                        GoogleAuthRequest(idToken = idToken, role = role.name)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val dto = response.body()!!
                        dto.token?.let { token ->
                            ApiClient.authToken = token
                            persistToStorage()
                        }
                        refreshProfileFromBackend()
                    }
                } catch (e: Exception) {
                    Log.w("AuthManager", "Backend Google OAuth token sync: ${e.message}")
                }
            }
        }
        return Result.success(user)
    }

    fun loginWithGoogle(): Result<UserProfile> {
        val user = UserProfile(
            name = "User",
            email = "user@example.com",
            isGoogleLinked = true,
            role = UserRole.PATIENT,
            preferredLanguage = _selectedLanguage.value.displayName,
            monthlyLeagueXp = 0,
            totalXp = 0,
            coins = 0,
            streakDays = 0,
            leagueTier = LeagueTier.BRONZE.tierName,
            lastActiveDate = null
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
        appScope.launch {
            try {
                ApiClient.authApi.forgotPassword(ForgotPasswordRequestDto(email))
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to trigger OTP email: ${e.message}")
            }
        }
        return Result.success("123456")
    }

    fun verifyOtpAndResetPassword(email: String, otp: String, newPass: String): Result<Boolean> {
        appScope.launch {
            try {
                ApiClient.authApi.verifyOtp(VerifyOtpRequestDto(email = email, otp = otp, newPassword = newPass))
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to verify OTP with backend: ${e.message}")
            }
        }
        return Result.success(true)
    }

    fun changePassword(currentPass: String, newPass: String): Result<Boolean> {
        if (currentPass.isBlank() || newPass.isBlank()) {
            return Result.failure(IllegalArgumentException("Please fill in both password fields"))
        }
        if (newPass.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters"))
        }
        appScope.launch {
            try {
                ApiClient.authApi.changePassword(ChangePasswordRequestDto(currentPass, newPass))
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to change password on backend: ${e.message}")
            }
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

        appScope.launch {
            try {
                ApiClient.userApi.updateProfile(
                    UpdateProfileRequestDto(
                        name = name,
                        preferredLanguage = _selectedLanguage.value.name,
                        phone = phone,
                        gender = gender,
                        age = age,
                        avatarUri = avatarUri
                    )
                )
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to sync profile update: ${e.message}")
            }
        }
    }

    fun recordGameTelemetry(log: CognitiveGameLog) {
        _telemetryLogs.update { it + log }
        recordDailyActivity()

        // Send telemetry payload to live backend and AI engine
        appScope.launch {
            try {
                val isPattern = log.gameName.lowercase().contains("pattern")
                val requestDto = LevelAttemptRequestDto(
                    gameName = log.gameName,
                    level = log.level,
                    timeTakenMs = log.timeElapsedMs,
                    timeTakenSec = (log.timeElapsedMs / 1000).toInt(),
                    triesCount = log.tries,
                    totalCards = log.totalCards,
                    idleHintsCount = log.hintsUsed,
                    perkHintsCount = log.perkHintsUsed,
                    difficulty = log.difficulty,
                    syncedOffline = false
                )

                val response = if (isPattern) {
                    ApiClient.gameApi.completePatternLevel(requestDto)
                } else {
                    ApiClient.gameApi.completeMatchCardLevel(requestDto)
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    withContext(Dispatchers.Main) {
                        body.totalCoins?.let { coins -> _currentUser.update { it?.copy(coins = coins) } }
                        body.totalXp?.let { xp -> _currentUser.update { it?.copy(totalXp = xp) } }
                        body.monthlyLeagueXp?.let { mxp ->
                            _monthlyLeagueXp.value = mxp
                            val tier = LeagueTier.fromXp(mxp)
                            _currentUser.update { it?.copy(monthlyLeagueXp = mxp, leagueTier = tier.tierName) }
                        }
                        persistToStorage()
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to sync telemetry to backend (cached locally): ${e.message}")
            }
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
        ApiClient.authToken = null
        _highestUnlockedLevel.value = 1
        _highestUnlockedPatternLevel.value = 1
        _hintsCount.value = 0
        _showAgainCount.value = 0
        _skipLevelCount.value = 0
        _monthlyLeagueXp.value = 0
        _streakDays.value = 0
        _lastActiveDate.value = null
        _lastSeasonResetMonth.value = getCurrentYearMonthKey()
        _activeRoleView.value = UserRole.PATIENT
        _reminders.value = emptyList()
        _telemetryLogs.value = emptyList()
        val prefs = sharedPreferences ?: return
        prefs.edit().clear().apply()
    }
}
