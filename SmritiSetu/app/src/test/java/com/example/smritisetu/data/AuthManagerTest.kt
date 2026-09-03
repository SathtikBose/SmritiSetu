package com.example.smritisetu.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthManagerTest {

    @Before
    fun setUp() {
        AuthManager.logout()
    }

    @Test
    fun defaultLanguage_isEnglish() {
        assertEquals(AppLanguage.ENGLISH, AuthManager.selectedLanguage.value)
    }

    @Test
    fun defaultPerks_zero() {
        assertEquals(0, AuthManager.hintsCount.value)
        assertEquals(0, AuthManager.skipLevelCount.value)
    }

    @Test
    fun buyPerk_hint_deducts1000Coins_andIncrementsCount() {
        AuthManager.login("user@smritisetu.org", "pass")
        // Start with 1000 coins
        assertEquals(1000, AuthManager.currentUser.value?.coins)

        val result = AuthManager.buyPerk(PerkType.HINT)
        assertTrue(result.isSuccess)
        assertEquals(0, AuthManager.currentUser.value?.coins)
        assertEquals(1, AuthManager.hintsCount.value)

        // Try to buy again with 0 coins -> failure
        val failedResult = AuthManager.buyPerk(PerkType.HINT)
        assertTrue(failedResult.isFailure)
        assertEquals(1, AuthManager.hintsCount.value)
    }

    @Test
    fun buyPerk_skipLevel_deducts2000Coins() {
        AuthManager.login("user@smritisetu.org", "pass")
        // Add 2000 coins (total 3000)
        AuthManager.addRewards(xp = 0, coins = 2000)
        assertEquals(3000, AuthManager.currentUser.value?.coins)

        val result = AuthManager.buyPerk(PerkType.SKIP_LEVEL)
        assertTrue(result.isSuccess)
        assertEquals(1000, AuthManager.currentUser.value?.coins)
        assertEquals(1, AuthManager.skipLevelCount.value)

        // Use skip
        val used = AuthManager.useSkipLevel()
        assertTrue(used)
        assertEquals(0, AuthManager.skipLevelCount.value)
    }

    @Test
    fun useHint_decrementsCountWhenAvailable() {
        AuthManager.login("user@smritisetu.org", "pass")
        // No hints initially
        assertFalse(AuthManager.useHint())

        // Buy a hint
        AuthManager.buyPerk(PerkType.HINT)
        assertEquals(1, AuthManager.hintsCount.value)

        // Use hint
        assertTrue(AuthManager.useHint())
        assertEquals(0, AuthManager.hintsCount.value)
    }

    @Test
    fun defaultLevels_firstFiveUnlocked() {
        assertEquals(5, AuthManager.highestUnlockedLevel.value)
        assertTrue(AuthManager.isLevelUnlocked(1))
        assertTrue(AuthManager.isLevelUnlocked(2))
        assertTrue(AuthManager.isLevelUnlocked(3))
        assertTrue(AuthManager.isLevelUnlocked(4))
        assertTrue(AuthManager.isLevelUnlocked(5))
        assertFalse(AuthManager.isLevelUnlocked(6))
        assertFalse(AuthManager.isLevelUnlocked(7))
    }

    @Test
    fun unlockNextLevel_progressiveUnlocking() {
        // Complete level 5 -> unlocks level 6
        AuthManager.unlockNextLevel(5)
        assertEquals(6, AuthManager.highestUnlockedLevel.value)
        assertTrue(AuthManager.isLevelUnlocked(6))
        assertFalse(AuthManager.isLevelUnlocked(7))

        // Complete level 6 -> unlocks level 7
        AuthManager.unlockNextLevel(6)
        assertEquals(7, AuthManager.highestUnlockedLevel.value)
        assertTrue(AuthManager.isLevelUnlocked(7))
    }

    @Test
    fun login_success_setsUserAndLoggedIn() {
        val result = AuthManager.login("test@smritisetu.org", "password123")
        assertTrue(result.isSuccess)
        assertTrue(AuthManager.isLoggedIn.value)
        assertEquals("test@smritisetu.org", AuthManager.currentUser.value?.email)
    }

    @Test
    fun signup_success_createsAccount() {
        val result = AuthManager.signup("Biren Gogoi", "biren@smritisetu.org", "password123")
        assertTrue(result.isSuccess)
        assertTrue(AuthManager.isLoggedIn.value)
        assertEquals("Biren Gogoi", AuthManager.currentUser.value?.name)
    }

    @Test
    fun changePassword_validPassword_success() {
        val result = AuthManager.changePassword("oldPassword123", "newPassword456")
        assertTrue(result.isSuccess)
    }

    @Test
    fun addRewards_incrementsXpAndCoins() {
        AuthManager.login("user@smritisetu.org", "pass")
        val initialXp = AuthManager.currentUser.value?.totalXp ?: 0
        val initialCoins = AuthManager.currentUser.value?.coins ?: 0

        AuthManager.addRewards(xp = 15, coins = 200)

        assertEquals(initialXp + 15, AuthManager.currentUser.value?.totalXp)
        assertEquals(initialCoins + 200, AuthManager.currentUser.value?.coins)
    }

    @Test
    fun recordGameTelemetry_recordsLogs() {
        val log = CognitiveGameLog(
            gameName = "MatchTheCard",
            level = 1,
            tries = 4,
            totalCards = 4,
            timeElapsedMs = 5200L,
            hintsUsed = 0,
            difficulty = "NORMAL"
        )
        AuthManager.recordGameTelemetry(log)
        assertTrue(AuthManager.telemetryLogs.value.contains(log))
    }

    @Test
    fun themeMode_updatesSuccessfully() {
        AuthManager.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, AuthManager.themeMode.value)
        AuthManager.setThemeMode(AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, AuthManager.themeMode.value)
        AuthManager.setThemeMode(AppThemeMode.HIGH_CONTRAST)
        assertEquals(AppThemeMode.HIGH_CONTRAST, AuthManager.themeMode.value)
    }

    @Test
    fun fontScale_updatesSuccessfully() {
        AuthManager.setFontScale(1.15f)
        assertEquals(1.15f, AuthManager.fontScale.value, 0.001f)
        AuthManager.setFontScale(1.30f)
        assertEquals(1.30f, AuthManager.fontScale.value, 0.001f)
    }

    @Test
    fun language_updatesSuccessfully() {
        AuthManager.setLanguage(AppLanguage.HINDI)
        assertEquals(AppLanguage.HINDI, AuthManager.selectedLanguage.value)
        val hindiStrings = getStringsForLanguage(AppLanguage.HINDI)
        assertEquals("स्मृति सेतु", hindiStrings.appNameNative)
        assertEquals("होम", hindiStrings.navHome)

        AuthManager.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, AuthManager.selectedLanguage.value)
        val enStrings = getStringsForLanguage(AppLanguage.ENGLISH)
        assertEquals("SmritiSetu", enStrings.appNameNative)
        assertEquals("Home", enStrings.navHome)
    }

    @Test
    fun updateProfile_updatesFields() {
        AuthManager.login("ananya@smritisetu.org", "pass")
        AuthManager.updateProfile("Dr. Ananya S.", "+91 99999 88888", "Female", 65, "camera://avatar1")
        val user = AuthManager.currentUser.value
        assertEquals("Dr. Ananya S.", user?.name)
        assertEquals("+91 99999 88888", user?.phone)
        assertEquals("Female", user?.gender)
        assertEquals(65, user?.age)
        assertEquals("camera://avatar1", user?.avatarUri)
    }
}
