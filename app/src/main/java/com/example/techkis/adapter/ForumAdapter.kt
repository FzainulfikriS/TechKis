package com.example.techkis.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.techkis.R
import com.example.techkis.model.ForumModel
import kotlinx.android.synthetic.main.rv_forum_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class ForumAdapter:RecyclerView.Adapter<ForumAdapter.ViewHolder>(),Filterable {

    private var dataForum: List<ForumModel> = ArrayList()
    private lateinit var allDataForum: List<ForumModel>
    private lateinit var itemClick: ItemClickListener

    interface ItemClickListener {
        fun onItemClickListener(forumModel: ForumModel)
    }

    inner class ViewHolder(view:View): RecyclerView.ViewHolder(view){
        val judulForum = view.tv_title_forum
        val namaAuthor = view.tv_author_forum
        val commentCount = view.tv_commentCount_forum
        val tanggalUpload = view.tv_tanggal_forum
        val jamUpload = view.tv_jam_forum

        fun onBind(forumModel: ForumModel, itemClickListener: ItemClickListener){
            judulForum.text = forumModel.judulForum
            namaAuthor.text = forumModel.namaAuthor
            commentCount.text = forumModel.commentCount.toString()
            val timestamp = Calendar.getInstance()
            timestamp.timeInMillis = forumModel.timestampForum * 1000L
            tanggalUpload.text = DateFormat.format("E, dd MMM yyyy",timestamp).toString()
            jamUpload.text = DateFormat.format("hh:mm a",timestamp).toString()

            itemView.setOnClickListener {
                itemClickListener.onItemClickListener(forumModel)
            }
        }
    }

    fun forumAdapter(forumList: List<ForumModel>,itemClickListener: ItemClickListener){
        dataForum = forumList
        allDataForum = ArrayList<ForumModel>(forumList)
        itemClick = itemClickListener
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
        holder.onBind(dataForum.get(position),itemClick)
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filteredList = ArrayList<ForumModel>()
                if(p0 == null || p0.length == 0){
                    filteredList.addAll(allDataForum)
                }else{
                    val filterPattern = p0.toString().toLowerCase().trim()
                    for(row in allDataForum){
                        if(row.judulForum.toLowerCase().contains(filterPattern)){
                            filteredList.add(row)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                dataForum = p1?.values as List<ForumModel>
                notifyDataSetChanged()
            }
        }
    }
}