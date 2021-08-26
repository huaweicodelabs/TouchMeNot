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
package com.huawei.touchmenot.java.hms.hand;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzer;
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzerFactory;
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzerSetting;
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoints;
import com.huawei.touchmenot.java.hms.camera.GraphicOverlay;
import com.huawei.touchmenot.java.hms.camera.LensEnginePreview;
import com.huawei.touchmenot.R;
import com.huawei.touchmenot.java.main.Utils;
import com.huawei.touchmenot.java.main.common.Constants;
import com.huawei.touchmenot.java.main.model.RatingInterface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LiveHandKeyPointAnalyseActivity extends AppCompatActivity implements View.OnClickListener, RatingInterface
{
    private static final String TAG = LiveHandKeyPointAnalyseActivity.class.getSimpleName();
    private static final int CAMERA_PERMISSION_CODE = 0;
    private static final String[] ALL_PERMISSION = new String[]{Manifest.permission.CAMERA,};
    private LensEnginePreview mPreview;
    private GraphicOverlay mOverlay;
    private Button mFacingSwitch;
    private MLHandKeypointAnalyzer mAnalyzer;
    private LensEngine mLensEngine;
    private int lensType = LensEngine.BACK_LENS;
    private int mLensType;
    private boolean isFront = false;
    private boolean isPermissionRequested;

    public Context mContext;
    String rating = "";
    RatingBar ratingBar;
    TextView tvRating;
    private Handler handler;
    private int THREAD_DELAY_2000 = 2000, THREAD_DELAY_10000 = 10000, THREAD_DELAY_5000 = 5000;
    private float FLOAT_25F = 25.0f;
    private int DISPLAY_HEIGHT = 640, DISPLAY_WIDTH = 480, SDK_VERSION = 23;

    @Override
    public void ratingCaptured(String value) {
        rating = value;
        Handler responseHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!value.isEmpty()) {
                    ratingBar.setRating(Float.valueOf(value));
                    Utils.getInstance(getApplicationContext()).setValue(Constants.STR_RATING, value);
                    tvRating.setText(Constants.STR_HAVE_RATED + value + Constants.STR_SHARING_EXPERIENCE);
                }
            }
        };
        responseHandler.postDelayed(runnable, THREAD_DELAY_2000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mPreview.getVisibility() == View.GONE) {
                mPreview.setVisibility(View.VISIBLE);
                handler.postDelayed(this, THREAD_DELAY_10000);
            } else {
                if (!rating.equalsIgnoreCase(String.valueOf(R.string.zero))) {
                    mPreview.setVisibility(View.GONE);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, THREAD_DELAY_5000);
                } else {
                    handler.postDelayed(this, THREAD_DELAY_5000);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_hand_key_point_analyse_java);
        ratingBar = findViewById(R.id.ratingBar);
        tvRating = findViewById(R.id.tvRating);
        mContext = this;
        if (savedInstanceState != null) {
            mLensType = savedInstanceState.getInt(Constants.STR_LENS_TYPE);
        }

        String rateValue = Utils.getInstance(getApplicationContext()).getValue(Constants.STR_RATING);
        if (rateValue != null && !rateValue.isEmpty()) {
            ratingBar.setRating(Float.valueOf(rateValue));
            tvRating.setText(Constants.STR_RATED + rateValue + Constants.STR_RATE_VALUE);
        }
        initView();
        createHandAnalyzer();
        if (Camera.getNumberOfCameras() == Constants.INIT_ONE) {
            mFacingSwitch.setVisibility(View.GONE);
        }
        // Checking Camera Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createLensEngine();
        } else {
            checkPermission();
        }

        handler = new Handler();
        handler.postDelayed(runnable, THREAD_DELAY_5000);
    }

    private void initView() {
        mPreview = findViewById(R.id.hand_preview);
        mOverlay = findViewById(R.id.hand_overlay);
        mFacingSwitch = findViewById(R.id.handswitch);
        mFacingSwitch.setOnClickListener(this);
    }

    private void createHandAnalyzer() {
        // Create a  analyzer. You can create an analyzer using the provided customized face detection parameter: MLHandKeypointAnalyzerSetting
        MLHandKeypointAnalyzerSetting setting =
                new MLHandKeypointAnalyzerSetting.Factory()
                        .setMaxHandResults(Constants.INIT_TWO)
                        .setSceneType(MLHandKeypointAnalyzerSetting.TYPE_ALL)
                        .create();
        mAnalyzer = MLHandKeypointAnalyzerFactory.getInstance().getHandKeypointAnalyzer(setting);
        mAnalyzer.setTransactor(new HandAnalyzerTransactor(this, mOverlay));
    }

    // Check the permissions required by the SDK.
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= SDK_VERSION  && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            for (String perm : getAllPermission()) {
                if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                }
            }

            if (!permissionsList.isEmpty()) {
                requestPermissions(permissionsList.toArray(new String[Constants.INIT_ZERO]), Constants.INIT_ZERO);
            }
        }
    }

    public static List<String> getAllPermission() {
        return Collections.unmodifiableList(Arrays.asList(ALL_PERMISSION));
    }

    private void createLensEngine() {
        Context context = this.getApplicationContext();
        // Create LensEngine.
        mLensEngine = new LensEngine.Creator(context, mAnalyzer)
                .setLensType(this.mLensType)
                .applyDisplayDimension(DISPLAY_HEIGHT, DISPLAY_WIDTH)
                .applyFps(FLOAT_25F)
                .enableAutomaticFocus(true)
                .create();
    }

    private void startLensEngine() {
        if (this.mLensEngine != null) {
            try {
                this.mPreview.start(this.mLensEngine, this.mOverlay);
            } catch (IOException e) {
                Log.d(TAG, Constants.STR_FAILED_ENGINE, e);
                this.mLensEngine.release();
                this.mLensEngine = null;
            }
        }
    }

    // Permission application callback.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean hasAllGranted = true;
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED) {
                this.createLensEngine();
            } else if (grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[Constants.INIT_ZERO])) {
                    showWaringDialog();
                } else {
                    Toast.makeText(this, R.string.toast, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(Constants.STR_LENS_TYPE, this.lensType);
        super.onSaveInstanceState(outState);
    }

    private class HandAnalyzerTransactor implements MLAnalyzer.MLTransactor<MLHandKeypoints> {
        private GraphicOverlay mGraphicOverlay;
        WeakReference<LiveHandKeyPointAnalyseActivity> mMainActivityWeakReference;

        HandAnalyzerTransactor(LiveHandKeyPointAnalyseActivity mainActivity, GraphicOverlay ocrGraphicOverlay) {
            mMainActivityWeakReference = new WeakReference<>(mainActivity);
            this.mGraphicOverlay = ocrGraphicOverlay;
        }

        /**
         * Process the results returned by the analyzer.
         *
         * @param result
         */
        @Override
        public void transactResult(MLAnalyzer.Result<MLHandKeypoints> result) {
            this.mGraphicOverlay.clear();
            SparseArray<MLHandKeypoints> handKeypointsSparseArray = result.getAnalyseList();
            List<MLHandKeypoints> list = new ArrayList<>();
            System.out.println(String.valueOf(R.string.point_list_size)+ handKeypointsSparseArray.size());
            for (int i = Constants.INIT_ZERO; i < handKeypointsSparseArray.size(); i++) {
                list.add(handKeypointsSparseArray.valueAt(i));
                System.out.println(String.valueOf(R.string.point_list_size_new) + handKeypointsSparseArray.valueAt(i).getHandKeypoints());
            }

            HandKeypointGraphic graphic = new HandKeypointGraphic(this.mGraphicOverlay, list, result, LiveHandKeyPointAnalyseActivity.this);
            this.mGraphicOverlay.add(graphic);
        }

        @Override
        public void destroy() {
            this.mGraphicOverlay.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.handswitch:
                switchCamera();
                break;
        }
    }

    private void switchCamera() {
        isFront = !isFront;
        if (this.isFront) {
            mLensType = LensEngine.FRONT_LENS;
        } else {
            mLensType = LensEngine.BACK_LENS;
        }
        if (this.mLensEngine != null) {
            this.mLensEngine.close();
        }
        this.createLensEngine();
        this.startLensEngine();
    }

    private void showWaringDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.Information_permission)
                .setPositiveButton(R.string.go_authorization, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts(String.valueOf(R.string.PACKAGE), getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createLensEngine();
            startLensEngine();
        } else {
            checkPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mLensEngine != null) {
            this.mLensEngine.release();
        }
        if (this.mAnalyzer != null) {
            this.mAnalyzer.stop();
        }
    }
}