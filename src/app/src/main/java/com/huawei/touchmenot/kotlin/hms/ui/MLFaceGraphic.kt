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
package com.huawei.touchmenot.kotlin.hms.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceShape
import com.huawei.touchmenot.kotlin.hms.camera.GraphicOverlay
import com.huawei.touchmenot.kotlin.hms.camera.GraphicOverlay.Graphic
import com.huawei.touchmenot.R
import com.huawei.touchmenot.kotlin.main.common.Constants
import java.text.DecimalFormat
import java.util.*

class MLFaceGraphic(private val overlay: GraphicOverlay, private val mFace: MLFace?) : Graphic(overlay) {
    private val facePositionPaint: Paint
    private val landmarkPaint: Paint
    private val boxPaint: Paint
    private val facePaint: Paint
    private val eyePaint: Paint
    private val eyebrowPaint: Paint
    private val lipPaint: Paint
    private val nosePaint: Paint
    private val noseBasePaint: Paint
    private val textPaint: Paint
    private val probilityPaint: Paint
    private val TEXT_SIZE_24 = 24
    private val TEXT_SIZE_35 = 35
    private val start = 350f
    private val width = 500f
    private val VALUE_POINT_5F = 0.5f

    fun sortHashMap(map: HashMap<String, Float>): List<String> {
        val entey: Set<Map.Entry<String, Float>> = map.entries
        val list: List<Map.Entry<String, Float>> = ArrayList(entey)
        Collections.sort(list) { o1, o2 ->
            if (o2.value - o1.value >= Constants.INIT_ZERO) {
                1
            } else {
                -1
            }
        }
        val emotions: MutableList<String> = ArrayList()
        for (i in Constants.INIT_ZERO until Constants.INIT_TWO) {
            emotions.add(list[i].key)
        }
        return emotions
    }

    override fun draw(canvas: Canvas) {
        if (mFace == null) {
            return
        }
        var x = start
        var y = overlay.height - Constants.RADIUS_300F
        val emotions = HashMap<String, Float>()
        emotions[Constants.STR_SMILING] = mFace.emotions.smilingProbability
        emotions[Constants.STR_NEUTRAL] = mFace.emotions.neutralProbability
        emotions[Constants.STR_ANGRY] = mFace.emotions.angryProbability
        emotions[Constants.STR_FEAR] = mFace.emotions.fearProbability
        emotions[Constants.STR_SAD] = mFace.emotions.sadProbability
        emotions[Constants.STR_DISGUST] = mFace.emotions.disgustProbability
        emotions[Constants.STR_SURPRISE] = mFace.emotions.surpriseProbability
        val result = sortHashMap(emotions)
        val decimalFormat = DecimalFormat(R.string.DECIMAL_PATTERN.toString())
        // Draw the facial feature value.
        canvas.drawText(R.string.Left_Eye.toString() + decimalFormat.format(mFace.features.leftEyeOpenProbability.toDouble()), x, y, probilityPaint)
        x = x + width
        canvas.drawText(R.string.Right_Eye.toString() + decimalFormat.format(mFace.features.rightEyeOpenProbability.toDouble()), x, y, probilityPaint)
        y = y - Constants.RADIUS_40F
        x = start
        canvas.drawText(R.string.Moutstache_Probability.toString() + decimalFormat.format(mFace.features.moustacheProbability.toDouble()), x, y, probilityPaint)
        x = x + width
        canvas.drawText(R.string.GLASS_PROBABILITY.toString() + decimalFormat.format(mFace.features.sunGlassProbability.toDouble()), x, y, probilityPaint)
        y = y - Constants.RADIUS_40F
        x = start
        canvas.drawText(R.string.HAT.toString() + decimalFormat.format(mFace.features.hatProbability.toDouble()), x, y, probilityPaint)
        x = x + width
        canvas.drawText(R.string.AGE.toString() + mFace.features.age, x, y, probilityPaint)
        y = y - Constants.RADIUS_40F
        x = start
        val sex = if (mFace.features.sexProbability > VALUE_POINT_5F) Constants.STR_GENDER_FEMALE else Constants.STR_GENDER_MALE
        canvas.drawText(R.string.GENDER.toString() + sex, x, y, probilityPaint)
        x = x + width
        canvas.drawText(R.string.EULER_ANGLE_Y.toString() + decimalFormat.format(mFace.rotationAngleY.toDouble()), x, y, probilityPaint)
        y = y - Constants.RADIUS_40F
        x = start
        canvas.drawText(R.string.EULER_ANGEL_Z.toString() + decimalFormat.format(mFace.rotationAngleZ.toDouble()), x, y, probilityPaint)
        x = x + width
        canvas.drawText(R.string.EULER_ANGLE_X.toString() + decimalFormat.format(mFace.rotationAngleX.toDouble()), x, y, probilityPaint)
        y = y - Constants.RADIUS_40F
        x = start
        canvas.drawText(result[Constants.INIT_ZERO], x, y, probilityPaint)

        // Draw a face contour.
        if (mFace.faceShapeList != null) {
            for (faceShape in mFace.faceShapeList) {
                if (faceShape == null) {
                    continue
                }
                val points = faceShape.points
                for (i in Constants.INIT_ZERO until points.size) {
                    val point = points[i]
                    canvas.drawPoint(translateX(point!!.x.toFloat()), translateY(point.y.toFloat()), boxPaint)
                    if (i != points.size - Constants.INIT_ONE) {
                        val next = points[i + Constants.INIT_ONE]
                        if (point != null && point.x != null && point.y != null) {
                            if (i % Constants.INIT_THREE == Constants.INIT_ZERO) {
                                canvas.drawText((i + Constants.INIT_ONE).toString(), translateX(point.x.toFloat()), translateY(point.y.toFloat()), textPaint)
                            }
                            canvas.drawLines(floatArrayOf(translateX(point.x.toFloat()), translateY(point.y.toFloat()),
                                    translateX(next.x.toFloat()), translateY(next.y.toFloat())), getPaint(faceShape))
                        }
                    }
                }
            }
        }
        // Face Key Points
        for (keyPoint in mFace.faceKeyPoints) {
            if (keyPoint != null) {
                val point = keyPoint.point
                canvas.drawCircle(
                        translateX(point.x),
                        translateY(point.y),
                        Constants.RADIUS_10F, landmarkPaint)
            }
        }
    }

