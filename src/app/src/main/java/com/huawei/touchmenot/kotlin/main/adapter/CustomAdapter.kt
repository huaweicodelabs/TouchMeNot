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
package com.huawei.touchmenot.kotlin.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huawei.touchmenot.kotlin.main.HomeActivity
import com.huawei.touchmenot.R
import com.huawei.touchmenot.kotlin.main.model.SpeechData

class CustomAdapter // Provide a suitable constructor (depends on the kind of dataset)
(private val values: ArrayList<SpeechData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    fun add(position: Int, item: SpeechData) {
        values.add(position, item)
        HomeActivity.instance?.updateList(values)
        notifyItemInserted(position)
    }

    fun add(item: SpeechData) {
        values.add(item)
        notifyDataSetChanged()
        HomeActivity.instance?.updateList(values)
    }

    fun add(item: SpeechData, recyclerView: RecyclerView) {
        values.add(item)
        HomeActivity.instance?.updateList(values)
        notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(values.size - 1)
    }

    fun remove(position: Int) {
        values.removeAt(position)
        HomeActivity.instance?.updateList(values)
        notifyItemRemoved(position)
    }

    override fun getItemViewType(position: Int): Int {
        return if (values[position].requestType == TYPE_NORMAL) {
            TYPE_NORMAL
        } else if (values[position].requestType == TYPE_SEARCH) {
            TYPE_SEARCH
        } else TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == TYPE_NORMAL) { // for normal layout
            val inflater = LayoutInflater.from(parent.context)
            view = inflater.inflate(R.layout.custom_item_view, parent, false)
            CustomViewHolder(view)
        } else { // for search layout
            val inflater = LayoutInflater.from(parent.context)
            view = inflater.inflate(R.layout.custom_search_item_view, parent, false)
            SearchViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = values[position]
        if (getItemViewType(position) == TYPE_NORMAL) {
            if (data.request != null) {
                (holder as CustomViewHolder).speechResponseText.visibility = View.GONE
                holder.speechRequestText.text = data.request
                holder.speechRequestText.visibility = View.VISIBLE
            } else {
                (holder as CustomViewHolder).speechRequestText.visibility = View.GONE
                holder.speechResponseText.text = data.response
                holder.speechResponseText.visibility = View.VISIBLE
            }
        } else {
            if (data.response != null) {
                (holder as SearchViewHolder).tvTitle.text = data.siteResponse?.name
                holder.tvAddress.text = data.siteResponse?.formatAddress
                val poi = data.siteResponse?.poi?.poiTypes
                if (poi != null) {
                    if (poi.size == 1) {
                        holder.tvPoi.text = data!!.siteResponse!!.poi.poiTypes[0]
                        holder.tvPoi2.visibility = View.GONE
                    } else {
                        holder.tvPoi.text = data!!.siteResponse!!.poi.poiTypes[0]
                        holder.tvPoi2.text = data.siteResponse!!.poi.poiTypes[1]
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class CustomViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        // each data item is just a string in this case
        var speechRequestText: TextView
        var speechResponseText: TextView

        init {
            speechRequestText = layout.findViewById(R.id.speechText)
            speechResponseText = layout.findViewById(R.id.speechResText)
        }
    }

    inner class SearchViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        // each data item is just a string in this case
        var tvTitle: TextView
        var tvAddress: TextView
        var tvPoi: TextView
        var tvPoi2: TextView

        init {
            tvTitle = layout.findViewById(R.id.tvTitle)
            tvAddress = layout.findViewById(R.id.tvAddress)
            tvPoi = layout.findViewById(R.id.tvPoi)
            tvPoi2 = layout.findViewById(R.id.tvPoi2)
        }
    }

    companion object {
        private const val TYPE_NORMAL = 1
        private const val TYPE_SEARCH = 2
    }

}