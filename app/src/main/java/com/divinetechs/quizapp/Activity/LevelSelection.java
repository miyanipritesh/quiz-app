package com.divinetechs.quizapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.divinetechs.quizapp.Adapter.LevelSelectionAdapter;
import com.divinetechs.quizapp.Model.LevelModel.LevelModel;
import com.divinetechs.quizapp.Model.LevelModel.Result;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.Constant;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.divinetechs.quizapp.Webservice.AppAPI;
import com.divinetechs.quizapp.Webservice.BaseURL;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LevelSelection extends AppCompatActivity {

    private PrefManager prefManager;
    Map<String, String> map;

    private ShimmerFrameLayout shimmer;

    private LinearLayout lyToolbar, lyBack, lyAdView, lyFbAdView;
    private TextView txtToolbarTitle, txtBack;

    private RecyclerView rvLevels;
    private List<Result> levelList;
    private LevelSelectionAdapter levelSelectionAdapter;

    String ID;

    com.facebook.ads.AdView fbAdView = null;
    AdView mAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);
        PrefManager.forceRTLIfSupported(getWindow(), LevelSelection.this);

        init();
        AdInit();
        GetLevel();

        txtToolbarTitle.setText("" + getResources().getString(R.string.select_level));
        txtToolbarTitle.setTextColor(getResources().getColor(R.color.text_gray));
        txtBack.setBackgroundTintList(getResources().getColorStateList(R.color.text_gray));

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LevelSelection.this.finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(LevelSelection.this);
            map = new HashMap<>();
            map = Utility.GetMap(LevelSelection.this);

            Intent intent = getIntent();
            if (intent.hasExtra("catId")) {
                ID = intent.getStringExtra("catId");
                Log.e("cat_id ==>", "" + ID);
                Constant.cat_id = ID;
            }

            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            lyBack = findViewById(R.id.lyBack);
            txtBack = findViewById(R.id.txtBack);

            shimmer = findViewById(R.id.shimmer);
            lyAdView = findViewById(R.id.lyAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);

            rvLevels = findViewById(R.id.rvLevels);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            lyAdView.setVisibility(View.VISIBLE);
            Utility.Admob(LevelSelection.this, mAdView, prefManager.getValue("banner_adid"), lyAdView);
        } else {
            lyAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utility.FacebookBannerAd(LevelSelection.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    //get_lavel API call
    private void GetLevel() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<LevelModel> call = bookNPlayAPI.get_lavel("" + Constant.cat_id,
                "" + prefManager.getLoginId());
        call.enqueue(new Callback<LevelModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<LevelModel> call, @NonNull Response<LevelModel> response) {
                try {
                    Log.e("get_lavel status", "" + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {
                            levelList = new ArrayList<>();
                            levelList = response.body().getResult();
                            Log.e("levelList size", "" + levelList.size());

                            levelSelectionAdapter = new LevelSelectionAdapter(LevelSelection.this, levelList);
                            rvLevels.setLayoutManager(new GridLayoutManager(LevelSelection.this, 1,
                                    LinearLayoutManager.VERTICAL, false));
                            rvLevels.setHasFixedSize(true);
                            rvLevels.setAdapter(levelSelectionAdapter);
                            rvLevels.setItemAnimator(new DefaultItemAnimator());
                            levelSelectionAdapter.notifyDataSetChanged();
                        }

                    } else {
                        Log.e("get_lavel massage", "" + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("get_lavel API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(@NonNull Call<LevelModel> call, @NonNull Throwable t) {
                Log.e("get_lavel Failure", "That didn't work!!!" + t.getMessage());
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
        Utility.shimmerHide(shimmer);
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
    }

}