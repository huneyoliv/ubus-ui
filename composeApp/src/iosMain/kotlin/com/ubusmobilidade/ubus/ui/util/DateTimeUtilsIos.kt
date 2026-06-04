package com.ubusmobilidade.ubus.ui.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.timeIntervalSinceReferenceDate
import platform.Foundation.timeIntervalSince1970

actual fun getTripDepartureMillis(tripDate: String, shift: String, direction: String, customDepartureTime: String?): Long {
    val parts = tripDate.split("-")
    if (parts.size != 3) return 0L
    val year = parts[0].toLongOrNull() ?: return 0L
    val month = parts[1].toLongOrNull() ?: return 0L
    val day = parts[2].toLongOrNull() ?: return 0L

    val (hour, minute) = if (!customDepartureTime.isNullOrEmpty() && customDepartureTime.contains(":")) {
        val timeParts = customDepartureTime.split(":")
        val h = timeParts.getOrNull(0)?.toLongOrNull()
        val m = timeParts.getOrNull(1)?.toLongOrNull()
        if (h != null && m != null) Pair(h, m) else null
    } else {
        null
    } ?: run {
        val isOutbound = direction.uppercase() != "INBOUND"
        when (shift.uppercase()) {
            "MORNING", "MANHA" -> if (isOutbound) Pair(6L, 30L) else Pair(12L, 0L)
            "AFTERNOON", "TARDE" -> if (isOutbound) Pair(12L, 0L) else Pair(18L, 0L)
            "NIGHT", "NOITE" -> if (isOutbound) Pair(18L, 0L) else Pair(22L, 0L)
            else -> if (isOutbound) Pair(6L, 30L) else Pair(12L, 0L)
        }
    }

    val components = NSDateComponents().apply {
        setYear(year)
        setMonth(month)
        setDay(day)
        setHour(hour)
        setMinute(minute)
    }

    val calendar = NSCalendar.currentCalendar
    val date = calendar.dateFromComponents(components) ?: return 0L
    return (date.timeIntervalSince1970 * 1000.0).toLong()
}

actual fun isToday(dateString: String): Boolean {
    val parts = dateString.split("-")
    if (parts.size != 3) return false
    val year = parts[0].toLongOrNull() ?: return false
    val month = parts[1].toLongOrNull() ?: return false
    val day = parts[2].toLongOrNull() ?: return false

    val calendar = NSCalendar.currentCalendar
    val today = platform.Foundation.NSDate()
    val todayComponents = calendar.components(
        platform.Foundation.NSCalendarUnitYear or
        platform.Foundation.NSCalendarUnitMonth or
        platform.Foundation.NSCalendarUnitDay,
        fromDate = today
    )

    return todayComponents.year == year &&
           todayComponents.month == month &&
           todayComponents.day == day
}
