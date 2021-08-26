/** Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.huawei.touchmenot.java.main.model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class TmnSingleton {
    private static TmnSingleton mInstance;
    private static Context mctx;
    private RequestQueue requestQueue;

    private TmnSingleton(Context context) {
        mctx = context;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(mctx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized TmnSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TmnSingleton(context);
        }
        return mInstance;
    }

    public <T> void addtorequestqueue(Request<T> request) {
        requestQueue.add(request);
    }
}
