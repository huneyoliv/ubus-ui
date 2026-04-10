package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.LoginPayload
import com.ubusmobilidade.ubus.data.model.LoginResponse
import com.ubusmobilidade.ubus.data.model.RegisterPayload
import com.ubusmobilidade.ubus.data.model.User

class AuthRepository(private val api: ApiClient) {

    suspend fun login(email: String, password: String): LoginResponse {
        return api.post("/auth/login", LoginPayload(email, password))
    }

    suspend fun register(payload: RegisterPayload): User {
        return api.post("/auth/register", payload)
    }

    suspend fun requestPasswordReset(email: String) {
        api.post<String>("/auth/reset-password", mapOf("email" to email))
    }
}
