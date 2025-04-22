package com.foxluo.mine.ui.activity

import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.foxluo.baselib.data.manager.AuthManager.userInfoStateFlow
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.DialogUtil.showConfirmDialog
import com.foxluo.baselib.util.DialogUtil.showInputDialog
import com.foxluo.baselib.util.ImageExt.loadUrl
import com.foxluo.mine.databinding.ActivityPersonalBinding
import com.foxluo.mine.ui.data.viewmodel.LoginViewModel
import com.foxluo.mine.ui.data.viewmodel.PersonalViewModel
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 个人资料
 */
class PersonalActivity : BaseBindingActivity<ActivityPersonalBinding>() {
    override fun initBinding() = ActivityPersonalBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View {
        return binding.title
    }

    private val loginViewModel by viewModels<LoginViewModel>()

    private val viewModel by viewModels<PersonalViewModel>()

    // 注册相册选择页面回调
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                //contentProvider会提供一个当前activity可以读取的uri，离开页面后会作废，使用takePersistableUriPermission让应用长时间持有文件访问权限
                //applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                applicationContext.contentResolver.query(
                    uri, null, null, null, null, null
                )?.let { cursor ->
                    try {
                        cursor.moveToFirst()
                        val dataColIndex = cursor.getColumnIndex("_data")
                        //使用contentResolver查询临时uri的文件路径，这个filePath只能用于上传，退出应用后将无法再访问文件
                        val dataPath: String = cursor.getString(dataColIndex)
                        viewModel.uploadFile(dataPath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        cursor.close()
                    }
                }
            }
        }

    override fun initData() {
        viewModel.initProfile()
    }

    override fun initListener() {
        binding.llHead.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.back.setOnClickListener {
            finish()
        }
        binding.llNiceName.setOnClickListener {
            showInputDialog("请输入用户昵称", preFill = binding.nickName.text.toString()) {
                viewModel.updateProfile(nickName = it)
            }
        }
        binding.llSign.setOnClickListener {
            showInputDialog("请输入签名", preFill = binding.sign.text.toString()) {
                viewModel.updateProfile(signature = it)
            }
        }
        binding.logOut.setOnClickListener {
            showConfirmDialog("是否确认退出登录") {
                loginViewModel.logout()
            }
        }
    }

    override fun initObserver() {
        val toastObserver = { it: Pair<Boolean?, String> ->
            if (it.first == true) {
                success(it.second)
            } else {
                XToastUtils.error(it.second)
            }
        }
        viewModel.toast.observe(this, toastObserver)
        loginViewModel.toast.observe(this, toastObserver)
        val loadingObserver = { it: Boolean ->
            setLoading(it)
        }
        viewModel.isLoading.observe(this, loadingObserver)
        loginViewModel.isLoading.observe(this, loadingObserver)
        viewModel.profile.observe(this) {}
        lifecycleScope.launch {
            userInfoStateFlow.collectLatest {
                it?.let {
                    binding.email.text = it.email
                    binding.userName.text = it.username
                    binding.icHead.loadUrl(it.avatar)
                    binding.sign.text = it.signature
                    binding.nickName.text = it.nickname
                } ?: finish()
            }
        }
    }
}