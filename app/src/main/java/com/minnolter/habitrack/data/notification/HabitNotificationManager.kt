package com.minnolter.habitrack.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.minnolter.habitrack.MainActivity

class HabitNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "habitract_duolingo_reminders"
        const val CHANNEL_NAME = "Daily Practice Reminders"
        const val NOTIFICATION_ID = 2026

        val DUOLINGO_PROMPTS = listOf(
            "Did you log your practice hours today, or are you letting your dreams slip away? 🦉",
            "Your 10,000-hour journey is waiting for you... don't break the momentum! 🔥",
            "Habitrack is missing you! Just 15 minutes today keeps the streak alive.",
            "Zero hours today? Even 10 minutes counts towards Mastery!",
            "Don't make your habits cry... log a session today! 😢",
            "Small daily gains stack into 10,000 hours of greatness. Log time today!",
            "Consistency beats intensity! Take 15 minutes to sharpen your craft today. ✨"
        )
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Playful daily Duolingo-style practice prompts"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendDuolingoPrompt() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prompt = DUOLINGO_PROMPTS.random()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Habitrack")
            .setContentText(prompt)
            .setStyle(NotificationCompat.BigTextStyle().bigText(prompt))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
