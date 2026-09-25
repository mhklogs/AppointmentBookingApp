package com.example.appointmentapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.appointmentapp.data.model.Appointment
import java.util.Calendar

object ReminderScheduler {

    fun schedule(appointment: Appointment, context: Context) {
        val (dateMillis, slotMillis) = parseDateTime(appointment.date, appointment.timeSlot) ?: return
        val reminderTime = dateMillis + slotMillis - (60 * 60 * 1000L) // 1 hour before
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("EXTRA_SERVICE", appointment.serviceName)
            putExtra("EXTRA_DATE", appointment.date)
            putExtra("EXTRA_TIME", appointment.timeSlot)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

    fun cancel(appointment: Appointment, context: Context) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun parseDateTime(date: String, time: String): Pair<Long, Long>? {
        return try {
            val parts = date.split("-")
            val timeParts = time.split(":")
            val cal = Calendar.getInstance().apply {
                isLenient = false
                set(Calendar.YEAR, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val allDay = Calendar.getInstance().apply {
                isLenient = false
                set(Calendar.YEAR, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            Pair(allDay.timeInMillis, cal.timeInMillis - allDay.timeInMillis)
        } catch (e: Exception) {
            null
        }
    }
}