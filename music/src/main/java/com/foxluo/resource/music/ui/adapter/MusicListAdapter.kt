package com.foxluo.resource.music.ui.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.databinding.ItemMusicListBinding

class MusicListAdapter(
    val moreVisible: Boolean,
    val onItemClick: (Int) -> Unit,
    val onMoreClick: ((Int) -> Unit)? = null,
    val onSelectChanged : () -> Unit = {}
) :
    PagingDataAdapter<MusicEntity, MusicListAdapter.MusicListViewHolder>(MUSIC_COMPARATOR) {
    companion object {
        private val MUSIC_COMPARATOR = object : DiffUtil.ItemCallback<MusicEntity>() {
            override fun areItemsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
                oldItem.musicId == newItem.musicId

            override fun areContentsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
                oldItem == newItem
        }
    }

    fun getPlayList() = (if (itemCount > 0) Array<MusicEntity>(itemCount) { getItem(it)!! }.toList()
    else listOf<MusicEntity>()).toMutableList()

    /**
     * 获取指定位置的音乐数据
     */
    fun getItemData(position: Int): MusicEntity? {
        return getItem(position)
    }

    val selectCount : Int
        get() = (0 until itemCount).count { getItem(it)?.isSelect == true }

    fun getSelectedList() = (0 until itemCount).filter { getItem(it)?.isSelect == true }.map { getItem(it)!! }

    fun hasSelected() = (0 until itemCount).any { getItem(it)?.isSelect == true }

    val isSelectAll : Boolean
        get() = (0 until itemCount).all { getItem(it)?.isSelect == true }

    var isSelectModel: Boolean = false
        set(value) {
            if (value){
                for (i in 0 until itemCount){
                    getItem(i)?.isSelect = false
                }
            }
            field = value
            notifyItemRangeChanged(0, itemCount, value)
            onSelectChanged.invoke()
        }

    fun allSelect(){
        val allSelectStatus = !isSelectAll
        for (i in 0 until itemCount){
            getItem(i)?.isSelect = allSelectStatus
        }
        notifyItemRangeChanged(0, itemCount, allSelectStatus)
        onSelectChanged.invoke()
    }

    private var currentIndex: Int? = null
        set(value) {
            val lastCurrentIndex = field
            field = value
            value?.let { notifyItemChanged(it) }
            lastCurrentIndex?.let { notifyItemChanged(it) }
        }

    private var currentMusicId: String? = null
        set(value) {
            field = value
            val musicList = getPlayList()
            currentIndex = musicList.indexOfFirst { it.musicId == value }
        }

    fun updateCurrentIndex(currentMusicId: String?) {
        if (this.currentMusicId != currentMusicId)
            this.currentMusicId = currentMusicId
    }

    inner class MusicListViewHolder(val binding: ItemMusicListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: MusicEntity) {
            binding.root.setBackgroundResource(if (currentMusicId == data.musicId) R.color.F7F7F7 else R.color.white)
            binding.cover.loadUrlWithCorner(processUrl(data.coverImg), 6)
            binding.more.visible(moreVisible && !isSelectModel)
            if (data.url.isNullOrEmpty()) {
                binding.name.text = SpannableString(data.title).apply {
                    setSpan(StrikethroughSpan(), 0, data.title?.length?:0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
                binding.singer.text = SpannableString(data.artist?.name).apply {
                    setSpan(StrikethroughSpan(), 0, data.artist?.name?.length?:0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            } else {
                binding.name.text = data.title
                binding.singer.text = data.artist?.name
            }
            binding.rbSelect.visible(isSelectModel)
            binding.rbSelect.isChecked = data.isSelect
        }

        fun refreshSelectStatus(data: MusicEntity){
            binding.rbSelect.visible(isSelectModel)
            binding.rbSelect.isChecked = data.isSelect
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicListBinding.inflate(inflater, parent, false)
        return MusicListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicListViewHolder, position: Int) {
        val data = getItem(position) ?: return
        holder.setData(data)
        holder.binding.more.setOnClickListener {
            if (!isSelectModel){
                onMoreClick?.invoke(position) ?: onItemClick.invoke(position)
            }
        }
        holder.binding.root.setOnClickListener {
            if (!isSelectModel){
                onItemClick.invoke(position)
            }else{
                data.isSelect = !data.isSelect
                holder.refreshSelectStatus(data)
                onSelectChanged.invoke()
            }
        }
        holder.binding.rbSelect.setOnClickListener {
            data.isSelect = !data.isSelect
            holder.refreshSelectStatus(data)
            onSelectChanged.invoke()
        }
    }

    override fun onBindViewHolder(
        holder: MusicListViewHolder,
        position: Int,
        payloads: List<Any?>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isNotEmpty()){
            val data = getItem(position) ?: return
            holder.refreshSelectStatus(data)
        }
    }
}