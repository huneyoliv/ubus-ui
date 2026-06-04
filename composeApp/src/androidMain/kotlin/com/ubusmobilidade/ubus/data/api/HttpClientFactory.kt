package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.storage.AuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun buildHttpClient(json: Json, authStorage: AuthStorage): HttpClient {
    val trustAllCerts = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustAllCerts), SecureRandom())
    }

    return HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            authStorage.token?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
        engine {
            config {
                sslSocketFactory(sslContext.socketFactory, trustAllCerts)
                hostnameVerifier { _, _ -> true }
            }
        }
    }
}
