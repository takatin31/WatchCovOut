package com.example.watchcovout_app


import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.FOCUS_RIGHT
import android.view.View.OnKeyListener
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_card.*
import java.io.IOException


class CardActivity : AppCompatActivity() {

    var uri: Uri? = null
    val IMAGE_CAPTURE_CODE = 1001
    private var imageData: ByteArray? = null
    var fileUploaded : Boolean = false
    var IdImageDownloadUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        continue_btn.setOnClickListener {
            if (someKeyEmpty() || !fileUploaded){
                Toast.makeText(this, "Please fill the empty fields", Toast.LENGTH_SHORT).show()
            }else{
                val nid = getNID()
                val intent = Intent(this, RoleActivity::class.java)
                intent.putExtra("nidUrl", IdImageDownloadUrl)
                intent.putExtra("NID", nid)
                startActivity(intent)
            }
        }

        return_btn.setOnClickListener {
            finish()
        }

        upload_btn.setOnClickListener {
            showPictureDialog()
        }

        initKeys()
    }

    private fun showPictureDialog() {
        val pictureDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf(
            "Select image from gallery",
            "Record image from camera"
        )
        pictureDialog.setItems(pictureDialogItems,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> chooseVideoFromGallary()
                    1 -> takeVideoFromCamera()
                }
            })
        pictureDialog.show()
    }

    fun chooseVideoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, IMAGE_CAPTURE_CODE)
    }

    private fun takeVideoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, IMAGE_CAPTURE_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured from camera intent
        Log.i("okee", "oeeeeeeeenoo"+ resultCode)
        if (resultCode == Activity.RESULT_OK){
            Log.i("okee", "oeeeeeeeennnn")
            if (data != null) {
                Log.i("okeeeee", data.toString())
                uri = data.data
                createImageData(uri!!)
                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                val userUId = pref.getString("userUID", "")
                if (userUId != null && userUId != ""){
                    uploadFile(userUId, uri!!)
                }else{
                    Toast.makeText(this, "There were a problem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun uploadFile(userUID : String, fileUri : Uri){
        val mStorageRef : StorageReference = FirebaseStorage.getInstance("gs://watchcovout.appspot.com").reference

        val riversRef: StorageReference = mStorageRef.child("IDs/$userUID.jpg")

        val taskUpload = riversRef.putFile(fileUri)

        taskUpload.continueWith {
            if (!it.isSuccessful) {

                it.exception?.let { t ->
                    throw t
                }
            }
            riversRef.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                it.result!!.addOnSuccessListener { task ->
                    Picasso.get().load(task.toString()).into(idImgView)
                    idImgView.visibility = View.VISIBLE
                    IdImageDownloadUrl = task.toString()
                    fileUploaded = true
                }
            }else{
                Toast.makeText(this, "There were a problem while upload", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
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

    fun getNID() : String{
        return (id_number0.text.toString() + id_number1.text.toString() + id_number2.text.toString() + id_number3.text.toString()
                + id_number4.text.toString() + id_number5.text.toString() + id_number6.text.toString() + id_number7.text.toString() + id_number2.text.toString() + id_number3.text.toString()
                + id_number8.text.toString() + id_number9.text.toString())
    }
}
