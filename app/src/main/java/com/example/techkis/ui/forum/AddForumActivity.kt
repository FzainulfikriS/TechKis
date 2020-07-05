package com.example.techkis.ui.forum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.techkis.R
import com.example.techkis.model.ForumModel
import com.example.techkis.ui.ForumActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_forum.*
import kotlinx.android.synthetic.main.activity_forum.*
import java.util.*
import kotlin.collections.HashMap

class AddForumActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_forum)

        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference

        setSupportActionBar(toolbar_addForum)
        supportActionBar?.setTitle("Add Forum")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btn_addForum_addForum.setOnClickListener {
            validasiForm()
        }

    }

    private fun addForum(){
        val authorForumID = mAuth.currentUser?.uid.toString()
        val judulForum = et_title_addForum.text.toString()
        val isiForum = et_isiForum_addForum.text.toString()
        val timestampForum = Calendar.getInstance().timeInMillis / 1000
        val forumID = mDatabase.child("forums").push().key.toString()
        val commentCount = 0

        val forums = HashMap<String,Any>()
        forums.put("forumID",forumID)
        forums.put("judulForum",judulForum)
        forums.put("isiForum",isiForum)
        forums.put("authorForumID",authorForumID)
        forums.put("commentCount",commentCount)
        forums.put("timestampForum",timestampForum)
        mDatabase.child("forums").child(forumID).setValue(forums)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val intent = Intent(this,ForumActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
            }
    }

    private fun validasiForm(){
        val judulField = et_title_addForum.text.toString()
        val isiField = et_isiForum_addForum.text.toString()
        if(judulField.isEmpty()){
            etLayout_title_addForum.isErrorEnabled = true
            etLayout_title_addForum.error = "Title tidak boleh kosong"
            etLayout_title_addForum.requestFocus()
            return
        }
        etLayout_title_addForum.isErrorEnabled = false

        if(isiField.isEmpty()){
            etLayout_isi_addForum.isErrorEnabled = true
            etLayout_isi_addForum.error = "Anda belum mengisi konten forum"
            etLayout_isi_addForum.requestFocus()
            return
        }
        etLayout_isi_addForum.isErrorEnabled = false

        addForum()
    }
}