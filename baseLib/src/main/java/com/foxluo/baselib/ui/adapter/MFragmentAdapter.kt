package com.foxluo.baselib.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MFragmentAdapter(val fragments: Array<Fragment>, activity: FragmentActivity) :
    FragmentStateAdapter(activity) {
    override fun createFragment(position: Int) = fragments[position]

    override fun getItemCount() = fragments.size
}