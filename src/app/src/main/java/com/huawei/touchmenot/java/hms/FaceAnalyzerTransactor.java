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
package com.huawei.touchmenot.java.hms;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceFeature;
import com.huawei.touchmenot.java.hms.camera.GraphicOverlay;
import com.huawei.touchmenot.java.main.HomeActivity;
import com.huawei.touchmenot.java.main.common.Constants;

public class FaceAnalyzerTransactor implements MLAnalyzer.MLTransactor<MLFace> {
    private static final float EYE_CLOSED_THRESHOLD = 0.4f;
    private GraphicOverlay mGraphicOverlay;
    private LiveFaceDetectionHMSActivity mContext;
    private int THREAD_DELAY = 800;

    FaceAnalyzerTransactor(GraphicOverlay ocrGraphicOverlay, LiveFaceDetectionHMSActivity context) {
        this.mGraphicOverlay = ocrGraphicOverlay;
        mContext = context;
    }

    @Override
    public void transactResult(MLAnalyzer.Result<MLFace> result) {
        this.mGraphicOverlay.clear();
        SparseArray<MLFace> faceSparseArray = result.getAnalyseList();
        for (int i = Constants.INIT_ZERO; i < faceSparseArray.size(); i++) {
            MLFaceFeature feature = faceSparseArray.get(i).getFeatures();
            float leftOpenScore = feature.getLeftEyeOpenProbability();
            float rightOpenScore = feature.getRightEyeOpenProbability();
            if (leftOpenScore < EYE_CLOSED_THRESHOLD && rightOpenScore < EYE_CLOSED_THRESHOLD) {
                Log.d(Constants.STR_EYE_BLINKED_CALLED, feature.getLeftEyeOpenProbability()
                        + Constants.STR_COLON+ feature.getRightEyeOpenProbability());
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, Constants.STR_EYE_BLINK_LOGIN, Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadHome();
                            }
                        }, THREAD_DELAY);
                    }
                });
            }
        }
    }

    private void loadHome() {
        mContext.startActivity(new Intent(mContext, HomeActivity.class));
        mContext.finish();
    }

    @Override
    public void destroy() {
        this.mGraphicOverlay.clear();
    }
}