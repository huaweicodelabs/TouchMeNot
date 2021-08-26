/**
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
package com.huawei.touchmenot.java.main.handar;

import android.opengl.GLES20;
import android.util.Log;

import com.huawei.hiar.ARHand;
import com.huawei.touchmenot.java.main.common.Constants;
import com.huawei.touchmenot.java.main.common.ShaderUtil;

import java.nio.FloatBuffer;
import java.util.Collection;

/**
 * Draw hand skeleton points based on the coordinates of the hand skeleton points.
 *
 * @author HW
 * @since 2020-03-16
 */
class HandSkeletonDisplay implements HandRelatedDisplay {
    private static final String TAG = HandSkeletonDisplay.class.getSimpleName();

    // Number of bytes occupied by each 3D coordinate.Float data occupies 4 bytes.
    // Each skeleton point represents a 3D coordinate
    private static final int BYTES_PER_POINT = 4 * 3;

    private static final int INITIAL_POINTS_SIZE = 150;

    private int mVbo;

    private int mVboSize;

    private int mProgram;

    private int mPosition;

    private int mModelViewProjectionMatrix;

    private int mColor;

    private int mPointSize;

    private int mNumPoints = 0;

    private float FLOAT_OF = 0.0f, FLOAT_1F = 1.0f, FLOAT_30F = 30.0f;

    /**
     * Create and build a shader for the hand skeleton points on the OpenGL thread,
     * which is called when {@link HandRenderManager#onSurfaceCreated}.
     */
    @Override
    public void init() {
        ShaderUtil.checkGlError(TAG, Constants.STR_INIT_START);
        int[] buffers = new int[Constants.INIT_ONE];
        GLES20.glGenBuffers(Constants.INIT_ONE, buffers, Constants.INIT_ZERO);
        mVbo = buffers[Constants.INIT_ZERO];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo);
        mVboSize = INITIAL_POINTS_SIZE * BYTES_PER_POINT;
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO);
        createProgram();
        ShaderUtil.checkGlError(TAG, Constants.STR_INIT_END);
    }

    private void createProgram() {
        ShaderUtil.checkGlError(TAG, Constants.STR_PROGRAM_START);
        mProgram = HandShaderUtil.createGlProgram();
        ShaderUtil.checkGlError(TAG, Constants.STR_PROGRAM);
        mPosition = GLES20.glGetAttribLocation(mProgram, Constants.STR_IN_POSITION);
        mColor = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_COLOR);
        mPointSize = GLES20.glGetUniformLocation(mProgram, Constants.STR_IN_POINT_SIZE);
        mModelViewProjectionMatrix = GLES20.glGetUniformLocation(mProgram, Constants.STR_MVP_MATRIX);
        ShaderUtil.checkGlError(TAG, Constants.STR_PROGRAM_END);
    }

    /**
     * Draw hand skeleton points. This method is called when {@link HandRenderManager#onDrawFrame}.
     *
     * @param hands ARHand data collection.
     * @param projectionMatrix Projection matrix(4 * 4).
     */
    @Override
    public void onDrawFrame(Collection<ARHand> hands, float[] projectionMatrix) {
        // Verify external input. If the hand data is empty, the projection matrix is empty,
        // or the projection matrix is not 4 x 4, rendering is not performed.
        if (hands.isEmpty() || projectionMatrix == null || projectionMatrix.length != Constants.MAX_PROJECTION_MATRIX) {
            Log.d(TAG, Constants.STR_ILLEGAL_EXTERNAL_INPUT);
            return;
        }
        for (ARHand hand : hands) {
            float[] handSkeletons = hand.getHandskeletonArray();
            if (handSkeletons.length == Constants.INIT_ZERO) {
                continue;
            }
            updateHandSkeletonsData(handSkeletons);
            drawHandSkeletons(projectionMatrix);
        }
    }

    /**
     * Update the coordinates of hand skeleton points.
     * @param handSkeletons
     */
    private void updateHandSkeletonsData(float[] handSkeletons) {
        ShaderUtil.checkGlError(TAG, Constants.STR_UPDATE_HAND_SKELETONS_START);
        // Each point has a 3D coordinate. The total number of coordinates
        // is three times the number of skeleton points.
        int mPointsNum = handSkeletons.length / Constants.INIT_THREE;

        Log.d(TAG, Constants.STR_ARHAND_SKELETON+ mPointsNum);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo);
        mNumPoints = mPointsNum;
        if (mVboSize < mNumPoints * BYTES_PER_POINT) {
            while (mVboSize < mNumPoints * BYTES_PER_POINT) {
                // If the size of VBO is insufficient to accommodate the new point cloud, resize the VBO.
                mVboSize *= Constants.INIT_TWO;
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW);
        }
        FloatBuffer mSkeletonPoints = FloatBuffer.wrap(handSkeletons);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO, mNumPoints * BYTES_PER_POINT,
            mSkeletonPoints);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO);

        ShaderUtil.checkGlError(TAG, Constants.STR_UPDATE_SKELETON_END);
    }

    /**
     * Draw hand skeleton points.
     *
     * @param projectionMatrix Projection matrix.
     */
    private void drawHandSkeletons(float[] projectionMatrix) {
        ShaderUtil.checkGlError(TAG, Constants.STR_SKELETON_START);
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo);

        // The size of the vertex attribute is 4, and each vertex has four coordinate components
        GLES20.glVertexAttribPointer(
            mPosition, Constants.INIT_FOUR, GLES20.GL_FLOAT, false, BYTES_PER_POINT, Constants.INIT_ZERO);

        // Set the color of the skeleton points to blue.
        GLES20.glUniform4f(mColor, FLOAT_OF, FLOAT_OF, FLOAT_1F, FLOAT_1F);
        GLES20.glUniformMatrix4fv(mModelViewProjectionMatrix, Constants.INIT_ONE, false, projectionMatrix, Constants.INIT_ZERO);

        // Set the size of the skeleton points.
        GLES20.glUniform1f(mPointSize,FLOAT_30F );

        GLES20.glDrawArrays(GLES20.GL_POINTS, Constants.INIT_ZERO, mNumPoints);
        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Constants.INIT_ZERO);

        ShaderUtil.checkGlError(TAG, Constants.STR_SKELETON_END);
    }
}