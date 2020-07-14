package com.example.techkis.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techkis.R
import com.example.techkis.model.DiscussionForumModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rv_discussion_layout.view.*
import java.util.*

class ForumDiscussionAdapter:RecyclerView.Adapter<ForumDiscussionAdapter.ViewHolder>() {

    private lateinit var diskusiList: List<DiscussionForumModel>
    private lateinit var clickListener: ItemClickListener

    class ViewHolder(
        v:View
    ):RecyclerView.ViewHolder(v){
        val imageUser = v.iv_authorImage_discLayout
        val namaUser = v.tv_authorName_discLayout
        val timestampDisc = v.tv_timestampForum_discLayout
        val isiForum = v.tv_isi_discLayout

        fun onBind(discussionModel: DiscussionForumModel,clickListener: ItemClickListener,position: Int){
            namaUser.text = discussionModel.userNama
            isiForum.text = discussionModel.isiDiskusi
            val timestamp = Calendar.getInstance()
            timestamp.timeInMillis = discussionModel.timestampDiskusi * 1000
            timestampDisc.text = DateFormat.format("MMM, dd yyyy mm:hh a",timestamp).toString()
            Picasso.get().load(discussionModel.userImage).into(imageUser)

            itemView.setOnClickListener {
                clickListener.itemClickListener(discussionModel,position)
            }
        }
    }

    fun diskusiAdapter(diskusiList: List<DiscussionForumModel>,clickListener: ItemClickListener){
        this.diskusiList = diskusiList
        this.clickListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_discussion_layout,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return diskusiList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(diskusiList.get(position),clickListener,position)
    }

    interface ItemClickListener{
        fun itemClickListener(discussionModel: DiscussionForumModel,position: Int)
    }
}