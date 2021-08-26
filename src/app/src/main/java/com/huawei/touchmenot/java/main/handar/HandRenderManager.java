/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.touchmenot.java.main.handar;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.widget.TextView;

import com.huawei.hiar.ARCamera;
import com.huawei.hiar.ARFrame;
import com.huawei.hiar.ARHand;
import com.huawei.hiar.ARSession;
import com.huawei.touchmenot.java.main.HomeActivity;
import com.huawei.touchmenot.java.main.common.ArDemoRuntimeException;
import com.huawei.touchmenot.java.main.common.Constants;
import com.huawei.touchmenot.java.main.common.DisplayRotationManager;
import com.huawei.touchmenot.java.main.common.TextDisplay;
import com.huawei.touchmenot.java.main.common.TextureDisplay;

import java.util.ArrayList;
import java.util.Collection;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class shows how to render data obtained from HUAWEI AR Engine.
 *
 * @author HW
 * @since 2020-03-21
 */
public class HandRenderManager implements GLSurfaceView.Renderer
{
    private static final String TAG = HandRenderManager.class.getSimpleName();
    private static final float PROJECTION_MATRIX_FAR = 100.0f;
    private static final int PROJECTION_MATRIX_MAX =  16;

    private static final float UPDATE_INTERVAL = 0.5f;

    private static final int PROJECTION_MATRIX_OFFSET = 0;

    private static final float PROJECTION_MATRIX_NEAR = 0.1f;

    float previousValue = 0;

    private int frames = 0;

    private long lastInterval;

    private ARSession mSession;

    private float fps;

    private Activity mActivity;

    private TextView mTextView;

    private TextureDisplay mTextureDisplay = new TextureDisplay();

    private TextDisplay mTextDisplay = new TextDisplay();

    private ArrayList<HandRelatedDisplay> mHandRelatedDisplays = new ArrayList<>();

    private DisplayRotationManager mDisplayRotationManager;

    private static String TEMP_MESSAGE = "";
    private float FLOAT_1000F = 1000.0f, TEXT_SIZE = 10f;


    /**
     * @param activity Activity
     */
    public HandRenderManager(Activity activity) {
        mActivity = activity;
        HandRelatedDisplay handBoxDisplay = new HandBoxDisplay();
        HandRelatedDisplay mHandSkeletonDisplay = new HandSkeletonDisplay();
        HandRelatedDisplay mHandSkeletonLineDisplay = new HandSkeletonLineDisplay();
        mHandRelatedDisplays.add(handBoxDisplay);
        mHandRelatedDisplays.add(mHandSkeletonDisplay);
        mHandRelatedDisplays.add(mHandSkeletonLineDisplay);
    }

    /**
     * Set the ARSession object, which is used to obtain the latest data in the onDrawFrame method.
     *
     * @param arSession ARSession.
     */
    public void setArSession(ARSession arSession) {
        if (arSession == null) {
            Log.d(TAG, Constants.ERR_SESSION);
            return;
        }
        mSession = arSession;
    }

    /**
     * Set the DisplayRotationManage object, which is used in onSurfaceChanged and onDrawFrame.
     *
     * @param displayRotationManager DisplayRotationManage.
     */
    public void setDisplayRotationManage(DisplayRotationManager displayRotationManager) {
        if (displayRotationManager == null) {
            Log.d(TAG, Constants.ERR_DISPLAY_ROTATION);
            return;
        }
        mDisplayRotationManager = displayRotationManager;
    }

    /**
     * Set the TextView object, which is called in the UI thread to display text.
     *
     * @param textView TextView.
     */
    public void setTextView(TextView textView) {
        if (textView == null) {
            Log.d(TAG, Constants.ERR_TEXT_VIEW);
            return;
        }
        mTextView = textView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Clear the original color and set a new color.
        GLES20.glClearColor(Constants.FLOAT_POINT_1F, Constants.FLOAT_POINT_1F, Constants.FLOAT_POINT_1F, Constants.FLOAT_1F);
        for (HandRelatedDisplay handRelatedDisplay : mHandRelatedDisplays) {
            handRelatedDisplay.init();
        }
        mTextureDisplay.init();
        mTextDisplay.setListener(new TextDisplay.OnTextInfoChangeListener() {
            @Override
            public void textInfoChanged(String text, float positionX, float positionY) {
                showHandTypeTextView(text, positionX, positionY);
            }
        });
    }

