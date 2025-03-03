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
        fun contentClick(comment: CommentBean)
        fun expandMore(id: String)
        fun likeClick(id: String)
    }

    data class CommentBean(
        val id: String,
        val userId: String,
        val name: String,
        val head: String?,
        val time: String,
        var likeCount: Int,
        var isLike: Boolean,
        val content: String,
        var hadMore: Boolean,
        val isReplay: Boolean,
        val parentId: String?,
        val replayToUserId: String?,
        val replayToName: String?,
        val replayToContent: String?,
        var page: Int = 0,
        var replyCount: Int,
        var displayReplyCount: Int = 0
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
                listener?.contentClick(data)
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
        private val replayToContent: TextView = itemView.findViewById(R.id.replay_to_content)
        private val replayToView: View = itemView.findViewById(R.id.replay_to_view)
        private val moreView: View = itemView.findViewById(R.id.more_view)
        override fun setData(data: CommentBean) {
            super.setData(data)
            replayToView.visible(data.replayToUserId != null)
            data.replayToName?.let { replayToName.text = it }
            data.replayToContent?.let { replayToContent.text = it }
            replayToName.setOnClickListener {
                data.replayToUserId?.let { id -> listener?.userClick(id) }
            }
            moreView.visible(data.hadMore)
            moreView.setOnClickListener {
                data.parentId?.let { commentId -> listener?.expandMore(commentId) }
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

    fun insertReplayListData(insertCommentId: String, list: List<CommentBean>) {
        val insertCommentBean = dataList.find { it.id == insertCommentId } ?: return
        val insertCommentPosition = dataList.indexOf(insertCommentBean) + 1
        var insertPosition = insertCommentPosition
        for (i in insertCommentPosition..dataList.size) {
            insertPosition = i
            if (dataList.getOrNull(i)?.isReplay == false) {
                break
            }
        }
        var startPosition = -1
        for (i in insertPosition - 1 downTo 0) {
            if (dataList.getOrNull(i)?.isReplay == false) break
            startPosition = i
        }
        dataList[insertPosition - 1] = dataList[insertPosition - 1].apply {
            hadMore = false
        }
        notifyItemChanged(insertPosition - 1)
        val insertList = list.toMutableList()
            .apply { removeIf { dataList.find { data -> data.id == it.id } != null } }
        dataList.addAll(insertPosition, insertList)
        notifyItemRangeInserted(insertPosition, insertList.size)
        insertCommentBean.displayReplyCount = startPosition + insertList.size + 1
    }

    fun likeStateChanged(commentId: String) {
        val comment = dataList.find { it.id == commentId } ?: return
        val commentIndex = dataList.indexOf(comment)
        dataList[commentIndex] = comment.apply {
            comment.isLike = comment.isLike.not()
            comment.likeCount += if (comment.isLike) 1 else -1
        }
        notifyItemChanged(commentIndex)
    }
}