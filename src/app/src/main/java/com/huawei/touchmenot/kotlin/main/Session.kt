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
import android.content.SharedPreferences
import com.huawei.touchmenot.kotlin.main.common.Constants

class Session(var _context: Context) {
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor

    // shared pref mode
    var PRIVATE_MODE = 0

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(Constants.IS_FIRST_TIME_LAUNCH, Constants.STATUS_SUCCESS)
        set(isFirstTime) {
            editor.putBoolean(Constants.IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    init {
        pref = _context.getSharedPreferences(Constants.PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}