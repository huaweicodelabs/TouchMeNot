/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.huawei.touchmenot.kotlin.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.MediaStore
import android.util.Log
import com.huawei.touchmenot.kotlin.main.common.Constants
import java.text.SimpleDateFormat
import java.util.*

class Utils private constructor() {
    private var CURRENT_TIME = "Current time => "
    fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        mContext!!.startActivity(cameraIntent)
    }

    val todayDate: String
        get() {
            val c = Calendar.getInstance().time
            CURRENT_TIME = Constants.STR_EMPTY
            Log.d(TAG, CURRENT_TIME + c)
            val df = SimpleDateFormat(Constants.STR_DATE_PATTERN, Locale.getDefault())
            val formattedDate = df.format(c)
            return Constants.STR_TODAY_DATE + formattedDate
        }

    val todayTime: String
        get() {
            val d = Date()
            val sdf = SimpleDateFormat(Constants.STR_HOUR_PATTERN)
            return sdf.format(d)
        }

    fun openGallery() {
        val intent = Intent()
        intent.type = Constants.STR_IMG_TYPE
        intent.action = Intent.ACTION_GET_CONTENT
        mContext!!.startActivity(Intent.createChooser(intent, Constants.STR_SELECT_PICTURE))
    }

    companion object {
        var preferences: SharedPreferences? = null
            private set
        private var instance: Utils? = null
        private var mContext: Context? = null
        private const val TAG = "UTILS"

        fun getInstance(context: Context?): Utils? {
            return if (instance == null) {
                mContext = context
                preferences = mContext!!.getSharedPreferences(Constants.STR_MYPREF,
                        Context.MODE_PRIVATE)
                Utils().also { instance = it }
            } else instance
        }

        fun setValue(key: String?, value: String?) {
            val editor = preferences!!.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun getValue(data: String?): String? {
            return if (preferences!!.contains(data)) {
                preferences!!.getString(data, Constants.STR_EMPTY)
            } else null
        }
    }
}