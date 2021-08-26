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

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.AudioFocusType
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.splash.SplashAdDisplayListener
import com.huawei.hms.ads.splash.SplashView
import com.huawei.hms.ads.splash.SplashView.SplashAdLoadListener
import com.huawei.touchmenot.kotlin.hms.LiveFaceDetectionHMSActivity
import com.huawei.touchmenot.kotlin.main.common.Constants
import com.huawei.touchmenot.R

class SplashActivity : AppCompatActivity() {
    private var hasPaused = false
    private var splashView: SplashView? = null
    private var session: Session? = null
    private var TEMP_MSG = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this)
        val mainHandler = Handler()
        mainHandler.postDelayed({ loadAd() }, Constants.DELAY_MILLIS.toLong())
    }

    private fun loadAd() {
        Log.d(TAG, Constants.STR_START_AD)
        val adParam = AdParam.Builder().build()
        splashView = findViewById(R.id.splash_ad_view)
        splashView?.setAdDisplayListener(adDisplayListener)
        // Set a logo image.
        splashView?.setLogoResId(R.mipmap.ic_launcher)
        // Set logo description.
        splashView?.setMediaNameResId(R.string.app_name)
        // Set the audio focus type for a video splash ad.
        splashView?.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE)
        splashView?.load(getString(R.string.ad_id_splash), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, adParam, splashAdLoadListener)
        Log.d(TAG, Constants.STR_END_AD)

        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        // Send a delay message to ensure that the app home screen can be displayed when the ad display times out.
        timeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT.toLong())
    }

    /**
     * Switch from the splash ad screen to the app home screen when the ad display is complete.
     */
    private fun jump() {
        TEMP_MSG = Constants.STR_PAUSED + hasPaused
        Log.d(TAG, TEMP_MSG)
        if (!hasPaused) {
            hasPaused = true
            Log.d(TAG, Constants.STR_JUMP_APPLICATION)
            session = Session(this)
            if (!session!!.isFirstTimeLaunch) {
                launchHomeScreen()
                finish()
            } else {
                session?.isFirstTimeLaunch = false
                val intent = Intent(this@SplashActivity, TransformationActivity::class.java)
                intent.putExtra(Constant.TRANSFORMATION, Constant.CLOCK_SPIN_TRANSFORMATION)
                startActivity(intent)
                finish()
            }
            val mainHandler = Handler()
            mainHandler.postDelayed({ finish() }, Constants.DELAY_MILLIS.toLong())
        }
    }

    private fun launchHomeScreen() {
        startActivity(Intent(this@SplashActivity, LiveFaceDetectionHMSActivity::class.java))
    }

    /**
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     */
    override fun onStop() {
        Log.d(TAG, Constants.STR_ON_STOP)
        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        hasPaused = true
        super.onStop()
    }

    /**
     * Call this method when returning to the splash ad screen from another screen to access the app home screen.
     */
    override fun onRestart() {
        Log.d(TAG, Constants.STR_ON_RESTART)
        super.onRestart()
        hasPaused = false
        jump()
    }

    override fun onDestroy() {
        Log.d(TAG, Constants.STR_ON_DESTROY)
        super.onDestroy()
        if (splashView != null) {
            splashView!!.destroyView()
        }
    }

    override fun onPause() {
        Log.d(TAG, Constants.STR_ON_PAUSE)
        super.onPause()
        if (splashView != null) {
            splashView!!.pauseView()
        }
    }

    override fun onResume() {
        Log.d(TAG, Constants.STR_ON_RESUME)
        super.onResume()
        if (splashView != null) {
            splashView!!.resumeView()
        }
    }

    private val splashAdLoadListener: SplashAdLoadListener = object : SplashAdLoadListener() {
        override fun onAdLoaded() {
            // Call this method when an ad is successfully loaded.
            Log.d(TAG, Constants.STR_ON_AD_LOADED)
        }

        override fun onAdFailedToLoad(errorCode: Int) {
            // Call this method when an ad fails to be loaded.
            TEMP_MSG = Constants.STR_ON_AD_FAILED + errorCode
            Log.d(TAG, TEMP_MSG)
            jump()
        }

        override fun onAdDismissed() {
            // Call this method when the ad display is complete.
            Log.d(TAG, Constants.STR_ON_AD_DISMISSED)
            jump()
        }
    }
    private val adDisplayListener: SplashAdDisplayListener = object : SplashAdDisplayListener() {
        override fun onAdShowed() {
            // Call this method when an ad is displayed.
            Log.d(TAG, Constants.STR_ON_AD_SHOWED)
        }

        override fun onAdClick() {
            // Call this method when an ad is clicked.
            Log.d(TAG, Constants.STR_ON_AD_CLICK)
        }
    }

    // Callback handler used when the ad display timeout message is received.
    private val timeoutHandler = Handler(Handler.Callback {
        if (hasWindowFocus()) {
            jump()
        }
        false
    })

    companion object {
        private val TAG = SplashActivity::class.java.simpleName

        // Ad display timeout interval, in milliseconds.
        private const val AD_TIMEOUT = 5000

        // Ad display timeout message flag.
        private const val MSG_AD_TIMEOUT = 1001
    }
}