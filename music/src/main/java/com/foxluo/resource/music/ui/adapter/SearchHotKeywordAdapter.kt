package com.foxluo.resource.music.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.resource.music.data.result.SearchHotKeyword
import com.foxluo.resource.music.databinding.ItemSearchHotKeywordBinding

class SearchHotKeywordAdapter : RecyclerView.Adapter<SearchHotKeywordAdapter.ViewHolder>() {

    private var dataList: List<SearchHotKeyword> = emptyList()

    private var onItemClickListener: ((SearchHotKeyword) -> Unit)? = null

    fun setOnItemClickListener(listener: (SearchHotKeyword) -> Unit) {
        this.onItemClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<SearchHotKeyword>) {
        this.dataList = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchHotKeywordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ItemSearchHotKeywordBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchHotKeyword) {
            binding.tvKeyword.text = item.keyword
            binding.tvSearchCount.text = item.search_count.toString()

            // 设置点击事件
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }
}