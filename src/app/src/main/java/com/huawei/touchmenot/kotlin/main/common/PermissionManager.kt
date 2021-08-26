/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.touchmenot.kotlin.main.common

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

object PermissionManager {
    private const val REQUEST_CODE_ASK_PERMISSIONS = 1
    private val PERMISSIONS_ARRAYS = arrayOf(Manifest.permission.CAMERA)

    // List of permissions to be applied for.
    private val permissionsList: MutableList<String> = ArrayList()

    /**
     * Check whether the current app has the necessary permissions (by default, the camera permission is required).
     * If not, apply for the permission. This method should be called in the onResume method of the main activity.
     *
     * @param activity Activity
     */
    @JvmStatic
    fun onResume(activity: Activity?) {
        var isHasPermission = true
        for (permission in PERMISSIONS_ARRAYS) {
            if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                isHasPermission = false
                break
            }
        }
        if (!isHasPermission) {
            for (permission in PERMISSIONS_ARRAYS) {
                if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission)
                }
            }
            ActivityCompat.requestPermissions(activity!!,
                    permissionsList.toTypedArray(), REQUEST_CODE_ASK_PERMISSIONS)
        }
    }

    /**
     * Check whether the current app has the required permissions.
     * This method will be called when [com.huawei.touchmenot.main.HomeActivity.onRequestPermissionsResult].
     *
     * @param activity Activity.
     * @return Has permission or not.
     */
    fun hasPermission(activity: Activity): Boolean {
        for (permission in PERMISSIONS_ARRAYS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}