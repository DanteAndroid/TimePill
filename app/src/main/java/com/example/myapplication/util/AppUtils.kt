package com.example.myapplication.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.FileIOUtils
import java.io.*

/**
 * @author Du Wenyu
 * 2021/5/29
 */
object AppUtils {

    fun pickImage(context: Fragment, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        context.startActivityForResult(intent, requestCode)
    }

    fun fetchImage(context: Context, data: Intent): File? {
        if (data.data == null) return null
        var picture: File? = null
        try {
            var suffix = ".jpg"
            val type: String? = if (data.type == null) {
                context.contentResolver.getType(data.data!!)
            } else data.type
            type?.let {
                if (it.contains("/"))
                    suffix = "." + type.split("/")[1]
            }
            Log.d("AppUtils", "saveToFile: $suffix")
            if (data.data!!.path!!.endsWith(".gif")) {
                suffix = ".gif"
            }
            picture =
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "picture$suffix")
            if (picture.exists()) picture.delete()
            val result = picture.createNewFile()
            if (result.not()) return null

            if (suffix.endsWith("gif")) {
                val inputStream: InputStream =
                    context.contentResolver.openInputStream(data.data!!)!!
                FileIOUtils.writeFileFromIS(picture, inputStream, true)
            } else {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            data.data!!
                        )
                    )
                } else MediaStore.Images.Media.getBitmap(context.contentResolver, data.data)
                if (bitmap != null) {
                    val os: OutputStream = BufferedOutputStream(FileOutputStream(picture))
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    Log.i(
                        "AppUtils",
                        "saveToFile: scale bitmap " + bitmap.width + ", " + bitmap.height
                    )
                    os.close()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return picture
    }

}