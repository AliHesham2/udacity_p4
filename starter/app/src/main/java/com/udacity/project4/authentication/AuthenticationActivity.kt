package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var firebaseAuth :FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null){
            navigateToReminderActivity()
        }
        login.setOnClickListener {
            startRegistration()
        }
    }

    private fun startRegistration(){
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), RESULT_CODE )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                navigateToReminderActivity()
            } else {
                Toast.makeText(this,"Sign in unsuccessful ${response?.error?.errorCode}",Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun navigateToReminderActivity(){
        startActivity(Intent(this,RemindersActivity::class.java))
        finish()
    }


    companion object {
        const val RESULT_CODE = 22
    }

}
