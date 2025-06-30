package com.foxluo.resource.music.ui.activity

import android.annotation.SuppressLint
import android.os.Handler
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.baselib.util.KeyboardHeightObserver
import com.foxluo.baselib.util.KeyboardHeightProvider
import com.foxluo.baselib.util.ViewExt.fastClick
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.R
import com.foxluo.resource.music.data.domain.viewmodel.MusicCommentViewModel
import com.foxluo.resource.music.databinding.ActivityMusicCommentBinding
import com.xuexiang.xui.utils.XToastUtils.toast


class MusicCommentActivity : BaseBindingActivity<ActivityMusicCommentBinding>(),
    KeyboardHeightObserver {
    private val musicId by lazy {
        intent.getStringExtra("music_id") ?: ""
    }
    private var mProvider: KeyboardHeightProvider? = null

    //虚拟导航栏的高度, 默认为0
    private var mVirtualBottomHeight = 0

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
        mProvider = KeyboardHeightProvider(this)
        binding.main.post {
            mProvider?.start(R.id.main)
        }
    }

    override fun onResume() {
        super.onResume()
        mProvider?.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        mProvider?.setKeyboardHeightObserver(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mProvider?.close()
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
            binding.commentSend.isEnabled = true
            if (!it.isNullOrEmpty()) {
                binding.commentSend.text = "发送"
                binding.commentSend.isSelected = false
            } else if (vm.replyComment.value != null) {
                binding.commentSend.text = "取消"
                binding.commentSend.isSelected = true
            } else {
                binding.commentSend.isEnabled = false
            }
        }
        binding.refresh.autoRefresh()
    }

    @SuppressLint("SetTextI18n")
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
                binding.commentSend.isEnabled = true
            } else {
                binding.commentContent.hint = "评论一下吧"
                binding.commentContent.clearFocus()
            }
            if (it != null && binding.commentContent.text.isNullOrEmpty()) {
                binding.commentSend.isSelected = true
                binding.commentSend.text = "取消"
            } else {
                binding.commentSend.isSelected = false
                binding.commentSend.text = "发送"
                binding.commentSend.isEnabled = !binding.commentContent.text.isNullOrEmpty()
            }
        }
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        //输入法弹出
        //输入法之上的布局(包括EditText+发送按钮)在整个屏幕中的位置是沉底的
        //这里将布局显示出来, 并设置MarginBottom, 这样就被输入法布局拖起来了
        //这里的MarginBottom已经包含了虚拟导航栏的高度mVirtualBottomHeight
        val params =
            binding.inputLayout.getLayoutParams() as MarginLayoutParams
        val marginBottom = if (height > 0) {
            height + mVirtualBottomHeight + dp2px(16f) * 2
        } else {
            dp2px(16f)
        }
        params.setMargins(0, 0, 0, marginBottom)
        binding.inputLayout.requestLayout()
    }

    override fun onVirtualBottomHeight(height: Int) {
        //虚拟导航栏高度赋值
        mVirtualBottomHeight = height;
    }
}