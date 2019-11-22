/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soumit.smartnews.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.soumit.smartnews.R;
import com.soumit.smartnews.model.Model;
import com.soumit.smartnews.model.entity.NYTimesStory;
import com.soumit.smartnews.ui.ItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements ItemClickListener{

    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refreshView;
    @BindView(R.id.list_view) RecyclerView listView;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;
    @BindView(R.id.spinner) Spinner spinner;

    MainPresenter presenter = new MainPresenter(this, Model.getInstance());
//    private ArrayAdapter<NYTimesStory> adapter;
    private NewsListRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup initial views
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = null;

        refreshView.setOnRefreshListener(() -> presenter.refreshList());
        progressBar.setVisibility(View.INVISIBLE);

        // After setup, notify presenter
        presenter.onCreate();
    }

    /**
     * Setup the toolbar spinner with the available sections
     */
    public void configureToolbar(List<String> sections) {
        String[] sectionList = sections.toArray(new String[sections.size()]);
        final ArrayAdapter adapter = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, sectionList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.titleSpinnerSectionSelected((String) adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    public void hideRefreshing() {
        refreshView.setRefreshing(false);
    }

    public void showList(List<NYTimesStory> items) {

        adapter = new NewsListRecyclerAdapter(this, items, this );
        listView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        listView.setAdapter(adapter);

//        if (adapter == null) {
//            adapter = new NewsListRecyclerAdapter(this, items, this);
//            listView.setAdapter(adapter);
//        } else {
////            adapter.clear();
////            adapter.addAll(items);
//            adapter.notifyDataSetChanged();
//        }

    }

    public void showNetworkLoading(Boolean networkInUse) {
        progressBar.setVisibility(networkInUse ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onItemClicked(int position, String viewTag) {
        presenter.listItemSelected(position);
    }

    //Recyclerview adapter class
    public static class NewsListRecyclerAdapter extends RecyclerView.Adapter<NewsListRecyclerAdapter.NewsViewHolder>{

        private static final String TAG = "NewsListRecyclerAdapter";
        private Context context;
        private List<NYTimesStory> initialData = new ArrayList<>();
        private ItemClickListener clickListener;
        @ColorInt
        private final int readColor;
        @ColorInt private final int unreadColor;

        public NewsListRecyclerAdapter(Context context, List<NYTimesStory> initialData, ItemClickListener clickListener) {
            this.context = context;
            this.initialData = initialData;
            this.clickListener = clickListener;
            readColor = context.getResources().getColor(android.R.color.darker_gray);
            unreadColor = context.getResources().getColor(android.R.color.primary_text_light);
        }

        @NonNull
        @Override
        public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
            NYTimesStory nyTimesStory = initialData.get(position);
//            if(nyTimesStory.getMultimedia() != null && nyTimesStory.getMultimedia().get(0) != null) {
//                Glide.with(context).load(nyTimesStory.getMultimedia().get(0).getUrl()).into(holder.newsImage);
//            }
            holder.headlineTxt.setText(nyTimesStory.getTitle());
            holder.headlineTxt.setTextColor(nyTimesStory.isRead() ? readColor : unreadColor);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClicked(position, "news_details");
                }
            });
        }

        @Override
        public int getItemCount() {
            return initialData.size();
        }

        public class NewsViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.news_image)
            AppCompatImageView newsImage;
            @BindView(R.id.headlineTxt)
            TextView headlineTxt;
            @BindView(R.id.timeTxt)
            TextView timeTxt;

            public NewsViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    // ListView adapter class
    public static class NewsListAdapter extends ArrayAdapter<NYTimesStory> {

        private final LayoutInflater inflater;
        @LayoutRes
        private final int layoutResource;
        @ColorInt
        private final int readColor;
        @ColorInt private final int unreadColor;

        public NewsListAdapter(Context context, List<NYTimesStory> initialData) {
            super(context, android.R.layout.simple_list_item_1);
            setNotifyOnChange(false);
            addAll(initialData);
            inflater = LayoutInflater.from(context);
            layoutResource = android.R.layout.simple_list_item_1;
            readColor = context.getResources().getColor(android.R.color.darker_gray);
            unreadColor = context.getResources().getColor(android.R.color.primary_text_light);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(layoutResource, parent, false);
                ViewHolder holder = new ViewHolder(view);
                view.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            NYTimesStory story = getItem(position);
            holder.titleView.setText(story.getTitle());
            holder.titleView.setTextColor(story.isRead() ? readColor : unreadColor);
            return view;
        }

        static class ViewHolder {
            @BindView(android.R.id.text1) TextView titleView;
            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