    /**
     * Create a text display thread that is used for text update tasks.
     *
     * @param text      Gesture information displayed on the screen
     * @param positionX The left padding in pixels.
     * @param positionY The right padding in pixels.
     */
    private void showHandTypeTextView(final String text, final float positionX, final float positionY) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setTextColor(Color.WHITE);
                // Set the font size.
                mTextView.setTextSize(TEXT_SIZE);
                if (text != null) {
                    mTextView.setText(text);
                    mTextView.setPadding((int) positionX, (int) positionY, Constants.INIT_ZERO, Constants.INIT_ZERO);
                } else {
                    mTextView.setText("");
                }
            }
        });
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        mTextureDisplay.onSurfaceChanged(width, height);
        GLES20.glViewport(Constants.INIT_ZERO, Constants.INIT_ZERO, width, height);
        mDisplayRotationManager.updateViewportRotation(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Clear the color buffer and notify the driver not to load the data of the previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mSession == null) {
            return;
        }
        if (mDisplayRotationManager.getDeviceRotation()) {
            mDisplayRotationManager.updateArSessionDisplayGeometry(mSession);
        }

        try {
            mSession.setCameraTextureName(mTextureDisplay.getExternalTextureId());
            ARFrame arFrame = mSession.update();
            ARCamera arCamera = arFrame.getCamera();

            // The size of the projection matrix is 4 * 4.
            float[] projectionMatrix = new float[PROJECTION_MATRIX_MAX];

            // Obtain the projection matrix through ARCamera.
            arCamera.getProjectionMatrix(projectionMatrix, PROJECTION_MATRIX_OFFSET, PROJECTION_MATRIX_NEAR,
                    PROJECTION_MATRIX_FAR);
            mTextureDisplay.onDrawFrame(arFrame);
            Collection<ARHand> hands = mSession.getAllTrackables(ARHand.class);
            if (hands.size() == Constants.INIT_ZERO) {
                mTextDisplay.onDrawFrame(null);
                return;
            }
            for (ARHand hand : hands) {
                // Update the hand recognition information to be displayed on the screen.
                StringBuilder sb = new StringBuilder();
                updateMessageData(sb, hand);

                // Display hand recognition information on the screen.
                TEMP_MESSAGE = sb.toString();
                Log.d(Constants.STR_HAND_SIGNAL,TEMP_MESSAGE);
                mTextDisplay.onDrawFrame(sb);
            }
            for (HandRelatedDisplay handRelatedDisplay : mHandRelatedDisplays) {
                handRelatedDisplay.onDrawFrame(hands, projectionMatrix);
            }
        } catch (ArDemoRuntimeException e) {
            Log.d(TAG, Constants.EXCP_ARDEMOE);
        } catch (Throwable t) {
            // This prevents the app from crashing due to unhandled exceptions.
            Log.d(TAG, Constants.EXCP_OPEN_GL, t);
        }
    }

    /**
     * Update gesture-related information.
     *
     * @param sb   String buffer.
     * @param hand ARHand.
     */
    private void updateMessageData(StringBuilder sb, ARHand hand) {
        float fpsResult = doFpsCalculate();
        sb.append(Constants.STR_FPS).append(fpsResult).append(System.lineSeparator());
        Log.d(Constants.STR_HAND_SIGNAL, hand + Constants.STR_EMPTY);
        addHandNormalStringBuffer(sb, hand);
        addGestureActionStringBuffer(sb, hand);
        addGestureCenterStringBuffer(sb, hand);
        float[] gestureHandBoxPoints = hand.getGestureHandBox();

        sb.append(Constants.STR_HAND_BOX_LENGTH).append(gestureHandBoxPoints.length).append(Constants.STR_CLOSE_BRACKET)
                .append(System.lineSeparator());
        for (int i = Constants.INIT_ZERO; i < gestureHandBoxPoints.length; i++) {
            TEMP_MESSAGE  = Constants.STR_GESTURE_POINTS+ gestureHandBoxPoints[i];
            Log.d(TAG, TEMP_MESSAGE );
            sb.append(Constants.STR_GESTURE_POINTS_START).append(i).append(Constants.STR_RATIO).append(gestureHandBoxPoints[i]).append(Constants.STR_CLOSE_BRACKET)
                    .append(System.lineSeparator());
        }
        addHandSkeletonStringBuffer(sb, hand);
    }

    private void addHandNormalStringBuffer(StringBuilder sb, ARHand hand) {
        if (hand.getGestureType() == Constants.INIT_ONE) {
            HomeActivity.getInstance().scrollUpMethod();
            sb.append(Constants.STR_THUBS_UP).append(System.lineSeparator());
        } else if (hand.getGestureType() == Constants.INIT_ZERO) {
            HomeActivity.getInstance().scrollDownMethod();
            sb.append(Constants.STR_THUMBS_DOWN).append(System.lineSeparator());
        }

        sb.append(Constants.STR_GESTURE_TYPE).append(hand.getGestureType()).append(System.lineSeparator());
        sb.append(Constants.STR_GESTURE_COORDINATE_SYSTEM).append(hand.getGestureCoordinateSystem()).append(System.lineSeparator());
        float[] gestureOrientation = hand.getGestureOrientation();
        sb.append(Constants.STR_GESTURE_ORIENTATION).append(gestureOrientation.length).append(Constants.STR_CLOSE_BRACKET)
                .append(System.lineSeparator());

        for (int i = Constants.INIT_ZERO; i < gestureOrientation.length; i++) {

            TEMP_MESSAGE = Constants.STR_GESTURE_ORIENTATION_+ gestureOrientation[i];

            Log.d(TAG, TEMP_MESSAGE );
            sb.append(Constants.STR_GESTURE_OPEN_BRACES).append(i).append(Constants.STR_RATIO).append(gestureOrientation[i])
                    .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
    }

    private void addGestureActionStringBuffer(StringBuilder sb, ARHand hand) {
        int[] gestureAction = hand.getGestureAction();
        sb.append(Constants.STR_GESTURE_LENGTH).append(gestureAction.length).append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator());
        for (int i = Constants.INIT_ZERO; i < gestureAction.length; i++)
        {
            TEMP_MESSAGE = Constants.STR_GESTURE_ACTION + gestureAction[i];
            Log.d(TAG,TEMP_MESSAGE );
            sb.append(Constants.STR_GESTURE_).append(i).append(Constants.STR_RATIO).append(gestureAction[i])
                    .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
    }

    private void addGestureCenterStringBuffer(StringBuilder sb, ARHand hand) {
        float[] gestureCenter = hand.getGestureCenter();
        sb.append(Constants.STR_GESTURE_CENTER).append(gestureCenter.length).append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator());
        for (int i = Constants.INIT_ZERO; i < gestureCenter.length; i++) {
            sb.append(Constants.STR_GESTURE_CENTER_).append(i).append(Constants.STR_RATIO).append(gestureCenter[i])
                    .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
    }

    private void addHandSkeletonStringBuffer(StringBuilder sb, ARHand hand) {
        sb.append(System.lineSeparator()).append(Constants.STR_HAND_TYPE).append(hand.getHandtype())
                .append(System.lineSeparator());
        sb.append(Constants.STR_SKELETON_COORDINATE).append(hand.getSkeletonCoordinateSystem());
        sb.append(System.lineSeparator());
        float[] skeletonArray = hand.getHandskeletonArray();
        sb.append(Constants.STR_SKELETON_ARRAY).append(skeletonArray.length).append(Constants.STR_CLOSE_BRACKET)
                .append(System.lineSeparator());
        TEMP_MESSAGE = Constants.STR_SKELETON_ARRAY_LENGTH + skeletonArray.length;
        Log.d(TAG, TEMP_MESSAGE );
        for (int i = Constants.INIT_ZERO; i < skeletonArray.length; i++) {
            TEMP_MESSAGE = Constants.STR_SKELETON+ skeletonArray[i];
            Log.d(TAG, TEMP_MESSAGE );
        }
        sb.append(System.lineSeparator());
        int[] handSkeletonConnection = hand.getHandSkeletonConnection();
        sb.append(Constants.STR_SKELETON_CONNECTION).append(handSkeletonConnection.length)
                .append(Constants.STR_CLOSE_BRACKET).append(System.lineSeparator());
        Log.d(TAG, Constants.STR_SKELETON_CONNECTION_LENGTH+ handSkeletonConnection.length);
        for (int i = Constants.INIT_ZERO; i < handSkeletonConnection.length; i++) {
            Log.d(TAG, Constants.STR_HAND_SKELETON_CONNECTION+ handSkeletonConnection[i]);
        }
        sb.append(System.lineSeparator()).append("-----------------------------------------------------");
    }

    private float doFpsCalculate() {
        ++frames;
        long timeNow = System.currentTimeMillis();

        // Convert millisecond to second.
        if (((timeNow - lastInterval) / FLOAT_1000F) > UPDATE_INTERVAL) {
            fps = frames / ((timeNow - lastInterval) / FLOAT_1000F);
            frames = Constants.INIT_ZERO;
            lastInterval = timeNow;
        }
        return fps;
    }
}