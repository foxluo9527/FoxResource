package com.foxluo.chat.ui

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.data.domain.viewmodel.RequestsViewModel
import com.foxluo.chat.databinding.ActivityFriendRequestBinding
import com.foxluo.chat.ui.adapter.FriendRequestAdapter
import com.xuexiang.xui.utils.XToastUtils

class FriendRequestActivity : BaseBindingActivity<ActivityFriendRequestBinding>() {
    private val viewModel by viewModels<RequestsViewModel>()

    private val adapter by lazy {
        FriendRequestAdapter().apply {
            itemClick = {
                viewModel.accept(it) {
                    setResult(RESULT_OK)
                }
            }
        }
    }

    override fun initStatusBarView() = binding.root

    override fun initBinding() = ActivityFriendRequestBinding.inflate(layoutInflater)

    override fun initView() {
        binding.refresh.setEnableRefresh(true)
        binding.refresh.setEnableLoadMore(false)
        binding.recycleView.adapter = adapter
    }

    override fun initObserver() {
        viewModel.requests.observe(this) {
            adapter.refresh(it)
            binding.emptyView.visible(it.isNullOrEmpty())
            binding.refresh.finishRefresh()
        }
        viewModel.toast.observe(this) {
            if (it.first == true) {
                XToastUtils.success(it.second)
            } else {
                XToastUtils.error(it.second)
            }
        }
    }

    override fun initListener() {
        binding.refresh.setOnRefreshListener {
            viewModel.getFriendRequests()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun initData() {
        binding.refresh.autoRefresh()
    }
}