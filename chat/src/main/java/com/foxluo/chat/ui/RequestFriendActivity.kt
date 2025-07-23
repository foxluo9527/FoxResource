package com.foxluo.chat.ui

import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.chat.databinding.ActivityRequestFriendBinding

class RequestFriendActivity : BaseBindingActivity<ActivityRequestFriendBinding>() {
    override fun initBinding() = ActivityRequestFriendBinding.inflate(layoutInflater)

    override fun initStatusBarView() = binding.main


    override fun initListener() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.send.setOnClickListener {
            val message = binding.message.contentText
            val mark = binding.mark.contentText
            setResult(RESULT_OK, intent.apply {
                putExtra("message", message)
                putExtra("mark", mark)
            })
            finish()
        }
    }
}