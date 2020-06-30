package com.example.watchcovout_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.activity_role.*
import kotlinx.android.synthetic.main.activity_role.return_btn

class RoleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role)

        finish_btn.setOnClickListener {
            val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
            val editor = pref.edit()
            editor.putBoolean("valid", true)
            editor.commit()

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        return_btn.setOnClickListener {
            finish()
        }
    }
}
