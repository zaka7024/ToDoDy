package com.zaka7024.todody.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zaka7024.todody.MainActivity

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        NotificationHelper.sendNotification(context, notificationIntent, "Hello, World", "Hello, World Again")
    }
}