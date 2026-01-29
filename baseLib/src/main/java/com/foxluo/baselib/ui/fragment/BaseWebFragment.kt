package com.foxluo.baselib.ui.fragment

import android.os.Message
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import com.foxluo.baselib.databinding.ActivityBaseWebBinding
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.util.ViewExt.visible

open class BaseWebFragment : BaseBindingFragment<ActivityBaseWebBinding>() {

    private val showBackIv by lazy {
        arguments?.getBoolean("showBackIv") ?: true
    }

    private val showProgress by lazy {
        arguments?.getBoolean("showProgress") ?: true
    }



    override fun initBinding() = ActivityBaseWebBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        binding.webView.setLifecycleOwner(this)
        binding.back.visible(showBackIv)
        initWebChromeClient()
    }

    override fun initListener() {
        super.initListener()
        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,binding.webView.onBackPressedCallback)
    }

    private fun initWebChromeClient() {
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100 || !showProgress) {
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
}