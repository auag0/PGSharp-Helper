package com.azarasi.pgsharphelper.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build

object PackageManagerHidden {
    fun PackageManager.getPackageInfoAsUser(
        packageName: String,
        flags: Int,
        userId: Int
    ): PackageInfo? {
        val clazz = PackageManager::class.java
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clazz.getDeclaredMethod(
                "getPackageInfoAsUser",
                String::class.java,
                PackageInfoFlags::class.java,
                Int::class.java
            ).invoke(this, packageName, PackageInfoFlags.of(flags.toLong()), userId)
        } else {
            clazz.getDeclaredMethod(
                "getPackageInfoAsUser",
                String::class.java,
                Int::class.java,
                Int::class.java
            ).invoke(this, packageName, flags, userId)
        } as? PackageInfo?
    }
}