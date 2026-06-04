package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.LoginPayload
import com.ubusmobilidade.ubus.data.model.*

class AuthRepository(private val api: ApiClient) {

    suspend fun login(payload: LoginPayload): LoginResponse =
        api.post("/auth/login", payload)

    suspend fun register(payload: RegisterPayload): User =
        api.post("/auth/register", payload)

    suspend fun requestPasswordRedefinition(email: String): Unit =
        api.post("/auth/password-redefinition/request", mapOf("email" to email))

    suspend fun resetPassword(payload: PasswordRedefinitionPayload): Unit =
        api.post("/auth/password-redefinition/reset", payload)

    suspend fun sendEmailCode(email: String, context: String): Unit =
        api.post("/auth/send-email-code", SendEmailCodePayload(email, context))

    suspend fun verifyEmailCode(email: String, code: String): VerifyEmailCodeResponse =
        api.post("/auth/verify-email-code", VerifyEmailCodePayload(email, code))
}
