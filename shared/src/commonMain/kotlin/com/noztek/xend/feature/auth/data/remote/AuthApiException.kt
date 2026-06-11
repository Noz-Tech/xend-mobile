package com.noztek.xend.feature.auth.data.remote

class AuthApiException(
    val code: String?,
    override val message: String,
) : Exception(message)
