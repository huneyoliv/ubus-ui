package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.LoginPayload
import com.ubusmobilidade.ubus.data.model.*

class AuthRepository(private val api: ApiClient) {

    suspend fun login(payload: LoginPayload): LoginResponse {
        val response = api.post<LoginResponse>("/auth/login", payload)
        api.authStorage.token = response.accessToken
        api.authStorage.user = response.user
        return response
    }

    suspend fun register(payload: RegisterPayload): User =
        api.post("/auth/register", payload)

    suspend fun requestPasswordRedefinition(email: String): Unit =
        api.post(
            "/auth/send-verification-code",
            SendVerificationCodePayload(
                identifier = email,
                channel = VerificationChannel.EMAIL,
                context = VerificationContext.RESET_PASSWORD,
            )
        )

    suspend fun resetPassword(payload: PasswordRedefinitionPayload): Unit =
        api.post("/auth/password-redefinition", payload)

    suspend fun sendVerificationCode(
        identifier: String,
        channel: VerificationChannel,
        context: VerificationContext,
    ): Unit = api.post(
        "/auth/send-verification-code",
        SendVerificationCodePayload(identifier, channel, context)
    )

    suspend fun verifyCode(
        identifier: String,
        code: String,
        channel: VerificationChannel,
        context: VerificationContext,
    ): VerifyCodeResponse = api.post(
        "/auth/verify",
        VerifyCodePayload(identifier, code, channel, context)
    )
}
