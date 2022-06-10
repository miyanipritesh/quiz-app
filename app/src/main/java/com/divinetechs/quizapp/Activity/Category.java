package com.divinetechs.quizapp.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.divinetechs.quizapp.Adapter.CategoryAdapter;
import com.divinetechs.quizapp.Model.CategoryModel.CategoryModel;
import com.divinetechs.quizapp.Model.CategoryModel.Result;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.divinetechs.quizapp.Webservice.AppAPI;
import com.divinetechs.quizapp.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Category extends AppCompatActivity implements View.OnClickListener {

    PrefManager prefManager;
    Map<String, String> map;

    ShimmerFrameLayout shimmer;

    LinearLayout lyBack, lyToolbar, lyCategory, lyAdView, lyFbAdView;

    TextView txtToolbarTitle;

    RecyclerView rvCategory;
    List<Result> categoryList;
    CategoryAdapter categoryAdapter;

    com.facebook.ads.AdView fbAdView = null;
    AdView mAdView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        PrefManager.forceRTLIfSupported(getWindow(), Category.this);
        Utility.screenCapOff(Category.this);

        init();
        AdInit();
        Category();

        txtToolbarTitle.setText("" + getResources().getString(R.string.category));

    }

    private void init() {
        try {
            prefManager = new PrefManager(Category.this);
            map = new HashMap<>();
            map = Utility.GetMap(Category.this);

            shimmer = findViewById(R.id.shimmer);

            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            lyBack = findViewById(R.id.lyBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            lyAdView = findViewById(R.id.lyAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            lyCategory = findViewById(R.id.lyCategory);
            rvCategory = findViewById(R.id.rvCategory);

            lyBack.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            lyAdView.setVisibility(View.VISIBLE);
            Utility.Admob(Category.this, mAdView, prefManager.getValue("banner_adid"), lyAdView);
        } else {
            lyAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utility.FacebookBannerAd(Category.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyBack:
                Category.this.finish();
                break;
        }
    }

    //get_category API call
    private void Category() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<CategoryModel> call = bookNPlayAPI.get_category();
        call.enqueue(new Callback<CategoryModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<CategoryModel> call, @NonNull Response<CategoryModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("status", "" + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            categoryList = new ArrayList<>();
                            categoryList = response.body().getResult();
                            Log.e("categoryList size", "" + categoryList.size());
                            categoryAdapter = new CategoryAdapter(Category.this, categoryList);
                            rvCategory.setLayoutManager(new GridLayoutManager(Category.this,
                                    2, LinearLayoutManager.VERTICAL, false));
                            rvCategory.setHasFixedSize(true);
                            rvCategory.setAdapter(categoryAdapter);
                            rvCategory.setItemAnimator(new DefaultItemAnimator());
                            categoryAdapter.notifyDataSetChanged();
                        } else {
                            rvCategory.setVisibility(View.GONE);
                        }

                    } else {
                        Log.e("massage", "" + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("get_category API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(@NonNull Call<CategoryModel> call, @NonNull Throwable t) {
                Log.e("get_category Failure", "That didn't work!!!" + t.getMessage());
                Utility.shimmerHide(shimmer);
            }
        });
    }

    @Override
    public void onPause() {
        Utility.shimmerHide(shimmer);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
    }

}