package com.example.peacemode

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val TASK_CHANNEL_ID = "task_channel"
        const val TASK_CHANNEL_NAME = "Task Notifications"
        const val TASK_CHANNEL_DESCRIPTION = "Notification channel for task reminders"

        const val SOS_CHANNEL_ID = "sos_channel"
        const val SOS_CHANNEL_NAME = "SOS Notifications"
        const val SOS_CHANNEL_DESCRIPTION = "Notification channel for SOS numbers"

        const val NOTIFICATION_ID = 1
        const val REQUEST_CODE_POST_NOTIFICATIONS = 1001
    }

    init {
        createNotificationChannel(TASK_CHANNEL_ID, TASK_CHANNEL_NAME, TASK_CHANNEL_DESCRIPTION)
        createNotificationChannel(SOS_CHANNEL_ID, SOS_CHANNEL_NAME, SOS_CHANNEL_DESCRIPTION)
    }

    private fun createNotificationChannel(
        channelId: String,
        channelName: String,
        channelDescription: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showTaskNotification(task: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
            return
        }

        val notification = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Replace with your app's icon
            .setContentTitle("Task Reminder")
            .setContentText(task)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
