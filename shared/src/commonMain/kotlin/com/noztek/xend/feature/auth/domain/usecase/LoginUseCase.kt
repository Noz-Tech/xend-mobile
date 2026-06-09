package com.noztek.xend.feature.auth.domain.usecase

import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.auth.domain.model.LoginParams
import com.noztek.xend.feature.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(params: LoginParams): AuthSessionModel = repository.login(params)
}
