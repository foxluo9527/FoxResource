package com.foxluo.chat.ui

import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.chat.databinding.FragmentChatBinding

class ChatFragment : MainPageFragment<FragmentChatBinding>() {
    override fun initBinding() = FragmentChatBinding.inflate(layoutInflater)

    override fun showPlayView() = false
}