package com.foxluo.baselib.ui

interface MainPage {
    fun showPlayView(): Boolean

    fun showNavBottom(): Boolean

    fun getLeftPlayPadding(): Int

    fun getTopPlayPadding(): Int

    fun getRightPlayPadding(): Int

    fun getBottomPlayPadding(): Int
}