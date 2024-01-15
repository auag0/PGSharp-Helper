package com.azarasi.pgsharphelper.utils

import android.os.Process
import android.os.UserHandle

object UserHandleHidden {
    fun getUserId(uid: Int = Process.myUid()): Int {
        val clazz = UserHandle::class.java
        val getUserId = clazz.getDeclaredMethod("getUserId", Int::class.java)
        return getUserId.invoke(null, uid) as Int
    }
}