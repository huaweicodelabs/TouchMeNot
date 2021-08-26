/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.touchmenot.kotlin.main.common

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.huawei.hiar.exceptions.ARFatalException
import com.huawei.touchmenot.kotlin.hms.LiveFaceDetectionHMSActivity
import com.huawei.touchmenot.R

/**
 * This activity is used to redirect the user to AppGallery and install the AR Engine server.
 * This activity is called when the AR Engine is not installed.
 *
 * @author HW
 * @since 2020-03-31
 */
class ConnectAppMarketActivity : Activity() {
    private var dialog: AlertDialog.Builder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_app_market)
        showSuggestiveDialog()
    }

    override fun onResume() {
        if (dialog != null) {
            Log.d(TAG, Constants.STR_SHOW_DIALOG)
            dialog!!.show()
        }
        super.onResume()
    }

    private fun showSuggestiveDialog() {
        Log.d(TAG, Constants.STR_EDC_DIALOG)
        dialog = AlertDialog.Builder(this)
        showAppMarket()
    }

    private fun showAppMarket() {
        dialog!!.setMessage(R.string.arengine_install_app)
        dialog!!.setNegativeButton(R.string.arengine_cancel) { dialogInterface, i ->
            Log.d(TAG, Constants.STR_SHOW_APP_MARKET)
            Toast.makeText(this@ConnectAppMarketActivity, R.string.AR_NOT_SUPPORTING, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@ConnectAppMarketActivity, LiveFaceDetectionHMSActivity::class.java))
            finish()
        }
        dialog!!.setPositiveButton(R.string.arengine_install) { dialogInterface, i ->
            try {
                Log.d(TAG, Constants.STR_AR_ENGINE_CALLBACK)
                downLoadArServiceApp()
                Toast.makeText(this@ConnectAppMarketActivity, R.string.AR_NOT_SUPPORTING, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ConnectAppMarketActivity, LiveFaceDetectionHMSActivity::class.java))
                finish()
            } catch (e: ActivityNotFoundException) {
                throw ARFatalException(Constants.STR_AR_INSTALL_ACTIVITY)
            }
        }
        dialog!!.setOnCancelListener { finish() }
    }

    private fun downLoadArServiceApp() {
        try {
            val intent = Intent(ACTION_HUAWEI_DOWNLOAD_QUIK)
            intent.putExtra(PACKAGE_NAME_KEY, PACKAGENAME_ARSERVICE)
            intent.setPackage(HUAWEI_MARTKET_NAME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: SecurityException) {
            Log.d(TAG, Constants.STR_NO_PERMISSION_MEDIA)
        } catch (e: ActivityNotFoundException) {
            Log.d(TAG, Constants.STR_TARGET_ACTIVITY_NOT_FOUND, e)
        }
    }

    companion object {
        private val TAG = ConnectAppMarketActivity::class.java.simpleName
        private const val ACTION_HUAWEI_DOWNLOAD_QUIK = "com.huawei.appmarket.intent.action.AppDetail"
        private const val HUAWEI_MARTKET_NAME = "com.huawei.appmarket"
        private const val PACKAGE_NAME_KEY = "APP_PACKAGENAME"
        private const val PACKAGENAME_ARSERVICE = "com.huawei.arengine.service"
    }
}