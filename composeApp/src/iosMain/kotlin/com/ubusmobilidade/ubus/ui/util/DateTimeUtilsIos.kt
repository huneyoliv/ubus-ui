package com.ubusmobilidade.ubus.ui.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.timeIntervalSinceReferenceDate
import platform.Foundation.timeIntervalSince1970

actual fun getTripDepartureMillis(tripDate: String, shift: String): Long {
    val parts = tripDate.split("-")
    if (parts.size != 3) return 0L
    val year = parts[0].toLongOrNull() ?: return 0L
    val month = parts[1].toLongOrNull() ?: return 0L
    val day = parts[2].toLongOrNull() ?: return 0L

    val (hour, minute) = when (shift) {
        "MORNING" -> Pair(6L, 30L)
        "AFTERNOON" -> Pair(12L, 0L)
        "NIGHT" -> Pair(18L, 0L)
        else -> Pair(6L, 30L)
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
