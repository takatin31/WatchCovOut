package com.example.watchcovout_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_notification.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationActivity : AppCompatActivity() {

    lateinit var adapter: NotificationAdapter
    lateinit var layoutManager : LinearLayoutManager
    val notificationsList = arrayListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        layoutManager = LinearLayoutManager(this)
        notificationRecycler.layoutManager = layoutManager

        adapter = NotificationAdapter(this, notificationsList)
        notificationRecycler.adapter = adapter


        getListNotifications()

        return_btn.setOnClickListener {
            finish()
        }
    }

    fun getListNotifications(){

        val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
        val nid = pref.getString("nid", "")
        val urlData = "${resources.getString(R.string.host)}/api/v0/users/$nid/notifications"

        // Request a string response from the provided URL.
        val jsonRequestData = JsonArrayRequest(
            Request.Method.GET, urlData, null,
            Response.Listener { response ->
                if (response.length() == 0){
                    Toast.makeText(this, "There is no notifications yet", Toast.LENGTH_SHORT).show()
                }else{
                    notificationsList.clear()
                    for (i in 0 until response.length()){
                        val item = response.getJSONObject(i)
                        val title = item.getString("title")
                        val date = item.getString("dateTime")
                        val content = item.getString("content")
                        val type = item.getString("type")
                        val visitTime = Notification(title, content,date, type)
                        notificationsList.add(visitTime)
                    }

                    adapter.notifyDataSetChanged()
                }
            },
            Response.ErrorListener {
                Log.d("Error", "Request error") })

        RequestHandler.getInstance(this).addToRequestQueue(jsonRequestData)

    }
}
