package com.barisgungorr.viewmodel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.barisgungorr.view.activity.MainActivity
import com.barisgungorr.notesapp.R
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MyWorkerNotifications (appContext: Context,workerParams: WorkerParameters): Worker(appContext,workerParams){

    private val quotes = listOf(
        "\"Başarı, başarısızlıkların ardından gelen sabrın sonucudur.\" - Albert Einstein",
        "\"Hayatta önemli olan düşmüş olmak değil, her düştüğünde yeniden ayağa kalkabilmektir.\" - Nelson Mandela",
        "\"Kendine inan ve yapabileceğini düşün, yarı yarıya başarının anahtarıdır.\" - Theodore Roosevelt",
        "\"Bir insanın en büyük zayıflığı pes etmeyi tercih etmesidir, en büyük gücü ise yeniden denemektir.\" - Steve Jobs",
        "\"Hedeflerinizi gerçekleştirecekseniz, yapmanız gereken tek şey harekete geçmektir.\" - Tony Robbins",
        "\"Hayatın anlamı, başkalarının hayatına dokunarak ve dünyayı daha iyi bir yer yaparak bulunur.\" - Maya Angelou",
        "\"Herkes bir gün ölecek, fakat asıl mesele yaşarken gerçekten yaşıyor olmak.\" - Charles Bukowski",
        "\"Bir insanın karakteri, zor zamanlarında gösterdiği tutumla belirlenir.\" - Martin Luther King Jr.",
        "\"Başarılı insanlar sorunlara odaklanmaz, çözümlere odaklanır.\" - Tony Robbins",
        "\"Değişimin kaynağı, kendinizdeki gücü bulabilmenizdir.\" - Oprah Winfrey"
    )

    override fun doWork(): Result {
        makeNotification()
        scheduleNextWork()
        return Result.success()
    }

    fun makeNotification() {
       val random = Random(System.currentTimeMillis())
        val randomQuote = quotes[random.nextInt(quotes.size)]

        val builder: NotificationCompat.Builder
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java)
        val goIntent = PendingIntent.getActivity(applicationContext,1,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(randomQuote)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chanelId = "kanalId"
            val chanelName = "kanalAdı"
            val chanelInfo = "kanalTanıtım"
            val chanelPriority = NotificationManager.IMPORTANCE_HIGH

            var chanel : NotificationChannel? = notificationManager.getNotificationChannel(chanelId)

            if (chanel == null) {
                chanel = NotificationChannel(chanelId,chanelName,chanelPriority)
                chanel.description = chanelInfo
                notificationManager.createNotificationChannel(chanel)
            }

            builder = NotificationCompat.Builder(applicationContext,chanelId)

            builder.setContentTitle("Notlarım")
                .setContentText((randomQuote))
                .setSmallIcon(R.drawable.push_icon)
                .setContentIntent(goIntent)
                .setAutoCancel(true)
                .setStyle(bigTextStyle)


        }else {

            builder = NotificationCompat.Builder(applicationContext)
            builder.setContentTitle("Notlarım")
                .setContentText(randomQuote)
                .setSmallIcon(R.drawable.push_icon)
                .setContentIntent(goIntent)
                .setAutoCancel(true)
                .setStyle(bigTextStyle)
                .priority = Notification.PRIORITY_HIGH
        }
        notificationManager.notify(1,builder.build())

    }
    private fun scheduleNextWork() {
        val nextWorkTime = System.currentTimeMillis() + 24 * 60 * 60 * 60 * 1000
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(false)
            .build()
        val nextWork = OneTimeWorkRequest.Builder(MyWorkerNotifications::class.java)
            .setInitialDelay(nextWorkTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "notificationWork",
            ExistingWorkPolicy.REPLACE,
            nextWork
        )
    }
}

