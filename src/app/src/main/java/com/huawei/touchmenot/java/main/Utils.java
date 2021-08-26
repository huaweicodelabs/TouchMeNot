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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.huawei.touchmenot.java.main.common.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static SharedPreferences sharedpreferences;
    private static Utils instance;
    private static Context mContext;

    private static final String TAG = "UTILS";
    private String CURRENT_TIME = "Current time => ";


    private Utils() {
    }

    public static SharedPreferences getPreferences() {
        return sharedpreferences;
    }

    public static Utils getInstance(Context context) {
        if (instance == null) {
            mContext = context;
            sharedpreferences = mContext.getSharedPreferences(Constants.STR_MYPREF,
                    Context.MODE_PRIVATE);
            return instance = new Utils();
        } else
            return instance;
    }

    public static void setValue(String key, String value) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getValue(String data) {
        if (sharedpreferences.contains(data)) {
            return sharedpreferences.getString(data, Constants.STR_EMPTY);
        } else
            return null;
    }

    public void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        mContext.startActivity(cameraIntent);
    }

    public String getTodayDate() {
        Date c = Calendar.getInstance().getTime();

        CURRENT_TIME = Constants.STR_EMPTY;
          Log.d(TAG,CURRENT_TIME + c);
      

        SimpleDateFormat df = new SimpleDateFormat(Constants.STR_DATE_PATTERN, Locale.getDefault());
        String formattedDate = df.format(c);
        return Constants.STR_TODAY_DATE + formattedDate;
    }

    public String getTodayTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.STR_HOUR_PATTERN);
        String formattedDate = sdf.format(d);
        return formattedDate;
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType(Constants.STR_IMG_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mContext.startActivity(Intent.createChooser(intent, Constants.STR_SELECT_PICTURE));
    }
}
