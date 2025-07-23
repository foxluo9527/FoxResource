package com.foxluo.chat.ui

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.chat.data.domain.viewmodel.SearchUserViewModel
import com.foxluo.chat.data.result.UserSearchResult
import com.foxluo.chat.databinding.ActivitySearchFriendBinding
import com.foxluo.chat.ui.adapter.SearchUserAdapter
import com.xuexiang.xui.utils.KeyboardUtils
import com.xuexiang.xui.utils.XToastUtils

class SearchFriendActivity : BaseBindingActivity<ActivitySearchFriendBinding>() {
    private val contract by lazy {
        object : ActivityResultContract<Int, Triple<String, String?, Int>?>() {
            override fun createIntent(
                context: Context,
                input: Int
            ): Intent {
                return Intent(context, RequestFriendActivity::class.java).apply {
                    putExtra("friendId", input)
                }
            }

            override fun parseResult(
                resultCode: Int,
                intent: Intent?
            ): Triple<String, String?, Int>? {
                intent ?: return null
                val message = "请求添加好友:${intent.getStringExtra("message") ?: ""}"
                val mark = intent.getStringExtra("mark")
                val friendId = intent.getIntExtra("friendId", 0)
                return Triple(message, mark, friendId)
            }
        }
    }

    private val resultLauncher = registerForActivityResult(contract) {
        it ?: return@registerForActivityResult
        val message = it.first
        val mark = it.second
        viewModel.request(it.third, message, mark)
    }

    private val viewModel by viewModels<SearchUserViewModel>()

    private val adapter by lazy {
        SearchUserAdapter()
    }

    override fun initBinding() = ActivitySearchFriendBinding.inflate(layoutInflater)

    override fun initStatusBarView() = binding.main

    override fun initView() {
        binding.recycleView.adapter = adapter
        binding.recycleView.addItemDecoration(DividerItemDecoration(this, 1))
    }

    override fun initObserver() {
        viewModel.toast.observe(this) {
            if (it.first == true) {
                XToastUtils.success(it.second)
            } else {
                XToastUtils.error(it.second)
            }
        }
        viewModel.isLoading.observe(this) {
            setLoading(it)
        }
        viewModel.users.observe(this) {
            binding.emptyView.visible(it.isNullOrEmpty())
            adapter.refresh(it ?: mutableListOf<UserSearchResult>())
        }
    }

    override fun initListener() {
        adapter.itemClick = { data ->
            AuthManager.authInfo?.user?.let { me ->
                if (data.id == me.id.toInt()) {
                    XToastUtils.toast("您不能添加自己为好友")
                } else {
                    resultLauncher.launch(data.id)
                }
            }
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.title.setOnEditorActionListener { _, actionId, keyEvent: KeyEvent? ->
            return@setOnEditorActionListener if (
                (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH)
                && (keyEvent?.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent?.keyCode == KeyEvent.KEYCODE_SEARCH)
            ) {
                val keyword = binding.title.text.toString()
                if (!(keyword.isNullOrEmpty())) {
                    viewModel.searchUser(keyword)
                    KeyboardUtils.hideSoftInput(binding.title)
                    true
                } else {
                    binding.title.setShakeAnimation()
                    KeyboardUtils.hideSoftInput(binding.title)
                    false
                }
            } else {
                false
            }
        };
    }
}