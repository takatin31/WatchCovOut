package com.example.watchcovout_app


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView




class NotificationAdapter (val activity: FragmentActivity, val listNotifications : ArrayList<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    class NotificationViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var NotificationTitle = v.findViewById<TextView>(R.id.notificationTitleView)
        var NotificationSeen = v.findViewById<ImageView>(R.id.notificationSeenView)
        var NotificationDate = v.findViewById<TextView>(R.id.notificationDateView)
        var NotificationContent = v.findViewById<TextView>(R.id.notificationContentView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            LayoutInflater.from(activity).inflate(R.layout.notification_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listNotifications.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = listNotifications[position]
        holder.NotificationContent.text = notification.contenu
        holder.NotificationTitle.text = notification.title
        holder.NotificationDate.text = notification.date


    }


}