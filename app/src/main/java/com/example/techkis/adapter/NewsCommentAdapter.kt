package com.example.techkis.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techkis.R
import com.example.techkis.model.CommentsModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rv_comments_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class NewsCommentAdapter:RecyclerView.Adapter<NewsCommentAdapter.ViewHolder>() {

    private var items: List<CommentsModel> = ArrayList()
    private lateinit var mItemClickListener: ItemClickListener

    class ViewHolder constructor(
        v: View
    ): RecyclerView.ViewHolder(v){
        val fullName = v.tv_username_rvComments
        val comment = v.tv_comment_rv_Comments
        val tanggal = v.tv_tanggal_rvComments
        val imageUser = v.iv_imageUser_rvComments

        fun bind(commentsModel: CommentsModel, clickListener:ItemClickListener){
            val cal = Calendar.getInstance()
            cal.timeInMillis = commentsModel.commentTimestamp.toLong() * 1000
            fullName.text = commentsModel.userFullname
            comment.text = commentsModel.isiComment
            tanggal.text = DateFormat.format("dd MMMM yyyy H:mm",cal).toString()
            Picasso.get().load(commentsModel.userImage).into(imageUser)

            itemView.setOnClickListener {
                clickListener.itemClickListener(commentsModel)
            }
        }
    }

    fun newsCommentAdapter(commentsModel: List<CommentsModel>, clickListener: ItemClickListener){
        items = commentsModel
        mItemClickListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_comments_layout,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items.get(position),mItemClickListener)
    }

    interface ItemClickListener{
        fun itemClickListener(commentsModel: CommentsModel)
    }
}