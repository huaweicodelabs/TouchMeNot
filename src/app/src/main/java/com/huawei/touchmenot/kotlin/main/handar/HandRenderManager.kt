/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.touchmenot.kotlin.main.handar

import android.app.Activity
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.widget.TextView
import com.huawei.hiar.ARHand
import com.huawei.hiar.ARSession
import com.huawei.touchmenot.kotlin.main.HomeActivity
import com.huawei.touchmenot.kotlin.main.common.*
import com.huawei.touchmenot.kotlin.main.common.TextDisplay.OnTextInfoChangeListener
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * This class shows how to render data obtained from HUAWEI AR Engine.
 *
 * @author HW
 * @since 2020-03-21
 */
class HandRenderManager(private val mActivity: Activity) : GLSurfaceView.Renderer {
    var previousValue = 0f
    private var frames = 0
    private var lastInterval: Long = 0
    private var mSession: ARSession? = null
    private var fps = 0f
    private var mTextView: TextView? = null
    private val mTextureDisplay = TextureDisplay()
    private val mTextDisplay = TextDisplay()
    private val mHandRelatedDisplays = ArrayList<HandRelatedDisplay>()
    private var mDisplayRotationManager: DisplayRotationManager? = null
    private val FLOAT_1000F = 1000.0f
    private val TEXT_SIZE = 10f

    /**
     * Set the ARSession object, which is used to obtain the latest data in the onDrawFrame method.
     *
     * @param arSession ARSession.
     */
    fun setArSession(arSession: ARSession?) {
        if (arSession == null) {
            Log.d(TAG, Constants.ERR_SESSION)
            return
        }
        mSession = arSession
    }

    /**
     * Set the DisplayRotationManage object, which is used in onSurfaceChanged and onDrawFrame.
     *
     * @param displayRotationManager DisplayRotationManage.
     */
    fun setDisplayRotationManage(displayRotationManager: DisplayRotationManager?) {
        if (displayRotationManager == null) {
            Log.d(TAG, Constants.ERR_DISPLAY_ROTATION)
            return
        }
        mDisplayRotationManager = displayRotationManager
    }

    /**
     * Set the TextView object, which is called in the UI thread to display text.
     *
     * @param textView TextView.
     */
    fun setTextView(textView: TextView?) {
        if (textView == null) {
            Log.d(TAG, Constants.ERR_TEXT_VIEW)
            return
        }
        mTextView = textView
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        // Clear the original color and set a new color.
        GLES20.glClearColor(Constants.FLOAT_POINT_1F, Constants.FLOAT_POINT_1F, Constants.FLOAT_POINT_1F, Constants.FLOAT_1F)
        for (handRelatedDisplay in mHandRelatedDisplays) {
            handRelatedDisplay.init()
        }
        mTextureDisplay.init()
        mTextDisplay.setListener(object : OnTextInfoChangeListener {
            override fun textInfoChanged(text: String?, positionX: Float, positionY: Float) {
                showHandTypeTextView(text, positionX, positionY)
            }
        })
    }

