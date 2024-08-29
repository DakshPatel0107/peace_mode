package com.example.peacemode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val task = intent.getStringExtra("task") ?: return
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showTaskNotification(task)
    }
}
