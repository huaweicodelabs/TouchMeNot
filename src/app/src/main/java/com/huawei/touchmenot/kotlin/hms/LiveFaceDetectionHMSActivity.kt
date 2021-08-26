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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import com.huawei.touchmenot.kotlin.hms.camera.CameraSourcePreview
import com.huawei.touchmenot.kotlin.hms.camera.GraphicOverlay
import com.huawei.touchmenot.R
import com.huawei.touchmenot.kotlin.main.common.Constants
import java.io.IOException

class LiveFaceDetectionHMSActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    var analyzer: MLFaceAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private var mPreview: CameraSourcePreview? = null
    private var mOverlay: GraphicOverlay? = null
    private var lensType = LensEngine.BACK_LENS
    private var eyeBlinkImgView: ImageView? = null
    var facingSwitch: ToggleButton? = null
    private val DISPLAY_WIDTH = 640
    private val DISPLAY_HEIGHT = 480
    private val DURATION = 50
    private val VALUE_OFFSET = 20
    private val VALUE_25F = 25.0f
    private val VALUE_POINT_0 = 0.0f
    private val VALUE_POINT_1 = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_face_detection_h_m_s)
        eyeBlinkImgView = findViewById(R.id.eyeBlinkImgView)
        mPreview = findViewById(R.id.preview)
        mOverlay = findViewById(R.id.overlay)
        createFaceAnalyzer()
        facingSwitch = findViewById(R.id.facingSwitch)
        facingSwitch?.setOnCheckedChangeListener(this)
        // Checking Camera Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Handler().postDelayed({ facingSwitch?.setChecked(true) }, Constants.DELAY_MILLIS.toLong())
            createLensEngine()
        } else {
            requestCameraPermission()
        }
        loadBannerAds()
        val anim: Animation = AlphaAnimation(VALUE_POINT_0, VALUE_POINT_1)
        anim.duration = DURATION.toLong() //You can manage the blinking time with this parameter
        anim.startOffset = VALUE_OFFSET.toLong()
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        val tvSuggestion = findViewById<TextView>(R.id.tvSuggestion)
        tvSuggestion.startAnimation(anim)
        Glide.with(this)
                .load(R.drawable.eye_blink)
                .centerCrop()
                .placeholder(R.drawable.eye_blink)
                .into(eyeBlinkImgView!!)
    }

    private fun loadBannerAds() {
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this)
        // Obtain BannerView based on the configuration in layout/ad_fragment.xml.
        val bottomBannerView = findViewById<BannerView>(R.id.hw_banner_view)
        val adParam = AdParam.Builder().build()
        bottomBannerView.loadAd(adParam)
        val bottomBannerView1 = findViewById<BannerView>(R.id.hw_banner_view1)
        val adParam1 = AdParam.Builder().build()
        bottomBannerView1.loadAd(adParam1)
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE)
            return
        } else ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE)
    }

    override fun onResume() {
        super.onResume()
        startLensEngine()
    }

    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLensEngine != null) {
            mLensEngine!!.release()
        }
        if (analyzer != null) {
            analyzer!!.destroy()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != CAMERA_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.size != Constants.INIT_ZERO && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED) {
            Handler().postDelayed({ facingSwitch!!.isChecked = true }, Constants.DELAY_MILLIS.toLong())
            createLensEngine()
            return
        } else finish()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (mLensEngine != null) {
            if (isChecked) {
                lensType = LensEngine.FRONT_LENS
            } else {
                lensType = LensEngine.BACK_LENS
            }
        }
        mLensEngine!!.close()
        createLensEngine()
        startLensEngine()
    }

    private fun createFaceAnalyzer(): MLFaceAnalyzer? {
        val setting = MLFaceAnalyzerSetting.Factory()
                .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
                .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
                .allowTracing()
                .create()
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)
        analyzer?.setTransactor(FaceAnalyzerTransactor(mOverlay, this))
        return analyzer
    }

    private fun createLensEngine() {
        val context = this.applicationContext
        // Create LensEngine
        mLensEngine = LensEngine.Creator(context, analyzer)
                .setLensType(lensType)
                .applyDisplayDimension(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                .applyFps(VALUE_25F)
                .enableAutomaticFocus(true)
                .create()
    }

    private fun startLensEngine() {
        if (mLensEngine != null) {
            try {
                mPreview!!.start(mLensEngine, mOverlay)
            } catch (e: IOException) {
                Log.d(TAG, Constants.STR_FAILED_ENGINE, e)
                mLensEngine!!.release()
                mLensEngine = null
            }
        }
    }

    companion object {
        private val TAG = LiveFaceDetectionHMSActivity::class.java.simpleName
        private const val CAMERA_PERMISSION_CODE = 2
    }
}