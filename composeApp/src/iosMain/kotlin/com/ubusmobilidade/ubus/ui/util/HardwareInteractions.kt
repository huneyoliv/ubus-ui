package com.ubusmobilidade.ubus.ui.util

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy

@Composable
actual fun rememberFilePickerLauncher(onResult: (String?) -> Unit): () -> Unit = {}

@Composable
actual fun rememberCameraLauncher(onResult: (String?) -> Unit): () -> Unit = {}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun readFileBytes(uri: String): ByteArray {
    val url = NSURL.URLWithString(uri) ?: throw IllegalStateException("URI inválida")
    val data = NSData.dataWithContentsOfURL(url) ?: throw IllegalStateException("Não foi possível ler o arquivo.")
    val bytes = ByteArray(data.length.toInt())
    if (bytes.isNotEmpty()) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
    }
    return bytes
}

actual fun getFileNameFromUri(uri: String): String =
    NSURL.URLWithString(uri)?.lastPathComponent ?: "arquivo"

actual fun getCurrentTimeMillis(): Long = (platform.Foundation.NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun getTodayDateString(): String {
    val date = platform.Foundation.NSDate()
    val calendar = platform.Foundation.NSCalendar.currentCalendar
    val components = calendar.components(
        platform.Foundation.NSCalendarUnitYear or platform.Foundation.NSCalendarUnitMonth or platform.Foundation.NSCalendarUnitDay,
        fromDate = date
    )
    val year = components.year
    val month = components.month
    val day = components.day
    val monthStr = if (month < 10) "0$month" else "$month"
    val dayStr = if (day < 10) "0$day" else "$day"
    return "$year-$monthStr-$dayStr"
}