    private fun getPaint(faceShape: MLFaceShape): Paint {
        return when (faceShape.faceShapeType) {
            MLFaceShape.TYPE_LEFT_EYE, MLFaceShape.TYPE_RIGHT_EYE -> eyePaint
            MLFaceShape.TYPE_BOTTOM_OF_LEFT_EYEBROW, MLFaceShape.TYPE_BOTTOM_OF_RIGHT_EYEBROW, MLFaceShape.TYPE_TOP_OF_LEFT_EYEBROW, MLFaceShape.TYPE_TOP_OF_RIGHT_EYEBROW -> eyebrowPaint
            MLFaceShape.TYPE_BOTTOM_OF_LOWER_LIP, MLFaceShape.TYPE_TOP_OF_LOWER_LIP, MLFaceShape.TYPE_BOTTOM_OF_UPPER_LIP, MLFaceShape.TYPE_TOP_OF_UPPER_LIP -> lipPaint
            MLFaceShape.TYPE_BOTTOM_OF_NOSE -> noseBasePaint
            MLFaceShape.TYPE_BRIDGE_OF_NOSE -> nosePaint
            else -> facePaint
        }
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 8.0f
        private const val LINE_WIDTH = 5.0f
        private const val getColour_1 = "#ffcc66"
        private const val getColour_2 = "#00ccff"
        private const val getColour_3 = "#006666"
        private const val getColor_4 = "#ffff00"
        private const val getColor_5 = "#ff6699"
        private const val getColor_6 = "#990000"
    }

    init {
        val selectedColor = Color.WHITE
        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor
        textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = TEXT_SIZE_24.toFloat()
        textPaint.typeface = Typeface.DEFAULT
        probilityPaint = Paint()
        probilityPaint.color = Color.WHITE
        probilityPaint.textSize = TEXT_SIZE_35.toFloat()
        probilityPaint.typeface = Typeface.DEFAULT
        landmarkPaint = Paint()
        landmarkPaint.color = Color.RED
        landmarkPaint.style = Paint.Style.FILL
        landmarkPaint.strokeWidth = Constants.RADIUS_10F
        boxPaint = Paint()
        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
        facePaint = Paint()
        facePaint.color = Color.parseColor(getColour_1)
        facePaint.style = Paint.Style.STROKE
        facePaint.strokeWidth = LINE_WIDTH
        eyePaint = Paint()
        eyePaint.color = Color.parseColor(getColour_2)
        eyePaint.style = Paint.Style.STROKE
        eyePaint.strokeWidth = LINE_WIDTH
        eyebrowPaint = Paint()
        eyebrowPaint.color = Color.parseColor(getColour_3)
        eyebrowPaint.style = Paint.Style.STROKE
        eyebrowPaint.strokeWidth = LINE_WIDTH
        nosePaint = Paint()
        nosePaint.color = Color.parseColor(getColor_4)
        nosePaint.style = Paint.Style.STROKE
        nosePaint.strokeWidth = LINE_WIDTH
        noseBasePaint = Paint()
        noseBasePaint.color = Color.parseColor(getColor_5)
        noseBasePaint.style = Paint.Style.STROKE
        noseBasePaint.strokeWidth = LINE_WIDTH
        lipPaint = Paint()
        lipPaint.color = Color.parseColor(getColor_6)
        lipPaint.style = Paint.Style.STROKE
        lipPaint.strokeWidth = LINE_WIDTH
    }
}