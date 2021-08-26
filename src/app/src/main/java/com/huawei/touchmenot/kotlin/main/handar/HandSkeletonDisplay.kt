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
import com.huawei.touchmenot.kotlin.main.common.Constants
import com.huawei.touchmenot.kotlin.main.common.ShaderUtil.checkGlError
import java.nio.FloatBuffer

/**
 * Draw hand skeleton points based on the coordinates of the hand skeleton points.
 *
 * @author HW
 * @since 2020-03-16
 */
internal class HandSkeletonDisplay : HandRelatedDisplay {
    private var mVbo = 0
    private var mVboSize = 0
    private var mProgram = 0
    private var mPosition = 0
    private var mModelViewProjectionMatrix = 0
    private var mColor = 0
    private var mPointSize = 0
    private var mNumPoints = 0
    private val FLOAT_OF = 0.0f
    private val FLOAT_1F = 1.0f
    private val FLOAT_30F = 30.0f

    /**
     * Create and build a shader for the hand skeleton points on the OpenGL thread,
     * which is called when [HandRenderManager.onSurfaceCreated].
     */
    override fun init() {
        checkGlError(TAG, Constants.STR_INIT_START)
        val buffers = IntArray(Constants.INIT_ONE)
        GLES20.glGenBuffers(Constants.INIT_ONE, buffers, Constants.INIT_ZERO)
        mVbo = buffers[Constants.INIT_ZERO]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        mVboSize = INITIAL_POINTS_SIZE * BYTES_PER_POINT
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        createProgram()
        checkGlError(TAG, Constants.STR_INIT_END)
    }

    private fun createProgram() {
        checkGlError(TAG, Constants.STR_PROGRAM_START)
        mProgram = HandShaderUtil.createGlProgram()
        checkGlError(TAG, Constants.STR_PROGRAM)
        mPosition = GLES20.glGetAttribLocation(mProgram, Constants.STR_IN_POSITION)
        mColor = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_COLOR)
        mPointSize = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_POINT_SIZE)
        mModelViewProjectionMatrix = GLES20.glGetUniformLocation(mProgram, Constants.STR_MVP_MATRIX)
        checkGlError(TAG, Constants.STR_PROGRAM_END)
    }

    /**
     * Draw hand skeleton points. This method is called when [HandRenderManager.onDrawFrame].
     *
     * @param hands ARHand data collection.
     * @param projectionMatrix Projection matrix(4 * 4).
     */
    override fun onDrawFrame(hands: Collection<ARHand>, projectionMatrix: FloatArray?) {
        // Verify external input. If the hand data is empty, the projection matrix is empty,
        // or the projection matrix is not 4 x 4, rendering is not performed.
        if (hands.isEmpty() || projectionMatrix == null || projectionMatrix.size != Constants.MAX_PROJECTION_MATRIX) {
            Log.d(TAG, Constants.STR_ILLEGAL_EXTERNAL_INPUT)
            return
        }
        for (hand in hands) {
            val handSkeletons = hand.handskeletonArray
            if (handSkeletons.size == Constants.INIT_ZERO) {
                continue
            }
            updateHandSkeletonsData(handSkeletons)
            drawHandSkeletons(projectionMatrix)
        }
    }

    /**
     * Update the coordinates of hand skeleton points.
     * @param handSkeletons
     */
    private fun updateHandSkeletonsData(handSkeletons: FloatArray) {
        checkGlError(TAG, Constants.STR_UPDATE_HAND_SKELETONS_START)
        // Each point has a 3D coordinate. The total number of coordinates
        // is three times the number of skeleton points.
        val mPointsNum = handSkeletons.size / Constants.INIT_THREE
        Log.d(TAG, Constants.STR_ARHAND_SKELETON + mPointsNum)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        mNumPoints = mPointsNum
        if (mVboSize < mNumPoints * BYTES_PER_POINT) {
            while (mVboSize < mNumPoints * BYTES_PER_POINT) {
                // If the size of VBO is insufficient to accommodate the new point cloud, resize the VBO.
                mVboSize *= Constants.INIT_TWO
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW)
        }
        val mSkeletonPoints = FloatBuffer.wrap(handSkeletons)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO, mNumPoints * BYTES_PER_POINT,
                mSkeletonPoints)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        checkGlError(TAG, Constants.STR_UPDATE_SKELETON_END)
    }

    /**
     * Draw hand skeleton points.
     *
     * @param projectionMatrix Projection matrix.
     */
    private fun drawHandSkeletons(projectionMatrix: FloatArray) {
        checkGlError(TAG, Constants.STR_SKELETON_START)
        GLES20.glUseProgram(mProgram)
        GLES20.glEnableVertexAttribArray(mPosition)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)

        // The size of the vertex attribute is 4, and each vertex has four coordinate components
        GLES20.glVertexAttribPointer(
                mPosition, Constants.INIT_FOUR, GLES20.GL_FLOAT, false, BYTES_PER_POINT, Constants.INIT_ZERO)

        // Set the color of the skeleton points to blue.
        GLES20.glUniform4f(mColor, FLOAT_OF, FLOAT_OF, FLOAT_1F, FLOAT_1F)
        GLES20.glUniformMatrix4fv(mModelViewProjectionMatrix, Constants.INIT_ONE, false, projectionMatrix, Constants.INIT_ZERO)

        // Set the size of the skeleton points.
        GLES20.glUniform1f(mPointSize, FLOAT_30F)
        GLES20.glDrawArrays(GLES20.GL_POINTS, Constants.INIT_ZERO, mNumPoints)
        GLES20.glDisableVertexAttribArray(mPosition)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        checkGlError(TAG, Constants.STR_SKELETON_END)
    }

    companion object {
        private val TAG = HandSkeletonDisplay::class.java.simpleName

        // Number of bytes occupied by each 3D coordinate.Float data occupies 4 bytes.
        // Each skeleton point represents a 3D coordinate
        private const val BYTES_PER_POINT = 4 * 3
        private const val INITIAL_POINTS_SIZE = 150
    }
}