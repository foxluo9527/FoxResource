package com.foxluo.baselib.ui

import androidx.viewbinding.ViewBinding

abstract class BaseLazyBindingFragment<Binding: ViewBinding>: BaseBindingFragment<Binding>() {
    private var isLoaded = false

}