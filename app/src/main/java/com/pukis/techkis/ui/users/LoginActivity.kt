package com.pukis.techkis.ui.users

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.pukis.techkis.R
import com.pukis.techkis.model.UsersModel
import com.pukis.techkis.ui.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var sharedPref: SharedPreferences
    private var NAME_PREF = "com-pukis-techkis"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        sharedPref = this.getSharedPreferences(NAME_PREF, Context.MODE_PRIVATE)

        btn_login_login.setOnClickListener {
            validasiForm()
        }
        btn_register_login.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validasiForm(){
        val emailForm = et_email_login.text.toString()
        val passForm = et_password_login.text.toString()
        if(emailForm.isEmpty()){
            etLayout_email_login.error = "You have not entered your email"
            return
        }
        etLayout_email_login.isErrorEnabled = false
        if(passForm.isEmpty()){
            etLayout_password_login.error = "You have not entered your email"
            return
        }
        etLayout_password_login.isErrorEnabled = false

        Toast.makeText(this,"Please wait...",Toast.LENGTH_SHORT).show()
        setUserLogin(emailForm,passForm)
    }

    private fun setUserLogin(email: String, pass: String){
        mAuth.signInWithEmailAndPassword(email,pass)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val userID = mAuth.currentUser?.uid.toString()
                    getUserRole(userID)
                }
                else{
                    Toast.makeText(this,"Login failed",Toast.LENGTH_SHORT).show()
                    Log.w("LOGIN_RESULT",it.exception)
                }
            }
    }

    private fun getUserRole(uid: String){
        mDatabase.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        val users = p0.getValue<UsersModel>()
                        var role = ""
                        if(p0.child("role").exists()){
                            Log.w("ROLE-LOGIN","ADMIN")
                            role = "admin"
                        }
                        val username = users?.username.toString()
                        val fullName = users?.fullName.toString()
                        val email = users?.email.toString()
                        val imageUrl = users?.imageUrl.toString()
                        setDataUserToPref(uid,username,fullName,email,imageUrl,role)
                    }
                }
            })
    }

    private fun setDataUserToPref(uid: String, username: String, fullname: String, email: String,
                                  imageUrl: String, role: String){
        val editPref = sharedPref.edit()
        editPref.putString("USER_ID",uid)
        editPref.putString("USER_NAME",username)
        editPref.putString("FULL_NAME",fullname)
        editPref.putString("USER_EMAIL",email)
        editPref.putString("IMAGE_URL",imageUrl)
        editPref.putString("ROLE",role)
        editPref.apply()
        editPref.commit()
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}