/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.touchmenot.kotlin.main.handar

import android.opengl.GLES20
import android.util.Log
import com.huawei.hiar.ARHand
import com.huawei.hiar.ARTrackable
import com.huawei.touchmenot.kotlin.main.common.Constants
import com.huawei.touchmenot.kotlin.main.common.MatrixUtil.originalMatrix
import com.huawei.touchmenot.kotlin.main.common.ShaderUtil.checkGlError
import java.nio.FloatBuffer
import java.util.*

/**
 * This class shows how to use the hand bounding box. With this class,
 * a rectangular box bounding the hand can be displayed on the screen.
 *
 * @author HW
 * @since 2020-03-16
 */
internal class HandBoxDisplay : HandRelatedDisplay {
    private var mVbo = 0
    private var mVboSize = INITIAL_BUFFER_POINTS * BYTES_PER_POINT
    private var mProgram = 0
    private var mPosition = 0
    private var mColor = 0
    private var mModelViewProjectionMatrix = 0
    private var mPointSize = 0
    private var mNumPoints = 0
    private lateinit var mMVPMatrix: FloatArray
    private var TEMP_MESSAGE = ""
    private val FLOAT_50F = 50.0f
    private val FLOAT_18F = 18.0f
    private val FLOAT_0F = 0.0f
    private val FLOAT_1F = 1.0f

    /**
     * Create and build a shader for the hand gestures on the OpenGL thread,
     * which is called when [HandRenderManager.onSurfaceCreated].
     */
    override fun init() {
        checkGlError(TAG, Constants.STR_INIT_START)
        mMVPMatrix = originalMatrix
        val buffers = IntArray(Constants.INIT_ONE)
        GLES20.glGenBuffers(Constants.INIT_ONE, buffers, Constants.INIT_ZERO)
        mVbo = buffers[Constants.INIT_ZERO]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        createProgram()
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        checkGlError(TAG, Constants.STR_INIT_END)
    }

    private fun createProgram() {
        checkGlError(TAG, Constants.STR_PROGRAM_START)
        mProgram = HandShaderUtil.createGlProgram()
        mPosition = GLES20.glGetAttribLocation(mProgram, Constants.STR_IN_POSITION)
        mColor = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_COLOR)
        mPointSize = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_POINT_SIZE)
        mModelViewProjectionMatrix = GLES20.glGetUniformLocation(mProgram, Constants.STR_MVP_MATRIX)
        checkGlError(TAG, Constants.STR_PROGRAM_START)
    }

    /**
     * Render the hand bounding box and hand information.
     * This method is called when [HandRenderManager.onDrawFrame].
     *
     * @param hands Hand data.
     * @param projectionMatrix ARCamera projection matrix.
     */
    override fun onDrawFrame(hands: Collection<ARHand>, projectionMatrix: FloatArray?) {
        if (hands.size == Constants.INIT_ZERO) {
            return
        }
        if (projectionMatrix != null) {
            TEMP_MESSAGE = Constants.CAMERA_PROJECTION + Arrays.toString(projectionMatrix)
            Log.d(TAG, TEMP_MESSAGE)
        }
        for (hand in hands) {
            val gestureHandBoxPoints = hand.gestureHandBox
            if (hand.trackingState == ARTrackable.TrackingState.TRACKING) {
                updateHandBoxData(gestureHandBoxPoints)
                drawHandBox()
            }
        }
    }

    /**
     * Update the coordinates of the hand bounding box.
     *
     * @param gesturePoints Gesture hand box data.
     */
    private fun updateHandBoxData(gesturePoints: FloatArray) {
        checkGlError(TAG, Constants.STR_UPDATE_BOX)
        val glGesturePoints = floatArrayOf( // Get the four coordinates of a rectangular box bounding the hand.
                gesturePoints[0], gesturePoints[1], gesturePoints[2],
                gesturePoints[3], gesturePoints[1], gesturePoints[2],
                gesturePoints[3], gesturePoints[4], gesturePoints[5],
                gesturePoints[0], gesturePoints[4], gesturePoints[5])
        val gesturePointsNum = glGesturePoints.size / COORDINATE_DIMENSION
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        mNumPoints = gesturePointsNum
        if (mVboSize < mNumPoints * BYTES_PER_POINT) {
            while (mVboSize < mNumPoints * BYTES_PER_POINT) {
                // If the size of VBO is insufficient to accommodate the new point cloud, resize the VBO.
                mVboSize *= Constants.INIT_TWO
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW)
        }
        TEMP_MESSAGE = Constants.STR_GESTURE_HAND_POINT + mNumPoints
        Log.d(TAG, TEMP_MESSAGE)
        val mVertices = FloatBuffer.wrap(glGesturePoints)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO, mNumPoints * BYTES_PER_POINT,
                mVertices)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        checkGlError(TAG, Constants.STR_UPDATE_BOX)
    }

    /**
     * Render the hand bounding box.
     */
    private fun drawHandBox() {
        checkGlError(TAG, Constants.STR_HAND_BOX_START)
        GLES20.glUseProgram(mProgram)
        GLES20.glEnableVertexAttribArray(mPosition)
        GLES20.glEnableVertexAttribArray(mColor)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        GLES20.glVertexAttribPointer(
                mPosition, COORDINATE_DIMENSION, GLES20.GL_FLOAT, false, BYTES_PER_POINT, Constants.INIT_ZERO)
        GLES20.glUniform4f(mColor, FLOAT_1F, FLOAT_0F, FLOAT_0F, FLOAT_1F)
        GLES20.glUniformMatrix4fv(mModelViewProjectionMatrix, Constants.INIT_ONE, false, mMVPMatrix, Constants.INIT_ZERO)

        // Set the size of the rendering vertex.
        GLES20.glUniform1f(mPointSize, FLOAT_50F)

        // Set the width of a rendering stroke.
        GLES20.glLineWidth(FLOAT_18F)
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, Constants.INIT_ZERO, mNumPoints)
        GLES20.glDisableVertexAttribArray(mPosition)
        GLES20.glDisableVertexAttribArray(mColor)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        checkGlError(TAG, Constants.STR_HAND_BOX_END)
    }

    companion object {
        private val TAG = HandBoxDisplay::class.java.simpleName

        // Number of bytes occupied by each 3D coordinate. Float data occupies 4 bytes.
        // Each skeleton point represents a 3D coordinate.
        private const val BYTES_PER_POINT = 4 * 3
        private const val INITIAL_BUFFER_POINTS = 150
        private const val COORDINATE_DIMENSION = 3
    }
}