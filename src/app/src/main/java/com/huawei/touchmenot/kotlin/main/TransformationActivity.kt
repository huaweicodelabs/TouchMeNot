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

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.huawei.touchmenot.kotlin.fragments.FirstFragment
import com.huawei.touchmenot.kotlin.fragments.SecondFragment
import com.huawei.touchmenot.kotlin.fragments.ThirdFragment
import com.huawei.touchmenot.kotlin.hms.LiveFaceDetectionHMSActivity
import com.huawei.touchmenot.kotlin.main.common.Constants
import java.util.*
import com.huawei.touchmenot.R

class TransformationActivity : AppCompatActivity() {
    val DELAY_MS: Long = 3000 //delay in milliseconds before task is to be executed
    val PERIOD_MS: Long = 3000 // time in milliseconds between successive task executions.
    var currentPage = 0
    var viewPager: ViewPager? = null
    var pagerAdapter: MyPagerAdapter? = null
    var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transformation)
        viewPager = findViewById(R.id.viewPager)
        pagerAdapter = MyPagerAdapter(supportFragmentManager)
        addingFragmentsTOpagerAdapter()
        viewPager?.setAdapter(pagerAdapter)
        val clockSpinTransformation = Clock_SpinTransformation()
        viewPager?.setPageTransformer(true, clockSpinTransformation)
        val handler = Handler()
        val Update = Runnable {
            if (currentPage == pagerAdapter!!.count - Constants.INIT_ONE) {
                Handler().postDelayed({
                    startActivity(Intent(this@TransformationActivity, LiveFaceDetectionHMSActivity::class.java))
                    finish()
                }, Constants.DELAY_MILLIS.toLong())
            }
            viewPager?.setCurrentItem(currentPage++, true)
        }
        timer = Timer() // This will create a new Thread
        timer!!.schedule(object : TimerTask() {
            // task to be scheduled
            override fun run() {
                handler.post(Update)
            }
        }, DELAY_MS, PERIOD_MS)
    }

    private fun addingFragmentsTOpagerAdapter() {
        pagerAdapter!!.addFragments(FirstFragment())
        pagerAdapter!!.addFragments(SecondFragment())
        pagerAdapter!!.addFragments(ThirdFragment())
    }
}