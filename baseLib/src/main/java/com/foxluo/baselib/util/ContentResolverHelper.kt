package com.foxluo.baselib.util

import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import com.foxluo.baselib.ui.adapter.AlbumAdapter.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContentResolverHelper {
    suspend fun ContentResolver.loadImages(block: (List<Image>) -> Unit) {
        withContext(Dispatchers.IO) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE
            )

            // 筛选图片和视频
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            val cursor = query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            if (cursor != null) {
                val imageList = mutableListOf<Image>()
                try {
                    while (cursor.moveToNext()) {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val mimeType =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))

                        val contentUri = when {
                            mimeType?.startsWith("image/") == true ->
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                                    .appendPath(id.toString())
                                    .build()

                            mimeType?.startsWith("video/") == true ->
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                                    .appendPath(id.toString())
                                    .build()

                            else -> null
                        }
                        if (contentUri != null) {
                            val date =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)) * 1000L
                            val isVideo =
                                !(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                                    .contains("image"))
                            imageList.add(Image(contentUri, date, isVideo = isVideo))
                        }
                    }
                    withContext(Dispatchers.Main) {
                        block(imageList)
                    }
                } catch (e: Exception) {
                    e.message
                } finally {
                    cursor.close()
                }
            }
        }
    }
}