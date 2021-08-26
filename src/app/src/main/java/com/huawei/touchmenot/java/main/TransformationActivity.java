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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.huawei.touchmenot.R;
import com.huawei.touchmenot.java.fragments.FirstFragment;
import com.huawei.touchmenot.java.fragments.SecondFragment;
import com.huawei.touchmenot.java.fragments.ThirdFragment;
import com.huawei.touchmenot.java.hms.LiveFaceDetectionHMSActivity;
import com.huawei.touchmenot.java.main.common.Constants;

import java.util.Timer;
import java.util.TimerTask;


public class TransformationActivity extends AppCompatActivity {
    final long DELAY_MS = 3000;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.
    int currentPage = 0;
    ViewPager viewPager;
    MyPagerAdapter pagerAdapter;
    Intent intent;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transformation);
        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        addingFragmentsTOpagerAdapter();
        viewPager.setAdapter(pagerAdapter);

        Clock_SpinTransformation clockSpinTransformation = new Clock_SpinTransformation();

        viewPager.setPageTransformer(true, clockSpinTransformation);
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == pagerAdapter.getCount() - Constants.INIT_ONE) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(TransformationActivity.this, LiveFaceDetectionHMSActivity.class));
                            finish();
                        }
                    }, Constants.DELAY_MILLIS);
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };
        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }

    private void addingFragmentsTOpagerAdapter() {
        pagerAdapter.addFragments(new FirstFragment());
        pagerAdapter.addFragments(new SecondFragment());
        pagerAdapter.addFragments(new ThirdFragment());
    }
}