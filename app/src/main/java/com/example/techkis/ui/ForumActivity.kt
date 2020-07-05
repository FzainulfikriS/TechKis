package com.example.techkis.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techkis.R
import com.example.techkis.adapter.ForumAdapter
import com.example.techkis.model.ForumModel
import com.example.techkis.ui.admin.AddNewsActivity
import com.example.techkis.ui.forum.AddForumActivity
import com.example.techkis.ui.users.LoginActivity
import com.example.techkis.ui.users.ProfileActivity
import com.google.android.material.navigation.NavigationView
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
import kotlinx.android.synthetic.main.activity_add_forum.*
import kotlinx.android.synthetic.main.activity_forum.*
import kotlinx.android.synthetic.main.activity_forum.btn_addForum_forum
import kotlinx.android.synthetic.main.nav_header_layout.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ForumActivity : AppCompatActivity() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mMenu: Menu
    private lateinit var mNavHeaderView: View
    private lateinit var mForumAdapter: ForumAdapter

    private lateinit var sharedPref: SharedPreferences
    private var NAME_PREF = "com-example-techkis"

    private lateinit var arrayListForum: ArrayList<ForumModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)

        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        mMenu = navView_forum.menu
        mNavHeaderView = navView_forum.getHeaderView(0)
        sharedPref = this.getSharedPreferences(NAME_PREF, Context.MODE_PRIVATE)

        getDataUserFromPref()

        /** SET TOOLBAR **/
        setSupportActionBar(toolbar_forum)
        supportActionBar?.setTitle("Froum")

        /** SET DRAWER NAVIGATION **/
        navView_forum.bringToFront()
        navView_forum.setCheckedItem(R.id.nav_forum_menu)
        val menuToggle = ActionBarDrawerToggle(this,drawerLayout_forum,toolbar_forum,
            R.string.openDrawerNavigation,R.string.closeDrawerNavigation)
        drawerLayout_forum.addDrawerListener(menuToggle)
        menuToggle.syncState()
        navView_forum.setNavigationItemSelectedListener(onNavigationItemSelectedListener())

        btn_addForum_forum.setOnClickListener {
            val intent = Intent(this, AddForumActivity::class.java)
            startActivity(intent)
        }

        arrayListForum = arrayListOf()

        getDataForumFromDB()
    }

    private fun getDataForumFromDB(){
        mDatabase.child("forums").orderByChild("timestampForum").addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        arrayListForum.clear()
                        for (forumSnapshot in p0.children){
                            val forums = forumSnapshot.getValue(ForumModel::class.java)
                            arrayListForum.add(forums!!)

                        }
                        arrayListForum.reverse()
                        initForumRecyclerView(arrayListForum)
                    }
                }
            }
        )
    }

    private fun initForumRecyclerView(listForum: ArrayList<ForumModel>){
        println("Data_result: $listForum")
        rv_listForum_forum.apply {
            layoutManager = LinearLayoutManager(this@ForumActivity)
            mForumAdapter = ForumAdapter()
            mForumAdapter.forumAdapter(listForum)
            this.adapter = mForumAdapter
        }

    }

    /** BAGIAN NAVIGATION VIEW / DRAWER NAVIGATION DAN TOOLBAR **/
    private fun onNavigationItemSelectedListener() = object : NavigationView.OnNavigationItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when(item.itemId){
                R.id.nav_home_menu -> {
                    val intent = Intent(this@ForumActivity,HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_profile_menu ->{
                    startActivity(Intent(this@ForumActivity,
                        ProfileActivity::class.java))
                }
                R.id.nav_signout_menu ->{
                    mAuth.signOut()
                    val editorPref = sharedPref.edit()
                    editorPref.clear()
                    editorPref.apply()
                    finish()
                    startActivity(intent)
                }
                R.id.nav_signin_menu ->{
                    startActivity(Intent(this@ForumActivity,
                        LoginActivity::class.java))
                }
                R.id.nav_addNews_menu -> startActivity(Intent(this@ForumActivity,
                    AddNewsActivity::class.java))
            }
            drawerLayout_forum.closeDrawer(GravityCompat.START)
            return true
        }
    }

    private fun getDataUserFromPref(){
        val uid = sharedPref.getString("USER_ID",null)
        val fullName = sharedPref.getString("FULL_NAME",null)
        val email = sharedPref.getString("USER_EMAIL",null)
        val imageUrl = sharedPref.getString("IMAGE_URL","-").toString()
        val role = sharedPref.getString("ROLE","-")
        if(uid == null){
            Log.w("USER_ID_PREF : ","Kosong")
        }
        else{
            mMenu.findItem(R.id.nav_signout_menu).setVisible(true)
            mMenu.findItem(R.id.nav_profile_menu).setVisible(true)
            mMenu.findItem(R.id.nav_bookmark_menu).setVisible(true)
            mMenu.findItem(R.id.nav_signin_menu).setVisible(false)
            mNavHeaderView.imageProfile_navHeader.visibility = View.VISIBLE
            mNavHeaderView.name_navHeader.visibility = View.VISIBLE
            mNavHeaderView.email_navHeader.visibility = View.VISIBLE
            mNavHeaderView.notloggedin_navHeader.visibility = View.GONE

            mNavHeaderView.name_navHeader.text = fullName
            mNavHeaderView.email_navHeader.text = email
            Picasso.get().load(imageUrl).into(mNavHeaderView.imageProfile_navHeader)
            Log.w("USER_ID_PREF : ", uid)
            Log.w("ROLE-HOME",role!!)
            updateAdminUI(role)
        }
    }

    private fun updateAdminUI(role: String){
        if(role == "admin"){
            val menuAdmin = mMenu.findItem(R.id.nav_admin_menu)
            menuAdmin.setVisible(true)
        }
    }
}