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
        val customDepartureTime = if (trip.direction.name.uppercase() == "OUTBOUND") {
            trip.route?.departureTimeOutbound
        } else {
            trip.route?.departureTimeInbound
        }
        val departureTime = getTripDepartureMillis(trip.tripDate, trip.shift, trip.direction.name, customDepartureTime)
        if (departureTime == 0L) return

        val secondsSince2001 = (departureTime / 1000.0) - 978307200.0
        val triggerTimeInterval = - (minutesBefore.toDouble() * 60.0)
        val triggerDate = NSDate(timeIntervalSinceReferenceDate = secondsSince2001 + triggerTimeInterval)

        if (triggerDate.timeIntervalSinceNow <= 0.0) return

        val triggerComponents = NSCalendar.currentCalendar.components(
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
