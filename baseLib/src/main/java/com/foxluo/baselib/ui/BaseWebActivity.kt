package com.foxluo.baselib.ui

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.foxluo.baselib.databinding.ActivityBaseWebBinding
import com.foxluo.baselib.ui.view.BaseWebView

@Route(path = "/base/web")
open class BaseWebActivity : BaseBindingActivity<ActivityBaseWebBinding>() {
    @Autowired
    @JvmField
    var url =""

    override fun initBinding() = ActivityBaseWebBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        binding.webView.setLifecycleOwner(this)
        initWebChromeClient()
    }

    override fun initListener() {
        super.initListener()
        binding.back.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(binding.webView.canGoBack()) {
            override fun handleOnBackPressed() {
                binding.webView.goBack()
            }
        })
    }

    override fun initData() {
        super.initData()
        binding.webView.loadUrl(url)
    }

    private fun initWebChromeClient() {
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    fun loadUrl(url: String) {
        binding.webView.loadUrl(url)
    }

    override fun initStatusBarView(): View {
        return binding.root
    }
}