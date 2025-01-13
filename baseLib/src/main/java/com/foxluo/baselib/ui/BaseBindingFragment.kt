package com.foxluo.baselib.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.xuexiang.xui.widget.dialog.LoadingDialog

abstract class BaseBindingFragment<Binding:ViewBinding>:Fragment() {
    val binding by lazy {
        initBinding()
    }

    val statusBarHeight by lazy {
        BarUtils.getStatusBarHeight()
    }

    private val loadingDialog by lazy {
        LoadingDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    fun setLoading(loading: Boolean) {
        if (loading) {
            loadingDialog.performShow()
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
        initStatusBarView()?.let { view->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val stateBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                v.setPadding(stateBars.left, stateBars.top, stateBars.right, stateBars.bottom)
                insets
            }
        }
    }

    open fun initView(){

    }

    open fun initListener(){

    }

    open fun initObserver(){

    }

    open fun initData(){

    }


    open fun initStatusBarView():View?{
        return null
    }

    abstract fun initBinding():Binding
}