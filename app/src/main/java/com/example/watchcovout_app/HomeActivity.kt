package com.example.watchcovout_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
        val userImgUrl = pref.getString("photoUrl", "")

        if (userImgUrl != ""){
            Picasso.get().load(userImgUrl).into(`userImageٍView`)
        }


    }
}
