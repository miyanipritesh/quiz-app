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

import com.divinetechs.quizapp.Adapter.RecentQuizAdapter;
import com.divinetechs.quizapp.Model.ProfileModel.ProfileModel;
import com.divinetechs.quizapp.Model.RecentQuizModel.RecentQuizModel;
import com.divinetechs.quizapp.Model.RecentQuizModel.Result;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.divinetechs.quizapp.Webservice.AppAPI;
import com.divinetechs.quizapp.Webservice.BaseURL;
import com.facebook.ads.Ad;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfile extends AppCompatActivity implements View.OnClickListener {

    PrefManager prefManager;
    Map<String, String> map;

    ShimmerFrameLayout shimmer;

    LinearLayout lyBack, lyNoData, lyAdView, lyFbAdView;

    RewardedAd mRewardedAd = null;
    AdView mAdView = null;
    com.facebook.ads.AdView fbAdView = null;
    RewardedVideoAd fbRewardedVideoAd = null;

    TextView txtUserName, txtCityName, txtLeaderboard, txtStatistics, txtWithdrawal;

    RoundedImageView rivUser;

    RecyclerView rvRecentmatch;
    RecentQuizAdapter recentQuizAdapter;
    List<Result> recentQuizList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        PrefManager.forceRTLIfSupported(getWindow(), UserProfile.this);
        Utility.screenCapOff(UserProfile.this);

        init();
        Profile();

    }

    private void init() {
        try {
            prefManager = new PrefManager(UserProfile.this);
            map = new HashMap<>();
            map = Utility.GetMap(UserProfile.this);

            shimmer = findViewById(R.id.shimmer);

            lyBack = findViewById(R.id.lyBack);
            lyNoData = findViewById(R.id.lyNoData);
            lyAdView = findViewById(R.id.lyAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);

            rivUser = findViewById(R.id.rivUser);
            txtUserName = findViewById(R.id.txtUserName);
            txtCityName = findViewById(R.id.txtCityName);
            txtLeaderboard = findViewById(R.id.txtLeaderboard);
            txtStatistics = findViewById(R.id.txtStatistics);
            txtWithdrawal = findViewById(R.id.txtWithdrawal);

            rvRecentmatch = findViewById(R.id.rvRecentmatch);

            lyBack.setOnClickListener(this);
            txtStatistics.setOnClickListener(this);
            txtWithdrawal.setOnClickListener(this);
            txtLeaderboard.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            lyAdView.setVisibility(View.VISIBLE);
            Utility.Admob(UserProfile.this, mAdView, prefManager.getValue("banner_adid"), lyAdView);
        } else {
            lyAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utility.FacebookBannerAd(UserProfile.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }

        Log.e("reward_ad", "" + prefManager.getValue("reward_ad"));
        if (prefManager.getValue("reward_ad").equalsIgnoreCase("yes")) {
            mRewardedAd = null;
            RewardedVideoAd();
        }

        Log.e("fb_rewardvideo_status", "" + prefManager.getValue("fb_rewardvideo_status"));
        if (prefManager.getValue("fb_rewardvideo_status").equalsIgnoreCase("on")) {
            fbRewardedVideoAd = null;
            FacebookRewardAd();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyBack:
                finish();
                break;

            case R.id.txtLeaderboard:
                startActivity(new Intent(UserProfile.this, UserLeaderBoard.class));
                break;

            case R.id.txtStatistics:
                startActivity(new Intent(UserProfile.this, Statistics.class));
                break;

            case R.id.txtWithdrawal:
                if (prefManager.getValue("fb_rewardvideo_status").equalsIgnoreCase("yes")) {
                    if (fbRewardedVideoAd != null && fbRewardedVideoAd.isAdLoaded()) {
                        fbRewardedVideoAd.show();
                    } else {
                        startActivity(new Intent(UserProfile.this, WithdrawHistory.class));
                    }
                } else {
                    if (mRewardedAd != null) {
                        mRewardedAd.show(UserProfile.this, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                Log.e("RewardItem amount =>", "" + rewardItem.getAmount());
                            }
                        });
                    } else {
                        startActivity(new Intent(UserProfile.this, WithdrawHistory.class));
                    }
                }
                break;
        }
    }

    //profile API call
    private void Profile() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<ProfileModel> call = bookNPlayAPI.profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<ProfileModel> call, @NonNull Response<ProfileModel> response) {
                try {
                    if (response.code() == 200 &&
                            response.body().getStatus() == 200) {
                        Log.e("profile API call : status", "" + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            txtUserName.setText("" + response.body().getResult().get(0).getFirstName());
                            txtCityName.setText("" + response.body().getResult().get(0).getBiodata());

                            if (!response.body().getResult().get(0).getProfileImg().equalsIgnoreCase("")) {
                                Picasso.get().load(response.body().getResult().get(0).getProfileImg())
                                        .into(rivUser);
                            }

                            recentQuiz();
                        }

                    } else {
                        Log.e("profile massage", "" + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("profile API error==>", "" + e);
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<ProfileModel> call, @NonNull Throwable t) {
                Utility.shimmerHide(shimmer);
                Log.e("profile API call : Failure", "That didn't work!!!" + t.getMessage());
            }
        });
    }

    private void recentQuiz() {
        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<RecentQuizModel> call = bookNPlayAPI.RecentQuizByUser("" + prefManager.getLoginId());
        call.enqueue(new Callback<RecentQuizModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<RecentQuizModel> call, @NonNull Response<RecentQuizModel> response) {
                try {
                    if (response.body().getStatus() == 200) {
                        Log.e("RecentQuizByUser API call : status", "" + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            recentQuizList = new ArrayList<Result>();
                            recentQuizList = response.body().getResult();
                            Log.e("recentQuizList size ==>", "" + recentQuizList.size());

                            recentQuizAdapter = new RecentQuizAdapter(UserProfile.this, recentQuizList);
                            rvRecentmatch.setLayoutManager(new GridLayoutManager(UserProfile.this, 1,
                                    LinearLayoutManager.VERTICAL, false));
                            rvRecentmatch.setHasFixedSize(true);
                            rvRecentmatch.setAdapter(recentQuizAdapter);
                            rvRecentmatch.setItemAnimator(new DefaultItemAnimator());
                            recentQuizAdapter.notifyDataSetChanged();

                            lyNoData.setVisibility(View.GONE);
                            rvRecentmatch.setVisibility(View.VISIBLE);
                        } else {
                            lyNoData.setVisibility(View.VISIBLE);
                            rvRecentmatch.setVisibility(View.GONE);
                        }

                    } else {
                        Log.e("RecentQuizByUser massage", "" + response.body().getMessage());
                        lyNoData.setVisibility(View.VISIBLE);
                        rvRecentmatch.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("RecentQuizByUser API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<RecentQuizModel> call, @NonNull Throwable t) {
                Utility.shimmerHide(shimmer);
                lyNoData.setVisibility(View.VISIBLE);
                rvRecentmatch.setVisibility(View.GONE);
                Log.e("RecentQuizByUser API call : Failure", "That didn't work!!!" + t.getMessage());
            }
        });
    }

    @Override
    public void onPause() {
        Utility.shimmerHide(shimmer);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        AdInit();
    }

    private void RewardedVideoAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Log.e("Ad failed to show.", "" + adError.toString());
                            mRewardedAd = null;
                            startActivity(new Intent(UserProfile.this, WithdrawHistory.class));
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            Log.e("TAG", "Ad was shown.");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            startActivity(new Intent(UserProfile.this, WithdrawHistory.class));
                            mRewardedAd = null;
                            Log.e("TAG", "Ad was dismissed.");
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }
                    };

            mRewardedAd.load(UserProfile.this, "" + prefManager.getValue("reward_adid"),
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            super.onAdLoaded(rewardedAd);
                            mRewardedAd = rewardedAd;
                            mRewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                        }
                    });

        } catch (Exception e) {
            Log.e("RewardAd Exception =>", "" + e);
        }
    }

    private void FacebookRewardAd() {
        try {
            fbRewardedVideoAd = new RewardedVideoAd(UserProfile.this,
                    "VID_HD_9_16_39S_LINK#" + prefManager.getValue("fb_rewardvideo_id"));

            fbRewardedVideoAd.loadAd(fbRewardedVideoAd.buildLoadAdConfig().withAdListener(new RewardedVideoAdListener() {
                @Override
                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                    Log.e("TAG", "Rewarded video adError => " + adError.getErrorMessage());
                    fbRewardedVideoAd.destroy();
                    fbRewardedVideoAd = null;
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e("TAG", "Rewarded video ad is loaded and ready to be displayed!");
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.e("TAG", "Rewarded video ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.e("TAG", "Rewarded video ad impression logged!");
                }

                @Override
                public void onRewardedVideoCompleted() {
                    Log.e("TAG", "Rewarded video completed!");
                }

                @Override
                public void onRewardedVideoClosed() {
                    Log.e("TAG", "Rewarded video ad closed!");
                    fbRewardedVideoAd.destroy();
                    fbRewardedVideoAd = null;
                    startActivity(new Intent(UserProfile.this, WithdrawHistory.class));
                }
            }).build());

        } catch (Exception e) {
            Log.e("AdView Exception=>", "" + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "Ads Destroyed");
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
        if (mRewardedAd != null) {
            mRewardedAd = null;
        }
        if (fbRewardedVideoAd != null) {
            fbRewardedVideoAd.destroy();
            fbRewardedVideoAd = null;
        }
    }

}