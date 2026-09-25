package com.example.appointmentapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.appointmentapp.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceName = intent.getStringExtra("EXTRA_SERVICE") ?: "your appointment"
        val date = intent.getStringExtra("EXTRA_DATE") ?: ""
        val time = intent.getStringExtra("EXTRA_TIME") ?: ""

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "appointment_reminders"
        val channel = NotificationChannel(
            channelId,
            "Appointment Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Reminders for upcoming appointments" }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming appointment: $serviceName")
            .setContentText("$date at $time")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Your appointment for $serviceName is scheduled for $date at $time."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(intent.hashCode(), notification)
    }
}