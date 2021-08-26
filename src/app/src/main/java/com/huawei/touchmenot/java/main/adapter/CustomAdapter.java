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

package com.huawei.touchmenot.java.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.touchmenot.java.main.HomeActivity;
import com.huawei.touchmenot.R;
import com.huawei.touchmenot.java.main.model.SpeechData;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static int TYPE_NORMAL = 1;
    private static int TYPE_SEARCH = 2;
    private List<SpeechData> values;

    public void add(int position, SpeechData item) {
        values.add(position, item);
        HomeActivity.getInstance().updateList(values);
        notifyItemInserted(position);
    }

    public void add(SpeechData item) {
        values.add(item);
        notifyDataSetChanged();
        HomeActivity.getInstance().updateList(values);
    }

    public void add(SpeechData item, RecyclerView recyclerView) {
        values.add(item);
        HomeActivity.getInstance().updateList(values);
        notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(values.size() - 1);
    }

    public void remove(int position) {
        values.remove(position);
        HomeActivity.getInstance().updateList(values);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CustomAdapter(List<SpeechData> myDataset) {
        values = myDataset;
    }

    @Override
    public int getItemViewType(int position) {
        if (values.get(position).getRequestType() == TYPE_NORMAL) {
            return TYPE_NORMAL;
        } else if (values.get(position).getRequestType() == TYPE_SEARCH) {
            return TYPE_SEARCH;
        } else
            return TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_NORMAL) { // for normal layout
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.custom_item_view, parent, false);
            return new CustomViewHolder(view);
        } else { // for search layout
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.custom_search_item_view, parent, false);
            return new SearchViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final SpeechData data = values.get(position);
        if (getItemViewType(position) == TYPE_NORMAL) {
            if (data.getRequest() != null) {
                ((CustomViewHolder) holder).speechResponseText.setVisibility(View.GONE);
                ((CustomViewHolder) holder).speechRequestText.setText(data.getRequest());
                ((CustomViewHolder) holder).speechRequestText.setVisibility(View.VISIBLE);
            } else {
                ((CustomViewHolder) holder).speechRequestText.setVisibility(View.GONE);
                ((CustomViewHolder) holder).speechResponseText.setText(data.getResponse());
                ((CustomViewHolder) holder).speechResponseText.setVisibility(View.VISIBLE);
            }
        } else {
            if (data.getResponse() != null) {
                ((SearchViewHolder) holder).tvTitle.setText(data.getSiteResponse().getName());
                ((SearchViewHolder) holder).tvAddress.setText(data.getSiteResponse().getFormatAddress());
                String[] poi = data.getSiteResponse().getPoi().getPoiTypes();
                if (poi != null) {
                    if (poi.length == 1) {
                        ((SearchViewHolder) holder).tvPoi.setText(data.getSiteResponse().getPoi().getPoiTypes()[0]);
                        ((SearchViewHolder) holder).tvPoi2.setVisibility(View.GONE);
                    } else {
                        ((SearchViewHolder) holder).tvPoi.setText(data.getSiteResponse().getPoi().getPoiTypes()[0]);
                        ((SearchViewHolder) holder).tvPoi2.setText(data.getSiteResponse().getPoi().getPoiTypes()[1]);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView speechRequestText;
        public TextView speechResponseText;
        public View layout;

        public CustomViewHolder(View v) {
            super(v);
            layout = v;
            speechRequestText = v.findViewById(R.id.speechText);
            speechResponseText = v.findViewById(R.id.speechResText);
        }
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvTitle;
        public TextView tvAddress;
        public TextView tvPoi;
        public TextView tvPoi2;
        public View layout;

        public SearchViewHolder(View v) {
            super(v);
            layout = v;
            tvTitle = v.findViewById(R.id.tvTitle);
            tvAddress = v.findViewById(R.id.tvAddress);
            tvPoi = v.findViewById(R.id.tvPoi);
            tvPoi2 = v.findViewById(R.id.tvPoi2);
        }
    }
}
