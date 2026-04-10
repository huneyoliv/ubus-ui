package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.storage.AuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiError(
    val status: Int,
    val statusText: String,
    val body: String?,
) : Exception("API Error $status: $statusText")

class ApiClient(
    private val authStorage: AuthStorage,
    private val baseUrl: String = "https://api.ubus.me/v1",
    private val onUnauthorized: () -> Unit = {},
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(this@ApiClient.json)
        }
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            authStorage.token?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    private suspend fun handleResponse(response: HttpResponse): String {
        if (response.status.value == 401) {
            authStorage.clear()
            onUnauthorized()
            throw ApiError(401, "Unauthorized", null)
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw ApiError(response.status.value, response.status.description, text)
        }
        return text
    }

    suspend inline fun <reified T> get(path: String): T {
        val response = httpClient.get(path)
        checkResponse(response)
        return response.body()
    }

    suspend inline fun <reified T> post(path: String, body: Any? = null): T {
        val response = httpClient.post(path) {
            if (body != null) setBody(body)
        }
        checkResponse(response)
        return response.body()
    }

    suspend inline fun <reified T> patch(path: String, body: Any? = null): T {
        val response = httpClient.patch(path) {
            if (body != null) setBody(body)
        }
        checkResponse(response)
        return response.body()
    }

    suspend inline fun <reified T> delete(path: String): T {
        val response = httpClient.delete(path)
        checkResponse(response)
        return response.body()
    }

    @PublishedApi
    internal suspend fun checkResponse(response: HttpResponse) {
        handleResponse(response)
    }
}
