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
 * Draw hand skeleton connection line based on the coordinates of the hand skeleton points..
 *
 * @author HW
 * @since 2020-03-09
 */
internal class HandSkeletonLineDisplay : HandRelatedDisplay {
    private var mVbo = 0
    private var mVboSize = INITIAL_BUFFER_POINTS * BYTES_PER_POINT
    private var mProgram = 0
    private var mPosition = 0
    private var mModelViewProjectionMatrix = 0
    private var mColor = 0
    private var mPointSize = 0
    private var mPointsNum = 0
    private var TEMP_MSG = ""

    /**
     * Create and build a shader for the hand skeleton line on the OpenGL thread,
     * which is called when [HandRenderManager.onSurfaceCreated].
     */
    override fun init() {
        checkGlError(TAG, Constants.STR_INIT_START)
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
        checkGlError(TAG, Constants.STR_PROGRAM)
        mPosition = GLES20.glGetAttribLocation(mProgram, Constants.STR_IN_POSITION)
        mColor = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_COLOR)
        mPointSize = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_POINT_SIZE)
        mModelViewProjectionMatrix = GLES20.glGetUniformLocation(mProgram, Constants.STR_MVP_MATRIX)
        checkGlError(TAG, Constants.STR_PROGRAM_END)
    }

    /**
     * Draw hand skeleton connection line.
     * This method is called when [HandRenderManager.onDrawFrame].
     *
     * @param hands ARHand data collection.
     * @param projectionMatrix ProjectionMatrix(4 * 4).
     */
    override fun onDrawFrame(hands: Collection<ARHand>, projectionMatrix: FloatArray?) {
        // Verify external input. If the hand data is empty, the projection matrix is empty,
        // or the projection matrix is not 4 * 4, rendering is not performed.
        if (hands.isEmpty() || projectionMatrix == null || projectionMatrix.size != Constants.MAX_PROJECTION_MATRIX) {
            Log.d(TAG, Constants.STR_ILLEGAL_EXTERNAL_INPUT)
            return
        }
        for (hand in hands) {
            val handSkeletons = hand.handskeletonArray
            val handSkeletonConnections = hand.handSkeletonConnection
            if (handSkeletons.size == Constants.INIT_ZERO || handSkeletonConnections.size == Constants.INIT_ZERO) {
                continue
            }
            updateHandSkeletonLinesData(handSkeletons, handSkeletonConnections)
            drawHandSkeletonLine(projectionMatrix)
        }
    }

    /**
     * This method updates the connection data of skeleton points and is called when any frame is updated.
     *
     * @param handSkeletons Bone point data of hand.
     * @param handSkeletonConnection Data of connection between bone points of hand.
     */
    private fun updateHandSkeletonLinesData(handSkeletons: FloatArray, handSkeletonConnection: IntArray) {
        checkGlError(TAG, Constants.STR_UPDATE_SKELETON)
        var pointsLineNum = Constants.INIT_ZERO

        // Each point is a set of 3D coordinate. Each connection line consists of two points.
        val linePoint = FloatArray(handSkeletonConnection.size * Constants.INIT_THREE * Constants.INIT_TWO)

        // The format of HandSkeletonConnection data is [p0,p1;p0,p3;p0,p5;p1,p2].
        // handSkeletonConnection saves the node indexes. Two indexes obtain a set
        // of connection point data. Therefore, j = j + 2. This loop obtains related
        // coordinates and saves them in linePoint.
        var j = Constants.INIT_ZERO
        while (j < handSkeletonConnection.size) {
            linePoint[pointsLineNum * Constants.INIT_THREE] = handSkeletons[Constants.INIT_THREE * handSkeletonConnection[j]]
            linePoint[pointsLineNum * Constants.INIT_THREE + Constants.INIT_ONE] = handSkeletons[Constants.INIT_THREE * handSkeletonConnection[j] + Constants.INIT_ONE]
            linePoint[pointsLineNum * Constants.INIT_THREE + Constants.INIT_TWO] = handSkeletons[Constants.INIT_THREE * handSkeletonConnection[j] + Constants.INIT_TWO]
            linePoint[pointsLineNum * Constants.INIT_THREE + Constants.INIT_THREE] = handSkeletons[Constants.INIT_THREE * handSkeletonConnection[j + Constants.INIT_ONE]]
            linePoint[pointsLineNum * Constants.INIT_THREE + Constants.INIT_FOUR] = handSkeletons[Constants.INIT_THREE * handSkeletonConnection[j + Constants.INIT_ONE] + Constants.INIT_ONE]
            linePoint[pointsLineNum * Constants.INIT_THREE + Constants.INIT_FIVE] = handSkeletons[Constants.INIT_THREE * handSkeletonConnection[j + Constants.INIT_ONE] + Constants.INIT_TWO]
            pointsLineNum += Constants.INIT_TWO
            j += Constants.INIT_TWO
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        mPointsNum = pointsLineNum

        // If the storage space is insufficient, apply for twice the memory each time.
        if (mVboSize < mPointsNum * BYTES_PER_POINT) {
            while (mVboSize < mPointsNum * BYTES_PER_POINT) {
                mVboSize *= Constants.INIT_TWO
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW)
        }
        val linePoints = FloatBuffer.wrap(linePoint)
        TEMP_MSG = Constants.STR_LINE_POINTS + mPointsNum
        Log.d(TAG, TEMP_MSG)
        TEMP_MSG = Constants.STR_LINEPOINTS + linePoints.toString()
        Log.d(TAG, TEMP_MSG)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO, mPointsNum * BYTES_PER_POINT,
                linePoints)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        checkGlError(TAG, Constants.STR_UPDATE_LINES_DATA)
    }

    /**
     * Draw hand skeleton connection line.
     *
     * @param projectionMatrix Projection matrix(4 * 4).
     */
    private fun drawHandSkeletonLine(projectionMatrix: FloatArray) {
        checkGlError(TAG, Constants.STR_SKELETON_LINE_START)
        GLES20.glUseProgram(mProgram)
        GLES20.glEnableVertexAttribArray(mPosition)
        GLES20.glEnableVertexAttribArray(mColor)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)

        // Set the width of the drawn line
        GLES20.glLineWidth(Constants.FLOAT_18F)

        // Represented each point by 4D coordinates in the shader.
        GLES20.glVertexAttribPointer(
                mPosition, Constants.INIT_FOUR, GLES20.GL_FLOAT, false, BYTES_PER_POINT, Constants.INIT_ZERO)
        GLES20.glUniform4f(mColor, Constants.FLOAT_OF, Constants.FLOAT_OF, Constants.FLOAT_OF, Constants.FLOAT_1F)
        GLES20.glUniformMatrix4fv(mModelViewProjectionMatrix, Constants.INIT_ONE, false, projectionMatrix, Constants.INIT_ZERO)
        GLES20.glUniform1f(mPointSize, JOINT_POINT_SIZE)
        GLES20.glDrawArrays(GLES20.GL_LINES, Constants.INIT_ZERO, mPointsNum)
        GLES20.glDisableVertexAttribArray(mPosition)
        GLES20.glDisableVertexAttribArray(mColor)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO)
        checkGlError(TAG, Constants.STR_SKELETON_LINE_END)
    }

    companion object {
        private val TAG = HandSkeletonLineDisplay::class.java.simpleName

        // Number of bytes occupied by each 3D coordinate.
        // Float data occupies 4 bytes. Each skeleton point represents a 3D coordinate
        private const val BYTES_PER_POINT = 4 * 3
        private const val INITIAL_BUFFER_POINTS = 150
        private const val JOINT_POINT_SIZE = 100f
    }
}