    /**
     * Create a text display thread that is used for text update tasks.
     *
     * @param text      Gesture information displayed on the screen
     * @param positionX The left padding in pixels.
     * @param positionY The right padding in pixels.
     */
    private fun showHandTypeTextView(text: String?, positionX: Float, positionY: Float) {
        mActivity.runOnUiThread {
            mTextView!!.setTextColor(Color.WHITE)
            // Set the font size.
            mTextView!!.textSize = TEXT_SIZE
            if (text != null) {
                mTextView!!.text = text
                mTextView!!.setPadding(positionX.toInt(), positionY.toInt(), Constants.INIT_ZERO, Constants.INIT_ZERO)
            } else {
                mTextView!!.text = ""
            }
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        mTextureDisplay.onSurfaceChanged(width, height)
        GLES20.glViewport(Constants.INIT_ZERO, Constants.INIT_ZERO, width, height)
        mDisplayRotationManager!!.updateViewportRotation(width, height)
    }

    override fun onDrawFrame(unused: GL10) {
        // Clear the color buffer and notify the driver not to load the data of the previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (mSession == null) {
            return
        }
        if (mDisplayRotationManager!!.deviceRotation) {
            mDisplayRotationManager!!.updateArSessionDisplayGeometry(mSession!!)
        }
        try {
            mSession!!.setCameraTextureName(mTextureDisplay.externalTextureId)
            val arFrame = mSession!!.update()
            val arCamera = arFrame.camera

            // The size of the projection matrix is 4 * 4.
            val projectionMatrix = FloatArray(PROJECTION_MATRIX_MAX)

            // Obtain the projection matrix through ARCamera.
            arCamera.getProjectionMatrix(projectionMatrix, PROJECTION_MATRIX_OFFSET, PROJECTION_MATRIX_NEAR,
                    PROJECTION_MATRIX_FAR)
            mTextureDisplay.onDrawFrame(arFrame)
            val hands = mSession!!.getAllTrackables(ARHand::class.java)
            if (hands.size == Constants.INIT_ZERO) {
                mTextDisplay.onDrawFrame(null)
                return
            }
            for (hand in hands) {
                // Update the hand recognition information to be displayed on the screen.
                val sb = StringBuilder()
                updateMessageData(sb, hand)

                // Display hand recognition information on the screen.
                TEMP_MESSAGE = sb.toString()
                Log.d(Constants.STR_HAND_SIGNAL, TEMP_MESSAGE)
                mTextDisplay.onDrawFrame(sb)
            }
            for (handRelatedDisplay in mHandRelatedDisplays) {
                handRelatedDisplay.onDrawFrame(hands, projectionMatrix)
            }
        } catch (e: ArDemoRuntimeException) {
            Log.d(TAG, Constants.EXCP_ARDEMOE)
        } catch (t: Throwable) {
            // This prevents the app from crashing due to unhandled exceptions.
            Log.d(TAG, Constants.EXCP_OPEN_GL, t)
        }
    }

    /**
     * Update gesture-related information.
     *
     * @param sb   String buffer.
     * @param hand ARHand.
     */
    private fun updateMessageData(sb: StringBuilder, hand: ARHand) {
        val fpsResult = doFpsCalculate()
        sb.append(Constants.STR_FPS).append(fpsResult).append(System.lineSeparator())
        addHandNormalStringBuffer(sb, hand)
        addGestureActionStringBuffer(sb, hand)
        addGestureCenterStringBuffer(sb, hand)
        val gestureHandBoxPoints = hand.gestureHandBox
        sb.append(Constants.STR_HAND_BOX_LENGTH).append(gestureHandBoxPoints.size).append(Constants.STR_CLOSE_BRACKET)
                .append(System.lineSeparator())
        for (i in Constants.INIT_ZERO until gestureHandBoxPoints.size) {
            TEMP_MESSAGE = Constants.STR_GESTURE_POINTS + gestureHandBoxPoints[i]
            Log.d(TAG, TEMP_MESSAGE)
            sb.append(Constants.STR_GESTURE_POINTS_START).append(i).append(Constants.STR_RATIO).append(gestureHandBoxPoints[i]).append(Constants.STR_CLOSE_BRACKET)
                    .append(System.lineSeparator())
        }
        addHandSkeletonStringBuffer(sb, hand)
    }

