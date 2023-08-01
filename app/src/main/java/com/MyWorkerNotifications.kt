package com

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.barisgungorr.notesapp.Activity.MainActivity
import com.barisgungorr.notesapp.R
import java.util.concurrent.TimeUnit

class MyWorkerNotifications (appContext: Context,workerParams: WorkerParameters): Worker(appContext,workerParams){
    override fun doWork(): Result {
        makeNotification()
        return Result.success()
    }

    fun makeNotification() {
        val builder: NotificationCompat.Builder
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext,MainActivity::class.java)
        val goIntent = PendingIntent.getActivity(applicationContext,1,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


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
                .setContentText("Not almayı unutmayın :):( ")
                .setSmallIcon(R.drawable.notesss)
                .setContentIntent(goIntent)
                .setAutoCancel(true)


        }else {

            builder = NotificationCompat.Builder(applicationContext)
            builder.setContentTitle("Notlarım")
                .setContentText("Bir süredir not almadınız :( ")
                .setSmallIcon(R.drawable.notesss)
                .setContentIntent(goIntent)
                .setAutoCancel(true)
                .priority = Notification.PRIORITY_HIGH
        }
        notificationManager.notify(1,builder.build())

    }
}

