package com.foxluo.chat.ui

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.chat.data.domain.viewmodel.MessageViewModel
import com.foxluo.chat.databinding.FragmentChatBinding
import com.foxluo.chat.ui.adapter.ChatListAdapter
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatFragment : BaseBindingFragment<FragmentChatBinding>() {
    private val viewModel by viewModels<MessageViewModel>()

    override fun initBinding() = FragmentChatBinding.inflate(layoutInflater)

    override fun initView() {
        binding.refresh.setEnableLoadMore(false)
        binding.recycleView.adapter = adapter
    }

    private val adapter by lazy { ChatListAdapter() }

    override fun initObserver() {
        viewModel.isLoading.observe(this) {
            setLoading(it)
        }
        viewModel.toast.observe(this) {
            toast(it.second)
        }
        lifecycleScope.launch {
            viewModel.chatPager.collectLatest {
                adapter.submitData(it)
            }
        }
        lifecycleScope.launch {
            AuthManager.userInfoStateFlow.collectLatest {
                if (it != null) {
                    viewModel.loadMessage()
                }
            }
        }
    }

    override fun initData() {
        viewModel.loadMessage()
    }

    override fun initListener() {
        binding.refresh.setOnRefreshListener {
            viewModel.loadMessage {
                binding.refresh.finishRefresh()
            }
        }
        adapter.itemClickListener = {
            lifecycleScope.launch {
                val userId = AuthManager.authInfo?.user?.id?.toInt() ?: return@launch
                val ids = it.chatId.split("_")
                val friendId = ids.find { it.toInt() != userId }?.toInt() ?: return@launch
                val friend = viewModel.friendDao.getFriendById(friendId)
                startActivity(Intent(context, ChatActivity::class.java).apply {
                    putExtra("data", friend?.toResult())
                })
            }
        }
    }
}