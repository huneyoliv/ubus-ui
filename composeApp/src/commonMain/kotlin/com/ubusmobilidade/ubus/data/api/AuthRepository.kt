package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.LoginPayload
import com.ubusmobilidade.ubus.data.model.LoginResponse
import com.ubusmobilidade.ubus.data.model.PasswordRedefinitionPayload
import com.ubusmobilidade.ubus.data.model.RegisterPayload
import com.ubusmobilidade.ubus.data.model.User

class AuthRepository(private val api: ApiClient) {

    /** POST /auth/login */
    suspend fun login(email: String, password: String): LoginResponse {
        return api.post("/auth/login", LoginPayload(email, password))
    }

    /** POST /auth/register */
    suspend fun register(payload: RegisterPayload): User {
        return api.post("/auth/register", payload)
    }

    /** POST /auth/password-email-send — requires auth token */
    suspend fun sendPasswordResetEmail(email: String) {
        api.post<String>("/auth/password-email-send", mapOf("email" to email))
    }

    /** POST /auth/password-redefinition — public, with token from email */
    suspend fun resetPassword(token: String, newPassword: String) {
        api.post<String>(
            "/auth/password-redefinition",
            PasswordRedefinitionPayload(token, newPassword)
        )
    }
}
