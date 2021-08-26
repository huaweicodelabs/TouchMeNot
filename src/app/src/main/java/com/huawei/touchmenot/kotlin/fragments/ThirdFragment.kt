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
package com.huawei.touchmenot.kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.huawei.touchmenot.R
import com.squareup.picasso.Picasso

class ThirdFragment : Fragment() {
    private var scroll1: ImageView? = null
    private var scroll2: ImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val three = view.findViewById<ImageView>(R.id.fragmentThreeBackground)
        scroll1 = view.findViewById(R.id.scroll1)
        scroll2 = view.findViewById(R.id.scroll2)
        Picasso.get().load(R.drawable.three).fit().centerCrop().into(three)
        Glide.with(this)
                .load(R.drawable.scroll1)
                .centerCrop()
                .placeholder(R.drawable.scroll1)
                .into(scroll1!!)
        Glide.with(this)
                .load(R.drawable.scroll2)
                .centerCrop()
                .placeholder(R.drawable.scroll2)
                .into(scroll2!!)
    }
}