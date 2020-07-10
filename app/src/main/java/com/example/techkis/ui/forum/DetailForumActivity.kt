package com.example.techkis.ui.forum

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techkis.R
import com.example.techkis.adapter.ForumDiscussionAdapter
import com.example.techkis.model.DiscussionForumModel
import com.example.techkis.model.ForumModel
import com.example.techkis.model.UsersModel
import com.example.techkis.ui.users.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail_forum.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DetailForumActivity : AppCompatActivity() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDiskusiAdapter: ForumDiscussionAdapter
    private lateinit var forumID: String
    private lateinit var diskusiList: ArrayList<DiscussionForumModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_forum)

        mDatabase = Firebase.database.reference
        mAuth = Firebase.auth
        forumID = intent.getStringExtra("FORUM_ID").toString()

        diskusiList = ArrayList()

        getIntentData()
        getDataDariDatabase()


        var titleDialog = ""
        val currentUser = mAuth.currentUser
        btn_addDisc_detailForum.setOnClickListener {
            if(currentUser == null){
                titleDialog = "You cannot create a forum"
                showDialog(titleDialog)
            }
            else{
                formAddDiskusiValidasi()
            }
        }

        btn_editForum_detailForum.setOnClickListener {

        }
    }

    private fun showDialog(title: String){
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            this.setTitle(title)
            this.setMessage("Sorry! You haven't signed yet, please sign in first :)")
            this.setPositiveButton("Go to Login",
                DialogInterface.OnClickListener { dialog, id ->
                    startActivity(Intent(this@DetailForumActivity,
                        LoginActivity::class.java))
                })
            this.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, i ->
                    dialog.cancel()
                })
        }
        alertDialogBuilder.create().show()
    }

    private fun getDataDariDatabase(){
        mDatabase.child("diskusi-forum").child(forumID).orderByChild("timestampDiskusi").addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    diskusiList.clear()
                    for (diskusiSnapshot in p0.children){
                        val diskusiData = diskusiSnapshot.getValue(DiscussionForumModel::class.java)
                        getDataUser(diskusiData!!)
                    }
                }
            }
        )
    }
    private fun getDataUser(diskusiData:DiscussionForumModel){
        mDatabase.child("users").child(diskusiData.userID).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    val users = p0.getValue(UsersModel::class.java)
                    val currentUserID = mAuth.currentUser?.uid
                    if(users?.uid!! == currentUserID){
                        btn_editForum_detailForum.visibility = View.VISIBLE
                    }
                    diskusiList.add(
                        DiscussionForumModel(
                            diskusiData.diskusiID,
                            diskusiData.isiDiskusi,
                            diskusiData.timestampDiskusi,
                            forumID,
                            users.uid,
                            users.username,
                            users.imageUrl
                        )
                    )
                    tampilDataRecyclerView(diskusiList)
                }
            }
        )
    }

    private fun tampilDataRecyclerView(diskusiList: ArrayList<DiscussionForumModel>){
        diskusiList.reverse()
        mDiskusiAdapter = ForumDiscussionAdapter()
        mDiskusiAdapter.diskusiAdapter(diskusiList)
        rv_diskusi_detailForum.apply {
            layoutManager = LinearLayoutManager(this@DetailForumActivity)
            adapter = mDiskusiAdapter
        }
    }

    private fun getIntentData(){
        val judulForum = intent.getStringExtra("TITLE_FORUM")
        val isiForum = intent.getStringExtra("ISI_FORUM")
        val authorName = intent.getStringExtra("NAMA_AUTHOR")
        val timestampForum = intent.getLongExtra("TIMESTAMP_FORUM",0)
        updateUIForum(judulForum!!,isiForum!!,authorName!!,timestampForum)
    }

    private fun updateUIForum(judulForum:String, isiForum:String, authorName:String,timestampForum:Long){
        tv_title_detailForum.text = judulForum
        tv_authorName_detailForum.text = authorName
        tv_isi_detaiForum.text = isiForum
        val timestamp = Calendar.getInstance()
        timestamp.timeInMillis = timestampForum * 1000
        tv_timestampForum_detailForum.text = DateFormat.format("MMM dd yyyy mm:hh a",timestamp).toString()

    }

    private fun formAddDiskusiValidasi(){
        val et_addDiskusi = et_addDisc_detailForum.text
        if(et_addDiskusi == null){
            et_addDisc_detailForum.error = "Anda belum menulis apapun"
            et_addDisc_detailForum.requestFocus()
            return
        }

        setDiskusiKeDatabase(et_addDiskusi.toString())
        et_addDisc_detailForum.clearFocus()
        et_addDisc_detailForum.text = null
    }

    private fun setDiskusiKeDatabase(isiDiskusi: String){
        val diskusiID = mDatabase.child("diskusi-forum").push().key.toString()
        val timestampDiskusi = Calendar.getInstance().timeInMillis / 1000
        val userID = mAuth.currentUser?.uid

        val diskusi = HashMap<String,Any>()
        diskusi.put("diskusiID",diskusiID)
        diskusi.put("isiDiskusi",isiDiskusi)
        diskusi.put("timestampDiskusi",timestampDiskusi)
        diskusi.put("userID",userID.toString())

        mDatabase.child("diskusi-forum").child(forumID).child(diskusiID).setValue(diskusi)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val refForum = mDatabase.child("forums").child(forumID)
                    refForum.addListenerForSingleValueEvent(
                        object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                            }
                            override fun onDataChange(p0: DataSnapshot) {
                                val forums = p0.getValue(ForumModel::class.java)
                                val commentCount = forums?.commentCount!!
                                val increastComment = commentCount + 1
                                refForum.child("commentCount").setValue(increastComment)
                            }
                        }
                    )
                }
            }
    }
}