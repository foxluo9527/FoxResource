package com.foxluo.baselib.util

import android.content.Context
import android.text.InputType
import com.foxluo.baselib.R
import com.xuexiang.xui.widget.dialog.DialogLoader
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog

object DialogUtil {
    fun Context.showInputDialog(
        content: String? = null,
        preFill: String? = null,
        hint: String? = null,
        positiveText: String? = null,
        negativeText: String? = null,
        block: (String) -> Unit
    ) {
        val builder = MaterialDialog.Builder(this)
            .content(content ?: "")
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(hint, preFill ?: "", false, MaterialDialog.InputCallback { _, _ -> })
            .positiveText(positiveText ?: getString(R.string.sure))
            .negativeText(negativeText ?: getString(R.string.cancel))
            .onPositive(MaterialDialog.SingleButtonCallback { dialog, _ ->
                dialog.getInputEditText()?.getText()?.toString()?.let { block.invoke(it) }
            })
            .cancelable(false)
            .show()
        builder.show()
    }

    fun Context.showConfirmDialog(
        content: String,
        positiveText: String? = null,
        negativeText: String? = null,
        block: () -> Unit
    ) {
        val builder = MaterialDialog.Builder(this)
            .content(content)
            .positiveText(positiveText ?: getString(R.string.sure))
            .negativeText(negativeText ?: getString(R.string.cancel))
            .onPositive(MaterialDialog.SingleButtonCallback { dialog, _ ->
                block.invoke()
            })
            .show()
        builder.show()
    }
}