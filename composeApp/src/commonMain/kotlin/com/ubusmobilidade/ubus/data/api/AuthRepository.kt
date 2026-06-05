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
        api.post("/auth/password-email-send")

    suspend fun resetPassword(payload: PasswordRedefinitionPayload): Unit =
        api.post("/auth/password-redefinition", payload)

    suspend fun sendEmailCode(email: String, context: String): Unit =
        api.post("/auth/send-email-code", SendEmailCodePayload(email, context))

    suspend fun verifyEmailCode(email: String, code: String): VerifyEmailCodeResponse =
        api.post("/auth/verify-email-code", VerifyEmailCodePayload(email, code))
}
