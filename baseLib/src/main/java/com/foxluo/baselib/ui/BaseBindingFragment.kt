package com.foxluo.baselib.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.foxluo.baselib.databinding.DialogLoadingBinding

abstract class BaseBindingFragment<Binding : ViewBinding> : Fragment() {
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
            .Builder(context)
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

    fun setLoading(loading: Boolean, text: String = "加载中") {
        if (loading) {
            loadingDialog.show()
            loadingBinding.content.text = text
            loadingDialog.window?.setBackgroundDrawable(null)
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
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
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
}