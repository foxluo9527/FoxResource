package com.foxluo.baselib.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.foxluo.baselib.data.manager.AuthManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BaseWebView @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val jsBridge by lazy {
        JsBridge()
    }

    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            goBack()
        }
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        initWebView()
    }

    private fun initWebView() {
        val webSettings = settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.allowFileAccess = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.userAgentString = "${webSettings.userAgentString} foxmusic/1.0"
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: "")
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onBackPressedCallback.isEnabled = canGoBack()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onBackPressedCallback.isEnabled = canGoBack()
            }
        }

        webChromeClient = object : WebChromeClient() {
        }

        addJavascriptInterface(jsBridge, "Android")
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            AuthManager.userInfoStateFlow.collectLatest {
                reload()
            }
        }
    }

    inner class JsBridge {
        @JavascriptInterface
        fun getToken(): String {
            return AuthManager.token ?: ""
        }

        @JavascriptInterface
        fun login() {
            post {
                ARouter.getInstance().build("/mine/login").navigation(context)
            }
        }
    }
}