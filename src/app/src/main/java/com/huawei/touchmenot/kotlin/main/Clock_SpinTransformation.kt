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
package com.huawei.touchmenot.kotlin.main

import android.view.View
import androidx.viewpager.widget.ViewPager.PageTransformer
import com.huawei.touchmenot.kotlin.main.common.Constants

class Clock_SpinTransformation : PageTransformer {
    private val SET_ROTATION = 360
    private val SET_REVERSE = -360
    private val POSITION = 0.5
    override fun transformPage(page: View, position: Float) {
        page.translationX = -position * page.width
        if (Math.abs(position) <= POSITION) {
            page.visibility = View.VISIBLE
            page.scaleX = Constants.INIT_ONE - Math.abs(position)
            page.scaleY = Constants.INIT_ONE - Math.abs(position)
        } else if (Math.abs(position) > POSITION) {
            page.visibility = View.GONE
        }
        if (position < Constants.INIT_MINUS_ONE) {  // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.alpha = Constants.INIT_ZERO.toFloat()
        } else if (position <= Constants.INIT_ZERO) {   // [-1,0]
            page.alpha = Constants.INIT_ONE.toFloat()
            page.rotation = SET_ROTATION * Math.abs(position)
        } else if (position <= Constants.INIT_ONE) {   // (0,1]
            page.alpha = Constants.INIT_ONE.toFloat()
            page.rotation = SET_REVERSE * Math.abs(position)
        } else {  // (1,+Infinity]
            // This page is way off-screen to the right.
            page.alpha = Constants.INIT_ZERO.toFloat()
        }
    }
}