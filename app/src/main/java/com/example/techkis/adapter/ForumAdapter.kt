package com.example.techkis.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techkis.R
import com.example.techkis.model.ForumModel
import kotlinx.android.synthetic.main.rv_forum_layout.view.*
import java.util.*

class ForumAdapter:RecyclerView.Adapter<ForumAdapter.ViewHolder>() {

    private var dataForum: List<ForumModel> = ArrayList()

    inner class ViewHolder(view:View): RecyclerView.ViewHolder(view){
        val judulForum = view.tv_title_forum
        val namaAuthor = view.tv_author_forum
        val commentCount = view.tv_commentCount_forum
        val tanggalUpload = view.tv_tanggal_forum
        val jamUpload = view.tv_jam_forum

        fun onBind(forumModel: ForumModel){
            judulForum.text = forumModel.judulForum
            namaAuthor.text = forumModel.namaAuthor
            commentCount.text = forumModel.commentCount.toString()
            val timestamp = Calendar.getInstance()
            timestamp.timeInMillis = forumModel.timestampForum * 1000L
            tanggalUpload.text = DateFormat.format("E, dd MMM yyyy",timestamp).toString()
            jamUpload.text = DateFormat.format("mm:hh a",timestamp).toString()
        }
    }

    fun forumAdapter(forumList: List<ForumModel>){
        dataForum = forumList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_forum_layout,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return dataForum.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(dataForum.get(position))
    }
}