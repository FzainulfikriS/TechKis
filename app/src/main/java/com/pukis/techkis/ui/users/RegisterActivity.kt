package com.pukis.techkis.ui.users

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.pukis.techkis.R
import com.pukis.techkis.model.UsersModel
import com.pukis.techkis.ui.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var sharedPref: SharedPreferences
    private var NAME_PREF = "com-pukis-techkis"


    private var mImageUri: Uri? = null
    private lateinit var userID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        mStorage = Firebase.storage.reference
        sharedPref = this.getSharedPreferences(NAME_PREF,Context.MODE_PRIVATE)

        btn_register_register.setOnClickListener {
            validasiForm()
        }
        btn_login_register.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        iv_imageUser_register.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.setType("image/*")
            startActivityForResult(intentImage,1)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && data != null){
            mImageUri = data.data!!
            Picasso.get().load(mImageUri).into(iv_imageUser_register)
            tv_selectImage_register.visibility = View.GONE
        }
    }

    private fun validasiForm(){
        val username = et_username_register.text.toString()
        val fullName = et_fullname_register.text.toString()
        val email = et_email_register.text.toString()
        val password = et_password_register.text.toString()

        if(username.isEmpty()){
            et_username_register.requestFocus()
            et_username_register.setError("Username tidak boleh kosong")
            return
        }
        if(fullName.isEmpty()){
            et_fullname_register.requestFocus()
            et_fullname_register.setError("Full Name tidak boleh kosong")
            return
        }
        if(email.isEmpty()){
            et_email_register.requestFocus()
            et_email_register.setError("Email tidak kosong")
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email_register.requestFocus()
            et_email_register.setError("Format email salah")
            return
        }
        if(password.isEmpty()){
            et_password_register.requestFocus()
            et_password_register.setError("Password tidak boleh kosong")
            return
        }

        Toast.makeText(this,"Please wait...",Toast.LENGTH_SHORT).show()
        signUpUser(username,fullName,email,password)

    }

    private fun signUpUser(username: String,fullName: String,email: String,password: String){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            if(it.isSuccessful){
                userID =  mAuth.currentUser?.uid.toString()
                setUserDataToFirebase(userID,username,fullName,email)
            }
            else{
                Toast.makeText(this,"Gagal melakukan register",Toast.LENGTH_SHORT).show()
                Log.w("REGISTER_RESULT",it.exception)
            }
        }
    }

    private fun setUserDataToFirebase(uid:String, username:String, fullName: String, email:String){

        if(mImageUri != null){
            val storageRef = mStorage.child("images/$userID")
            storageRef.putFile(mImageUri!!).addOnCompleteListener {
                if(it.isSuccessful){
                    storageRef.downloadUrl.addOnSuccessListener {
                        val users = UsersModel(uid,username,fullName,email, it.toString())
                        mDatabase.child("users").child(uid).setValue(users)
                        setDataUserToPref(uid,username,fullName,email,it.toString())
                    }
                }
            }
        }
        else{
            mStorage.child("images/default-user.png").downloadUrl.addOnSuccessListener {
                val users = UsersModel(uid,username,fullName,email, it.toString())
                mDatabase.child("users").child(uid).setValue(users)
                setDataUserToPref(uid,username,fullName,email,it.toString())
            }
        }
    }

    private fun setDataUserToPref(uid: String, username: String, fullName: String, email: String, imageUrl: String){
        val editorPref = sharedPref.edit()
        editorPref.putString("USER_ID",uid)
        editorPref.putString("USER_NAME",username)
        editorPref.putString("FULL_NAME",fullName)
        editorPref.putString("USER_EMAIL",email)
        editorPref.putString("IMAGE_URL",imageUrl)
        editorPref.apply()
        editorPref.commit()

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}