package com.foxluo.chat.ui

import android.net.Uri
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.CaptureVideo
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.blankj.utilcode.util.KeyboardUtils
import com.foxluo.baselib.ui.AlbumSelectorActivity
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.ImageViewInfo
import com.foxluo.baselib.ui.adapter.AlbumAdapter
import com.foxluo.baselib.ui.contract.CommonResultContract.resultDataContract
import com.foxluo.baselib.util.CropImageContract
import com.foxluo.baselib.util.CropImageResult
import com.foxluo.baselib.util.KeyboardHeightObserver
import com.foxluo.baselib.util.KeyboardHeightProvider
import com.foxluo.baselib.util.TimeUtil.nowTime
import com.foxluo.baselib.util.ViewExt.gone
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.baselib.util.getOutPutUri
import com.foxluo.chat.R
import com.foxluo.chat.data.database.MessageEntity
import com.foxluo.chat.data.domain.viewmodel.ChatViewModel
import com.foxluo.chat.data.result.FriendResult
import com.foxluo.chat.databinding.ActivityChatBinding
import com.foxluo.chat.ui.adapter.MessageListAdapter
import com.xuexiang.xui.utils.ViewUtils
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.toast
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs


class ChatActivity : BaseBindingActivity<ActivityChatBinding>(), KeyboardHeightObserver {
    private val friendData by lazy {
        intent.getSerializableExtra("data") as? FriendResult
    }

    private val adapter by lazy {
        MessageListAdapter(
            showUserDetail = {

            }, cancelSend = {
                viewModel.deleteMessage(it)
            }, retrySend = {
                viewModel.retrySendMessage(it, processSendWorkResult)
            }, showImage = showImage, playVoice = {

            }, openFile = {

            })
    }

    private var currentTakePictureUri: Uri? = null

    private var currentTakeVideoUri: Uri? = null

