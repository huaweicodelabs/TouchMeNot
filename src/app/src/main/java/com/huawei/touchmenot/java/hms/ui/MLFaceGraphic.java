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
package com.huawei.touchmenot.java.hms.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceKeyPoint;
import com.huawei.hms.mlsdk.face.MLFaceShape;
import com.huawei.touchmenot.java.hms.camera.GraphicOverlay;
import com.huawei.touchmenot.R;
import com.huawei.touchmenot.java.main.common.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MLFaceGraphic extends GraphicOverlay.Graphic {
    private static final float BOX_STROKE_WIDTH = 8.0f;
    private static final float LINE_WIDTH = 5.0f;

    private final GraphicOverlay overlay;

    private final Paint facePositionPaint;
    private final Paint landmarkPaint;
    private final Paint boxPaint;

    private final Paint facePaint;
    private final Paint eyePaint;
    private final Paint eyebrowPaint;
    private final Paint lipPaint;
    private final Paint nosePaint;
    private final Paint noseBasePaint;
    private final Paint textPaint;
    private final Paint probilityPaint;
    private static String getColour_1 = "#ffcc66", getColour_2 = "#00ccff", getColour_3 = "#006666",
            getColor_4 = "#ffff00", getColor_5 = "#ff6699", getColor_6 = "#990000";
    private int TEXT_SIZE_24 = 24, TEXT_SIZE_35 = 35;

    private float start = 350f, width = 500f, VALUE_POINT_5F = 0.5f;

    private volatile MLFace mFace;

    public MLFaceGraphic(GraphicOverlay overlay, MLFace face) {
        super(overlay);
        mFace = face;
        this.overlay = overlay;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TEXT_SIZE_24);
        textPaint.setTypeface(Typeface.DEFAULT);

        probilityPaint = new Paint();
        probilityPaint.setColor(Color.WHITE);
        probilityPaint.setTextSize(TEXT_SIZE_35);
        probilityPaint.setTypeface(Typeface.DEFAULT);

        landmarkPaint = new Paint();
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setStrokeWidth(Constants.RADIUS_10F);

        boxPaint = new Paint();
        boxPaint.setColor(Color.WHITE);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        facePaint = new Paint();
        facePaint.setColor(Color.parseColor(getColour_1));
        facePaint.setStyle(Paint.Style.STROKE);
        facePaint.setStrokeWidth(LINE_WIDTH);

        eyePaint = new Paint();
        eyePaint.setColor(Color.parseColor(getColour_2));
        eyePaint.setStyle(Paint.Style.STROKE);
        eyePaint.setStrokeWidth(LINE_WIDTH);

        eyebrowPaint = new Paint();
        eyebrowPaint.setColor(Color.parseColor(getColour_3));
        eyebrowPaint.setStyle(Paint.Style.STROKE);
        eyebrowPaint.setStrokeWidth(LINE_WIDTH);

        nosePaint = new Paint();
        nosePaint.setColor(Color.parseColor(getColor_4));
        nosePaint.setStyle(Paint.Style.STROKE);
        nosePaint.setStrokeWidth(LINE_WIDTH);

        noseBasePaint = new Paint();
        noseBasePaint.setColor(Color.parseColor(getColor_5));
        noseBasePaint.setStyle(Paint.Style.STROKE);
        noseBasePaint.setStrokeWidth(LINE_WIDTH);

        lipPaint = new Paint();
        lipPaint.setColor(Color.parseColor(getColor_6));
        lipPaint.setStyle(Paint.Style.STROKE);
        lipPaint.setStrokeWidth(LINE_WIDTH);
    }

    public List<String> sortHashMap(HashMap<String, Float> map) {
        Set<Map.Entry<String, Float>> entey = map.entrySet();
        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(entey);
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                if (o2.getValue() - o1.getValue() >= Constants.INIT_ZERO) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        List<String> emotions = new ArrayList<>();
        for (int i = Constants.INIT_ZERO; i < Constants.INIT_TWO; i++) {
            emotions.add(list.get(i).getKey());
        }
        return emotions;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mFace == null) {
            return;
        }

        float x = start;
        float y = overlay.getHeight() - Constants.RADIUS_300F;
        HashMap<String, Float> emotions = new HashMap<>();
        emotions.put(Constants.STR_SMILING, mFace.getEmotions().getSmilingProbability());
        emotions.put(Constants.STR_NEUTRAL, mFace.getEmotions().getNeutralProbability());
        emotions.put(Constants.STR_ANGRY, mFace.getEmotions().getAngryProbability());
        emotions.put(Constants.STR_FEAR, mFace.getEmotions().getFearProbability());
        emotions.put(Constants.STR_SAD, mFace.getEmotions().getSadProbability());
        emotions.put(Constants.STR_DISGUST, mFace.getEmotions().getDisgustProbability());
        emotions.put(Constants.STR_SURPRISE, mFace.getEmotions().getSurpriseProbability());
        List<String> result = sortHashMap(emotions);

        DecimalFormat decimalFormat = new DecimalFormat(String.valueOf(R.string.DECIMAL_PATTERN));
        // Draw the facial feature value.
        canvas.drawText(R.string.Left_Eye + decimalFormat.format(mFace.getFeatures().getLeftEyeOpenProbability()), x, y, probilityPaint);
        x = x + width;
        canvas.drawText(R.string.Right_Eye + decimalFormat.format(mFace.getFeatures().getRightEyeOpenProbability()), x, y, probilityPaint);
        y = y - Constants.RADIUS_40F;
        x = start;
        canvas.drawText(R.string.Moutstache_Probability + decimalFormat.format(mFace.getFeatures().getMoustacheProbability()), x, y, probilityPaint);
        x = x + width;
        canvas.drawText(R.string.GLASS_PROBABILITY + decimalFormat.format(mFace.getFeatures().getSunGlassProbability()), x, y, probilityPaint);
        y = y - Constants.RADIUS_40F;
        x = start;
        canvas.drawText(R.string.HAT + decimalFormat.format(mFace.getFeatures().getHatProbability()), x, y, probilityPaint);
        x = x + width;
        canvas.drawText(String.valueOf(R.string.AGE) + mFace.getFeatures().getAge(), x, y, probilityPaint);
        y = y - Constants.RADIUS_40F;
        x = start;
        String sex = (mFace.getFeatures().getSexProbability() > VALUE_POINT_5F) ? Constants.STR_GENDER_FEMALE : Constants.STR_GENDER_MALE;
        canvas.drawText(R.string.GENDER + sex, x, y, probilityPaint);
        x = x + width;
        canvas.drawText(R.string.EULER_ANGLE_Y + decimalFormat.format(mFace.getRotationAngleY()), x, y, probilityPaint);
        y = y - Constants.RADIUS_40F;
        x = start;
        canvas.drawText(R.string.EULER_ANGEL_Z + decimalFormat.format(mFace.getRotationAngleZ()), x, y, probilityPaint);
        x = x + width;
        canvas.drawText(R.string.EULER_ANGLE_X + decimalFormat.format(mFace.getRotationAngleX()), x, y, probilityPaint);
        y = y - Constants.RADIUS_40F;
        x = start;
        canvas.drawText(result.get(Constants.INIT_ZERO), x, y, probilityPaint);

        // Draw a face contour.
        if (mFace.getFaceShapeList() != null) {
            for (MLFaceShape faceShape : mFace.getFaceShapeList()) {
                if (faceShape == null) {
                    continue;
                }
                List<MLPosition> points = faceShape.getPoints();
                for (int i = Constants.INIT_ZERO; i < points.size(); i++) {
                    MLPosition point = points.get(i);
                    canvas.drawPoint(translateX(point.getX().floatValue()), translateY(point.getY().floatValue()), boxPaint);
                    if (i != (points.size() - Constants.INIT_ONE)) {
                        MLPosition next = points.get(i + Constants.INIT_ONE);
                        if (point != null && point.getX() != null && point.getY() != null) {
                            if (i % Constants.INIT_THREE == Constants.INIT_ZERO) {
                                canvas.drawText(i + Constants.INIT_ONE + Constants.STR_EMPTY, translateX(point.getX().floatValue()), translateY(point.getY().floatValue()), textPaint);
                            }
                            canvas.drawLines(new float[]{translateX(point.getX().floatValue()), translateY(point.getY().floatValue()),
                                    translateX(next.getX().floatValue()), translateY(next.getY().floatValue())}, getPaint(faceShape));
                        }
                    }
                }
            }
        }
        // Face Key Points
        for (MLFaceKeyPoint keyPoint : mFace.getFaceKeyPoints()) {
            if (keyPoint != null) {
                MLPosition point = keyPoint.getPoint();
                canvas.drawCircle(
                        translateX(point.getX()),
                        translateY(point.getY()),
                        Constants.RADIUS_10F, landmarkPaint);
            }
        }
    }

    private Paint getPaint(MLFaceShape faceShape) {
        switch (faceShape.getFaceShapeType()) {
            case MLFaceShape.TYPE_LEFT_EYE:
            case MLFaceShape.TYPE_RIGHT_EYE:
                return eyePaint;
            case MLFaceShape.TYPE_BOTTOM_OF_LEFT_EYEBROW:

            case MLFaceShape.TYPE_BOTTOM_OF_RIGHT_EYEBROW:
            case MLFaceShape.TYPE_TOP_OF_LEFT_EYEBROW:
            case MLFaceShape.TYPE_TOP_OF_RIGHT_EYEBROW:
                return eyebrowPaint;
            case MLFaceShape.TYPE_BOTTOM_OF_LOWER_LIP:
            case MLFaceShape.TYPE_TOP_OF_LOWER_LIP:
            case MLFaceShape.TYPE_BOTTOM_OF_UPPER_LIP:
            case MLFaceShape.TYPE_TOP_OF_UPPER_LIP:
                return lipPaint;
            case MLFaceShape.TYPE_BOTTOM_OF_NOSE:
                return noseBasePaint;
            case MLFaceShape.TYPE_BRIDGE_OF_NOSE:
                return nosePaint;
            default:
                return facePaint;
        }
    }
}