package com.foxluo.baselib.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.loadUrlWithCircle
import com.foxluo.baselib.util.ViewExt.visible
import com.xuexiang.xui.widget.textview.ExpandableTextView

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentHolder>() {
    private var listener: CommentClickListener? = null

    interface CommentClickListener {
        fun userClick(userId: String)
        fun contentClick(id: String)
        fun expandMore(id: String)
        fun likeClick(id: String)
    }

    data class CommentBean(
        val id: String,
        val userId: String,
        val name: String,
        val head: String?,
        val time: String,
        val likeCount: Int,
        val isLike: Boolean,
        val content: String,
        val hadMore: Boolean,
        val isReplay: Boolean,
        val replayToUserId: String?,
        val replayToName: String?
    )

    open inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val head: ImageView = itemView.findViewById(R.id.head)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val content: ExpandableTextView = itemView.findViewById(R.id.content)
        private val likeCount: TextView = itemView.findViewById(R.id.like)
        open fun setData(data: CommentBean) {
            head.loadUrlWithCircle(data.head)
            name.text = data.name
            time.text = data.time
            likeCount.text = if (data.likeCount > 0) data.likeCount.toString() else ""
            likeCount.isSelected = data.isLike
            content.text = data.content
            itemView.setOnClickListener {
                listener?.contentClick(data.id)
            }
            head.setOnClickListener {
                listener?.userClick(data.userId)
            }
            likeCount.setOnClickListener {
                listener?.likeClick(data.id)
            }
        }
    }

    inner class ReplayCommentHolder(itemView: View) : CommentHolder(itemView) {
        private val replayToName: TextView = itemView.findViewById(R.id.replay_to_name)
        private val replayToView: View = itemView.findViewById(R.id.replay_to_view)
        private val moreView: View = itemView.findViewById(R.id.more_view)
        override fun setData(data: CommentBean) {
            super.setData(data)
            replayToView.visible(data.replayToName != null)
            data.replayToName?.let { replayToName.text = it }
            replayToName.setOnClickListener {
                data.replayToUserId?.let { id -> listener?.userClick(id) }
            }
            moreView.visible(data.hadMore)
            moreView.setOnClickListener {
                listener?.expandMore(data.id)
            }
        }
    }

    private val dataList = mutableListOf<CommentBean>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentAdapter.CommentHolder {
        return if (viewType > 0)
            LayoutInflater
                .from(parent.context).inflate(R.layout.item_comment_replay, parent, false)
                .let {
                    ReplayCommentHolder(it)
                }
        else
            LayoutInflater
                .from(parent.context).inflate(R.layout.item_comment, parent, false)
                .let {
                    CommentHolder(it)
                }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position].isReplay) 1 else 0
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: CommentAdapter.CommentHolder, position: Int) {
        holder.setData(dataList[position])
    }

    fun setCommentClickListener(listener: CommentClickListener) {
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCommentListData(list: List<CommentBean>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun appendCommentListData(list: List<CommentBean>) {
        val oldSize = dataList.size
        dataList.addAll(list)
        notifyItemRangeInserted(oldSize, list.size)
    }

    fun insertReplayListData(insetCommentId: String, list: List<CommentBean>) {
        val insertCommentBean = dataList.find { it.id == insetCommentId } ?: return
        val insertCommentPosition = dataList.indexOf(insertCommentBean) + 1
        var insertPosition = insertCommentPosition
        for (i in insertCommentPosition..dataList.size) {
            insertPosition = i
            if (!dataList[i].isReplay) {
                break
            }
        }
        dataList.addAll(insertPosition, list)
        notifyItemRangeInserted(insertPosition, list.size)
    }
}