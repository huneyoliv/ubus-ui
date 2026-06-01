package com.ubusmobilidade.ubus.data.storage

import com.russhwolf.settings.Settings
import com.ubusmobilidade.ubus.data.model.Reservation
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalTicketStorage {
    private val settings = Settings()
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    var latestTicket: Reservation?
        get() {
            val raw = settings.getStringOrNull(KEY_LATEST_TICKET) ?: return null
            return try {
                json.decodeFromString<Reservation>(raw)
            } catch (_: SerializationException) {
                settings.remove(KEY_LATEST_TICKET)
                null
            }
        }
        set(value) {
            if (value != null) settings.putString(KEY_LATEST_TICKET, json.encodeToString(value))
            else settings.remove(KEY_LATEST_TICKET)
        }

    fun clear() {
        settings.remove(KEY_LATEST_TICKET)
    }

    companion object {
        private const val KEY_LATEST_TICKET = "ubus_latest_ticket"
    }
}
