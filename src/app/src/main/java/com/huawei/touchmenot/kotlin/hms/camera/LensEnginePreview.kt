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
package com.huawei.touchmenot.kotlin.hms.camera

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.touchmenot.kotlin.main.common.Constants
import java.io.IOException

class LensEnginePreview(private val mContext: Context, attrs: AttributeSet?) : ViewGroup(mContext, attrs) {
    private val mSurfaceView: SurfaceView
    private var mStartRequested = false
    private var mSurfaceAvailable = false
    private var mLensEngine: LensEngine? = null
    private var mOverlay: GraphicOverlay? = null
    var previewWidth = 480
    var previewHeight = 360
    var INIT_MINUS_ONE = -1

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?) {
        if (lensEngine == null) {
            stop()
        }
        mLensEngine = lensEngine
        if (mLensEngine != null) {
            mStartRequested = true
            startIfReady()
        }
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?, overlay: GraphicOverlay?) {
        mOverlay = overlay
        this.start(lensEngine)
    }

    fun stop() {
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
    }

    fun release() {
        if (mLensEngine != null) {
            mLensEngine!!.release()
            mLensEngine = null
        }
    }

    @Throws(IOException::class)
    private fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            mLensEngine!!.run(mSurfaceView.holder)
            if (mOverlay != null) {
                val size = mLensEngine!!.displayDimension
                val min = Math.min(size.width, size.height)
                val max = Math.max(size.width, size.height)
                if (isPortraitMode) {
                    // Swap width and height sizes when in portrait, since it will be rotated by 90 degrees.
                    mOverlay!!.setCameraInfo(min, max, mLensEngine!!.lensType)
                } else {
                    mOverlay!!.setCameraInfo(max, min, mLensEngine!!.lensType)
                }
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.d(TAG, Constants.CAMERE_NOT_SOURCE, e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (mLensEngine != null) {
            val size = mLensEngine!!.displayDimension
            if (size != null) {
                previewWidth = size.width
                previewHeight = size.height
            }
        }
        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode) {
            val tmp = previewWidth
            previewWidth = previewHeight
            previewHeight = tmp
        }
        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = Constants.INIT_ZERO
        var childYOffset = Constants.INIT_ZERO
        val widthRatio = viewWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = viewHeight.toFloat() / previewHeight.toFloat()
        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions. We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = viewWidth
            childHeight = (previewHeight.toFloat() * widthRatio).toInt()
            childYOffset = (childHeight - viewHeight) / Constants.INIT_TWO
        } else {
            childWidth = (previewWidth.toFloat() * heightRatio).toInt()
            childHeight = viewHeight
            childXOffset = (childWidth - viewWidth) / Constants.INIT_TWO
        }
        for (i in Constants.INIT_ZERO until this.childCount) {
            // One dimension will be cropped. We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i).layout(INIT_MINUS_ONE * childXOffset, INIT_MINUS_ONE * childYOffset, childWidth - childXOffset,
                    childHeight - childYOffset)
        }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.d(TAG, Constants.CAMERE_NOT_SOURCE, e)
        }
    }

    private val isPortraitMode: Boolean
        private get() {
            val orientation = mContext.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }
            Log.d(TAG, Constants.STR_IS_POTRAIT_MODE)
            return false
        }

    companion object {
        private val TAG = LensEnginePreview::class.java.simpleName
    }

    init {
        mSurfaceView = SurfaceView(mContext)
        mSurfaceView.holder.addCallback(SurfaceCallback())
        this.addView(mSurfaceView)
    }
}