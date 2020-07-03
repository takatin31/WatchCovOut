package com.example.watchcovout_app


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action.equals("NEWNOTIFICATIONRECEIVED")){
            val titre = intent.getStringExtra("title")
            val content = intent.getStringExtra("content")


            val pIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    "ch00", "ch00", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(mChannel)
                val noti = Notification.Builder(context,"ch00")
                    .setContentTitle(titre)
                    .setContentText(content).setSmallIcon(android.R.drawable.btn_star)
                    .setContentIntent(pIntent).setAutoCancel(true)

                    .build()
                notificationManager.notify(0, noti)

            }else {

                val noti = Notification.Builder(context)
                    .setContentTitle(titre)
                    .setContentText(content).setSmallIcon(android.R.drawable.btn_star)
                    .setContentIntent(pIntent).setAutoCancel(true)

                    .build()
                notificationManager.notify(0, noti)

            }
        }


    }



}