package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.LoginPayload
import com.ubusmobilidade.ubus.data.model.*

class AuthRepository(private val api: ApiClient) {

    suspend fun login(payload: LoginPayload): LoginResponse {
        println("DEBUG: AuthRepository - login for email: ${payload.email}")
        return api.post("/auth/login", payload)
    }

    suspend fun register(payload: RegisterPayload): User {
        println("DEBUG: AuthRepository - register for email: ${payload.email}")
        return api.post("/auth/register", payload)
    }

    suspend fun requestPasswordRedefinition(email: String) {
        println("DEBUG: AuthRepository - requestPasswordRedefinition for: $email")
        api.post<String>("/auth/password-redefinition/request", mapOf("email" to email))
    }

    suspend fun resetPassword(payload: PasswordRedefinitionPayload) {
        println("DEBUG: AuthRepository - resetPassword")
        api.post<String>("/auth/password-redefinition/reset", payload)
    }

    suspend fun sendEmailCode(email: String, context: String) {
        println("DEBUG: AuthRepository - sendEmailCode ($context) to: $email")
        api.post<String>("/auth/send-email-code", SendEmailCodePayload(email, context))
    }

    suspend fun verifyEmailCode(email: String, code: String): VerifyEmailCodeResponse {
        println("DEBUG: AuthRepository - verifyEmailCode for: $email")
        return api.post("/auth/verify-email-code", VerifyEmailCodePayload(email, code))
    }
}
