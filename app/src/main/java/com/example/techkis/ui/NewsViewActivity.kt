package com.example.techkis.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techkis.R
import com.example.techkis.adapter.NewsCommentAdapter
import com.example.techkis.model.CommentsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_news_view.*
import java.util.*
import kotlin.collections.HashMap

class NewsViewActivity : AppCompatActivity() {

    private lateinit var newsID: String
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUserID: String
    private var mLike: Boolean = false
    private var mBookmark: Boolean = false

    private lateinit var sharedPref: SharedPreferences
    private var NAME_PREF = "com-example-techkis"
    private lateinit var prefUserFullname: String
    private lateinit var prefUserImage: String

    private lateinit var commentList: MutableList<CommentsModel>
    private lateinit var mCommentAdapter: NewsCommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_view)

        newsID = intent.getStringExtra("NEWS_ID").toString()
        mDatabase = Firebase.database.reference
        mAuth = Firebase.auth
        mUserID = mAuth.currentUser?.uid.toString()
        commentList = mutableListOf()
        var titleDialog = ""
        val currentUser = mAuth.currentUser

        setSupportActionBar(toolbar_newsView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        sharedPref = this.getSharedPreferences(NAME_PREF,Context.MODE_PRIVATE)

        getDataFromPref()
        setUIWithData()
        getLikes()
        getBookmark()
        getComment()

        btn_like_navViews.setOnClickListener {
            if(currentUser == null){
                titleDialog = "Failed to set like"
                showDialog(titleDialog)
            }
            else{
                if(mLike == false){
                    setLike()
                }
                else{
                    mDatabase.child("news").child(newsID).child("likes").child(mUserID).setValue(null)
                }
            }
        }
        btn_bookmark_newsView.setOnClickListener {

            if(currentUser == null){
                titleDialog = "Failed to set bookmark"
                showDialog(titleDialog)
            }
            else{
                if(mBookmark == false){
                    setBookmark()
                }
                else{
                    mDatabase.child("users").child(mUserID).child("bookmarks").child(newsID).setValue(null)
                }
            }
        }
        btn_comment_newsView.setOnClickListener {
            btn_comment_newsView.isEnabled = false
            validasiComment()
        }
    }

    private fun showDialog(title: String){
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            this.setTitle(title)
            this.setMessage("Sorry! You haven't signed yet, please sign in first :)")
            this.setPositiveButton("Go to Login",
                DialogInterface.OnClickListener { dialog, id ->
                    startActivity(Intent(this@NewsViewActivity,
                        LoginActivity::class.java))
            })
            this.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, i ->
                    dialog.cancel()
            })
        }
        alertDialogBuilder.create().show()
    }

    private fun getDataFromPref(){
        val fullName = sharedPref.getString("FULL_NAME","-").toString()
        val imageUrl = sharedPref.getString("IMAGE_URL","-").toString()
        if(fullName != "-"){
            prefUserFullname = fullName
            prefUserImage = imageUrl
        }

    }

    private fun validasiComment(){
        if(et_comment_newsView.text.isEmpty()){
            et_comment_newsView.setError("Anda belum mengisi apapun")
            return
        }
        sendComment()
    }

    private fun getComment(){
        mDatabase.child("news-comments").child(newsID).orderByChild("commentTimestamp")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        commentList.clear()
                        for (commentSnapshot in p0.children){
                            val asd = commentSnapshot.getValue(CommentsModel::class.java)
                            Log.w("TIMESTAMP",asd?.userID.toString())
                            commentList.add(asd!!)
                        }
                        commentList.reverse()

                        rv_comment_newsView.apply {
                            layoutManager = LinearLayoutManager(this@NewsViewActivity)
                            mCommentAdapter = NewsCommentAdapter()
                            mCommentAdapter.newsCommentAdapter(commentList)
                            adapter = mCommentAdapter
                        }
                    }
                }
            })
    }
    private fun sendComment(){
        val commentID = UUID.randomUUID().toString()
        val isiComment = et_comment_newsView.text.toString()
        val commentsTimestamp = System.currentTimeMillis() / 1000
        val comments = CommentsModel(commentID,newsID,mUserID,prefUserFullname,prefUserImage,isiComment,commentsTimestamp.toString())
        mDatabase.child("news-comments").child(newsID).child(commentID).setValue(comments)
            .addOnCompleteListener {
            if(it.isSuccessful){
                Log.w("SENT COMMENT","BERHASIL")
                startActivity(intent)
                finish()
                btn_comment_newsView.isEnabled = true
            }
            else{
                btn_comment_newsView.isEnabled = true
            }
        }
    }

    private fun getBookmark(){
        val usersBookmarkRef = mDatabase.child("users").child(mUserID).child("bookmarks").child(newsID)
        usersBookmarkRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        mBookmark = true
                        btn_bookmark_newsView.setBackgroundResource(R.drawable.ic_bookmark_clicked)
                    }
                    else{
                        mBookmark = false
                        btn_bookmark_newsView.setBackgroundResource(R.drawable.ic_bookmark_unclick)
                    }
                }
            })
    }
    private fun setBookmark(){
        val setBookmark = HashMap<String,Any>()
        setBookmark.put(newsID,true)
        mDatabase.child("users").child(mUserID).child("bookmarks").updateChildren(setBookmark)
    }

    private fun getLikes(){
        val likesRef =  mDatabase.child("news").child(newsID)
        likesRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
                override fun onDataChange(p0: DataSnapshot) {
                    var likes_count = 0
                    if(p0.child("likes").child(mUserID).exists()){
                        mLike = true
                        btn_like_navViews.setBackgroundResource(R.drawable.ic_favorite_clicked)
                    }
                    else{
                        mLike = false
                        btn_like_navViews.setBackgroundResource(R.drawable.ic_favorite_unclick)
                    }
                    if(p0.child("likes").exists()){
                        val likes = p0.child("likes")
                        for (likesSnapshot in likes.children){
                            likes_count = likes_count + 1
                        }
                    }
                    likesRef.child("likes_count").setValue(likes_count)
                    tv_likes_newsView.text = likes_count.toString()
                }
            })
    }
    private fun setLike(){
        val likeMap = HashMap<String,Any>()
        likeMap.put(mUserID,true)
        mDatabase.child("news").child(newsID).child("likes").updateChildren(likeMap)
    }

    private fun setUIWithData(){
        val newsTitle = intent.getStringExtra("NEWS_TITLE")
        val newsKonten = intent.getStringExtra("NEWS_KONTEN")
        val newsTimestamp = intent.getStringExtra("NEWS_TIMESTAMP")
        val newsImage = intent.getStringExtra("NEWS_IMAGE")

        val cal = Calendar.getInstance()
        cal.timeInMillis = newsTimestamp!!.toLong() * 1000L
        val newsDate = DateFormat.format("dd MMMM yyyy",cal).toString()

        tv_title_newsView.text = newsTitle
        tv_konten_newsView.text = newsKonten
        tv_tanggal_newsView.text = newsDate
        if(newsImage.toString().isNotEmpty()){
            Picasso.get()
                .load(newsImage)
                .into(iv_thumbnail_newsView)
        }
        else{
            Picasso.get()
                .load(R.drawable.default_thumbnail_news)
                .resize(1200,1200)
                .centerCrop()
                .into(iv_thumbnail_newsView)
        }
    }
}