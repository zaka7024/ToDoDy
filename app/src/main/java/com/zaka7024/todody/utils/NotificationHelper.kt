package com.zaka7024.todody.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zaka7024.todody.R

class NotificationHelper {

    companion object {
        fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean,
                                      name: String, description: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "${context.packageName}-$name"
                val channel = NotificationChannel(channelId, name, importance)
                channel.description = description
                channel.setShowBadge(showBadge)
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun sendNotification(context: Context, intent: Intent, title: String, message: String) {
            val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
            val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
                setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                setContentTitle(title)
                setContentText(message)
                setStyle(NotificationCompat.BigTextStyle())
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(true)
                setContentIntent(PendingIntent.getActivity(context,0,intent,0))
            }

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, notificationBuilder.build())
        }
    }
}