package com.example.smritisetu.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val name: String,
    val role: String? = "PATIENT",
    val preferredLanguage: String? = "en"
)

@Serializable
data class GoogleAuthRequest(
    val idToken: String,
    val role: String? = "PATIENT"
)

@Serializable
data class AuthResponseDto(
    val token: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val username: String? = null,
    val role: String? = null,
    val patientLinkCode: String? = null,
    val linkedPatientCode: String? = null,
    val coins: Int? = 1000,
    val hintsCount: Int? = 0,
    val skipLevelCount: Int? = 0,
    val showAgainCount: Int? = 0,
    val totalXp: Int? = 1450,
    val monthlyLeagueXp: Int? = 0,
    val leagueTier: String? = "Bronze Division",
    val highestUnlockedLevel: Int? = 5,
    val highestUnlockedPatternLevel: Int? = 5,
    val phone: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val avatarUri: String? = null,
    val preferredLanguage: String? = null,
    val streakDays: Int? = 0,
    val lastActiveDate: String? = null
)

@Serializable
data class UpdateProfileRequestDto(
    val name: String,
    val preferredLanguage: String? = null,
    val phone: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val avatarUri: String? = null
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String
)

@Serializable
data class VerifyOtpRequestDto(
    val email: String,
    val otp: String,
    val newPassword: String
)

@Serializable
data class GenericApiResponseDto(
    val success: Boolean? = true,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class LevelAttemptRequestDto(
    val gameId: String? = null,
    val gameName: String? = null,
    val level: Int? = null,
    val timeTakenSec: Int? = null,
    val timeTakenMs: Long? = null,
    val triesCount: Int? = null,
    val totalCards: Int? = null,
    val hintsUsed: Int? = null,
    val idleHintsCount: Int? = null,
    val perkHintsCount: Int? = null,
    val peekUsedCount: Int? = null,
    val previewTimeSec: Int? = null,
    val previewDurationSec: Int? = null,
    val patternLength: Int? = null,
    val difficulty: String? = null,
    val skipped: Boolean? = false,
    val syncedOffline: Boolean? = false
)

@Serializable
data class LevelAttemptResponseDto(
    val passed: Boolean? = true,
    val xpEarned: Int? = 15,
    val coinsEarned: Int? = 200,
    val totalCoins: Int? = 1000,
    val totalXp: Int? = 1450,
    val monthlyLeagueXp: Int? = 0,
    val leagueTier: String? = "Bronze Division",
    val newLeague: String? = null,
    val highestUnlockedLevel: Int? = 5,
    val highestUnlockedPatternLevel: Int? = 5,
    val nextUnlockedLevel: Int? = 6,
    val nextLevel: Int? = 6,
    val newDifficulty: Int? = 1,
    val predictedDifficulty: String? = null,
    val difficultyReasoning: String? = null,
    val aiReasoningMessage: String? = null,
    val streakDays: Int? = 0
)

@Serializable
data class LeagueStatusResponseDto(
    val currentLeague: String? = "Bronze Division",
    val currentTier: String? = "Bronze Division",
    val monthlyXp: Int? = 0,
    val lifetimeTotalXp: Int? = 1450,
    val totalXp: Int? = 1450,
    val nextTier: String? = "Silver Division",
    val xpToNextLeague: Int? = 225,
    val xpNeededForNextTier: Int? = 225,
    val levelsNeededForNextTier: Int? = 15,
    val tierProgressPercentage: Float? = 0f,
    val seasonName: String? = "September 2026",
    val daysUntilMonthlyReset: Int? = 24,
    val streakDays: Int? = 0
)

@Serializable
data class LeaderboardEntryDto(
    val rank: Int? = 1,
    val userId: String? = null,
    val userName: String? = null,
    val monthlyXp: Int? = 0,
    val avatarUri: String? = null,
    val streak: Int? = 0,
    val leagueTier: String? = null
)

@Serializable
data class LeaderboardResponseDto(
    val tier: String? = null,
    val season: String? = null,
    val standings: List<LeaderboardEntryDto>? = emptyList()
)

@Serializable
data class ShopItemDto(
    val id: String,
    val name: String,
    val costCoins: Int,
    val description: String
)

@Serializable
data class BuyPerkRequestDto(
    val perkType: String
)

@Serializable
data class BuyPerkResponseDto(
    val success: Boolean? = true,
    val message: String? = null,
    val remainingCoins: Int? = null,
    val hintsCount: Int? = null,
    val showAgainCount: Int? = null,
    val skipLevelCount: Int? = null,
    val error: String? = null
)

@Serializable
data class UsePerkRequestDto(
    val perkType: String
)

@Serializable
data class UsePerkResponseDto(
    val success: Boolean? = true,
    val remainingCount: Int? = 0,
    val error: String? = null
)

@Serializable
data class ReminderRequestDto(
    val type: String,
    val scheduledTime: String,
    val message: String,
    val active: Boolean? = true
)

@Serializable
data class ReminderResponseDto(
    val id: String,
    val type: String,
    val scheduledTime: String,
    val message: String,
    val active: Boolean? = true
)

@Serializable
data class LinkByCodeRequestDto(
    val linkCode: String
)

@Serializable
data class LinkByCodeResponseDto(
    val success: Boolean? = true,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class CaregiverPatientSummaryDto(
    val hasLinkedPatient: Boolean? = true,
    val patientId: String? = null,
    val name: String? = null,
    val linkCode: String? = null,
    val age: Int? = 68,
    val gender: String? = "Female",
    val leagueTier: String? = "Bronze Division",
    val totalXp: Int? = 1450,
    val monthlyLeagueXp: Int? = 0,
    val coins: Int? = 1000,
    val highestLevelReached: Int? = 5,
    val highestUnlockedLevel: Int? = 5,
    val highestUnlockedPatternLevel: Int? = 5,
    val streakDays: Int? = 0
)
