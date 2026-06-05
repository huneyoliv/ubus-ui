@file:JvmName("MapWebViewAndroid")
package com.ubusmobilidade.ubus.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun MapWebView(
    lat: Double,
    lng: Double,
    points: List<MapPoint>,
    modifier: Modifier,
) {
    val html = buildLeafletHtml(lat, lng, points)
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}
