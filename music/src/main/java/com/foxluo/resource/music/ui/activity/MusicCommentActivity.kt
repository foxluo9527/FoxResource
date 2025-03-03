package com.foxluo.resource.music.ui.activity

import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.baselib.util.ViewExt.fastClick
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.domain.viewmodel.MusicCommentViewModel
import com.foxluo.resource.music.databinding.ActivityMusicCommentBinding
import com.xuexiang.xui.utils.KeyboardUtils
import com.xuexiang.xui.utils.XToastUtils.toast

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
        KeyboardUtils.setSoftInputAdjustPan(this)
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener { finish() }
        binding.refresh.setOnRefreshListener { vm.getCommentList(musicId.toString()) }
        binding.refresh.setOnLoadMoreListener { vm.getCommentList(musicId.toString(), true) }
        adapter.setCommentClickListener(object : CommentAdapter.CommentClickListener {
            override fun userClick(userId: String) {
            }

            override fun contentClick(comment: CommentAdapter.CommentBean) {
                vm.replyComment.value = comment
            }

            override fun expandMore(id: String) {
                vm.getReplyList(id)
            }

            override fun likeClick(id: String) {
                vm.likeComment(id) {
                    adapter.likeStateChanged(id)
                }
            }

        })
        binding.commentSend.fastClick { view ->
            if (view.isSelected) {
                view.isSelected = false
                vm.replyComment.value = null
            } else {
                vm.postComment(musicId, binding.commentContent.text.toString()) {
                    if (it == false) return@postComment
                    binding.commentContent.setText("")
                    vm.replyComment.value?.let { reply ->
                        vm.getReplyList(if (reply.isReplay) reply.parentId!! else reply.id, true)
                    } ?: run {
                        vm.getCommentList(musicId)
                    }
                    vm.replyComment.value = null
                }
            }
        }
        binding.commentContent.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                binding.commentSend.text = "发送"
                binding.commentSend.isSelected = false
            } else if (vm.replyComment.value != null) {
                binding.commentSend.text = "取消"
                binding.commentSend.isSelected = true
            }
        }
        binding.refresh.autoRefresh()
    }

    override fun initObserver() {
        vm.processLoading.observe(this) {
            setLoading(it)
        }
        vm.insertReplyList.observe(this) {
            adapter.insertReplayListData(it.first, it.second)
        }
        vm.commentList.observe(this) {
            binding.emptyView.visible(it.isNullOrEmpty())
            adapter.setCommentListData(it)
        }
        vm.appendCommentList.observe(this) {
            adapter.appendCommentListData(it)
        }
        vm.isLoading.observe(this) {
            if (!it) {
                binding.refresh.finishRefresh()
                binding.refresh.finishLoadMore()
            }
        }
        vm.toast.observe(this) {
            toast(it.second)
        }
        vm.hadMore.observe(this) {
            binding.refresh.setEnableLoadMore(it)
        }
        vm.commentCount.observe(this) {
            binding.title.text = getString(com.foxluo.baselib.R.string.comment) + "（${it}）"
        }
        vm.replyComment.observe(this) {
            if (it != null) {
                binding.commentContent.hint = "回复 ${it.name}：${it.content}"
                binding.commentContent.requestFocus()
                KeyboardUtils.showSoftInput(binding.commentContent)
            } else {
                binding.commentContent.hint = "评论一下吧"
                binding.commentContent.clearFocus()
                if (KeyboardUtils.isSoftInputShow(this)) {
                    KeyboardUtils.hideSoftInput(binding.commentContent)
                }
            }
            if (it != null && binding.commentContent.text.isNullOrEmpty()) {
                binding.commentSend.isSelected = true
                binding.commentSend.text = "取消"
            } else {
                binding.commentSend.isSelected = false
                binding.commentSend.text = "发送"
            }
        }
    }
}