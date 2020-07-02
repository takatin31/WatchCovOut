package com.example.watchcovout_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        logOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
        val userImgUrl = pref.getString("photoUrl", "")

        if (userImgUrl != ""){
            Picasso.get().load(userImgUrl).into(`userImageٍView`)
        }

        overCrowdedBtn.setOnClickListener {

        }

        confirmCaseBtn.setOnClickListener {
            val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
            val nid = pref.getString("nid", "")
            if (nid != ""){
                var postURL: String = "${resources.getString(R.string.host)}/api/v0/users/$nid?isConfirmedCase=true"

                val request = object : FileUploadRequest(
                    Method.POST,
                    postURL,
                    Response.Listener {

                        Toast.makeText(this, "Your request was succefull", Toast.LENGTH_SHORT).show()

                    },
                    Response.ErrorListener {
                        //Toast.makeText(this, "There were a problem", Toast.LENGTH_SHORT).show()
                        Log.i("error", "error while posting comment")
                        Log.i("error", it.toString())
                    }
                ){}
                Volley.newRequestQueue(this).add(request)
            }else{
                Toast.makeText(this, "There were a problem", Toast.LENGTH_SHORT).show()
            }
        }


        val provider = pref.getBoolean("provider", false)

        if (provider){
            serviceBtn.visibility = View.VISIBLE
        }else{
            serviceBtn.visibility = View.GONE
        }

        serviceBtn.setOnClickListener {
            val intent = Intent(this, ServiceActivity::class.java)
            startActivity(intent)
        }
    }
}
