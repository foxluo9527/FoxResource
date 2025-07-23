package com.foxluo.chat.ui.adapter

import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.R
import com.foxluo.chat.data.result.UserSearchResult
import com.foxluo.chat.databinding.ItemUserBinding
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder

class SearchUserAdapter : BaseRecyclerAdapter<UserSearchResult>() {
    var itemClick: ((UserSearchResult) -> Unit)? = null

    override fun getItemLayoutId(type: Int): Int {
        return R.layout.item_user
    }

    override fun bindData(
        holder: RecyclerViewHolder,
        position: Int,
        data: UserSearchResult?
    ) {
        data ?: return
        val binding = ItemUserBinding.bind(holder.itemView)
        binding.head.loadUrlWithCorner(processUrl(data.avatar), 5)
        binding.name.text = data.mark?.ifEmpty { data.nickname } ?: data.nickname
        binding.message.text = data.signature
        binding.message.visible(!data.signature.isNullOrEmpty())
        binding.add.isEnabled = !data.is_friend && !data.is_requested
        if (data.is_friend) {
            binding.add.text = "已添加"
        } else if (data.is_requested) {
            binding.add.text = "已申请"
        } else {
            binding.add.setText(com.foxluo.baselib.R.string.add_friend)
        }
        binding.add.setOnClickListener {
            itemClick?.invoke(data)
        }
    }
}