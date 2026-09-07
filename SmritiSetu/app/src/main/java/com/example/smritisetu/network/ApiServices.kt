package com.example.smritisetu.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponseDto>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequestDto): Response<GenericApiResponseDto>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<GenericApiResponseDto>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequestDto): Response<GenericApiResponseDto>
}

interface UserApiService {
    @GET("user/profile")
    suspend fun getProfile(): Response<AuthResponseDto>

    @PUT("user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): Response<AuthResponseDto>

    @Multipart
    @POST("user/avatar/upload")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<AuthResponseDto>

    @POST("user/perks/use")
    suspend fun usePerk(@Body request: UsePerkRequestDto): Response<UsePerkResponseDto>

    @GET("user/reminders")
    suspend fun getOwnReminders(): Response<List<ReminderResponseDto>>
}

interface GameApiService {
    @POST("api/v1/games/match-card/level-complete")
    suspend fun completeMatchCardLevel(@Body request: LevelAttemptRequestDto): Response<LevelAttemptResponseDto>

    @POST("api/v1/games/pattern/level-complete")
    suspend fun completePatternLevel(@Body request: LevelAttemptRequestDto): Response<LevelAttemptResponseDto>

    @POST("game/level/complete")
    suspend fun completeLevel(@Body request: LevelAttemptRequestDto): Response<LevelAttemptResponseDto>
}

interface LeagueApiService {
    @GET("api/v1/league/status")
    suspend fun getLeagueStatus(): Response<LeagueStatusResponseDto>

    @GET("api/v1/league/leaderboard")
    suspend fun getLeaderboard(@Query("tier") tier: String = "Bronze Division"): Response<LeaderboardResponseDto>
}

interface ShopApiService {
    @GET("shop/items")
    suspend fun getShopItems(): Response<List<ShopItemDto>>

    @POST("shop/buy")
    suspend fun buyPerk(@Body request: BuyPerkRequestDto): Response<BuyPerkResponseDto>
}

interface CaregiverApiService {
    @POST("caregiver/link-by-code")
    suspend fun linkPatientByCode(@Body request: LinkByCodeRequestDto): Response<LinkByCodeResponseDto>

    @GET("caregiver/patient/summary")
    suspend fun getPatientSummary(): Response<CaregiverPatientSummaryDto>

    @GET("caregiver/patient/{patientId}/reminders")
    suspend fun getPatientReminders(@Path("patientId") patientId: String): Response<List<ReminderResponseDto>>

    @POST("caregiver/patient/{patientId}/reminders")
    suspend fun createReminder(@Path("patientId") patientId: String, @Body request: ReminderRequestDto): Response<ReminderResponseDto>

    @PUT("caregiver/patient/{patientId}/reminders/{reminderId}")
    suspend fun updateReminder(
        @Path("patientId") patientId: String,
        @Path("reminderId") reminderId: String,
        @Body request: ReminderRequestDto
    ): Response<ReminderResponseDto>

    @PATCH("caregiver/patient/{patientId}/reminders/{reminderId}/toggle")
    suspend fun toggleReminder(
        @Path("patientId") patientId: String,
        @Path("reminderId") reminderId: String
    ): Response<ReminderResponseDto>

    @DELETE("caregiver/patient/{patientId}/reminders/{reminderId}")
    suspend fun deleteReminder(
        @Path("patientId") patientId: String,
        @Path("reminderId") reminderId: String
    ): Response<Unit>
}
