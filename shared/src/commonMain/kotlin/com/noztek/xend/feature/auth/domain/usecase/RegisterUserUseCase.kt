package com.noztek.xend.feature.auth.domain.usecase

import com.noztek.xend.feature.auth.domain.model.RegisterParams
import com.noztek.xend.feature.auth.domain.model.RegisterResult
import com.noztek.xend.feature.auth.domain.repository.AuthRepository

class RegisterUserUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(params: RegisterParams): RegisterResult = repository.register(params)
}
