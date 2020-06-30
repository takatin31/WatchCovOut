package com.example.watchcovout_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        callbackManager = CallbackManager.Factory.create()

        google_login.setOnClickListener{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            signInGoogle()
        }



        facebook_login.setOnClickListener {


            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))

            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                    val editor = pref.edit()
                    editor.putString("token", loginResult.accessToken.token)
                    editor.commit()
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    // ...
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    // ...
                }
            })


        }



        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
            val isUserValid = pref.getBoolean("valid", false)

            if (isUserValid){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                auth.signOut()
            }
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("Log", "firebaseAuthWithGoogle:" + account.id)

                val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                val editor = pref.edit()
                editor.putString("token", account.idToken)
                editor.commit()
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("Log", "Google sign in failed", e)

            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Log", "signInWithCredential:success")
                    val user = auth.currentUser

                    val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                    val editor = pref.edit()
                    editor.putString("photoUrl", user!!.photoUrl.toString())

                    user!!.getIdToken(false).addOnSuccessListener {
                        if (it.claims.containsKey("roles")){
                            val userServiceProvider = it.claims["roles"].toString()
                            val userObject = JSONObject(userServiceProvider)
                            if (userObject.has("SERVICE_PROVIDER")){
                                editor.putBoolean("provider", userObject.getBoolean("SERVICE_PROVIDER"))
                            }
                        }
                    }


                    editor.commit()

                    val intent = Intent(this, CardActivity::class.java)
                    intent.putExtra("UID", user!!.uid)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Log", "signInWithCredential:failure", task.exception)
                    // ...
                    Log.i("Log", "Authentication Failed.")

                }

            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val pref = getSharedPreferences(resources.getString(R.string.shared_pref),0)
                    val editor = pref.edit()
                    editor.putString("photoUrl", user!!.photoUrl.toString())

                    user!!.getIdToken(false).addOnSuccessListener {
                        if (it.claims.containsKey("roles")){
                            val userServiceProvider = it.claims["roles"].toString()
                            val userObject = JSONObject(userServiceProvider)
                            if (userObject.has("SERVICE_PROVIDER")){
                                editor.putBoolean("provider", userObject.getBoolean("SERVICE_PROVIDER"))
                            }
                        }
                    }

                    editor.commit()

                    val intent = Intent(this, CardActivity::class.java)
                    intent.putExtra("UID", user!!.uid)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }

            }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }
}
