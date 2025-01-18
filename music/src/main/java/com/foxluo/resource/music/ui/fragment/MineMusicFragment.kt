package com.foxluo.resource.music.ui.fragment

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.ui.fragment.TempFragment
import com.foxluo.resource.music.R
import com.foxluo.resource.music.databinding.FragmentMineMusicBinding

@Route(path = "/resource/music/mine")
class MineMusicFragment : BaseBindingFragment<FragmentMineMusicBinding>() {
    companion object {
        @JvmStatic
        fun newInstance() = MineMusicFragment()
    }

    private val tags by lazy {
        mutableListOf<View>().apply {
            for (i in 0..binding.mineMusicTabContainer.childCount) {
                val tag = binding.mineMusicTabContainer.getChildAt(i)
                tag ?: continue
                add(tag)
            }
        }
    }
    private val fragments by lazy {
        arrayOf(MusicListFragment(), TempFragment().apply {
            arguments = bundleOf("type" to "关注")
        })
    }

    override fun initView() {
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
        binding.mineMusicTabContainer.getChildAt(0).performClick()
    }

    override fun initBinding() = FragmentMineMusicBinding.inflate(layoutInflater)

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(R.id.mine_music_fragment_container, fragment)
            .commit()
    }
}