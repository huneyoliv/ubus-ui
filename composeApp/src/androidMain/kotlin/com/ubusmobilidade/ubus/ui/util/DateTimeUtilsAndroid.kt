package com.ubusmobilidade.ubus.ui.util

import java.util.Calendar

actual fun getTripDepartureMillis(tripDate: String, shift: String, direction: String): Long {
    val parts = tripDate.split("-")
    if (parts.size != 3) return 0L
    val year = parts[0].toIntOrNull() ?: return 0L
    val month = parts[1].toIntOrNull()?.minus(1) ?: return 0L
    val day = parts[2].toIntOrNull() ?: return 0L

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        val isOutbound = direction.uppercase() != "INBOUND"
        val (hour, minute) = when (shift.uppercase()) {
            "MORNING", "MANHA" -> if (isOutbound) Pair(6, 30) else Pair(12, 0)
            "AFTERNOON", "TARDE" -> if (isOutbound) Pair(12, 0) else Pair(18, 0)
            "NIGHT", "NOITE" -> if (isOutbound) Pair(18, 0) else Pair(22, 0)
            else -> if (isOutbound) Pair(6, 30) else Pair(12, 0)
        }
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return calendar.timeInMillis
}

actual fun isToday(dateString: String): Boolean {
    val parts = dateString.split("-")
    if (parts.size != 3) return false
    val year = parts[0].toIntOrNull() ?: return false
    val month = parts[1].toIntOrNull()?.minus(1) ?: return false
    val day = parts[2].toIntOrNull() ?: return false

    val today = Calendar.getInstance()
    return today.get(Calendar.YEAR) == year &&
           today.get(Calendar.MONTH) == month &&
           today.get(Calendar.DAY_OF_MONTH) == day
}
