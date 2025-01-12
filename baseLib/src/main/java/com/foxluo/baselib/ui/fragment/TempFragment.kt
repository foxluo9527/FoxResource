package com.foxluo.baselib.ui.fragment

import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.databinding.FragmentTempBinding

class TempFragment : BaseBindingFragment<FragmentTempBinding>() {
    override fun initView() {
        arguments?.getString("type")?.let {
            binding.type.text = it
        }
    }

    override fun initBinding() =
        FragmentTempBinding.inflate(layoutInflater)
}