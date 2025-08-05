package com.example.app_my_university.data.repository

import com.example.app_my_university.data.api.UniversityApiService
import com.example.app_my_university.data.api.model.AcademicGroupDTO
import com.example.app_my_university.data.api.model.AuthRequest
import com.example.app_my_university.data.api.model.AuthResponse
import com.example.app_my_university.data.api.model.InstituteDTO
import com.example.app_my_university.data.api.model.LoginResponse
import com.example.app_my_university.data.api.model.RegistrationRequest
import com.example.app_my_university.data.api.model.RegistrationResponse
import com.example.app_my_university.data.api.model.ChangePasswordRequest
import com.example.app_my_university.data.api.model.ChangePasswordResponse
import com.example.app_my_university.data.api.model.ProfileResponse
import com.example.app_my_university.data.api.model.StudyDirectionDTO
import com.example.app_my_university.data.api.model.SubjectDTO
import com.example.app_my_university.data.api.model.UpdateProfileRequest
import com.example.app_my_university.data.api.model.UniversityDTO
import com.example.app_my_university.data.api.model.UserProfile
import com.example.app_my_university.model.University
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UniversityRepository @Inject constructor(
    private val apiService: UniversityApiService
) {
    suspend fun getUniversities(): Flow<Result<List<UniversityDTO>>> = flow {
        try {
            val response = apiService.getUniversities()
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getInstitutes(universityId: Int): Flow<Result<List<InstituteDTO>>> = flow {
        try {
            val response = apiService.getInstitutes(universityId)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getDirections(instituteId: Int): Flow<Result<List<StudyDirectionDTO>>> = flow {
        try {
            val response = apiService.getDirections(instituteId)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getGroups(directionId: Int): Flow<Result<List<AcademicGroupDTO>>> = flow {
        try {
            val response = apiService.getGroups(directionId)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getGroupsByCourse(directionId: Int, course: Int): Flow<Result<List<AcademicGroupDTO>>> = flow {
        try {
            val response = apiService.getGroupsByCourse(directionId, course)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getAllSubjects(): Flow<Result<List<SubjectDTO>>> = flow {
        try {
            val response = apiService.getAllSubjects()
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getSubjectsByUniversity(universityId: Int): Flow<Result<List<SubjectDTO>>> = flow {
        try {
            val response = apiService.getSubjectsByUniversity(universityId)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun searchSubjects(query: String): Flow<Result<List<SubjectDTO>>> = flow {
        try {
            val response = apiService.searchSubjects(query)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun registerUser(request: RegistrationRequest): Flow<Result<RegistrationResponse>> = flow {
        try {
            val response = apiService.registerUser(request)
            if (response.success) {
                emit(Result.success(response))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun registerUserAlternative(request: RegistrationRequest): Flow<Result<RegistrationResponse>> = flow {
        try {
            val response = apiService.registerUserAlternative(request)
            if (response.success) {
                emit(Result.success(response))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun login(request: AuthRequest): Flow<Result<AuthResponse>> = flow {
        try {
            val response = apiService.login(request)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Методы для работы с профилем пользователя
    suspend fun getUserProfile(): Flow<Result<UserProfile>> = flow {
        try {
            val response = apiService.getUserProfile()
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun updateUserProfile(request: UpdateProfileRequest): Flow<Result<UserProfile>> = flow {
        try {
            val response = apiService.updateUserProfile(request)
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Flow<Result<ChangePasswordResponse>> = flow {
        try {
            val response = apiService.changePassword(request)
            if (response.success) {
                emit(Result.success(response))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 