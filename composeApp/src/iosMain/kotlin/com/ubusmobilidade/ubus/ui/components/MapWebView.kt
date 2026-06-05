package com.ubusmobilidade.ubus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapWebView(
    lat: Double,
    lng: Double,
    points: List<MapPoint>,
    modifier: Modifier,
) {
    val html = buildLeafletHtml(lat, lng, points)
    UIKitView(
        factory = {
            WKWebView().apply {
                loadHTMLString(html, baseURL = null)
            }
        },
        update = { webView ->
            webView.loadHTMLString(html, baseURL = null)
        },
        modifier = modifier
    )
}
