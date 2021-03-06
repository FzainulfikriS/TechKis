package com.pukis.techkis.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.pukis.techkis.R
import com.pukis.techkis.adapter.ForumAdapter
import com.pukis.techkis.model.ForumModel
import com.pukis.techkis.model.UsersModel
import com.pukis.techkis.ui.admin.AddNewsActivity
import com.pukis.techkis.ui.forum.AddForumActivity
import com.pukis.techkis.ui.forum.DetailForumActivity
import com.pukis.techkis.ui.users.LoginActivity
import com.pukis.techkis.ui.users.ProfileActivity
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
import kotlinx.android.synthetic.main.activity_forum.*
import kotlinx.android.synthetic.main.activity_forum.btn_addForum_forum
import kotlinx.android.synthetic.main.nav_header_layout.view.*
import kotlin.collections.ArrayList

class ForumActivity : AppCompatActivity(), ForumAdapter.ItemClickListener {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mMenu: Menu
    private lateinit var mNavHeaderView: View
    private lateinit var mForumAdapter: ForumAdapter

    private lateinit var sharedPref: SharedPreferences
    private var NAME_PREF = "com-pukis-techkis"

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

        /** SET DRAWER NAVIGATION **/
        navView_forum.bringToFront()
        navView_forum.setCheckedItem(R.id.nav_forum_menu)
        val menuToggle = ActionBarDrawerToggle(this,drawerLayout_forum,toolbar_forum,
            R.string.openDrawerNavigation,R.string.closeDrawerNavigation)
        drawerLayout_forum.addDrawerListener(menuToggle)
        menuToggle.syncState()
        navView_forum.setNavigationItemSelectedListener(onNavigationItemSelectedListener())

        var titleDialog = ""
        val currentUser = mAuth.currentUser
        btn_addForum_forum.setOnClickListener {
            if(currentUser == null){
                titleDialog = "You cannot create a forum"
                showDialog(titleDialog)
            }
            else{
                val intent = Intent(this, AddForumActivity::class.java)
                startActivity(intent)
            }
        }

        arrayListForum = arrayListOf()

        getDataForumFromDB()

        btn_searchForum_forum.setOnClickListener {
            if(lin_search_forum.visibility == View.GONE){
                lin_search_forum.visibility = View.VISIBLE
            }else lin_search_forum.visibility = View.GONE
        }

        sv_forum_forum.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                mForumAdapter.filter.filter(newText)
                return false
            }
        })
    }
    private fun showDialog(title: String){
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            this.setTitle(title)
            this.setMessage("Sorry! You haven't signed yet, please sign in first :)")
            this.setPositiveButton("Go to Login",
                DialogInterface.OnClickListener { dialog, id ->
                    startActivity(Intent(this@ForumActivity,
                        LoginActivity::class.java))
                })
            this.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, i ->
                    dialog.cancel()
                })
        }
        alertDialogBuilder.create().show()
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
                            getDataUser(forums!!)
                        }
                    }
                }
            }
        )
    }

    private fun getDataUser(dataForum: ForumModel){
        val userID = dataForum.authorForumID
        val forumID = dataForum.forumID
        val judulForum = dataForum.judulForum
        val isiForum = dataForum.isiForum
        val commentCount = dataForum.commentCount
        val timestampForum = dataForum.timestampForum
        mDatabase.child("users").child(userID).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    val users = p0.getValue(UsersModel::class.java)
                    arrayListForum.add(
                        ForumModel(forumID,judulForum,isiForum,userID,users?.fullName.toString(),commentCount,timestampForum)
                    )
                    initForumRecyclerView(arrayListForum)
                }
            }
        )
    }

    private fun initForumRecyclerView(listForum: ArrayList<ForumModel>){
        listForum.reverse()
        println("Data_result: $listForum")
        rv_listForum_forum.apply {
            layoutManager = LinearLayoutManager(this@ForumActivity)
            mForumAdapter = ForumAdapter()
            mForumAdapter.forumAdapter(listForum,this@ForumActivity)
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

    override fun onItemClickListener(forumModel: ForumModel) {
        val intent = Intent(this, DetailForumActivity::class.java)
        intent.putExtra("FORUM_ID",forumModel.forumID)
        startActivity(intent)
    }
}