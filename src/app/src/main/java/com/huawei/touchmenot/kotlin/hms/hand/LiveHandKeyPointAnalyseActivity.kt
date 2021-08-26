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
package com.huawei.touchmenot.kotlin.hms.hand

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLAnalyzer.MLTransactor
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzer
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzerFactory
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypointAnalyzerSetting
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoints
import com.huawei.touchmenot.kotlin.hms.camera.GraphicOverlay
import com.huawei.touchmenot.kotlin.hms.camera.LensEnginePreview
import com.huawei.touchmenot.R
import com.huawei.touchmenot.kotlin.main.Utils
import com.huawei.touchmenot.kotlin.main.common.Constants
import com.huawei.touchmenot.kotlin.main.model.RatingInterface
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class LiveHandKeyPointAnalyseActivity : AppCompatActivity(), View.OnClickListener, RatingInterface {
    private var mPreview: LensEnginePreview? = null
    private var mOverlay: GraphicOverlay? = null
    private var mFacingSwitch: Button? = null
    private var mAnalyzer: MLHandKeypointAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private val lensType = LensEngine.BACK_LENS
    private var mLensType = 0
    private var isFront = false
    private var isPermissionRequested = false
    var mContext: Context? = null
    var rating = ""
    var ratingBar: RatingBar? = null
    var tvRating: TextView? = null
    private var handler: Handler? = null
    private val THREAD_DELAY_2000 = 2000
    private val THREAD_DELAY_10000 = 10000
    private val THREAD_DELAY_5000 = 5000
    private val FLOAT_25F = 25.0f
    private val DISPLAY_HEIGHT = 640
    private val DISPLAY_WIDTH = 480
    private val SDK_VERSION = 23
    override fun ratingCaptured(value: String?) {
        if (value != null) {
            rating = value
        }
        val responseHandler = Handler()
        val runnable = Runnable {
            if (!value!!.isEmpty()) {
                ratingBar!!.rating = java.lang.Float.valueOf(value)
                Utils.setValue(Constants.STR_RATING, value)
                tvRating!!.text = Constants.STR_HAVE_RATED + value + Constants.STR_SHARING_EXPERIENCE
            }
        }
        responseHandler.postDelayed(runnable, THREAD_DELAY_2000.toLong())
    }

    var runnable: Runnable = object : Runnable {
        override fun run() {
            if (mPreview!!.visibility == View.GONE) {
                mPreview!!.visibility = View.VISIBLE
                handler!!.postDelayed(this, THREAD_DELAY_10000.toLong())
            } else {
                if (!rating.equals(R.string.zero.toString(), ignoreCase = true)) {
                    mPreview!!.visibility = View.GONE
                    handler!!.postDelayed({ finish() }, THREAD_DELAY_5000.toLong())
                } else {
                    handler!!.postDelayed(this, THREAD_DELAY_5000.toLong())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_hand_key_point_analyse)
        ratingBar = findViewById(R.id.ratingBar)
        tvRating = findViewById(R.id.tvRating)
        mContext = this
        if (savedInstanceState != null) {
            mLensType = savedInstanceState.getInt(Constants.STR_LENS_TYPE)
        }
        val rateValue = Utils.getValue(Constants.STR_RATING)
        if (rateValue != null && !rateValue.isEmpty()) {
            ratingBar?.setRating(java.lang.Float.valueOf(rateValue))
            tvRating?.setText(Constants.STR_RATED + rateValue + Constants.STR_RATE_VALUE)
        }
        initView()
        createHandAnalyzer()
        if (Camera.getNumberOfCameras() == Constants.INIT_ONE) {
            mFacingSwitch!!.visibility = View.GONE
        }
        // Checking Camera Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createLensEngine()
        } else {
            checkPermission()
        }
        handler = Handler()
        handler!!.postDelayed(runnable, THREAD_DELAY_5000.toLong())
    }

    private fun initView() {
        mPreview = findViewById(R.id.hand_preview)
        mOverlay = findViewById(R.id.hand_overlay)
        mFacingSwitch = findViewById(R.id.handswitch)
        mFacingSwitch?.setOnClickListener(this)
    }

    private fun createHandAnalyzer() {
        // Create a  analyzer. You can create an analyzer using the provided customized face detection parameter: MLHandKeypointAnalyzerSetting
        val setting = MLHandKeypointAnalyzerSetting.Factory()
                .setMaxHandResults(Constants.INIT_TWO)
                .setSceneType(MLHandKeypointAnalyzerSetting.TYPE_ALL)
                .create()
        mAnalyzer = MLHandKeypointAnalyzerFactory.getInstance().getHandKeypointAnalyzer(setting)
        mAnalyzer?.setTransactor(HandAnalyzerTransactor(this, mOverlay))
    }

    // Check the permissions required by the SDK.
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= SDK_VERSION && !isPermissionRequested) {
            isPermissionRequested = true
            val permissionsList = ArrayList<String>()
            for (perm in allPermission) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm)
                }
            }
            if (!permissionsList.isEmpty()) {
                requestPermissions(permissionsList.toTypedArray(), Constants.INIT_ZERO)
            }
        }
    }

    private fun createLensEngine() {
        val context = this.applicationContext
        // Create LensEngine.
        mLensEngine = LensEngine.Creator(context, mAnalyzer)
                .setLensType(mLensType)
                .applyDisplayDimension(DISPLAY_HEIGHT, DISPLAY_WIDTH)
                .applyFps(FLOAT_25F)
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

    // Permission application callback.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var hasAllGranted = true
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED) {
                createLensEngine()
            } else if (grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[Constants.INIT_ZERO])) {
                    showWaringDialog()
                } else {
                    Toast.makeText(this, R.string.toast, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(Constants.STR_LENS_TYPE, lensType)
        super.onSaveInstanceState(outState)
    }

    private inner class HandAnalyzerTransactor internal constructor(mainActivity: LiveHandKeyPointAnalyseActivity, ocrGraphicOverlay: GraphicOverlay?) : MLTransactor<MLHandKeypoints> {
        private val mGraphicOverlay: GraphicOverlay?
        var mMainActivityWeakReference: WeakReference<LiveHandKeyPointAnalyseActivity>

        /**
         * Process the results returned by the analyzer.
         *
         * @param result
         */
        override fun transactResult(result: MLAnalyzer.Result<MLHandKeypoints>) {
            mGraphicOverlay!!.clear()
            val handKeypointsSparseArray = result.analyseList
            val list: MutableList<MLHandKeypoints> = ArrayList()
            println(R.string.point_list_size.toString() + handKeypointsSparseArray.size())
            for (i in Constants.INIT_ZERO until handKeypointsSparseArray.size()) {
                list.add(handKeypointsSparseArray.valueAt(i))
                println(R.string.point_list_size_new.toString() + handKeypointsSparseArray.valueAt(i).handKeypoints)
            }
            val graphic = HandKeypointGraphic(mGraphicOverlay, list, result, this@LiveHandKeyPointAnalyseActivity)
            mGraphicOverlay.add(graphic)
        }

        override fun destroy() {
            mGraphicOverlay!!.clear()
        }

        init {
            mMainActivityWeakReference = WeakReference(mainActivity)
            mGraphicOverlay = ocrGraphicOverlay
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.handswitch -> switchCamera()
        }
    }

    private fun switchCamera() {
        isFront = !isFront
        mLensType = if (isFront) {
            LensEngine.FRONT_LENS
        } else {
            LensEngine.BACK_LENS
        }
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
        createLensEngine()
        startLensEngine()
    }

    private fun showWaringDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(R.string.Information_permission)
                .setPositiveButton(R.string.go_authorization) { dialog, which ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts(R.string.PACKAGE.toString(), applicationContext.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton(R.string.Cancel) { dialog, which -> finish() }.setOnCancelListener { }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createLensEngine()
            startLensEngine()
        } else {
            checkPermission()
        }
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
        if (mAnalyzer != null) {
            mAnalyzer!!.stop()
        }
    }

    companion object {
        private val TAG = LiveHandKeyPointAnalyseActivity::class.java.simpleName
        private const val CAMERA_PERMISSION_CODE = 0
        private val ALL_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        val allPermission: List<String>
            get() = Collections.unmodifiableList(Arrays.asList(*ALL_PERMISSION))
    }
}