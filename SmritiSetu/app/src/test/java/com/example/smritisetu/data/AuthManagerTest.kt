package com.example.smritisetu.data

import org.junit.Assert.assertEquals
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
    fun verifyOtp_validOtp_resetsPassword() {
        val result = AuthManager.verifyOtpAndResetPassword("biren@smritisetu.org", "123456", "newpass123")
        assertTrue(result.isSuccess)
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