    private fun addHandNormalStringBuffer(sb: StringBuilder, hand: ARHand) {
        if (hand.gestureType == Constants.INIT_ONE) {
            HomeActivity.instance?.scrollUpMethod()
            sb.append(Constants.STR_THUBS_UP).append(System.lineSeparator())
        } else if (hand.gestureType == Constants.INIT_ZERO) {
            HomeActivity.instance?.scrollDownMethod()
            sb.append(Constants.STR_THUMBS_DOWN).append(System.lineSeparator())
        }
        sb.append(Constants.STR_GESTURE_TYPE).append(hand.gestureType).append(System.lineSeparator())
        sb.append(Constants.STR_GESTURE_COORDINATE_SYSTEM).append(hand.gestureCoordinateSystem).append(System.lineSeparator())
        val gestureOrientation = hand.gestureOrientation
        sb.append(Constants.STR_GESTURE_ORIENTATION).append(gestureOrientation.size).append(Constants.STR_CLOSE_BRACKET)
                .append(System.lineSeparator())
        for (i in Constants.INIT_ZERO until gestureOrientation.size) {
            TEMP_MESSAGE = Constants.STR_GESTURE_ORIENTATION_ + gestureOrientation[i]
            Log.d(TAG, TEMP_MESSAGE)
            sb.append(Constants.STR_GESTURE_OPEN_BRACES).append(i).append(Constants.STR_RATIO).append(gestureOrientation[i])
                    .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator())
        }
        sb.append(System.lineSeparator())
    }

    private fun addGestureActionStringBuffer(sb: StringBuilder, hand: ARHand) {
        val gestureAction = hand.gestureAction
        sb.append(Constants.STR_GESTURE_LENGTH).append(gestureAction.size).append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator())
        for (i in Constants.INIT_ZERO until gestureAction.size) {
            TEMP_MESSAGE = Constants.STR_GESTURE_ACTION + gestureAction[i]
            Log.d(TAG, TEMP_MESSAGE)
            sb.append(Constants.STR_GESTURE_).append(i).append(Constants.STR_RATIO).append(gestureAction[i])
                    .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator())
        }
        sb.append(System.lineSeparator())
    }

    private fun addGestureCenterStringBuffer(sb: StringBuilder, hand: ARHand) {
        val gestureCenter = hand.gestureCenter
        sb.append(Constants.STR_GESTURE_CENTER).append(gestureCenter.size).append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator())
        for (i in Constants.INIT_ZERO until gestureCenter.size) {
            sb.append(Constants.STR_GESTURE_CENTER_).append(i).append(Constants.STR_RATIO).append(gestureCenter[i])
                    .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator())
        }
        sb.append(System.lineSeparator())
    }

    private fun addHandSkeletonStringBuffer(sb: StringBuilder, hand: ARHand) {
        sb.append(System.lineSeparator()).append(Constants.STR_HAND_TYPE).append(hand.handtype)
                .append(System.lineSeparator())
        sb.append(Constants.STR_SKELETON_COORDINATE).append(hand.skeletonCoordinateSystem)
        sb.append(System.lineSeparator())
        val skeletonArray = hand.handskeletonArray
        sb.append(Constants.STR_SKELETON_ARRAY).append(skeletonArray.size).append(Constants.STR_CLOSE_BRACKET)
                .append(System.lineSeparator())
        TEMP_MESSAGE = Constants.STR_SKELETON_ARRAY_LENGTH + skeletonArray.size
        Log.d(TAG, TEMP_MESSAGE)
        for (i in Constants.INIT_ZERO until skeletonArray.size) {
            TEMP_MESSAGE = Constants.STR_SKELETON + skeletonArray[i]
            Log.d(TAG, TEMP_MESSAGE)
        }
        sb.append(System.lineSeparator())
        val handSkeletonConnection = hand.handSkeletonConnection
        sb.append(Constants.STR_SKELETON_CONNECTION).append(handSkeletonConnection.size)
                .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator())
        Log.d(TAG, Constants.STR_SKELETON_CONNECTION_LENGTH + handSkeletonConnection.size)
        for (i in Constants.INIT_ZERO until handSkeletonConnection.size) {
            Log.d(TAG, Constants.STR_HAND_SKELETON_CONNECTION + handSkeletonConnection[i])
        }
        sb.append(System.lineSeparator()).append("-----------------------------------------------------")
    }

    private fun doFpsCalculate(): Float {
        ++frames
        val timeNow = System.currentTimeMillis()

        // Convert millisecond to second.
        if ((timeNow - lastInterval) / FLOAT_1000F > UPDATE_INTERVAL) {
            fps = frames / ((timeNow - lastInterval) / FLOAT_1000F)
            frames = Constants.INIT_ZERO
            lastInterval = timeNow
        }
        return fps
    }

    companion object {
        private val TAG = HandRenderManager::class.java.simpleName
        private const val PROJECTION_MATRIX_FAR = 100.0f
        private const val PROJECTION_MATRIX_MAX = 16
        private const val UPDATE_INTERVAL = 0.5f
        private const val PROJECTION_MATRIX_OFFSET = 0
        private const val PROJECTION_MATRIX_NEAR = 0.1f
        private var TEMP_MESSAGE = ""
    }

    /**
     * @param activity Activity
     */
    init {
        val handBoxDisplay: HandRelatedDisplay = HandBoxDisplay()
        val mHandSkeletonDisplay: HandRelatedDisplay = HandSkeletonDisplay()
        val mHandSkeletonLineDisplay: HandRelatedDisplay = HandSkeletonLineDisplay()
        mHandRelatedDisplays.add(handBoxDisplay)
        mHandRelatedDisplays.add(mHandSkeletonDisplay)
        mHandRelatedDisplays.add(mHandSkeletonLineDisplay)
    }
}