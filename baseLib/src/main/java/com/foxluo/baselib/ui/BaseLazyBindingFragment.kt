package com.foxluo.baselib.ui

import androidx.viewbinding.ViewBinding

abstract class BaseLazyBindingFragment<Binding: ViewBinding>: BaseBindingFragment<Binding>() {
    private var isFirstLoad = true// 是否第一次加载
    private var shouldLoad = false

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            shouldLoad = true
            initView()
            initListener()
            initObserver()
            initData()
            isFirstLoad = false
            shouldLoad = false
        }
    }

    override fun initView() {
        super.initView()
        if (shouldLoad) {
            lazySetupView()
        }
    }

    override fun initObserver() {
        super.initObserver()
        if (shouldLoad) {
            lazyObserve()
        }
    }

    override fun initData() {
        super.initData()
        if (shouldLoad) {
            lazyFetchData()
        }
    }

    override fun initListener() {
        super.initListener()
        if (shouldLoad) {
            lazySetListener()
        }
    }

    protected open fun lazySetupView() {}
    protected open fun lazyObserve(){}
    protected open fun lazyFetchData() {}
    protected open fun lazySetListener(){}

}