    private val showImage by lazy {
        { adapter: MessageListAdapter, message: MessageEntity, itemView: View ->
            val list = adapter.getDataList().mapNotNull {
                val url = it.getFileExistsPath()
                (if (it.type != "file")
                    null
                else if (it.file_type == "video") ImageViewInfo(url, url)
                else ImageViewInfo(url))?.apply {
                    bounds = ViewUtils.calculateViewScreenLocation(itemView)
                }
            }
            PreviewBuilder.from(this)
                .setImgs<ImageViewInfo>(list)
                .setCurrentIndex(list.indexOfFirst { it.url == message.getFileExistsPath() })
                .setType(PreviewBuilder.IndicatorType.Number)
                .start()
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(resultDataContract<AlbumSelectorActivity, AlbumAdapter.Image>("image") { image, intent ->
            intent.data?.let {
                image.uri = it
            }
        }) {
            it ?: return@registerForActivityResult
            it.uri ?: return@registerForActivityResult
            viewModel.sendImageMessage(friendData!!, it, processSendWorkResult)
        }

    private val takePicture = registerForActivityResult(TakePicture()) { result ->
        if (result) {
            currentTakePictureUri?.let { cropImageContract.launch(CropImageResult(it, 0f, 0f)) }
        }
    }

    private val takeVideo = registerForActivityResult(CaptureVideo()) { result ->
        if (result) {
            currentTakeVideoUri?.let { uri ->
                val image =
                    AlbumAdapter.Image(uri, nowTime, isVideo = true)
                lifecycleScope.launch {
                    val processedImage = AlbumSelectorActivity.getProcessedImage(
                        this@ChatActivity,
                        image,
                        false
                    ) { show, text, cancel ->
                        setLoading(show, text, cancel)
                    }
                    viewModel.sendImageMessage(friendData!!, processedImage, processSendWorkResult)
                }
            }
        }
    }

    private val cropImageContract = registerForActivityResult(CropImageContract()) { croppedUri ->
        croppedUri ?: return@registerForActivityResult
        val originUri = currentTakePictureUri ?: return@registerForActivityResult
        val image =
            AlbumAdapter.Image(originUri, nowTime, croppedUri, isCropped = true)
        lifecycleScope.launch {
            val processedImage =
                AlbumSelectorActivity.getProcessedImage(
                    this@ChatActivity,
                    image,
                    false
                ) { show, text, cancel ->
                    setLoading(show, text, cancel)
                }
            viewModel.sendImageMessage(friendData!!, processedImage, processSendWorkResult)
        }
    }

    //虚拟导航栏的高度, 默认为0
    private var mVirtualBottomHeight = 0
    private var mProvider: KeyboardHeightProvider? = null

    private val viewModel by viewModels<ChatViewModel>()

    override fun initBinding() = ActivityChatBinding.inflate(layoutInflater)

    override fun initStatusBarView() = binding.root

    override fun initView() {
        binding.rvList.adapter = adapter
        (binding.rvList.layoutManager as LinearLayoutManager).stackFromEnd = true
        mProvider = KeyboardHeightProvider(this)
        binding.main.post {
            mProvider?.start(R.id.main)
        }
    }

    override fun onResume() {
        super.onResume()
        mProvider?.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        mProvider?.setKeyboardHeightObserver(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mProvider?.close()
    }

    override fun initData() {
        if (friendData == null) {
            toast("获取用户信息失败")
            finish()
            return
        } else {
            val data = friendData ?: return
            viewModel.loadMessage(data.id)
            binding.name.text = data.mark?.ifEmpty { data.nickname } ?: data.nickname
        }
    }

    override fun initListener() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.add.isSelected) {
                    binding.add.isSelected = false
                    binding.actions.gone()
                } else {
                    finish()
                }
            }
        })
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.content.doAfterTextChanged {
            binding.send.visible(!it.toString().isNullOrEmpty())
            binding.add.visible(it.toString().isNullOrEmpty())
        }
        binding.send.setOnClickListener {
            viewModel.sendTextMessage(
                friendData!!,
                binding.content.text.toString(),
                processSendWorkResult
            )
            binding.content.setText("")
            KeyboardUtils.hideSoftInput(binding.content)
        }
        binding.add.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                binding.actions.visible()
                KeyboardUtils.hideSoftInput(binding.content)
            } else {
                binding.actions.gone()
                KeyboardUtils.showSoftInput(binding.content)
            }
        }
        binding.actionSendImage.setOnClickListener {
            pickImageLauncher.launch(Unit)
        }
        binding.actionTakePhoto.setOnClickListener {
            showTakePhotoOrVideo()
        }
    }

    override fun initObserver() {
        viewModel.isLoading.observe(this) {
            setLoading(it)
        }
        viewModel.toast.observe(this) {
            toast(it.second)
        }
        lifecycleScope.launch {
            viewModel.messagePager.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        val params =
            binding.bottom.getLayoutParams() as MarginLayoutParams
        val marginBottom = if (height > 0) {
            height + mVirtualBottomHeight
        } else {
            0
        }
        params.setMargins(0, 0, 0, marginBottom)
        binding.bottom.requestLayout()
        if (abs(height) > mVirtualBottomHeight) {
            binding.actions.gone()
            binding.add.isSelected = false
        }
    }

    override fun onVirtualBottomHeight(height: Int) {
        //虚拟导航栏高度赋值
        mVirtualBottomHeight = height;
    }

    private fun scrollToBottom() {
        (binding.rvList.layoutManager as LinearLayoutManager)
            .scrollToPositionWithOffset(adapter.itemCount - 1, Integer.MIN_VALUE);
    }

    private val processSendWorkResult = { liveData: LiveData<WorkInfo?> ->
        liveData.observe(this) { workInfo ->
            if (workInfo?.state == WorkInfo.State.FAILED) {
                workInfo.outputData.getString("message")?.let {
                    XToastUtils.error(it)
                }
            } else if (workInfo?.state == WorkInfo.State.ENQUEUED) {
                scrollToBottom()
                liveData.removeObservers(this)
            }
        }
    }

    private fun showTakePhotoOrVideo() {
        ChooseTakePhotoDialog(takePicture = {
            takePicture.launch(
                getOutPutUri(
                    prefix = "IMG_",
                    suffix = ".jpg"
                ).also { currentTakePictureUri = it })
        }, takeVideo = {
            takeVideo.launch(
                getOutPutUri(
                    prefix = "VIDEO_",
                    suffix = ".mp4"
                ).also { currentTakeVideoUri = it })
        }).show(supportFragmentManager)
    }
}