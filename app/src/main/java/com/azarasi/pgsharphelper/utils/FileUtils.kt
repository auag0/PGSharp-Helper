package com.azarasi.pgsharphelper.utils

import android.annotation.SuppressLint
import android.os.Build
import java.io.File

object FileUtils {
    @SuppressLint("SdCardPath")
    fun getDataFile(packageName: String, userId: Int = 0): File {
        val dataPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "/data_mirror/data_ce/null/$userId/$packageName"
        } else {
            "/data/user/$userId/$packageName"
        }
        return File(dataPath)
    }
}