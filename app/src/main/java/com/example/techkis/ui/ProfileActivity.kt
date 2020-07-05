package com.example.techkis.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techkis.R
import com.example.techkis.adapter.NewsAdapter
import com.example.techkis.adapter.NewsCommentAdapter
import com.example.techkis.model.CommentsModel
import com.example.techkis.model.NewsModel
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
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_news_view.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.dialog_editprofil.*
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
    private lateinit var mNewImageUri: Uri
    private var cekNewImage: Boolean = false

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
                if(cekNewImage == true){
                    uploadImageToStorage(mNewImageUri)
                    Log.w("IMAGE_URI_FIREBASE2","Berhasil")
                }
                else{
                    updateUserData(fullName,username,imageUri)
                }
            })
            .setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
                cekNewImage = false
                dialogInterface.cancel()
            })
        dialogBuilder.create().show()
    }

    @SuppressLint("InflateParams")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && data != null){
            mNewImageUri = data.data!!
            cekNewImage = true
        }
    }

    private fun uploadImageToStorage(newImageUri:Uri) {
        val imageId = userID
        val storageRef = mStorage.reference.child("images/$imageId")
        var newImage = ""
        storageRef.putFile(newImageUri).addOnCompleteListener {
            if(it.isSuccessful){
                storageRef.downloadUrl.addOnSuccessListener {
                    newImage =  it.toString()
                    imageUri = newImage
                    updateUserData(fullName,username,newImage)
                    Log.w("IMAGE_URI_FIREBASE1",newImage)
                }
            }
        }
    }

    private fun updateUserData(fullname:String, username:String, newImageUri:String){
        val newDataUser = HashMap<String,Any>()
        newDataUser.put("fullName",fullname)
        newDataUser.put("username",username)
        newDataUser.put("imageUrl",newImageUri)

        mDatabase.child("users").child(userID).updateChildren(newDataUser).addOnCompleteListener {
            if(it.isSuccessful){
                updateSharedPref(fullname,username,newImageUri)
                updateProfileUi()
            }
        }
        cekNewImage = false
    }

    private fun updateSharedPref(fullname: String,username: String,newImageUri: String){
        val edit = sharedPref.edit()
        edit.putString("FULL_NAME",fullname)
        edit.putString("USER_NAME",username)
        edit.putString("IMAGE_URL",newImageUri)
        edit.apply()
        edit.commit()
    }
}