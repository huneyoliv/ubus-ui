package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.storage.AuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
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
    internal val authStorage: AuthStorage,
    private val baseUrl: String = "https://api.ubus.me/v1",
    private val onUnauthorized: () -> Unit = {},
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        coerceInputValues = true
    }

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(this@ApiClient.json)
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            authStorage.token?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    @PublishedApi
    internal fun fullUrl(path: String): String = "$baseUrl$path"

    @PublishedApi
    internal suspend fun handleResponse(response: HttpResponse) {
        if (response.status.value == 401) {
            authStorage.clear()
            onUnauthorized()
            throw ApiError(401, "Unauthorized", null)
        }
        if (!response.status.isSuccess()) {
            val text = response.bodyAsText()
            throw ApiError(response.status.value, response.status.description, text)
        }
    }
    suspend inline fun <reified T> get(path: String, params: Map<String, String>? = null): T {
        val response = httpClient.get(fullUrl(path)) {
            params?.forEach { (k, v) -> parameter(k, v) }
        }
        handleResponse(response)
        return response.body()
    }

    suspend inline fun <reified T> post(path: String, body: Any? = null): T {
        val response = httpClient.post(fullUrl(path)) {
            if (body != null) setBody(body)
        }
        handleResponse(response)
        return if (T::class == Unit::class) Unit as T
        else if (T::class == String::class) response.bodyAsText() as T
        else response.body()
    }

    suspend inline fun <reified T> patch(path: String, body: Any? = null): T {
        val response = httpClient.patch(fullUrl(path)) {
            if (body != null) setBody(body)
        }
        handleResponse(response)
        return if (T::class == Unit::class) Unit as T
        else if (T::class == String::class) response.bodyAsText() as T
        else response.body()
    }

    suspend inline fun <reified T> delete(path: String): T {
        val response = httpClient.delete(fullUrl(path))
        handleResponse(response)
        return if (T::class == Unit::class) Unit as T else response.body()
    }
}
