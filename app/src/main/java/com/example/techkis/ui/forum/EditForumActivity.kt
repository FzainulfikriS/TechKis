package com.example.techkis.ui.forum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.techkis.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit_forum.*

class EditForumActivity : AppCompatActivity() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var forumID: String
    private lateinit var judulForum: String
    private lateinit var isiForum: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_forum)

        setSupportActionBar(toolbar_editForum)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        mDatabase = Firebase.database.reference


        initDataIntentExtra()

        btn_updateForum_editForum.setOnClickListener {
            if(!::forumID.isInitialized){
                initDataIntentExtra()
            }
            validasiForm(forumID)
        }
    }

    private fun initDataIntentExtra(){
        val intentExtra = intent.extras
        if(intentExtra != null){
            forumID = intentExtra.getString("FORUM_ID").toString()
            judulForum = intentExtra.getString("TITLE_FORUM").toString()
            isiForum = intentExtra.getString("ISI_FORUM").toString()
            et_title_editForum.setText(judulForum)
            et_isiForum_editForum.setText(isiForum)
        }
    }

    private fun validasiForm(forumID: String){
        val etTitle = et_title_editForum.text
        val etIsi = et_isiForum_editForum.text
        if(etTitle.isNullOrEmpty()){
            etLayout_title_editForum.isErrorEnabled = true
            etLayout_title_editForum.error = "Title tidak boleh kosong"
            return
        }
        etLayout_title_editForum.isErrorEnabled = false
        if(etIsi.isNullOrEmpty()){
            etLayout_isi_editForum.isErrorEnabled = true
            etLayout_isi_editForum.error = "Isi forum tidak boleh kosong"
            return
        }
        etLayout_isi_editForum.isErrorEnabled = false

        val newForm = HashMap<String,Any>()
        val newTitleForum = etTitle.toString()
        val newIsiForum = etIsi.toString()
        newForm.put("forumID",forumID)
        newForm.put("judulForum",newTitleForum)
        newForm.put("isiForum",newIsiForum)

        mDatabase.child("forums").child(forumID).updateChildren(newForm)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    onBackPressed()
                }
                else{
                    Toast.makeText(this,"Gagal update forum",Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}