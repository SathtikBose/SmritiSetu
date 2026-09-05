package com.example.smritisetu.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun signup_patientRole_generatesUniqueLinkCode() {
        val result = AuthManager.signup("Biren Gogoi", "biren@smritisetu.org", "pass123", UserRole.PATIENT)
        assertTrue(result.isSuccess)
        val user = AuthManager.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.PATIENT, user?.role)
        assertTrue(user?.patientLinkCode?.startsWith("SM-") == true)
        assertEquals(UserRole.PATIENT, AuthManager.activeRoleView.value)
    }

    @Test
    fun signup_caregiverRole_linksPatientCode() {
        val result = AuthManager.signup(
            name = "Rita Gogoi",
            email = "rita@caregiver.org",
            pass = "pass123",
            role = UserRole.CAREGIVER,
            patientCodeToLink = "SM-8492"
        )
        assertTrue(result.isSuccess)
        val user = AuthManager.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.CAREGIVER, user?.role)
        assertEquals("SM-8492", user?.linkedPatientCode)
        assertEquals(UserRole.CAREGIVER, AuthManager.activeRoleView.value)
    }

    @Test
    fun toggleActiveRoleView_switchesBetweenPatientAndCaregiver() {
        AuthManager.setActiveRoleView(UserRole.PATIENT)
        assertEquals(UserRole.PATIENT, AuthManager.activeRoleView.value)

        AuthManager.toggleActiveRoleView()
        assertEquals(UserRole.CAREGIVER, AuthManager.activeRoleView.value)

        AuthManager.toggleActiveRoleView()
        assertEquals(UserRole.PATIENT, AuthManager.activeRoleView.value)
    }

    @Test
    fun linkPatientByCode_updatesLinkedPatientCode() {
        AuthManager.login("caregiver@domain.com", "pass")
        val result = AuthManager.linkPatientByCode("SM-9944")
        assertTrue(result.isSuccess)
        assertEquals("SM-9944", AuthManager.currentUser.value?.linkedPatientCode)

        val invalidResult = AuthManager.linkPatientByCode(" ")
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun caregiverReminders_addToggleDelete() {
        val initialCount = AuthManager.reminders.value.size
        AuthManager.addReminder("Medicine", "09:00 AM", "Morning blood pressure pill")
        assertEquals(initialCount + 1, AuthManager.reminders.value.size)

        val added = AuthManager.reminders.value.last()
        assertTrue(added.isActive)
        assertEquals("09:00 AM", added.time)

        // Toggle active status
        AuthManager.toggleReminder(added.id)
        assertFalse(AuthManager.reminders.value.first { it.id == added.id }.isActive)

        // Delete reminder
        AuthManager.deleteReminder(added.id)
        assertEquals(initialCount, AuthManager.reminders.value.size)
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
    fun endlessProgression_unlocksInfinitelyBeyondLevel20() {
        // Unlock Level 20
        AuthManager.unlockNextLevel(19)
        assertEquals(20, AuthManager.highestUnlockedLevel.value)

        // Complete Level 20 -> Unlocks Level 21
        AuthManager.unlockNextLevel(20)
        assertEquals(21, AuthManager.highestUnlockedLevel.value)
        assertTrue(AuthManager.isLevelUnlocked(21))

        // Complete Level 99 -> Unlocks Level 100
        AuthManager.unlockNextLevel(99)
        assertEquals(100, AuthManager.highestUnlockedLevel.value)
        assertTrue(AuthManager.isLevelUnlocked(100))
        assertFalse(AuthManager.isLevelUnlocked(101))
    }

    @Test
    fun login_success_setsUserAndLoggedIn() {
        val result = AuthManager.login("test@smritisetu.org", "password123")
        assertTrue(result.isSuccess)
        assertTrue(AuthManager.isLoggedIn.value)
        assertEquals("test@smritisetu.org", AuthManager.currentUser.value?.email)
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
            perkHintsUsed = 0,
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

    @Test
    fun leagueTiers_fromXp_correctlyMapsAll5Tiers() {
        // 15 levels * 15 XP = 225 XP per tier milestone
        assertEquals(LeagueTier.BRONZE, LeagueTier.fromXp(0))
        assertEquals(LeagueTier.BRONZE, LeagueTier.fromXp(224))
        assertEquals(LeagueTier.SILVER, LeagueTier.fromXp(225))
        assertEquals(LeagueTier.SILVER, LeagueTier.fromXp(449))
        assertEquals(LeagueTier.GOLD, LeagueTier.fromXp(450))
        assertEquals(LeagueTier.GOLD, LeagueTier.fromXp(674))
        assertEquals(LeagueTier.PLATINUM, LeagueTier.fromXp(675))
        assertEquals(LeagueTier.PLATINUM, LeagueTier.fromXp(899))
        assertEquals(LeagueTier.DIAMOND, LeagueTier.fromXp(900))
        assertEquals(LeagueTier.DIAMOND, LeagueTier.fromXp(2500))
    }

    @Test
    fun addRewards_updatesMonthlyLeagueXpAndPromotesTier() {
        AuthManager.login("player@smritisetu.org", "pass")
        AuthManager.setMonthlyLeagueXpForTesting(210) // 14 levels completed (Bronze)
        assertEquals(LeagueTier.BRONZE.tierName, AuthManager.currentUser.value?.leagueTier)

        // Clear 15th level (+15 XP) -> crosses 225 XP milestone!
        AuthManager.addRewards(xp = 15, coins = 200)

        assertEquals(225, AuthManager.monthlyLeagueXp.value)
        assertEquals(LeagueTier.SILVER.tierName, AuthManager.currentUser.value?.leagueTier)
    }

    @Test
    fun monthlyLeagueReset_onNewMonth_resetsMonthlyXpToBronze() {
        AuthManager.login("player@smritisetu.org", "pass")
        // Simulate previous month season with 550 XP (Gold tier)
        AuthManager.setMonthlyLeagueXpForTesting(550, "2026-08")
        assertEquals(LeagueTier.GOLD.tierName, AuthManager.currentUser.value?.leagueTier)

        // Trigger monthly check on new month
        val resetOccurred = AuthManager.checkAndPerformMonthlyLeagueReset()
        assertTrue(resetOccurred)
        assertEquals(0, AuthManager.monthlyLeagueXp.value)
        assertEquals(LeagueTier.BRONZE.tierName, AuthManager.currentUser.value?.leagueTier)
    }
}
