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
import com.example.techkis.adapter.NewsAdapter
import com.example.techkis.model.NewsModel
import com.example.techkis.ui.admin.AddNewsActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.nav_header_layout.view.*

class HomeActivity : AppCompatActivity(), NewsAdapter.OnItemClickListener {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mMenu: Menu
    private lateinit var mNavHeaderView: View

    private var mNewsAdapter: NewsAdapter? = null
    private lateinit var sharedPref: SharedPreferences
    private lateinit var newsList: MutableList<NewsModel>

    private var NAME_PREF = "com-example-techkis"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference
        mMenu = navView_home.menu
        newsList = mutableListOf()
        mNavHeaderView = navView_home.getHeaderView(0)
        sharedPref = this.getSharedPreferences(NAME_PREF,Context.MODE_PRIVATE)

        getAllNewsFromFirebase()

        /** Setup toolbar **/
        setSupportActionBar(toolbar_home)
        supportActionBar?.setTitle("News / Artikel")

        /** Setup Drawer Navigation **/
        navView_home.bringToFront()
        navView_home.setCheckedItem(R.id.nav_home_menu)
        navView_home.setNavigationItemSelectedListener(navigationItemSelectedListener())
        val toggle = ActionBarDrawerToggle(this,drawerLayout_home,toolbar_home,
            R.string.openDrawerNavigation,
            R.string.closeDrawerNavigation
        )
        drawerLayout_home.addDrawerListener(toggle)
        toggle.syncState()

        getDataUserFromPref()
    }

    override fun onResume() {
        super.onResume()
        navView_home.setCheckedItem(R.id.nav_home_menu)
    }

    private fun getAllNewsFromFirebase(){
        mDatabase.child("news").orderByChild("timestamp").addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        newsList.clear()
                        for (newsSnapshot in p0.children){
                            val news = newsSnapshot.getValue(NewsModel::class.java)
                            newsList.add(news!!)
                        }
                        newsList.reverse()
                        rv_news_home.apply {
                            layoutManager = LinearLayoutManager(this@HomeActivity)
                            mNewsAdapter = NewsAdapter()
                            mNewsAdapter!!.newsAdapter(newsList,this@HomeActivity)
                            adapter = mNewsAdapter
                        }
                    }
                }
            }
        )
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
    override fun itemClickListener(newsModel: NewsModel) {
        val intent = Intent(this, NewsViewActivity::class.java)
        intent.putExtra("NEWS_ID",newsModel.id)
        intent.putExtra("NEWS_TITLE",newsModel.title)
        intent.putExtra("NEWS_KONTEN",newsModel.isiKonten)
        intent.putExtra("NEWS_TIMESTAMP",newsModel.timestamp)
        intent.putExtra("NEWS_IMAGE",newsModel.image_url)
        startActivity(intent)
    }

    /** BAGIAN NAVIGATION VIEW / DRAWER NAVIGATION DAN TOOLBAR **/
    private fun updateAdminUI(role: String){
        if(role == "admin"){
            val menuAdmin = mMenu.findItem(R.id.nav_admin_menu)
            menuAdmin.setVisible(true)
        }
    }
    fun navigationItemSelectedListener() = object : NavigationView.OnNavigationItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when(item.itemId){
                R.id.nav_forum_menu -> {
                    startActivity(Intent(this@HomeActivity,ForumActivity::class.java))
                    finish()
                }
                R.id.nav_profile_menu ->{
                    startActivity(Intent(this@HomeActivity,
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
                    startActivity(Intent(this@HomeActivity,
                        LoginActivity::class.java))
                }
                R.id.nav_addNews_menu -> startActivity(Intent(this@HomeActivity,
                    AddNewsActivity::class.java))
            }
            drawerLayout_home.closeDrawer(GravityCompat.START)
            return true
        }
    }
}