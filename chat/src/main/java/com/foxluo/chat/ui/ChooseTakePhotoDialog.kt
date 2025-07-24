package com.foxluo.chat.ui

import androidx.fragment.app.FragmentManager
import com.foxluo.baselib.ui.BaseBottomSheetDialogFragment
import com.foxluo.chat.databinding.FragmentChooseTakePhotoBinding

class ChooseTakePhotoDialog(
    private val takePicture: () -> Unit,
    private val takeVideo: () -> Unit
) : BaseBottomSheetDialogFragment<FragmentChooseTakePhotoBinding>() {
    override fun initBinding() = FragmentChooseTakePhotoBinding.inflate(layoutInflater)

    override fun initListener() {
        binding.cancel.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.choosePicture.setOnClickListener {
            takePicture.invoke()
            dismissAllowingStateLoss()
        }
        binding.chooseVideo.setOnClickListener {
            takeVideo.invoke()
            dismissAllowingStateLoss()
        }
    }

    fun show(fm: FragmentManager) {
        show(fm, "ChooseShowHeadDialog")
    }
}