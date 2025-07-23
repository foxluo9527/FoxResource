package com.foxluo.baselib.ui.contract

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object CommonResultContract {
    /**
     * 根据resultCode返回是否更改的Contract
     */
    inline fun <reified T> resultChangedContract(): ActivityResultContract<Unit, Boolean> {
        return object : ActivityResultContract<Unit, Boolean>() {
            override fun createIntent(
                context: Context,
                input: Unit
            ) = Intent(context, T::class.java)

            override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
                return resultCode == RESULT_OK
            }
        }
    }

    /**
     * 根据resultCode返回是否更改的Contract
     * 同时具有入参
     */
    inline fun <reified T, I> sendResultChangedContract(crossinline putExtras: (Intent, I) -> Unit): ActivityResultContract<I, Boolean> {
        return object : ActivityResultContract<I, Boolean>() {
            override fun createIntent(
                context: Context,
                input: I
            ) = Intent(context, T::class.java).apply {
                putExtras(this, input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
                return resultCode == RESULT_OK
            }
        }
    }

    /**
     * 获取data
     */
    inline fun <reified T, reified R> resultDataContract(
        key: String,
        crossinline block: (R, Intent) -> Unit
    ): ActivityResultContract<Unit, R?> {
        return object : ActivityResultContract<Unit, R?>() {
            override fun createIntent(
                context: Context,
                input: Unit
            ) = Intent(context, T::class.java)

            override fun parseResult(resultCode: Int, intent: Intent?): R? {
                return if (resultCode == RESULT_OK) (intent?.getSerializableExtra(key) as R).also {
                    block.invoke(
                        it,
                        intent!!
                    )
                } else null
            }
        }
    }
}