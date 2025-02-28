package com.foxluo.resource.music.ui.activity

import android.view.View
import androidx.activity.viewModels
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.resource.music.data.domain.viewmodel.MusicCommentViewModel
import com.foxluo.resource.music.databinding.ActivityMusicCommentBinding

class MusicCommentActivity : BaseBindingActivity<ActivityMusicCommentBinding>() {
    private val musicId by lazy {
        intent.getStringExtra("music_id") ?: ""
    }

    override fun initStatusBarView(): View {
        return binding.main
    }

    private val vm by viewModels<MusicCommentViewModel>()

    private val adapter by lazy {
        CommentAdapter()
    }

    override fun initBinding() = ActivityMusicCommentBinding.inflate(layoutInflater)

    override fun initView() {
        if (musicId.isNullOrEmpty()) {
            finish()
        }
        binding.recycleView.adapter = adapter
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener { finish() }
        binding.refresh.setOnRefreshListener { initData() }
        binding.refresh.setOnLoadMoreListener { binding.refresh.finishLoadMore() }
        adapter.setCommentClickListener(object : CommentAdapter.CommentClickListener {
            override fun userClick(userId: String) {
            }

            override fun contentClick(id: String) {
            }

            override fun expandMore(id: String) {
            }

            override fun likeClick(id: String) {
            }

        })
    }

    override fun initData() {
        vm.getCommentList(musicId.toString())
    }

    override fun initObserver() {
        vm.commentList.observe(this) {
            adapter.setCommentListData(it)
        }
        vm.isLoading.observe(this) {
            if (!it) {
                binding.refresh.finishRefresh()
                binding.refresh.finishLoadMore()
            }
        }
    }
}