package com.foxluo.chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.viewModels
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.DialogUtil.showConfirmDialog
import com.foxluo.baselib.util.DialogUtil.showInputDialog
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.chat.data.domain.viewmodel.UserDetailViewModel
import com.foxluo.chat.data.result.FriendResult
import com.foxluo.chat.databinding.ActivityUserPageBinding
import com.xuexiang.xui.utils.XToastUtils

class UserPageActivity : BaseBindingActivity<ActivityUserPageBinding>() {
    private val userData by lazy {
        intent?.getSerializableExtra("data") as? FriendResult
    }
    private val viewModel by viewModels<UserDetailViewModel>()
    override fun initBinding() = ActivityUserPageBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun initData() {
        userData?.let {
            binding.username.text = "用户名：${it.username}"
            binding.nickname.text = "昵称：${it.nickname ?: "未设置"}"
            binding.sign.text = "签名：${it.signature ?: "这个人很懒，什么都没留下~"}"
            binding.mark.text = it.mark ?: "未设置"
            binding.head.loadUrlWithCorner(processUrl(it.avatar), 8)
        } ?: {
            XToastUtils.toast("获取用户数据失败!")
            finish()
        }
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.markName.setOnClickListener {
            showInputDialog(content = "请输入备注名称", preFill = userData!!.mark) {
                viewModel.remark(userData!!, it) {
                    binding.mark.text = it
                }
            }
        }
        binding.delete.setOnClickListener {
            showConfirmDialog("是否确认删除好友？") {
                viewModel.delete(userData!!) {
                    finish()
                }
            }
        }
        binding.sendMessage.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("data", userData)
            })
        }
    }

    override fun initObserver() {
        viewModel.toast.observe(this) {
            if (it.first == true) {
                XToastUtils.success(it.second)
            } else {
                XToastUtils.error(it.second)
            }
        }
    }

    override fun initStatusBarView() = binding.root
}