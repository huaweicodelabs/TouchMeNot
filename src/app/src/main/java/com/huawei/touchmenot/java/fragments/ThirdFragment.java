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
package com.huawei.touchmenot.java.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.huawei.touchmenot.R;
import com.squareup.picasso.Picasso;

public class ThirdFragment extends Fragment {
    private ImageView scroll1;
    private ImageView scroll2;

    public ThirdFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_third, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView three = view.findViewById(R.id.fragmentThreeBackground);
        scroll1 = view.findViewById(R.id.scroll1);
        scroll2 = view.findViewById(R.id.scroll2);
        Picasso.get().load(R.drawable.three).fit().centerCrop().into(three);
        Glide.with(this)
                .load(R.drawable.scroll1)
                .centerCrop()
                .placeholder(R.drawable.scroll1)
                .into(scroll1);
        Glide.with(this)
                .load(R.drawable.scroll2)
                .centerCrop()
                .placeholder(R.drawable.scroll2)
                .into(scroll2);
    }
}