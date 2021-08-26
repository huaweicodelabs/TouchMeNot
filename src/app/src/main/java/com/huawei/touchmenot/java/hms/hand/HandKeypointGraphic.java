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

package com.huawei.touchmenot.java.hms.hand;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoints;
import com.huawei.touchmenot.java.hms.camera.GraphicOverlay;
import com.huawei.touchmenot.java.main.common.Constants;
import com.huawei.touchmenot.java.main.model.RatingInterface;

import java.util.List;

/**
 * Graphic instance for rendering hand position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class HandKeypointGraphic extends GraphicOverlay.Graphic {
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private final Paint rectPaint;
    private final Paint idPaintnew;
    private List<MLHandKeypoints> handKeypoints;
    private MLAnalyzer.Result<MLHandKeypoints> mResult;
    LiveHandKeyPointAnalyseActivity context;
    public RatingInterface mInterface;
    private String getColor_1 = "#6200EE";
    private int  TEXT_SIZE_32 = 32;
    private float FLOAT_2F = 2F;
    private float FLOAT_100F =  100f;

    public void setInterface(RatingInterface mInterface) {
        this.mInterface = mInterface;
    }

    public HandKeypointGraphic(GraphicOverlay overlay, List<MLHandKeypoints> handKeypoints, MLAnalyzer.Result<MLHandKeypoints> result, LiveHandKeyPointAnalyseActivity liveHandKeyPointAnalyseActivity) {
        super(overlay);
        setInterface(liveHandKeyPointAnalyseActivity);
        this.handKeypoints = handKeypoints;
        mResult = result;
        final int selectedColor = Color.WHITE;
        context = liveHandKeyPointAnalyseActivity;
        idPaintnew = new Paint();
        idPaintnew.setColor(Color.RED);
        idPaintnew.setTextSize(TEXT_SIZE_32);

        rectPaint = new Paint();
        rectPaint.setColor(selectedColor);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        String data = Hand.analyzeHandsAndGetNumber(mResult);
        mInterface.ratingCaptured(data);
        callMe(data, canvas);
    }

    public void callMe(String text, Canvas canvas) {
        float centerX = canvas.getWidth() / FLOAT_2F;
        float centerY = canvas.getHeight() / FLOAT_2F;
        Paint paint;
        Paint circlePaint;

        paint = new Paint();
        circlePaint = new Paint();

        paint.setColor(Color.WHITE);
        paint.setTextSize(FLOAT_100F);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect bounds = new Rect();
        paint.getTextBounds(text, Constants.INIT_ZERO, text.length(), bounds);

        circlePaint.setColor(Color.parseColor(getColor_1));
        circlePaint.setAntiAlias(true);

        canvas.drawCircle(centerX, centerY - (bounds.height() / Constants.INIT_TWO), bounds.width() + Constants.INIT_FIVE, circlePaint);
        canvas.drawText(text, centerX, centerY, paint);
    }

    /*
     * @param rect
     * @return Rect
     */
    public Rect translateRect(Rect rect) {
        float left = translateX(rect.left);
        float right = translateX(rect.right);
        float bottom = translateY(rect.bottom);
        float top = translateY(rect.top);
        if (left > right) {
            float size = left;
            left = right;
            right = size;
        }
        if (bottom < top) {
            float size = bottom;
            bottom = top;
            top = size;
        }
        return new Rect((int) left, (int) top, (int) right, (int) bottom);
    }
}
