package com.example.watchcovout_app


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_card.*


class CardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        continue_btn.setOnClickListener {
            if (someKeyEmpty()){
                Toast.makeText(this, "Please fill the empty fields", Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(this, RoleActivity::class.java)
                startActivity(intent)
            }

        }

        return_btn.setOnClickListener {
            finish()
        }


        initKeys()
    }

    fun initKeys(){

        val validChars = arrayListOf("0","1","2","3","4","5","6","7","8","9")

        val key: OnKeyListener = object : OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent?): Boolean {
                val text = (v as EditText).text.toString()
                if(validChars.contains(text)){
                    v.focusSearch(FOCUS_RIGHT)
                        .requestFocus()
                }

                return false
            }
        }

        id_number0.setOnKeyListener(key)
        id_number1.setOnKeyListener(key)
        id_number2.setOnKeyListener(key)
        id_number3.setOnKeyListener(key)
        id_number4.setOnKeyListener(key)
        id_number5.setOnKeyListener(key)
        id_number6.setOnKeyListener(key)
        id_number7.setOnKeyListener(key)
        id_number8.setOnKeyListener(key)
    }

    fun someKeyEmpty() : Boolean{
        return id_number0.text.isEmpty() || id_number1.text.isEmpty() || id_number2.text.isEmpty() || id_number3.text.isEmpty()
                || id_number4.text.isEmpty() || id_number5.text.isEmpty() || id_number6.text.isEmpty() || id_number7.text.isEmpty() || id_number2.text.isEmpty() || id_number3.text.isEmpty()
                || id_number8.text.isEmpty() || id_number9.text.isEmpty()
    }
}
