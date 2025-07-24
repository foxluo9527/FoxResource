package com.foxluo.baselib.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.foxluo.baselib.R
import com.foxluo.baselib.databinding.DialogLoadingBinding
import com.xuexiang.xui.utils.WidgetUtils
import com.xuexiang.xui.widget.dialog.LoadingDialog

abstract class BaseBindingActivity<Binding : ViewBinding> : AppCompatActivity() {
    val statusBarHeight by lazy {
        BarUtils.getStatusBarHeight()
    }

    val binding by lazy {
        initBinding()
    }

    val loadingBinding by lazy {
        DialogLoadingBinding.inflate(layoutInflater)
    }

    private val loadingDialog by lazy {
        AlertDialog
            .Builder(this)
            .setView(loadingBinding.root)
            .setCancelable(false)
            .create()
    }

    fun setLoading(loading: Boolean,text: String="加载中") {
        if (loading) {
            loadingDialog.show()
            loadingBinding.content.text = text
            loadingDialog.window?.setBackgroundDrawable(null)
        } else {
            loadingDialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        initView()
        initListener()
        initObserver()
        initData()
        initStatusBarView()?.let { view ->
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

    fun onBackPressed(isEnabled: Boolean, callback: () -> Unit) {
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(isEnabled) {
                override fun handleOnBackPressed() {
                    callback()
                }
            })
    }
}