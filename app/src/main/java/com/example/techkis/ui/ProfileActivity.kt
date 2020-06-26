package com.example.techkis.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.techkis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var mAuth: FirebaseAuth
    private var NAME_PREF = "com-example-techkis"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = Firebase.auth

        sharedPref = this.getSharedPreferences(NAME_PREF,Context.MODE_PRIVATE)

        getDataFromSharedPref()
    }

    private fun getDataFromSharedPref(){
        val uid = sharedPref.getString("USER_ID",null)
        val fullName = sharedPref.getString("FULL_NAME",null)
        val username = sharedPref.getString("USER_NAME",null)
        val email = sharedPref.getString("USER_EMAIL",null)
        val imageUrl = sharedPref.getString("IMAGE_URL","-").toString()
        val role = sharedPref.getString("ROLE","-")
        if(uid != null){
            tv_fullname_profile.text = fullName
            tv_username_profile.text = username
            tv_fullnameBody_profile.text = fullName
            tv_usernameBody_profile.text = username
            tv_emailBody_profile.text = email
            Picasso.get().load(imageUrl).into(iv_imageUser_profile)
        }
    }
}