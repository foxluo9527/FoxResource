package com.foxluo.chat.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.util.DialogUtil
import com.foxluo.baselib.util.DialogUtil.showConfirmDialog
import com.foxluo.baselib.util.ImageExt.loadUrl
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.data.database.MessageEntity
import com.foxluo.chat.databinding.ItemMessageImageBinding
import com.foxluo.chat.databinding.ItemMessageImageMineBinding
import com.foxluo.chat.databinding.ItemMessageTextBinding
import com.foxluo.chat.databinding.ItemMessageTextMineBinding
import com.foxluo.chat.databinding.LayoutPopMessageMenuBinding
import com.foxluo.chat.databinding.LayoutPopMessageMenuTextBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.popupwindow.easypopup.EasyPopup
import com.xuexiang.xui.widget.popupwindow.easypopup.HorizontalGravity
import com.xuexiang.xui.widget.popupwindow.easypopup.VerticalGravity

class MessageListAdapter(
    private val showUserDetail: (userId: Int) -> Unit,
    private val retrySend: (message: MessageEntity) -> Unit,
    private val cancelSend: (message: MessageEntity) -> Unit,
    private val showImage: (adapter: MessageListAdapter, message: MessageEntity, itemView: View) -> Unit,
    private val playVoice: (position: Int) -> Unit
) : PagingDataAdapter<MessageEntity, RecyclerView.ViewHolder>(MESSAGE_COMPARATOR) {
    companion object {
        private val MESSAGE_COMPARATOR = object : DiffUtil.ItemCallback<MessageEntity>() {
            override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: MessageEntity, newItem: MessageEntity
            ): Boolean = oldItem == newItem
        }
        private const val TYPE_TEXT = 1
        private const val TYPE_TEXT_MINE = 11
        private const val TYPE_FILE = 2
        private const val TYPE_FILE_MINE = 12
        private const val TYPE_VOICE = 3
        private const val TYPE_VOICE_MINE = 13
        private const val TYPE_VOICE_CALL = 4
        private const val TYPE_VOICE_CALL_MINE = 14
        private const val TYPE_VIDEO_CALL = 5
        private const val TYPE_VIDEO_CALL_MINE = 15
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val type = when (item?.type) {
            "text" -> TYPE_TEXT
            "file" -> TYPE_FILE
            else -> TYPE_TEXT
        }
        val myMessage = AuthManager.authInfo?.user?.id?.toInt() == item?.sender_id
        return type + if (myMessage) 10 else 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> TextHolder(ItemMessageTextBinding.inflate(inflater, parent, false))
            TYPE_FILE -> ImageHolder(ItemMessageImageBinding.inflate(inflater, parent, false))
            TYPE_TEXT_MINE -> TextMineHolder(
                ItemMessageTextMineBinding.inflate(
                    inflater, parent, false
                )
            )

            TYPE_FILE_MINE -> ImageMineHolder(
                ItemMessageImageMineBinding.inflate(
                    inflater, parent, false
                )
            )

            else -> TextHolder(ItemMessageTextBinding.inflate(inflater, parent, false))
        }
    }

    fun getDataList(): List<MessageEntity> {
        return Array<MessageEntity?>(itemCount) { getItem(it) }.mapNotNull { it }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int
    ) {
        when (holder) {
            is TextHolder -> holder.setData(position)
            is TextMineHolder -> holder.setData(position)
            is ImageHolder -> holder.setData(position)
            is ImageMineHolder -> holder.setData(position)
        }
    }

    inner class TextHolder(val binding: ItemMessageTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(position: Int) {
            val message = getItem(position) ?: return
            binding.head.loadUrl(message.sender_avatar)
            binding.content.text = message.content
            binding.content.visible(!(message.content.isNullOrEmpty()))
            binding.head.setOnClickListener {
                showUserDetail.invoke(message.sender_id)
            }
            binding.retry.setOnClickListener {
                retrySend.invoke(message)
            }
            binding.time.postDelayed({
                processSentTime(message, position, binding.time)
            }, 200)
            binding.retry.visible(message.sendStatus == 0)
            binding.sending.visible(message.sendStatus == -1)
            processContentLongClick(binding.content, message)
        }
    }

    inner class ImageHolder(val binding: ItemMessageImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(position: Int) {
            val message = getItem(position) ?: return
            binding.head.loadUrl(message.sender_avatar)
            binding.content.loadUrlWithCorner(message.getFileExistsPath(), 8)
            binding.videoFlag.visible(message.file_type == "video")
            binding.head.setOnClickListener {
                showUserDetail.invoke(message.sender_id)
            }
            binding.content.setOnClickListener {
                showImage.invoke(this@MessageListAdapter, message, binding.content)
            }
            binding.retry.setOnClickListener {
                retrySend.invoke(message)
            }
            binding.time.postDelayed({
                processSentTime(message, position, binding.time)
            }, 200)
            binding.retry.visible(message.sendStatus == 0)
            binding.sending.visible(message.sendStatus == -1)
            processContentLongClick(binding.content, message)
        }
    }

    inner class TextMineHolder(val binding: ItemMessageTextMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(position: Int) {
            val message = getItem(position) ?: return
            binding.head.loadUrl(message.sender_avatar)
            binding.content.text = message.content
            binding.content.visible(!(message.content.isNullOrEmpty()))
            binding.head.setOnClickListener {
                showUserDetail.invoke(message.sender_id)
            }
            binding.retry.setOnClickListener {
                retrySend.invoke(message)
            }
            binding.time.postDelayed({
                processSentTime(message, position, binding.time)
            }, 200)
            binding.retry.visible(message.sendStatus == 0)
            binding.sending.visible(message.sendStatus == -1)
            processContentLongClick(binding.content, message)
        }
    }

    inner class ImageMineHolder(val binding: ItemMessageImageMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(position: Int) {
            val message = getItem(position) ?: return
            binding.head.loadUrl(message.sender_avatar)
            binding.content.loadUrlWithCorner(message.getFileExistsPath(), 8)
            binding.head.setOnClickListener {
                showUserDetail.invoke(message.sender_id)
            }
            binding.content.setOnClickListener {
                showImage.invoke(this@MessageListAdapter, message, binding.content)
            }
            binding.retry.setOnClickListener {
                retrySend.invoke(message)
            }
            binding.videoFlag.visible(message.file_type == "video")
            binding.time.postDelayed({
                processSentTime(message, position, binding.time)
            }, 200)
            binding.retry.visible(message.sendStatus == 0)
            binding.sending.visible(message.sendStatus == -1)
            processContentLongClick(binding.content, message)
        }
    }

    private fun processSentTime(message: MessageEntity, position: Int, timeView: TextView) {
        val sentTime = message.sent_at
        val lastSentTime = if (position > 0) {
            getItem(position - 1)?.sent_at
        } else null
        if (sentTime == lastSentTime) {
            timeView.text = ""
        } else {
            timeView.text = sentTime
        }
    }

    private fun processContentLongClick(contentView: View, message: MessageEntity) {
        val myMessage = AuthManager.authInfo?.user?.id?.toInt() == message.sender_id
        val context = contentView.context
        contentView.setOnLongClickListener {
            val popup = EasyPopup(context)
            val deleteAction = {_:View->
                popup.dismiss()
                context.showConfirmDialog("是否确认删除此记录\n若对方未接收此消息将无法再接收"){
                    cancelSend.invoke(message)
                }
            }
            val binding = if (message.type == "text") LayoutPopMessageMenuTextBinding.inflate(
                LayoutInflater.from(context), null, false
            ).apply {
                delete.setOnClickListener(deleteAction)
                copy.setOnClickListener {
                    ClipboardUtils.copyText(message.content)
                    XToastUtils.success("已复制到剪切板")
                    popup.dismiss()
                }
                contentView.requestFocus()
            }
            else LayoutPopMessageMenuBinding.inflate(LayoutInflater.from(context), null, false).apply {
                delete.setOnClickListener(deleteAction)
            }
            popup.apply {
                setContentView<EasyPopup>(binding.root)
                setHeight<EasyPopup>(dp2px(42f))
                setFocusAndOutsideEnable<EasyPopup>(true)
                createPopup<EasyPopup>()
            }
            popup.showAtAnchorView(
                contentView,
                VerticalGravity.ALIGN_TOP,
                HorizontalGravity.CENTER,
                if (myMessage) -dp2px(5f) else dp2px(5f),
                -dp2px(42f)
            );
            true
        }
    }
}