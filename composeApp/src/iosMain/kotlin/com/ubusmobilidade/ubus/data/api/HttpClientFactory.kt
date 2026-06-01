package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.storage.AuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun buildHttpClient(json: Json, authStorage: AuthStorage): HttpClient {
    return HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            authStorage.token?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }
}
