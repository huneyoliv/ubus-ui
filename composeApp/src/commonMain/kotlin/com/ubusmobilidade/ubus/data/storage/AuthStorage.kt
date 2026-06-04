package com.ubusmobilidade.ubus.data.storage

import com.russhwolf.settings.Settings
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.User
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthStorage {
    private val settings = Settings()
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    var token: String?
        get() = settings.getStringOrNull(KEY_TOKEN)
        set(value) {
            if (value != null) settings.putString(KEY_TOKEN, value)
            else settings.remove(KEY_TOKEN)
        }

    var user: User?
        get() {
            val raw = settings.getStringOrNull(KEY_USER) ?: return null
            return try {
                json.decodeFromString<User>(raw)
            } catch (_: SerializationException) {
                settings.remove(KEY_USER)
                null
            }
        }
        set(value) {
            if (value != null) settings.putString(KEY_USER, json.encodeToString(value))
            else settings.remove(KEY_USER)
        }

    val isAuthenticated: Boolean get() = token != null && user != null

    val userRole: RoleUsuario? get() = user?.role

    fun setAuth(token: String, user: User) {
        this.token = token
        this.user = user
    }

    fun clear() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER)
    }

    companion object {
        private const val KEY_TOKEN = "ubus_auth_token"
        private const val KEY_USER = "ubus_auth_user"
    }
}
