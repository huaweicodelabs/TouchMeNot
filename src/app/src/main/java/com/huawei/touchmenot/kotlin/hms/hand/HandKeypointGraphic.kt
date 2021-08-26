/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.huawei.touchmenot.kotlin.hms.hand

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoints
import com.huawei.touchmenot.kotlin.hms.camera.GraphicOverlay
import com.huawei.touchmenot.kotlin.hms.camera.GraphicOverlay.Graphic
import com.huawei.touchmenot.kotlin.hms.hand.Hand.Companion.analyzeHandsAndGetNumber
import com.huawei.touchmenot.kotlin.main.common.Constants
import com.huawei.touchmenot.kotlin.main.model.RatingInterface

/**
 * Graphic instance for rendering hand position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class HandKeypointGraphic(overlay: GraphicOverlay?, handKeypoints: List<MLHandKeypoints>, result: MLAnalyzer.Result<MLHandKeypoints>, liveHandKeyPointAnalyseActivity: LiveHandKeyPointAnalyseActivity?) : Graphic(overlay) {
    private val rectPaint: Paint
    private val idPaintnew: Paint
    private val handKeypoints: List<MLHandKeypoints>
    private val mResult: MLAnalyzer.Result<MLHandKeypoints>
    var context: LiveHandKeyPointAnalyseActivity?
    var mInterface: RatingInterface? = null
    private val getColor_1 = "#6200EE"
    private val TEXT_SIZE_32 = 32
    private val FLOAT_2F = 2f
    private val FLOAT_100F = 100f
    fun setInterface(mInterface: RatingInterface?) {
        this.mInterface = mInterface
    }

    override fun draw(canvas: Canvas) {
        val data = analyzeHandsAndGetNumber(mResult)
        mInterface!!.ratingCaptured(data)
        callMe(data, canvas)
    }

    fun callMe(text: String, canvas: Canvas) {
        val centerX = canvas.width / FLOAT_2F
        val centerY = canvas.height / FLOAT_2F
        val paint: Paint
        val circlePaint: Paint
        paint = Paint()
        circlePaint = Paint()
        paint.color = Color.WHITE
        paint.textSize = FLOAT_100F
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        val bounds = Rect()
        paint.getTextBounds(text, Constants.INIT_ZERO, text.length, bounds)
        circlePaint.color = Color.parseColor(getColor_1)
        circlePaint.isAntiAlias = true
        canvas.drawCircle(centerX, centerY - bounds.height() / Constants.INIT_TWO, bounds.width() + Constants.INIT_FIVE.toFloat(), circlePaint)
        canvas.drawText(text, centerX, centerY, paint)
    }

    /*
     * @param rect
     * @return Rect
     */
    fun translateRect(rect: Rect): Rect {
        var left = translateX(rect.left.toFloat())
        var right = translateX(rect.right.toFloat())
        var bottom = translateY(rect.bottom.toFloat())
        var top = translateY(rect.top.toFloat())
        if (left > right) {
            val size = left
            left = right
            right = size
        }
        if (bottom < top) {
            val size = bottom
            bottom = top
            top = size
        }
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

    init {
        setInterface(liveHandKeyPointAnalyseActivity)
        this.handKeypoints = handKeypoints
        mResult = result
        val selectedColor = Color.WHITE
        context = liveHandKeyPointAnalyseActivity
        idPaintnew = Paint()
        idPaintnew.color = Color.RED
        idPaintnew.textSize = TEXT_SIZE_32.toFloat()
        rectPaint = Paint()
        rectPaint.color = selectedColor
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = BOX_STROKE_WIDTH
    }
}