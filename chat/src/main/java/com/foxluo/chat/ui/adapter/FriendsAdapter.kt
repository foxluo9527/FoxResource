// chat/src/main/java/com/foxluo/chat/ui/adapter/FriendsAdapter.kt
package com.foxluo.chat.ui.adapter

import android.widget.TextView
import com.foxluo.baselib.ui.view.LetterView
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.R
import com.foxluo.chat.data.result.FriendResult
import com.foxluo.chat.databinding.ItemFriendNameFirstBinding
import com.foxluo.chat.databinding.ItemFriendsBinding
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder
import net.sourceforge.pinyin4j.PinyinHelper

class FriendsAdapter : BaseRecyclerAdapter<FriendsAdapter.Item>() {
    private var letterView: LetterView? = null

    // 密封类定义列表项类型
    sealed class Item {
        object AddFriend : Item()                      // 添加好友项
        data class LetterTitle(val letter: Char) : Item() // 字母标题
        data class FriendItem(val data: FriendResult) : Item() // 好友项
        data class EmptyItem(var emptyText: String = "好友列表为空") : Item()
    }

    fun setLetterView(letterView: LetterView) {
        this.letterView = letterView
    }

    // 分组后的数据列表
    private val groupedItems = mutableListOf<Item>()

    // 设置原始数据并自动分组
    fun setFriendData(originList: List<FriendResult>) {
        groupedItems.clear()

        // 1. 添加"添加好友"项
        groupedItems.add(Item.AddFriend)

        // 2. 分组处理
        if (originList.isNotEmpty()) {
            val letters = originList
                .groupBy { getFirstLetter(it.mark?.ifEmpty { it.nickname } ?: it.nickname) }
                .toSortedMap()
                .map { (letter, list) ->
                    // 添加字母标题
                    groupedItems.add(Item.LetterTitle(letter))
                    // 添加该组好友
                    list.sortedBy { it.mark?.ifEmpty { it.nickname } ?: it.nickname }.forEach {
                        groupedItems.add(Item.FriendItem(it))
                    }
                    letter.toString()
                }.toMutableList()
            letterView?.setData(letters)
        } else {
            groupedItems.add(Item.EmptyItem())
            letterView?.setData(mutableListOf<String>())
        }
        // 3. 通知数据更新
        refresh(groupedItems)
    }

    override fun getItemLayoutId(viewType: Int): Int {
        return when (viewType) {
            TYPE_ADD -> R.layout.item_friend_add
            TYPE_LETTER -> R.layout.item_friend_name_first
            TYPE_FRIEND -> R.layout.item_friends
            TYPE_EMPTY -> R.layout.item_friend_empty
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun bindData(holder: RecyclerViewHolder, pos: Int, item: Item?) {
        when (val data = item ?: return) {
            is Item.LetterTitle -> bindLetter(holder, data.letter)
            is Item.FriendItem -> bindFriend(holder, data.data)
            is Item.EmptyItem -> (holder.itemView as TextView).text = data.emptyText
            else -> Unit
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (groupedItems.getOrNull(position)) {
            is Item.AddFriend -> TYPE_ADD
            is Item.LetterTitle -> TYPE_LETTER
            is Item.FriendItem -> TYPE_FRIEND
            is Item.EmptyItem -> TYPE_EMPTY
            else -> super.getItemViewType(position)
        }
    }

    override fun getItemCount(): Int = groupedItems.size

    private fun bindLetter(holder: RecyclerViewHolder, letter: Char) {
        val binding = ItemFriendNameFirstBinding.bind(holder.itemView)
        binding.root.text = letter.toString()
    }

    private fun bindFriend(holder: RecyclerViewHolder, data: FriendResult) {
        val binding = ItemFriendsBinding.bind(holder.itemView)
        binding.head.loadUrlWithCorner(processUrl(data.avatar), 5)
        binding.name.text = data.mark?.ifEmpty { data.nickname } ?: data.nickname
        binding.message.visible(false)
    }

    // 首字母处理（支持中文转拼音）
    private fun getFirstLetter(nickname: String?): Char {
        val firstChar = nickname?.firstOrNull() ?: '#'
        return when {
            firstChar in 'A'..'Z' -> firstChar
            firstChar in 'a'..'z' -> firstChar.uppercaseChar()
            isChinese(firstChar) -> PinyinHelper.toHanyuPinyinStringArray(firstChar)
                .first().first().uppercaseChar()

            else -> '#'
        }
    }

    private fun isChinese(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
    }

    companion object {
        private const val TYPE_ADD = 1
        const val TYPE_LETTER = 2
        private const val TYPE_FRIEND = 3
        private const val TYPE_EMPTY = 4
    }
}