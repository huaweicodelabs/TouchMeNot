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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting;
import com.huawei.touchmenot.java.hms.camera.CameraSourcePreview;
import com.huawei.touchmenot.java.hms.camera.GraphicOverlay;
import com.huawei.touchmenot.R;
import com.huawei.touchmenot.java.main.common.Constants;

import java.io.IOException;

public class LiveFaceDetectionHMSActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = LiveFaceDetectionHMSActivity.class.getSimpleName();
    private static final int CAMERA_PERMISSION_CODE = 2;
    MLFaceAnalyzer analyzer;
    private LensEngine mLensEngine;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mOverlay;
    private int lensType = LensEngine.BACK_LENS;
    private ImageView eyeBlinkImgView;
    ToggleButton facingSwitch;
    private int DISPLAY_WIDTH = 640, DISPLAY_HEIGHT = 480, DURATION = 50, VALUE_OFFSET = 20;
    private float VALUE_25F = 25.0f, VALUE_POINT_0=0.0F, VALUE_POINT_1 = 1.0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_face_detection_h_m_s_java);
        eyeBlinkImgView = findViewById(R.id.eyeBlinkImgView);
        this.mPreview = this.findViewById(R.id.preview);
        this.mOverlay = this.findViewById(R.id.overlay);
        this.createFaceAnalyzer();
        facingSwitch = this.findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Checking Camera Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    facingSwitch.setChecked(true);
                }
            }, Constants.DELAY_MILLIS);
            this.createLensEngine();
        } else {
            this.requestCameraPermission();
        }
        loadBannerAds();
        Animation anim = new AlphaAnimation(VALUE_POINT_0, VALUE_POINT_1);
        anim.setDuration(DURATION); //You can manage the blinking time with this parameter
        anim.setStartOffset(VALUE_OFFSET);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        TextView tvSuggestion = findViewById(R.id.tvSuggestion);
        tvSuggestion.startAnimation(anim);

        Glide.with(this)
                .load(R.drawable.eye_blink)
                .centerCrop()
                .placeholder(R.drawable.eye_blink)
                .into(eyeBlinkImgView);
    }

    private void loadBannerAds() {
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this);
        // Obtain BannerView based on the configuration in layout/ad_fragment.xml.
        BannerView bottomBannerView = findViewById(R.id.hw_banner_view);
        AdParam adParam = new AdParam.Builder().build();
        bottomBannerView.loadAd(adParam);

        BannerView bottomBannerView1 = findViewById(R.id.hw_banner_view1);
        AdParam adParam1 = new AdParam.Builder().build();
        bottomBannerView1.loadAd(adParam1);
    }


    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, LiveFaceDetectionHMSActivity.CAMERA_PERMISSION_CODE);
            return;
        } else
            ActivityCompat.requestPermissions(this, permissions, LiveFaceDetectionHMSActivity.CAMERA_PERMISSION_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.startLensEngine();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mLensEngine != null) {
            this.mLensEngine.release();
        }
        if (this.analyzer != null) {
            this.analyzer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LiveFaceDetectionHMSActivity.CAMERA_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != Constants.INIT_ZERO && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    facingSwitch.setChecked(true);
                }
            }, Constants.DELAY_MILLIS);
            this.createLensEngine();
            return;
        } else
            finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (this.mLensEngine != null) {
            if (isChecked) {
                this.lensType = LensEngine.FRONT_LENS;
            } else {
                this.lensType = LensEngine.BACK_LENS;
            }
        }
        this.mLensEngine.close();
        this.createLensEngine();
        this.startLensEngine();
    }

    private MLFaceAnalyzer createFaceAnalyzer() {
        MLFaceAnalyzerSetting setting = new MLFaceAnalyzerSetting.Factory()
                .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
                .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
                .allowTracing()
                .create();
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting);
        this.analyzer.setTransactor(new FaceAnalyzerTransactor(this.mOverlay, this));
        return this.analyzer;
    }

    private void createLensEngine() {
        Context context = this.getApplicationContext();
        // Create LensEngine
        this.mLensEngine = new LensEngine.Creator(context, this.analyzer)
                .setLensType(this.lensType)
                .applyDisplayDimension(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                .applyFps(VALUE_25F)
                .enableAutomaticFocus(true)
                .create();
    }

    private void startLensEngine() {
        if (this.mLensEngine != null) {
            try {
                this.mPreview.start(this.mLensEngine, this.mOverlay);
            } catch (IOException e) {
                Log.d(LiveFaceDetectionHMSActivity.TAG, Constants.STR_FAILED_ENGINE, e);
                this.mLensEngine.release();
                this.mLensEngine = null;
            }
        }
    }
}