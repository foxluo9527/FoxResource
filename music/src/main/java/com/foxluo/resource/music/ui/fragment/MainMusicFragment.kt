package com.foxluo.resource.music.ui.fragment

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.foxluo.resource.music.R
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.ui.fragment.TempFragment
import com.foxluo.resource.music.databinding.FragmentMainMusicBinding

class MainMusicFragment : BaseBindingFragment<FragmentMainMusicBinding>() {
    private val fragments by lazy {
        arrayOf(TempFragment().apply {
            arguments = bundleOf("type" to "最近播放")
        }, RecommendMusicFragment(), TempFragment().apply {
            arguments = bundleOf("type" to "歌单")
        }, TempFragment().apply {
            arguments = bundleOf("type" to "歌手")
        })
    }

    private val tags by lazy {
        mutableListOf<View>().apply {
            for (i in 0..binding.musicTabContainer.childCount) {
                val tag = binding.musicTabContainer.getChildAt(i)
                tag ?: continue
                add(tag)
            }
        }
    }

    override fun initView() {

    }

    override fun initListener() {
        tags.forEachIndexed { i, tag ->
            tag.setOnClickListener { _ ->
                replaceFragment(fragments[i])
                tags.forEachIndexed { index, view ->
                    val isCurrent = i == index
                    view.isSelected = isCurrent
                    (view as TextView).setTypeface(
                        if (isCurrent)
                            Typeface.DEFAULT_BOLD
                        else
                            Typeface.DEFAULT
                    )
                }
            }
        }
        binding.musicTabContainer.getChildAt(0).performClick()
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(R.id.music_fragment_container, fragment)
            .commit()
    }

    override fun initBinding() = FragmentMainMusicBinding.inflate(layoutInflater)
}