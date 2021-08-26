package com.huawei.touchmenot.java.main.common;

import android.util.Log;

public class ExceptionHandling {
    public ExceptionHandling(){}
    public  void PrintExceptionInfo(String tag, Exception e) {
        Log.d(tag,Constants.STR_ERROR_MSG,e);
    }
}