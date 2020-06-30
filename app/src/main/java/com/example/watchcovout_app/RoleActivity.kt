package com.example.watchcovout_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.activity_role.*
import kotlinx.android.synthetic.main.activity_role.return_btn

class RoleActivity : AppCompatActivity() {

    var roleSelected : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role)
        val nid = intent.getStringExtra("NID")
        val nidUrl = intent.getStringExtra("nidUrl")

        layout1.setOnClickListener {
            checkBox2.isChecked = false
            checkBox2.visibility = View.GONE

            checkBox1.isChecked = true
            checkBox1.visibility = View.VISIBLE
            roleSelected = "SIMPLE"
        }

        layout2.setOnClickListener {
            checkBox1.isChecked = false
            checkBox1.visibility = View.GONE

            checkBox2.isChecked = true
            checkBox2.visibility = View.VISIBLE
            roleSelected = "PROVIDER"
        }

        finish_btn.setOnClickListener {
            if (roleSelected != ""){
                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)

                val uid = pref.getString("userUID", "")

                if (uid != null && uid != ""){
                    createUser(uid, roleSelected, nid, nidUrl)
                }


            }else{
                Toast.makeText(this, "Please select a role first", Toast.LENGTH_SHORT).show()
            }

        }

        return_btn.setOnClickListener {
            finish()
        }
    }

    fun createUser(uid : String, role : String, nid : String, nidUrl : String){
        //var postURL: String = "${resources.getString(R.string.host)}/api/v0/users/:$uid"
        var postURL = "https://ptsv2.com/t/i6zey-1593550940/post"

        if (role == "PROVIDER"){
            postURL += "?serviceProvider"
        }

        Log.i("userInfo", "uid : $uid")
        Log.i("userInfo", "nid : $nid")
        Log.i("userInfo", "nidUrl : $nidUrl")
        val request = object : FileUploadRequest(
            Method.POST,
            postURL,
            Response.Listener {
                Log.i("success", "comment posted succefully")

                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                val editor = pref.edit()
                editor.putBoolean("valid", true)
                editor.commit()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            },
            Response.ErrorListener {
                val err = String(it.networkResponse.data)
                Log.i("error", "error while posting comment")
                Log.i("error", err)
            }
        ){
            override fun getParams(): MutableMap<String, String> {
                var params = HashMap<String, String>()
                params["nid"] = nid
                params["nationalCardPicURL"] = nidUrl

                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                val token = pref.getString("token", "")
                val headers = java.util.HashMap<String, String>()
                headers.put("Authorization", "Bearer $token")
                return headers
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}
