package com.huawei.touchmenot.kotlin.main.common

import android.util.Log

class ExceptionHandling {
    fun PrintExceptionInfo(tag: String?, e: Exception?) {
        Log.d(tag, Constants.STR_ERROR_MSG, e)
    }
}