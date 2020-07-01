package com.example.watchcovout_app

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.mapboxsdk.Mapbox
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    var chosenPlaceId : String = ""
    var chosenTime : String = ""
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
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
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
                if (chosenPlaceId == ""){
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
                        if (chosenTime == ""){
                            Toast.makeText(this, "Please choose a timing first", Toast.LENGTH_SHORT).show()
                        }else{
                            flouLayout.visibility = View.GONE
                            dialogLayout.visibility = View.GONE
                        }
                    }
                }
            }
        }

    }

    override fun onBackPressed() {
        flouLayout.visibility = View.GONE
        dialogLayout.visibility = View.GONE
    }

}
