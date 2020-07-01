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
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_service.*
import kotlinx.android.synthetic.main.activity_service.confirm_btn
import kotlinx.android.synthetic.main.activity_service.dialogLayout
import kotlinx.android.synthetic.main.activity_service.flouLayout
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class ServiceActivity : AppCompatActivity() {

    var selectedPlace : LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        return_btn.setOnClickListener {
            finish()
        }

        confirm_btn.setOnClickListener {
            val title = editTextTitle.text.toString()
            val chunk = editTextChunk.text.toString()
            val number = editTextNumber.text.toString()
            if (title == "" || chunk == "" ||
                    number == "" || selectedPlace == null){
                Toast.makeText(this, "Please fill all the fields correctly", Toast.LENGTH_SHORT).show()
            }else{
                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                val nid = pref.getString("nid", "")
                if (nid != null){
                    createPlace(nid, title, number.toInt(), chunk.toInt(), selectedPlace!!)
                }

            }
        }

        chooseLocation.setOnClickListener {
            flouLayout.visibility = View.VISIBLE
            dialogLayout.visibility = View.VISIBLE

            var contentFr: Fragment =
                MapFragment()

            (contentFr as MapFragment).isProvider = true
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, contentFr)
            transaction.commit()
        }

        confirm_btn.setOnClickListener {
            if (selectedPlace != null){
                flouLayout.visibility = View.GONE
                dialogLayout.visibility = View.GONE
            }
        }
    }

    fun createPlace(
        nid : String,
        title: String,
        number: Int,
        chunk: Int,
        selectedPlace: LatLng
    ) {
        var postURL: String = "${resources.getString(R.string.host)}/api/v0/places/"
        //var postURL = "https://ptsv2.com/t/7a3sq-1593610683/post"

        val request = object : FileUploadRequest(
            Method.PUT,
            postURL,
            Response.Listener {
                Log.i("success", "comment posted succefully")
                val resp = String(it.data)
                val jsonResp = JSONObject(resp)
                if (jsonResp.has("error")){
                    Toast.makeText(this, "National ID Already exists", Toast.LENGTH_SHORT).show()


                }else if (jsonResp.has("message")){

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
                jsonUser.put("type", "BUSINESS")
                jsonUser.put("title", title)
                jsonUser.put("cunksOfTime", chunk)
                jsonUser.put("numberOfPlaces", number)
                jsonUser.put("nid", nid)
                val location = JSONObject()
                location.put("latitude", selectedPlace.latitude)
                location.put("longitude", selectedPlace.longitude)
                jsonUser.put("location", location)
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
}
