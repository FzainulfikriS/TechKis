package com.pukis.techkis.ui.forum

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.pukis.techkis.R
import com.pukis.techkis.adapter.ForumDiscussionAdapter
import com.pukis.techkis.model.DiscussionForumModel
import com.pukis.techkis.model.ForumModel
import com.pukis.techkis.model.UsersModel
import com.pukis.techkis.ui.users.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_forum.*
import kotlinx.android.synthetic.main.dialog_layout_diskusi.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DetailForumActivity : AppCompatActivity(), ForumDiscussionAdapter.ItemClickListener {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDiskusiAdapter: ForumDiscussionAdapter
    private lateinit var diskusiList: ArrayList<DiscussionForumModel>


    // Variabel Data ID
    private lateinit var forumID: String
    private lateinit var judulForum: String
    private lateinit var isiForum: String
    private lateinit var authorName: String
    private var forumTimestamp: Long = 0
    private lateinit var authorForumID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_forum)

        setSupportActionBar(toolbar_detailForum)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        mDatabase = Firebase.database.reference
        mAuth = Firebase.auth

        diskusiList = ArrayList()

        getForumData()
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
            val intentEditForum = Intent(this,EditForumActivity::class.java)
            intentEditForum.putExtra("FORUM_ID",forumID)
            intentEditForum.putExtra("TITLE_FORUM",judulForum)
            intentEditForum.putExtra("ISI_FORUM",isiForum)
            startActivity(intentEditForum)
        }
    }

    override fun onStart() {
        super.onStart()
        getForumData()
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
                    if(p0.exists()){
                        cv_belumAdaDiskusi_detailForum.visibility = View.GONE
                        for (diskusiSnapshot in p0.children){
                            val diskusiData = diskusiSnapshot.getValue(DiscussionForumModel::class.java)
                            getDataUser(diskusiData!!)
                        }
                    }
                }
            }
        )
    }
    private fun getDataUser(diskusiData:DiscussionForumModel){
        mDatabase.child("users").child(diskusiData.userID).addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    val users = p0.getValue(UsersModel::class.java)
                    diskusiList.add(
                        DiscussionForumModel(
                            diskusiData.diskusiID,
                            diskusiData.isiDiskusi,
                            diskusiData.timestampDiskusi,
                            forumID,
                            users!!.uid,
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
        mDiskusiAdapter.diskusiAdapter(diskusiList,this)
        rv_diskusi_detailForum.apply {
            layoutManager = LinearLayoutManager(this@DetailForumActivity)
            adapter = mDiskusiAdapter
        }
    }

    private fun getForumData() {
        forumID = intent.getStringExtra("FORUM_ID").toString()
        mDatabase.child("forums").child(forumID).addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val forum = p0.getValue<ForumModel>()
                    if (forum != null) {
                        val currentUserID = mAuth.currentUser?.uid
                        forumID = forum.forumID
                        judulForum = forum.judulForum
                        isiForum = forum.isiForum
                        authorName = forum.namaAuthor
                        forumTimestamp = forum.timestampForum
                        supportActionBar?.setTitle(judulForum)
                        if (forum.authorForumID == currentUserID.toString()) {
                            btn_editForum_detailForum.visibility = View.VISIBLE
                        }
                        mDatabase.child("users").child(forum.authorForumID)
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                }
                                override fun onDataChange(p0: DataSnapshot) {
                                    val users = p0.getValue<UsersModel>()
                                    authorName = users!!.username
                                    val authorImage = users.imageUrl
                                    updateUIForum(judulForum, isiForum, authorName, authorImage, forumTimestamp)
                                }
                            })
                    }
                }
            }
        )
    }

    private fun updateUIForum(judulForum:String, isiForum:String, authorName:String,authorImage:String,timestampForum:Long){
        tv_title_detailForum.text = judulForum
        tv_authorName_detailForum.text = authorName
        tv_isi_detaiForum.text = isiForum
        val timestamp = Calendar.getInstance()
        timestamp.timeInMillis = timestampForum * 1000
        tv_timestampForum_detailForum.text = DateFormat.format("MMM dd yyyy hh:mm a",timestamp).toString()
        Picasso.get().load(authorImage).into(iv_authorImage_detailForum)
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

    @SuppressLint("InflateParams")
    private fun showDialog(forumID: String, diskusiID: String,isiDiskusi: String){
        val diskusiForumRef = mDatabase.child("diskusi-forum").child(forumID).child(diskusiID)
        val alertDialog = AlertDialog.Builder(this)
        val view = this.layoutInflater.inflate(R.layout.dialog_layout_diskusi,null)
        view.et_isiDuskusi_diskusiDialog.setText(isiDiskusi)
        alertDialog.setView(view)
            .setTitle("View Comment")
            .setPositiveButton("Update",DialogInterface.OnClickListener { dialogInterface, i ->
                val newDiskusi = view.et_isiDuskusi_diskusiDialog.text
                val diskusiMap = HashMap<String,Any>()
                diskusiMap.put("isiDiskusi",newDiskusi.toString())
                diskusiForumRef.updateChildren(diskusiMap)
            })
            .setNegativeButton("Delete",DialogInterface.OnClickListener { dialogInterface, i ->
                diskusiForumRef.removeValue().addOnCompleteListener {
                    if(it.isSuccessful){
                        val refForum = mDatabase.child("forums").child(forumID)
                        refForum.addListenerForSingleValueEvent(
                            object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                }
                                override fun onDataChange(p0: DataSnapshot) {
                                    val forums = p0.getValue(ForumModel::class.java)
                                    val commentCount = forums?.commentCount!!
                                    val increastComment = commentCount - 1
                                    refForum.child("commentCount").setValue(increastComment)
                                }
                            }
                        )
                    }
                }
            })
            .setNeutralButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
            })
        alertDialog.create().show()
    }

    override fun itemClickListener(discussionModel: DiscussionForumModel, position: Int) {
        val forumID = discussionModel.forumID
        val diskusiID = discussionModel.diskusiID
        val isiDiskusi = discussionModel.isiDiskusi
        val userID = discussionModel.userID
        val currentUserID = mAuth.currentUser?.uid
        if(userID == currentUserID){
            showDialog(forumID,diskusiID,isiDiskusi)
        }
    }
}