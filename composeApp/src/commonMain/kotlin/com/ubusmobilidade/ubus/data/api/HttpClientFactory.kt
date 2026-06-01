package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.storage.AuthStorage
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

expect fun buildHttpClient(json: Json, authStorage: AuthStorage): HttpClient
