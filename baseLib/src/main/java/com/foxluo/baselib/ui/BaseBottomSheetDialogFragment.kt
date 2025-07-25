package com.foxluo.baselib.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.foxluo.baselib.databinding.DialogLoadingBinding
import com.foxluo.baselib.util.ViewExt.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xuexiang.xui.widget.dialog.LoadingDialog

abstract class BaseBottomSheetDialogFragment<Binding : ViewBinding> : BottomSheetDialogFragment() {
    val binding by lazy {
        initBinding()
    }

    val statusBarHeight by lazy {
        BarUtils.getStatusBarHeight()
    }

    val loadingBinding by lazy {
        DialogLoadingBinding.inflate(layoutInflater)
    }

    private val loadingDialog by lazy {
        AlertDialog
            .Builder(requireContext())
            .setView(loadingBinding.root)
            .setCancelable(false)
            .create()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    fun setLoading(loading: Boolean, text: String = "加载中", cancel: (() -> Unit)? = null) {
        if (loading) {
            loadingDialog.show()
            loadingBinding.content.text = text
            loadingDialog.window?.setBackgroundDrawable(null)
            loadingBinding.cancel.visible(cancel != null)
            loadingBinding.cancel.setOnClickListener {
                cancel?.invoke()
                loadingDialog.dismiss()
            }
        } else {
            loadingDialog.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
        initData()
        initStatusBarView()?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val stateBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                v.setPadding(stateBars.left, stateBars.top, stateBars.right, stateBars.bottom)
                insets
            }
        }
    }

    open fun initView() {

    }

    open fun initListener() {

    }

    open fun initObserver() {

    }

    open fun initData() {

    }


    open fun initStatusBarView(): View? {
        return null
    }

    abstract fun initBinding(): Binding

    open fun initHeightPercent(): Int? {
        return null
    }

    override fun onStart() {
        super.onStart()
        initHeightPercent()?.let {
            setupRatio(requireContext(), dialog as BottomSheetDialog, it)
        }
    }

    private fun getBottomSheetDialogDefaultHeight(context: Context, percetage: Int): Int {
        return getWindowHeight(context) * percetage / 100
    }

    private fun getWindowHeight(context: Context): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun setupRatio(context: Context, bottomSheetDialog: BottomSheetDialog, percetage: Int) {
        //id = com.google.android.material.R.id.design_bottom_sheet for Material Components
        //id = android.support.design.R.id.design_bottom_sheet for support librares
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(context, percetage)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }
}