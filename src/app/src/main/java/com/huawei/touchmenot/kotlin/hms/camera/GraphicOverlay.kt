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
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.touchmenot.kotlin.main.common.Constants
import java.util.*

class GraphicOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val mLock = Any()
    private var mPreviewWidth = 0
    private var mWidthScaleFactor = 1.0f
    private var mPreviewHeight = 0
    private var mHeightScaleFactor = 1.0f
    private var mFacing = LensEngine.BACK_LENS
    private val mGraphics: MutableSet<Graphic> = HashSet()

    abstract class Graphic(private val mOverlay: GraphicOverlay?) {
        abstract fun draw(canvas: Canvas)
        fun scaleX(horizontal: Float): Float {
            return horizontal * mOverlay!!.mWidthScaleFactor
        }

        fun scaleY(vertical: Float): Float {
            return vertical * mOverlay!!.mHeightScaleFactor
        }

        fun translateX(x: Float): Float {
            return if (mOverlay!!.mFacing == LensEngine.FRONT_LENS) {
                mOverlay.width - scaleX(x)
            } else {
                scaleX(x)
            }
        }

        fun translateY(y: Float): Float {
            return scaleY(y)
        }

        fun postInvalidate() {
            mOverlay!!.postInvalidate()
        }

    }

    fun clear() {
        synchronized(mLock) { mGraphics.clear() }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(mLock) { mGraphics.add(graphic) }
        postInvalidate()
    }

    fun remove(graphic: Graphic?) {
        synchronized(mLock) { mGraphics.remove(graphic) }
        postInvalidate()
    }

    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: Int) {
        synchronized(mLock) {
            mPreviewWidth = previewWidth
            mPreviewHeight = previewHeight
            mFacing = facing
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(mLock) {
            if (mPreviewWidth != Constants.INIT_ZERO && mPreviewHeight != Constants.INIT_ZERO) {
                mWidthScaleFactor = canvas.width.toFloat() / mPreviewWidth.toFloat()
                mHeightScaleFactor = canvas.height.toFloat() / mPreviewHeight.toFloat()
            }
            for (graphic in mGraphics) {
                graphic.draw(canvas)
            }
        }
    }
}