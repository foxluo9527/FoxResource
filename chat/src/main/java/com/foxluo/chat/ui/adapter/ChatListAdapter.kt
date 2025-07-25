package com.foxluo.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.foxluo.baselib.util.ImageExt.loadUrl
import com.foxluo.baselib.util.TimeUtil.getChatTime
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.R
import com.foxluo.chat.data.database.ChatEntity
import com.foxluo.chat.databinding.ItemChatBinding

class ChatListAdapter : PagingDataAdapter<ChatEntity, RecyclerView.ViewHolder>(CHAT_COMPARATOR) {
    var itemClickListener: ((ChatEntity) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        type: Int
    ): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        ) {}
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val binding = ItemChatBinding.bind(holder.itemView)
        val data = getItem(position) ?: return
        binding.head.loadUrl(data.chatAvatar)
        binding.name.text = data.chatName
        binding.content.text = data.lastMessagePreview
        binding.time.text = getChatTime(data.lastMessageAt ?: 0L)
        binding.root.setOnClickListener {
            itemClickListener?.invoke(data)
        }
        binding.state.visible(data.lastSendStatus != 1)

        binding.state.setImageResource(
            if (data.lastSendStatus == 0) {
                com.foxluo.baselib.R.drawable.ic_error
            } else {
                com.foxluo.baselib.R.drawable.ic_sending
            }
        )

    }

    companion object {
        private val CHAT_COMPARATOR = object : DiffUtil.ItemCallback<ChatEntity>() {
            override fun areItemsTheSame(oldItem: ChatEntity, newItem: ChatEntity): Boolean =
                oldItem.chatId == newItem.chatId

            override fun areContentsTheSame(
                oldItem: ChatEntity,
                newItem: ChatEntity
            ): Boolean =
                oldItem == newItem
        }
    }
}