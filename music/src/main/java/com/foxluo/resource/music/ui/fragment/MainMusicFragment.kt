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
        arrayOf(RecommendMusicFragment(), RecentMusicFragment(), TempFragment().apply {
            arguments = bundleOf("type" to "歌单")
        }, TempFragment().apply {
            arguments = bundleOf("type" to "歌手")
        })
    }

    private var currentFragment: Fragment? = null

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
                switchFragment(fragments[i])
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

    private fun switchFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()

        currentFragment?.let {
            transaction.hide(it)
        }

        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.music_fragment_container, fragment)
        }

        transaction.commit()
        currentFragment = fragment
    }

    override fun initBinding() = FragmentMainMusicBinding.inflate(layoutInflater)
}