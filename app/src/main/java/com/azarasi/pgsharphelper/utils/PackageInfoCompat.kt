package com.azarasi.pgsharphelper.utils

import android.content.pm.PackageInfo
import android.os.Build

object PackageInfoCompat {
    fun PackageInfo.getCVersionCode(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            @Suppress("DEPRECATION")
            versionCode.toLong()
        }
    }
}