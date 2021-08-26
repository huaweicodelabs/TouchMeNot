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
package com.huawei.touchmenot.java.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.splash.SplashAdDisplayListener;
import com.huawei.hms.ads.splash.SplashView;
import com.huawei.touchmenot.R;
import com.huawei.touchmenot.java.hms.LiveFaceDetectionHMSActivity;
import com.huawei.touchmenot.java.main.common.Constants;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    // Ad display timeout interval, in milliseconds.
    private static final int AD_TIMEOUT = 5000;
    // Ad display timeout message flag.
    private static final int MSG_AD_TIMEOUT = 1001;
    private boolean hasPaused = false;
    private SplashView splashView;
    private Session session;
    private String TEMP_MSG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this);
        Handler mainHandler = new Handler();
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAd();
            }
        }, Constants.DELAY_MILLIS);
    }

    private void loadAd() {
        Log.d(TAG, Constants.STR_START_AD);
        AdParam adParam = new AdParam.Builder().build();
        splashView = findViewById(R.id.splash_ad_view);
        splashView.setAdDisplayListener(adDisplayListener);
        // Set a logo image.
        splashView.setLogoResId(R.mipmap.ic_launcher);
        // Set logo description.
        splashView.setMediaNameResId(R.string.app_name);
        // Set the audio focus type for a video splash ad.
        splashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);

        splashView.load(getString(R.string.ad_id_splash), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, adParam, splashAdLoadListener);
        Log.d(TAG, Constants.STR_END_AD);

        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT);
        // Send a delay message to ensure that the app home screen can be displayed when the ad display times out.
        timeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT);
    }

    /**
     * Switch from the splash ad screen to the app home screen when the ad display is complete.
     */
    private void jump() {
        TEMP_MSG = Constants.STR_PAUSED+hasPaused;
        Log.d(TAG, TEMP_MSG);
        if (!hasPaused) {
            hasPaused = true;
            Log.d(TAG, Constants.STR_JUMP_APPLICATION);
            session = new Session(this);
            if (!session.isFirstTimeLaunch()) {
                launchHomeScreen();
                finish();
            } else {
                session.setFirstTimeLaunch(false);
                Intent intent = new Intent(SplashActivity.this, TransformationActivity.class);
                intent.putExtra(Constant.TRANSFORMATION, Constant.CLOCK_SPIN_TRANSFORMATION);
                startActivity(intent);
                finish();
            }
            Handler mainHandler = new Handler();
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, Constants.DELAY_MILLIS);
        }
    }

    private void launchHomeScreen() {
        startActivity(new Intent(SplashActivity.this, LiveFaceDetectionHMSActivity.class));
    }

    /**
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     */
    @Override
    protected void onStop() {
        Log.d(TAG, Constants.STR_ON_STOP);
        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT);
        hasPaused = true;
        super.onStop();
    }

    /**
     * Call this method when returning to the splash ad screen from another screen to access the app home screen.
     */
    @Override
    protected void onRestart() {
        Log.d(TAG, Constants.STR_ON_RESTART);
        super.onRestart();
        hasPaused = false;
        jump();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, Constants.STR_ON_DESTROY);
        super.onDestroy();
        if (splashView != null) {
            splashView.destroyView();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, Constants.STR_ON_PAUSE);
        super.onPause();
        if (splashView != null) {
            splashView.pauseView();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, Constants.STR_ON_RESUME);
        super.onResume();
        if (splashView != null) {
            splashView.resumeView();
        }
    }

    private SplashView.SplashAdLoadListener splashAdLoadListener = new SplashView.SplashAdLoadListener() {
        @Override
        public void onAdLoaded() {
            // Call this method when an ad is successfully loaded.
            Log.d(TAG, Constants.STR_ON_AD_LOADED);
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            // Call this method when an ad fails to be loaded.
            TEMP_MSG = Constants.STR_ON_AD_FAILED + errorCode;
            Log.d(TAG,  TEMP_MSG);
            jump();
        }

        @Override
        public void onAdDismissed() {
            // Call this method when the ad display is complete.
            Log.d(TAG, Constants.STR_ON_AD_DISMISSED);
            jump();
        }
    };

    private SplashAdDisplayListener adDisplayListener = new SplashAdDisplayListener() {
        @Override
        public void onAdShowed() {
            // Call this method when an ad is displayed.
            Log.d(TAG, Constants.STR_ON_AD_SHOWED);
        }

        @Override
        public void onAdClick() {
            // Call this method when an ad is clicked.
            Log.d(TAG, Constants.STR_ON_AD_CLICK);
        }
    };

    // Callback handler used when the ad display timeout message is received.
    private Handler timeoutHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (SplashActivity.this.hasWindowFocus()) {
                jump();
            }
            return false;
        }
    });
}