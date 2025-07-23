package com.foxluo.mine.ui.fragment

import android.view.View
import androidx.fragment.app.FragmentManager
import com.foxluo.baselib.ui.BaseBottomSheetDialogFragment
import com.foxluo.baselib.ui.ImageViewInfo
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.mine.databinding.FragmentChooseHeadShowBinding
import com.xuexiang.xui.utils.ViewUtils
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder

class ChooseShowHeadDialog : BaseBottomSheetDialogFragment<FragmentChooseHeadShowBinding>() {
    private var headView :View?=null
    override fun initBinding() = FragmentChooseHeadShowBinding.inflate(layoutInflater)

    private var head: String? = null

    private var chooseHead: (() -> Unit)? = null

    override fun initListener() {
        binding.cancel.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.showHead.setOnClickListener {
            PreviewBuilder.from(this)
                .setImg<ImageViewInfo>(ImageViewInfo(processUrl(head), ViewUtils.calculateViewScreenLocation(headView)))
                .setType(PreviewBuilder.IndicatorType.Dot)
                .start();
            dismissAllowingStateLoss()
        }
        binding.chooseHead.setOnClickListener {
            chooseHead?.invoke()
            dismissAllowingStateLoss()
        }
    }

    fun show(fm: FragmentManager, head: String, headView: View, chooseHead: () -> Unit) {
        this.headView = headView
        this.head = head
        this.chooseHead = chooseHead
        show(fm, "ChooseShowHeadDialog")
    }
}