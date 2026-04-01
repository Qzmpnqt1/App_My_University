package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.api.model.ChangeEmailRequest
import com.example.app_my_university.data.api.model.ChangePasswordRequest
import com.example.app_my_university.data.api.model.UpdatePersonalProfileRequest
import com.example.app_my_university.data.api.model.UserProfileResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getProfile(): Result<UserProfileResponse> = safeApiCall { apiService.getProfile() }

    suspend fun getProfileMe(): Result<UserProfileResponse> = safeApiCall { apiService.getProfileMe() }

    suspend fun updatePersonalProfile(firstName: String, lastName: String, middleName: String?): Result<Unit> =
        safeApiCall { apiService.updatePersonalProfile(UpdatePersonalProfileRequest(firstName, lastName, middleName)) }

    suspend fun changeEmail(newEmail: String, currentPassword: String): Result<Unit> =
        safeApiCall { apiService.changeEmail(ChangeEmailRequest(newEmail, currentPassword)) }

    suspend fun changePassword(oldPassword: String, newPassword: String, newPasswordConfirm: String): Result<Unit> =
        safeApiCall {
            apiService.changePassword(
                ChangePasswordRequest(oldPassword, newPassword, newPasswordConfirm)
            )
        }
}
