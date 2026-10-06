package com.example.novaplustrader.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.novaplustrader.R

class NovaNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_TRANSACTIONS = "channel_transactions"
        const val CHANNEL_TRADING = "channel_trading"
        const val CHANNEL_TRADING_BOT = "channel_trading_bot"
        const val CHANNEL_SECURITY = "channel_security"
        const val CHANNEL_SYSTEM = "channel_system"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TRANSACTIONS,
                    "Transactions & Wallet",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Deposit, withdrawal, and transfer confirmations" },

                NotificationChannel(
                    CHANNEL_TRADING,
                    "Trading Execution",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Option trade settlements and market updates" },

                NotificationChannel(
                    CHANNEL_TRADING_BOT,
                    "Trading Bot Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Automated bot strategy signals and execution events" },

                NotificationChannel(
                    CHANNEL_SECURITY,
                    "Security & Authentication",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Login alerts, password changes, and security events" },

                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "System & Announcements",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Platform updates, maintenance, and system news" }
            )

            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    @SuppressLint("MissingPermission")
    fun notifyEvent(
        title: String,
        message: String,
        channelId: String = CHANNEL_SYSTEM,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                "android.permission.POST_NOTIFICATIONS"
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission denied
        }
    }

    fun notifyBotEvent(title: String, message: String, notificationId: Int = 101) {
        notifyEvent(title, message, CHANNEL_TRADING_BOT, notificationId)
    }
}
