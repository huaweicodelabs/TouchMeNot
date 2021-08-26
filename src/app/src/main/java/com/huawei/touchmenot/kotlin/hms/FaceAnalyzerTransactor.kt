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
package com.huawei.touchmenot.kotlin.hms

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLAnalyzer.MLTransactor
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.touchmenot.kotlin.hms.camera.GraphicOverlay
import com.huawei.touchmenot.kotlin.main.HomeActivity
import com.huawei.touchmenot.kotlin.main.common.Constants

class FaceAnalyzerTransactor internal constructor(private val mGraphicOverlay: GraphicOverlay?, private val mContext: LiveFaceDetectionHMSActivity) : MLTransactor<MLFace> {
    private val THREAD_DELAY = 800
    override fun transactResult(result: MLAnalyzer.Result<MLFace>) {
        mGraphicOverlay!!.clear()
        val faceSparseArray = result.analyseList
        for (i in Constants.INIT_ZERO until faceSparseArray.size()) {
            val feature = faceSparseArray[i].features
            val leftOpenScore = feature.leftEyeOpenProbability
            val rightOpenScore = feature.rightEyeOpenProbability
            if (leftOpenScore < EYE_CLOSED_THRESHOLD && rightOpenScore < EYE_CLOSED_THRESHOLD) {
                Log.d(Constants.STR_EYE_BLINKED_CALLED, feature.leftEyeOpenProbability
                        .toString() + Constants.STR_COLON + feature.rightEyeOpenProbability)
                mContext.runOnUiThread {
                    Toast.makeText(mContext, Constants.STR_EYE_BLINK_LOGIN, Toast.LENGTH_SHORT).show()
                    Handler().postDelayed({ loadHome() }, THREAD_DELAY.toLong())
                }
            }
        }
    }

    private fun loadHome() {
        mContext.startActivity(Intent(mContext, HomeActivity::class.java))
        mContext.finish()
    }

    override fun destroy() {
        mGraphicOverlay!!.clear()
    }

    companion object {
        private const val EYE_CLOSED_THRESHOLD = 0.4f
    }

}