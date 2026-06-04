package com.ubusmobilidade.ubus.ui.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ubusmobilidade.ubus.AndroidApp
import com.ubusmobilidade.ubus.data.model.Reservation
import java.util.Calendar

actual class NotificationScheduler actual constructor() {
    private val context = AndroidApp.context
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    actual fun scheduleEmbarkAlert(reservation: Reservation, minutesBefore: Int) {
        val trip = reservation.trip ?: return
        val dateParts = trip.tripDate.split("-")
        if (dateParts.size != 3) return
        val year = dateParts[0].toIntOrNull() ?: return
        val month = dateParts[1].toIntOrNull()?.minus(1) ?: return
        val day = dateParts[2].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            val isOutbound = trip.direction.name.uppercase() != "INBOUND"
            val (hour, minute) = when (trip.shift.uppercase()) {
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

        val departureTime = calendar.timeInMillis
        val triggerTime = departureTime - (minutesBefore * 60 * 1000)

        if (triggerTime <= System.currentTimeMillis()) return

        val notificationId = reservation.id.hashCode() + minutesBefore
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Embarque ${if (trip.shift == "MORNING") "Matutino" else if (trip.shift == "AFTERNOON") "Vespertino" else "Noturno"}")
            putExtra("message", "Seu ônibus está agendado para partir em $minutesBefore minutos! Prepare seu bilhete.")
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    actual fun cancelAlerts(reservationId: String) {
        val minutesArray = intArrayOf(30, 60)
        for (minutes in minutesArray) {
            val notificationId = reservationId.hashCode() + minutes
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
