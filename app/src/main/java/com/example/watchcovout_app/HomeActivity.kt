package com.example.watchcovout_app

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer

class HomeActivity : AppCompatActivity() {

    var chosenPlace : Place? = null
    var chosenTime = ""
    var selectedDate : String = ""
    var page2 : Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient


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
        val context = this

        fixedRateTimer("timer",false,0,1000*60*60){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            Log.i("dangerVerif", "we are here")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    Log.i("dangerVerif", "we are here2")
                    verifDangerZoneInBack(latLng)
                }
        }
    }


    fun verifDangerZoneInBack(latLng: LatLng){


        // this is just a test because there is no data
        val urlData = "${resources.getString(R.string.host)}/api/v0/zones/"

        // Request a string response from the provided URL.
        val jsonRequestData = JsonArrayRequest(
            Request.Method.GET, urlData, null,
            Response.Listener { response ->

                for (i in 0 until response.length()){
                    val zone = response.getJSONObject(i)
                    val zoneId = zone.getString("id")
                    val zoneLat = zone.getJSONObject("location").getDouble("latitude")
                    val zoneLon = zone.getJSONObject("location").getDouble("longitude")
                    val rayon = zone.getString("rayon").toDouble()
                    val zoneRisqueLatLng = LatLng(zoneLat, zoneLon)

                    val distance = distance(latLng.latitude, latLng.longitude, zoneRisqueLatLng.latitude, zoneRisqueLatLng.longitude)
                    Log.i("dangerVerifLat", zoneRisqueLatLng.toString())
                    Log.i("dangerVerif", "we are here4  "+distance)

                    if (distance <= rayon){
                        Log.i("dangerVerif", "we are here5")
                        addNotification()
                        addUserToZone(zoneId)
                    }
                }


            },Response.ErrorListener { Log.d("Error", "Request error") })
        RequestHandler.getInstance(this)
            .addToRequestQueue(jsonRequestData)

    }

    fun addUserToZone(zoneId : String){
        val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)

        val nid = pref.getString("nid", "")
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())

        var postURL: String = "${resources.getString(R.string.host)}/api/v0/zones/$zoneId/visits?nid=$nid&date=$currentDate"

        val request = object : FileUploadRequest(
            Method.PUT,
            postURL,
            Response.Listener {
                Log.i("userAdded", "succefully added")
            },
            Response.ErrorListener {
                Log.i("error", "error while posting comment")
                Log.i("error", it.toString())
            }
        ){}
        Volley.newRequestQueue(this).add(request)
    }

    fun addNotification(){

        val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)

        val nid = pref.getString("nid", "")

        var postURL: String = "${resources.getString(R.string.host)}/api/v0/users/$nid/notifications"
        Log.i("dangerVerif", "we are here6")
        val request = object : FileUploadRequest(
            Method.PUT,
            postURL,
            Response.Listener {
                Log.i("success", "comment posted succefully")
                Log.i("dangerVerif", "we are here7")
                val titre = "منطقة خطر"
                val content = "حذار لقد دخلت منطقة خطر"


                val i = Intent()
                i.putExtra("title", titre)
                i.putExtra("content", content)
                i.action = "NEWNOTIFICATIONRECEIVED"
                i.setClass(this, NotificationReceiver::class.java)
                sendBroadcast(i)

            },
            Response.ErrorListener {
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

                val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm")
                val currentDate = sdf.format(Date())

                jsonUser.put("typeUser", "U")
                jsonUser.put("title", "DangerZone")
                jsonUser.put("content", "You entered a danger zone")
                jsonUser.put("type", "warning")
                jsonUser.put("dateTime",currentDate )

                Log.i("jsonUser", jsonUser.toString())

                dataOutputStream.writeBytes(jsonUser.toString())
                return byteArrayOutputStream.toByteArray()
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun distance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val theta = lon1 - lon2
        var dist = (Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }


    fun visitPlace(
        date: String,
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
                    layout1.visibility = View.VISIBLE
                    layout2.visibility = View.GONE
                    chosenPlace = null
                    selectedDate = ""
                    page2 = false
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
                jsonUser.put("date", date)
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
