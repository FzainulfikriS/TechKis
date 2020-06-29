package com.example.techkis.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.techkis.R
import com.example.techkis.model.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.dialog_editprofil.view.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: FirebaseStorage
    private var NAME_PREF = "com-example-techkis"

    private lateinit var username:String
    private lateinit var fullName:String
    private lateinit var email:String
    private lateinit var imageUri:String
    private lateinit var userID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        mStorage = Firebase.storage

        sharedPref = this.getSharedPreferences(NAME_PREF,Context.MODE_PRIVATE)

        userValidation()

        btn_editProfile_profile.setOnClickListener {
            dialogEditProfile()
        }

    }

    private fun userValidation(){
        val uidPref = sharedPref.getString("USER_ID","-")
        val uidExtra = intent.getStringExtra("USER_ID_EXTRA")
        if(uidExtra != null){
            if(uidPref != uidExtra){
                // ambil data dari firebase online
                val iniUid = uidExtra
                getUserFromFirebase(iniUid)
                btn_editProfile_profile.visibility = View.GONE
                return
            }
        }
        // ambil data dari shared preference
        getDataUserFromSharedPref()
    }

    private fun getUserFromFirebase(iniUid:String){
        mDatabase.child("users").child(iniUid).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    val users = p0.getValue<UsersModel>()
                    if(users != null){
                        userID = users.uid
                        fullName = users.fullName
                        username = users.username
                        email = users.email
                        imageUri = users.imageUrl
                        updateProfileUi()
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )
    }

    private fun getDataUserFromSharedPref(){
        val uidPref = sharedPref.getString("USER_ID","-")
        val fullnamePref = sharedPref.getString("FULL_NAME","-")
        val usernamePref = sharedPref.getString("USER_NAME","-")
        val userEmailPref = sharedPref.getString("USER_EMAIL","-")
        val userImagePref = sharedPref.getString("IMAGE_URL","-")
        userID = uidPref.toString()
        fullName = fullnamePref.toString()
        username = usernamePref.toString()
        email = userEmailPref.toString()
        imageUri = userImagePref.toString()
        updateProfileUi()
    }

    private fun updateProfileUi(){
        tv_fullname_profile.text = fullName
        tv_fullnameBody_profile.text = fullName
        tv_username_profile.text = username
        tv_usernameBody_profile.text = username
        tv_emailBody_profile.text = email
        Picasso.get().load(imageUri).into(iv_imageUser_profile)
    }

    @SuppressLint("InflateParams")
    private fun dialogEditProfile(){
        val dialogBuilder = AlertDialog.Builder(this)
        val mView = this.layoutInflater.inflate(R.layout.dialog_editprofil,null)

        val editFullname = mView.findViewById<EditText>(R.id.et_fullname_dialog)
        val editUsername = mView.findViewById<EditText>(R.id.et_username_dialog)
        val ivUserImage = mView.findViewById<ImageView>(R.id.iv_imageProfile_dialog)
        editFullname.setText(fullName)
        editUsername.setText(username)
        Picasso.get().load(imageUri).into(ivUserImage)

        ivUserImage.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.setType("image/*")
            startActivityForResult(intentImage,1)
        }

        dialogBuilder.setView(mView)
            .setPositiveButton("Edit",DialogInterface.OnClickListener { dialogInterface, i ->
                fullName = editFullname.text.toString()
                username = editUsername.text.toString()
            })
            .setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
            })
        dialogBuilder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && data != null){
            val newImageUri = data.data
        }
    }
}