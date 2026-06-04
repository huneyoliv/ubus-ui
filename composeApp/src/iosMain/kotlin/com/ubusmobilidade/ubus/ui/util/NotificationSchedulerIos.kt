package com.ubusmobilidade.ubus.ui.util

import com.ubusmobilidade.ubus.data.model.Reservation
import platform.UserNotifications.*
import platform.Foundation.NSDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSDateComponents
import platform.Foundation.timeIntervalSinceReferenceDate
import platform.Foundation.timeIntervalSinceNow

actual class NotificationScheduler actual constructor() {
    actual fun scheduleEmbarkAlert(reservation: Reservation, minutesBefore: Int) {
        val trip = reservation.trip ?: return
        val dateParts = trip.tripDate.split("-")
        if (dateParts.size != 3) return
        val year = dateParts[0].toLongOrNull() ?: return
        val month = dateParts[1].toLongOrNull() ?: return
        val day = dateParts[2].toLongOrNull() ?: return

        val isOutbound = trip.direction.name.uppercase() != "INBOUND"
        val (hour, minute) = when (trip.shift.uppercase()) {
            "MORNING", "MANHA" -> if (isOutbound) Pair(6L, 30L) else Pair(12L, 0L)
            "AFTERNOON", "TARDE" -> if (isOutbound) Pair(12L, 0L) else Pair(18L, 0L)
            "NIGHT", "NOITE" -> if (isOutbound) Pair(18L, 0L) else Pair(22L, 0L)
            else -> if (isOutbound) Pair(6L, 30L) else Pair(12L, 0L)
        }

        val components = NSDateComponents().apply {
            setYear(year)
            setMonth(month)
            setDay(day)
            setHour(hour)
            setMinute(minute)
        }

        val calendar = NSCalendar.currentCalendar
        val departureDate = calendar.dateFromComponents(components) ?: return
        val triggerTimeInterval = - (minutesBefore.toDouble() * 60.0)
        val triggerDate = NSDate(departureDate.timeIntervalSinceReferenceDate + triggerTimeInterval)

        if (triggerDate.timeIntervalSinceNow <= 0.0) return

        val triggerComponents = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute,
            fromDate = triggerDate
        )

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            triggerComponents,
            repeats = false
        )

        val content = UNMutableNotificationContent().apply {
            setTitle("Embarque ${if (trip.shift == "MORNING") "Matutino" else if (trip.shift == "AFTERNOON") "Vespertino" else "Noturno"}")
            setBody("Seu ônibus está agendado para partir em $minutesBefore minutos! Prepare seu bilhete.")
            setSound(UNNotificationSound.defaultSound)
        }

        val notificationId = "${reservation.id}_$minutesBefore"
        val request = UNNotificationRequest.requestWithIdentifier(
            notificationId,
            content,
            trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ -> }
    }

    actual fun cancelAlerts(reservationId: String) {
        val identifiers = listOf("${reservationId}_30", "${reservationId}_60")
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(identifiers)
    }
}
