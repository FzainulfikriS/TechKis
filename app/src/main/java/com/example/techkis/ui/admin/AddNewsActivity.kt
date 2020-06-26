package com.example.techkis.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import com.example.techkis.ui.HomeActivity
import com.example.techkis.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_add_news.*
import java.util.*
import kotlin.collections.HashMap

class AddNewsActivity : AppCompatActivity() {

    private lateinit var mStorage: FirebaseStorage
    private lateinit var mDatbase: DatabaseReference
    private var mImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        mStorage = Firebase.storage
        mDatbase = Firebase.database.reference

        setSupportActionBar(toolbar_addNews)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        btn_chooseImage_addNews.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK)
            pickPhotoIntent.setType("image/*")
            startActivityForResult(pickPhotoIntent,1)
        }

        btn_postNews_addNews.setOnClickListener {
            formValidation()
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && data != null){
            val imageUri = data.data!!
            val imageName = getFilenameFromPath(imageUri)
            tv_imageName_addNews.text = imageName
            uploadImageToStorage(imageUri)
        }
    }

    private fun formValidation(){
        val titleForm = et_title_addNews.text.toString()
        val kontenForm = et_konten_addNews.text.toString()
        if(titleForm.isEmpty()){
            etLayout_title_addNews.error = "The Title field is required"
            return
        }
        etLayout_title_addNews.isErrorEnabled = false
        if(kontenForm.isEmpty()){
            etLayour_konten_addNews.error = "The Content field is required"
            return
        }
        etLayour_konten_addNews.isErrorEnabled = false
        Toast.makeText(this,"Please wait..",Toast.LENGTH_SHORT).show()
        setDataToFirebaseDatabase(titleForm,kontenForm)
    }

    private fun setDataToFirebaseDatabase(title: String, isiKontent: String){
        val newsID = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()/1000
        val news = HashMap<String,Any>()
        if(mImageUrl.isNotEmpty()){
            news.put("id",newsID)
            news.put("isiKonten",isiKontent)
            news.put("timestamp",timestamp.toString())
            news.put("image_url",mImageUrl)
            news.put("title",title)
        }
        news.put("id",newsID)
        news.put("isiKonten",isiKontent)
        news.put("timestamp",timestamp.toString())
        news.put("title",title)
        mDatbase.child("news").child(newsID).setValue(news).addOnCompleteListener {
            if(it.isSuccessful){
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            else{
                Toast.makeText(this,"Failed create News",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToStorage(imageUri: Uri){
        val imageID = UUID.randomUUID()
        if(imageUri.toString().isNotEmpty()){
            val storageRef = mStorage.reference.child("images/$imageID")
            storageRef.putFile(imageUri).addOnCompleteListener {
                    if(it.isSuccessful){
                        storageRef.downloadUrl.addOnSuccessListener {
                            mImageUrl = it.toString()
                        }
                    }
                }
        }
    }

    private fun getFilenameFromPath(imageUri: Uri):String{
        val cursor = contentResolver.query(imageUri,null,null,null,null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val imageName = cursor?.getString(nameIndex!!).toString()
        cursor?.close()
        return imageName
    }
}