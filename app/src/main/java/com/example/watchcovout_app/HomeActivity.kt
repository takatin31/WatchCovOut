package com.example.watchcovout_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class HomeActivity : AppCompatActivity() {

    var chosenPlace : Place? = null
    var chosenTime = ""
    var selectedDate : String = ""
    var page2 : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
        val userImgUrl = pref.getString("photoUrl", "")

        if (userImgUrl != ""){
            Picasso.get().load(userImgUrl).into(`userImageٍView`)
        }

        `userImageٍView`.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            flouLayout.visibility = View.VISIBLE
            dialogLayout.visibility = View.VISIBLE

            var contentFr: Fragment =
                MapFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, contentFr)
            transaction.commit()

            continue_btn.setOnClickListener {
                if (chosenPlace == null){
                    Toast.makeText(this, "Please choose a place first", Toast.LENGTH_SHORT).show()
                }else{
                    if (!page2){
                        page2 = true
                        var contentFr: Fragment =
                            TimeFragment()

                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frameLayout, contentFr)
                        transaction.commit()

                    }else{
                        if (chosenTime == "" || selectedDate == ""){
                            Toast.makeText(this, "Please choose a timing first", Toast.LENGTH_SHORT).show()
                        }else{

                            flouLayout.visibility = View.GONE
                            dialogLayout.visibility = View.GONE
                            layout1.visibility = View.GONE
                            layout2.visibility = View.VISIBLE
                            destinationInfo.text = chosenPlace!!.title
                            timeInfo.text = chosenTime
                        }
                    }
                }
            }

            confirm_btn.setOnClickListener {
                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                val nid = pref.getString("nid", "")
                if (nid != ""){
                    visitPlace(selectedDate, chosenPlace!!.id, chosenTime, nid)
                }else{
                    Toast.makeText(this, "There were a problem please retry", Toast.LENGTH_SHORT).show()
                }
            }

        }

        notificationIconView.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }

    fun visitPlace(
        selectedDate: String,
        chosenPlaceId: String,
        time: String,
        nid: String?
    ) {
        var postURL: String = "${resources.getString(R.string.host)}/api/v0/places/$chosenPlaceId/users"
        //var postURL = "https://ptsv2.com/t/7a3sq-1593610683/post"


        val request = object : FileUploadRequest(
            Method.PUT,
            postURL,
            Response.Listener {
                Log.i("success", "comment posted succefully")
                val resp = String(it.data)
                val jsonResp = JSONObject(resp)
                if (jsonResp.has("error")){
                    Toast.makeText(this, "There were a problem please retry", Toast.LENGTH_SHORT).show()

                }else if (jsonResp.has("msg")){

                    Toast.makeText(this, "The request was succefull", Toast.LENGTH_SHORT).show()
                }

            },
            Response.ErrorListener {
                Toast.makeText(this, "There were a problem", Toast.LENGTH_SHORT).show()
                Log.i("error", "error while posting comment")
                Log.i("error", it.toString())
            }
        ){

            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getBody(): ByteArray {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val dataOutputStream = DataOutputStream(byteArrayOutputStream)
                val jsonUser = JSONObject()
                jsonUser.put("nid", nid)
                jsonUser.put("date", selectedDate)
                jsonUser.put("time", time)
                dataOutputStream.writeBytes(jsonUser.toString())
                return byteArrayOutputStream.toByteArray()
            }
            /*
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                val token = pref.getString("token", "")
                val headers = java.util.HashMap<String, String>()
                headers.put("Authorization", "Bearer $token")
                return headers
            }*/
        }
        Volley.newRequestQueue(this).add(request)
    }

    override fun onBackPressed() {
        flouLayout.visibility = View.GONE
        dialogLayout.visibility = View.GONE
    }

}
