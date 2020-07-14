package com.example.techkis.adapter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.techkis.R
import com.example.techkis.model.NewsModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rv_news_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.ViewHolder>(), Filterable {

    private lateinit var items: List<NewsModel>
    private lateinit var itemsAll: List<NewsModel>
    private lateinit var mItemClickListener: OnItemClickListener

    class ViewHolder constructor(
        v:View
    ):RecyclerView.ViewHolder(v){
        val title = v.tv_title_rvNewsLayout
        val tanggal = v.tv_tanggal_rvNewsLayout
        val isiKonten = v.tv_isiKonten_rvNewsLayout
        val thumbnail = v.iv_thumbnail_rvNewsLayout

        fun onBind(newsModel: NewsModel, clickListener: OnItemClickListener){
            val cal = Calendar.getInstance()
            cal.timeInMillis = newsModel.timestamp.toLong() * 1000

            title.text = newsModel.title
            isiKonten.text = newsModel.isiKonten
            tanggal.text = DateFormat.format("dd MMMM yyyy | H:mm a", cal).toString()
            if(newsModel.image_url.isNotEmpty()){
                Picasso.get()
                    .load(newsModel.image_url)
                    .resize(500,500)
                    .centerCrop()
                    .into(thumbnail)
            }
            else{
                Picasso.get()
                    .load(R.drawable.default_thumbnail_news)
                    .resize(500,500)
                    .centerCrop()
                    .into(thumbnail)
            }

            itemView.setOnClickListener {
                clickListener.itemClickListener(newsModel)
            }
        }
    }

    fun newsAdapter(newsModel: List<NewsModel>, clickListener: OnItemClickListener){
        items = newsModel
        mItemClickListener = clickListener
        itemsAll = ArrayList<NewsModel>(newsModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_news_layout,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(items.get(position),mItemClickListener)
    }

    interface OnItemClickListener{
        fun itemClickListener(newsModel: NewsModel)
    }

    override fun getFilter(): Filter {
        return itemFilter()
    }

    private fun itemFilter() = object : Filter(){
        @SuppressLint("DefaultLocale")
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val filteredList = ArrayList<NewsModel>()
            if(p0 == null || p0.length == 0){
                filteredList.addAll(itemsAll)
            }else{
                val filterPattern = p0.toString().toLowerCase().trim()
//                itemsAll.forEach {
//                    if(it.title.toLowerCase().contains(filterPattern)){
//                        filteredList.add(it)
//                    }
//                }
                for (row in itemsAll){
                    if(row.title.toLowerCase().contains(filterPattern)){
                        filteredList.add(row)
                    }
                }
            }
            val result = FilterResults()
            result.values = filteredList
            return result
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            items = p1?.values as List<NewsModel>
            notifyDataSetChanged()
        }
    }
}