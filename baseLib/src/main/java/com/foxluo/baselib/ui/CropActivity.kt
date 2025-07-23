package com.foxluo.baselib.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CropActivity : com.yalantis.ucrop.UCropActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = findViewById<View>(com.yalantis.ucrop.R.id.ucrop_photobox)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val stateBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(stateBars.left, stateBars.top, stateBars.right, stateBars.bottom)
            insets
        }
    }
}