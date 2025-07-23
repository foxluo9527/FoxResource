package com.foxluo.baselib.util

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

private fun Activity.checkPermissionResult(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        && (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, READ_MEDIA_VIDEO) == PERMISSION_GRANTED)
    ) {
        return true
    } else if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        ContextCompat.checkSelfPermission(
            this,
            READ_MEDIA_VISUAL_USER_SELECTED
        ) == PERMISSION_GRANTED
    ) {
        return true
    } else if (ContextCompat.checkSelfPermission(
            this,
            READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED
    ) {
        return true
    } else {
        return false
    }
}

fun Activity.requestMediaPermission(block: (Boolean) -> Unit) {
    if (checkPermissionResult()) {
        block(true)
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        XXPermissions.with(this)
            .permission(Permission.READ_MEDIA_VISUAL_USER_SELECTED,Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    block(true)
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    block(false)
                }
            })
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        XXPermissions.with(this)
            .permission(Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    block(true)
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    block(false)
                }
            })
    } else {
        XXPermissions.with(this)
            .permission(Permission.READ_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    block(true)
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    block(false)
                }
            })
    }

}