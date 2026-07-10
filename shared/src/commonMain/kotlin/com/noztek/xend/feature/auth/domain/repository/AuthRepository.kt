package com.noztek.xend.feature.auth.domain.repository

import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.auth.domain.model.LoginParams
import com.noztek.xend.feature.auth.domain.model.RegisterParams
import com.noztek.xend.feature.auth.domain.model.RegisterResult
import com.noztek.xend.feature.auth.domain.model.UserProfileModel

interface AuthRepository {
    suspend fun register(params: RegisterParams): RegisterResult
    suspend fun verifyEmail(email: String, token: String)
    suspend fun resendVerification(email: String)
    suspend fun login(params: LoginParams): AuthSessionModel
    suspend fun refresh(): AuthSessionModel
    suspend fun logout()
    suspend fun getCurrentSession(): AuthSessionModel?
    suspend fun getCurrentUserProfile(): UserProfileModel?
    suspend fun hasAnyLocalUserProfile(): Boolean
}
