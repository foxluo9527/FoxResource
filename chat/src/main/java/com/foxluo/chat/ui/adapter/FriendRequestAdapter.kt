package com.foxluo.chat.ui.adapter

import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.R
import com.foxluo.chat.data.result.FriendRequestResult
import com.foxluo.chat.data.result.UserSearchResult
import com.foxluo.chat.databinding.ItemUserBinding
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder

class FriendRequestAdapter : BaseRecyclerAdapter<FriendRequestResult>() {
    var itemClick: ((FriendRequestResult) -> Unit)? = null

    override fun getItemLayoutId(type: Int): Int {
        return R.layout.item_user
    }

    override fun bindData(
        holder: RecyclerViewHolder,
        position: Int,
        data: FriendRequestResult?
    ) {
        data ?: return
        val binding = ItemUserBinding.bind(holder.itemView)
        binding.head.loadUrlWithCorner(processUrl(data.avatar), 5)
        binding.name.text = data.nickname
        binding.message.text = data.message
        binding.message.visible(!data.message.isNullOrEmpty())
        binding.add.isEnabled = true
        binding.add.text = "同意申请"
        binding.add.setOnClickListener {
            itemClick?.invoke(data)
        }
    }
}