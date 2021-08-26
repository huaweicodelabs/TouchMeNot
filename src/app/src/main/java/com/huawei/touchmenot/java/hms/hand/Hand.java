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

package com.huawei.touchmenot.java.hms.hand;

import android.util.SparseArray;

import androidx.core.util.SparseArrayKt;

import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoint;
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

public class Hand {

    private ArrayList<MLHandKeypoint> firstFinger = new ArrayList<>();
    private ArrayList<MLHandKeypoint> middleFinger = new ArrayList<>();
    private ArrayList<MLHandKeypoint> ringFinger = new ArrayList<>();
    private ArrayList<MLHandKeypoint> littleFinger = new ArrayList<>();
    private ArrayList<MLHandKeypoint> thumb = new ArrayList<>();

    private MLHandKeypoint wrist;
    public static int ACCEPTED_RANGE_IN_DEGREE = 30;
    private static final int DEGREE = 360;
    private static final int FINGER_COUNT_LESS_THAN = 6;

    public static String analyzeHandsAndGetNumber(MLAnalyzer.Result<MLHandKeypoints> result) {
        ArrayList hands = new ArrayList();
        int number = 0;
        if (result != null) {
            SparseArray analyseList = result.getAnalyseList();
            Intrinsics.checkNotNullExpressionValue(analyseList, "result.analyseList");
            if (analyseList != null) {
                Iterator keyIterator = (Iterator) SparseArrayKt.keyIterator(analyseList);
                while (keyIterator.hasNext()) {
                    int key = ((Number) keyIterator.next()).intValue();
                    hands.add(new Hand());
                    analyseList = result.getAnalyseList();
                    Intrinsics.checkNotNullExpressionValue(analyseList, "result.analyseList");
                    Iterator iterator = SparseArrayKt.valueIterator(analyseList);
                    Hand hand;
                    List list;
                    for (Iterator var11 = iterator; var11.hasNext(); number += hand.createHand(list).getNumber()) {
                        MLHandKeypoints value = (MLHandKeypoints) var11.next();
                        hand = (Hand) CollectionsKt.last((List) hands);
                        Intrinsics.checkNotNullExpressionValue(value, "value");
                        list = value.getHandKeypoints();
                        Intrinsics.checkNotNullExpressionValue(list, "value.handKeypoints");
                    }
                }
            }

        }
        if (number < FINGER_COUNT_LESS_THAN)
            return String.valueOf(number);
        else
            return "";
    }

    private Hand createHand(List<MLHandKeypoint> keyPoints) {
        for (MLHandKeypoint keyPoint : keyPoints) {
            switch (keyPoint.getType()) {
                case MLHandKeypoint.TYPE_WRIST:
                    wrist = keyPoint;
                    break;

                case MLHandKeypoint.TYPE_THUMB_FIRST:
                    thumb.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_THUMB_SECOND:
                    thumb.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_THUMB_THIRD:
                    thumb.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_THUMB_FOURTH:
                    thumb.add(keyPoint);
                    break;

                case MLHandKeypoint.TYPE_FOREFINGER_FIRST:
                    firstFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_FOREFINGER_SECOND:
                    firstFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_FOREFINGER_THIRD:
                    firstFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_FOREFINGER_FOURTH:
                    firstFinger.add(keyPoint);
                    break;

                case MLHandKeypoint.TYPE_MIDDLE_FINGER_FIRST:
                    middleFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_MIDDLE_FINGER_SECOND:
                    middleFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_MIDDLE_FINGER_THIRD:
                    middleFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_MIDDLE_FINGER_FOURTH:
                    middleFinger.add(keyPoint);
                    break;

                case MLHandKeypoint.TYPE_RING_FINGER_FIRST:
                    ringFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_RING_FINGER_SECOND:
                    ringFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_RING_FINGER_THIRD:
                    ringFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_RING_FINGER_FOURTH:
                    ringFinger.add(keyPoint);
                    break;

                case MLHandKeypoint.TYPE_LITTLE_FINGER_FIRST:
                    littleFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_LITTLE_FINGER_SECOND:
                    littleFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_LITTLE_FINGER_THIRD:
                    littleFinger.add(keyPoint);
                    break;
                case MLHandKeypoint.TYPE_LITTLE_FINGER_FOURTH:
                    littleFinger.add(keyPoint);
                    break;
            }
        }
        return this;
    }

    private int getNumber() {
        int number = 0;
        if (wrist != null) {
            ArrayList var10001 = this.firstFinger;
            MLHandKeypoint var10002 = this.wrist;
            Intrinsics.checkNotNull(var10002);
            if (this.isFingerUp(var10001, var10002)) {
                ++number;
            }

            var10001 = this.middleFinger;
            var10002 = this.wrist;
            Intrinsics.checkNotNull(var10002);
            if (this.isFingerUp(var10001, var10002)) {
                ++number;
            }

            var10001 = this.ringFinger;
            var10002 = this.wrist;
            Intrinsics.checkNotNull(var10002);
            if (this.isFingerUp(var10001, var10002)) {
                ++number;
            }

            var10001 = this.littleFinger;
            var10002 = this.wrist;
            Intrinsics.checkNotNull(var10002);
            if (this.isFingerUp(var10001, var10002)) {
                ++number;
            }

            var10001 = this.thumb;
            var10002 = this.wrist;
            Intrinsics.checkNotNull(var10002);
            if (this.isFingerUp(var10001, var10002)) {
                ++number;
            }
        }
        return number;
    }

    private boolean isFingerUp(ArrayList<MLHandKeypoint> points, MLHandKeypoint wrist) {
        if (points.size() == 4) {
            double degreeFirst2Last = this.getDegree((MLHandKeypoint) CollectionsKt.first((List) points), (MLHandKeypoint) CollectionsKt.last((List) points));
            MLHandKeypoint var10001 = (MLHandKeypoint) CollectionsKt.first((List) points);
            Object var10002 = points.get(1);
            Intrinsics.checkNotNullExpressionValue(var10002, "points[1]");
            double degreeFirst2Second = this.getDegree(var10001, (MLHandKeypoint) var10002);
            double var7 = degreeFirst2Last - degreeFirst2Second;
            boolean var9 = false;
            if (Math.abs(var7) < (double) ACCEPTED_RANGE_IN_DEGREE) {
                return true;
            }
        }
        return false;
    }

    private double getDegree(MLHandKeypoint point1, MLHandKeypoint point2) {
        float width = point2.getPointX() - point1.getPointX();
        float height = point2.getPointY() - point1.getPointY();
        double angle = height;
        double val = width;
        boolean value = false;
        double theta = Math.atan2(angle, val);
        theta++;
        angle = Math.toDegrees(theta);
        if (angle < 0) {
            angle += (double) DEGREE;
        }
        return angle;
    }

}
