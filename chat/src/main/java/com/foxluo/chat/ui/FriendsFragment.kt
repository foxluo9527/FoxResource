package com.foxluo.chat.ui

import android.graphics.Rect
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.ui.contract.CommonResultContract.resultChangedContract
import com.foxluo.baselib.ui.contract.CommonResultContract.sendResultChangedContract
import com.foxluo.baselib.ui.view.LetterView.UpdateListView
import com.foxluo.chat.data.domain.viewmodel.FriendsViewModel
import com.foxluo.chat.data.result.FriendResult
import com.foxluo.chat.databinding.FragmentFriendsBinding
import com.foxluo.chat.ui.adapter.FriendsAdapter
import com.xuexiang.xui.adapter.recyclerview.sticky.StickyItemDecoration
import com.xuexiang.xui.utils.XToastUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FriendsFragment : BaseBindingFragment<FragmentFriendsBinding>(), UpdateListView {
    private val viewModel by viewModels<FriendsViewModel>()

    private val adapter by lazy {
        FriendsAdapter()
    }

    private val friendDetailResultLauncher =
        registerForActivityResult(sendResultChangedContract<UserPageActivity, FriendResult> { intent, data ->
            intent.putExtra("data", data)
        }) {
            if (it) {
                viewModel.getFriends()
            }
        }

    private val friendRequestResultLauncher =
        registerForActivityResult(resultChangedContract<FriendRequestActivity>()) {
            if (it) {
                viewModel.getFriends()
            }
        }

    override fun initBinding() = FragmentFriendsBinding.inflate(layoutInflater)

    override fun initView() {
        adapter.setLetterView(binding.letterView)
        binding.rvList.adapter = adapter
        binding.rvList.addItemDecoration(object : StickyItemDecoration(
            binding.header,
            FriendsAdapter.TYPE_LETTER
        ) {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.indexOfChild(view)
                val prevItem: FriendsAdapter.Item? = adapter.getItem(position - 1)
                val currentItem: FriendsAdapter.Item? = adapter.getItem(position)
                if (currentItem is FriendsAdapter.Item.AddFriend) {
                    outRect.set(0, dp2px(1f), 0, 0)
                } else if (currentItem is FriendsAdapter.Item.FriendItem) {
                    val top = if (prevItem is FriendsAdapter.Item.LetterTitle) 0f else 1f
                    outRect.set(0, dp2px(top), 0, 0)
                }
            }
        })
        binding.header.setOnStickyPositionChangedListener { position ->
            val item = adapter.getItem(position)
            if (item is FriendsAdapter.Item.LetterTitle) {
                binding.headContent.root.text = item.letter.toString()
                binding.letterView.updateLetterIndexView(position)
            }
        }
        binding.letterView.setUpdateListView(this)
    }

    override fun initListener() {
        adapter.setOnItemClickListener { _, item, position ->
            if (item is FriendsAdapter.Item.AddFriend) {
                friendRequestResultLauncher.launch(Unit)
            } else if (item is FriendsAdapter.Item.FriendItem) {
                friendDetailResultLauncher.launch(item.data)
            }
        }
    }

    override fun initObserver() {
        viewModel.toast.observe(this) {
            lifecycleScope.launchWhenResumed {
                if (it.first == true) {
                    XToastUtils.success(it.second)
                } else {
                    XToastUtils.error(it.second)
                }
            }
        }
        viewModel.friends.observe(this) {
            adapter.setFriendData(it)
        }
        viewModel.initObserver()
        lifecycleScope.launch {
            AuthManager.userInfoStateFlow.collectLatest {
                if (it != null) {
                    viewModel.getFriends()
                }
            }
        }
    }

    override fun updateListView(letter: String?) {

    }
}