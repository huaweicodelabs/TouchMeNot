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
package com.huawei.touchmenot.java.main;


import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.huawei.touchmenot.java.main.common.Constants;

public class Clock_SpinTransformation implements ViewPager.PageTransformer
{
        private int SET_ROTATION = 360, SET_REVERSE = -360;
        private double POSITION  = 0.5;

    @Override
  public void transformPage(View page, float position) {
        page.setTranslationX(-position * page.getWidth());

        if (Math.abs(position) <= POSITION) {
            page.setVisibility(View.VISIBLE);
            page.setScaleX(Constants.INIT_ONE - Math.abs(position));
            page.setScaleY(Constants.INIT_ONE - Math.abs(position));
        } else if (Math.abs(position) > POSITION) {
            page.setVisibility(View.GONE);
        }

        if (position < Constants.INIT_MINUS_ONE) {  // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(Constants.INIT_ZERO);
        } else if (position <= Constants.INIT_ZERO) {   // [-1,0]
            page.setAlpha(Constants.INIT_ONE);
            page.setRotation(SET_ROTATION * Math.abs(position));
        } else if (position <= Constants.INIT_ONE) {   // (0,1]
            page.setAlpha(Constants.INIT_ONE);
            page.setRotation(SET_REVERSE * Math.abs(position));
        } else {  // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(Constants.INIT_ZERO);
        }
    }
}