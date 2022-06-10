package com.divinetechs.quizapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.divinetechs.quizapp.Adapter.LevelResultAdapter;
import com.divinetechs.quizapp.BuildConfig;
import com.divinetechs.quizapp.Model.TodayLeaderBoardModel.Result;
import com.divinetechs.quizapp.Model.TodayLeaderBoardModel.TodayLeaderBoardModel;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.divinetechs.quizapp.Webservice.AppAPI;
import com.divinetechs.quizapp.Webservice.BaseURL;
import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
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

public class LevelResult extends AppCompatActivity implements View.OnClickListener {

    PrefManager prefManager;
    Map<String, String> map;

    ShimmerFrameLayout shimmer;

    private InterstitialAd mInterstitialAd = null;
    private com.facebook.ads.InterstitialAd fbInterstitialAd = null;
    private RewardedAd mRewardedAd = null;
    private RewardedVideoAd fbRewardedVideoAd = null;
    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    RecyclerView rvContestantResult;
    List<Result> todayList;
    LevelResultAdapter levelResultAdapter;

    RoundedImageView rivContestant;
    LinearLayout lyHome, lyAdView, lyFbAdView, lyShareResult, lyPlayNextLevel;

    TextView txtPlayNextLevel, txtPointEarn, txtTotalLevel, txtLevelNumber, txtTotalScore,
            txtCurrentRank, txtShareResult, txtGreetings;

    String levelID, currentLevel, totalLevel, TYPE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_result);
        PrefManager.forceRTLIfSupported(getWindow(), LevelResult.this);

        init();
        GetTodayLeaderBoard();

    }

    private void init() {
        try {
            Intent intent = getIntent();
            if (intent.hasExtra("levelID")) {
                levelID = intent.getStringExtra("levelID");
                currentLevel = intent.getStringExtra("currentLevel");
                totalLevel = intent.getStringExtra("TotalLevel");
                Log.e("Result levelID =>", "" + levelID);
                Log.e("Result totalLevel =>", "" + totalLevel);
                Log.e("Result currentLevel =>", "" + currentLevel);
            }

            prefManager = new PrefManager(LevelResult.this);
            map = new HashMap<>();
            map = Utility.GetMap(LevelResult.this);

            shimmer = findViewById(R.id.shimmer);
            lyHome = findViewById(R.id.lyHome);
            lyAdView = findViewById(R.id.lyAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            lyShareResult = findViewById(R.id.lyShareResult);
            lyPlayNextLevel = findViewById(R.id.lyPlayNextLevel);

            rvContestantResult = findViewById(R.id.rvContestantResult);
            rivContestant = findViewById(R.id.rivContestant);

            txtGreetings = findViewById(R.id.txtGreetings);
            txtPlayNextLevel = findViewById(R.id.txtPlayNextLevel);
            txtTotalLevel = findViewById(R.id.txtTotalLevel);
            txtLevelNumber = findViewById(R.id.txtLevelNumber);
            txtTotalScore = findViewById(R.id.txtTotalScore);
            txtPointEarn = findViewById(R.id.txtPointEarn);
            txtCurrentRank = findViewById(R.id.txtCurrentRank);
            txtShareResult = findViewById(R.id.txtShareResult);

            lyHome.setOnClickListener(this);
            txtPlayNextLevel.setOnClickListener(this);
            txtShareResult.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            lyAdView.setVisibility(View.VISIBLE);
            Utility.Admob(LevelResult.this, mAdView, prefManager.getValue("banner_adid"), lyAdView);
        } else {
            lyAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_ad", "" + prefManager.getValue("fb_banner_ad"));
        if (prefManager.getValue("fb_banner_ad").equalsIgnoreCase("yes")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utility.FacebookBannerAd(LevelResult.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
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

        Log.e("interstital_ad", "" + prefManager.getValue("interstital_ad"));
        if (prefManager.getValue("interstital_ad").equalsIgnoreCase("yes")) {
            mInterstitialAd = null;
            InterstitialAd();
        }

        Log.e("fb_interstiatial_ad", "" + prefManager.getValue("fb_interstiatial_ad"));
        if (prefManager.getValue("fb_interstiatial_ad").equalsIgnoreCase("yes")) {
            fbInterstitialAd = null;
            FacebookInterstitialAd();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtPlayNextLevel:
                ShowAdByClick("NEXTLEVEL");
                break;

            case R.id.txtShareResult:
                ShowAdByClick("SHARERESULT");
                break;

            case R.id.lyHome:
                ShowAdByClick("HOME");
                break;
        }
    }

    //getTodayLeaderBoard API
    private void GetTodayLeaderBoard() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<TodayLeaderBoardModel> call = bookNPlayAPI.getTodayLeaderBoard(levelID, "" + prefManager.getLoginId());
        call.enqueue(new Callback<TodayLeaderBoardModel>() {
            @SuppressLint({"LongLogTag", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<TodayLeaderBoardModel> call, @NonNull Response<TodayLeaderBoardModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("getTodayLeaderBoard : status", "" + response.body().getStatus());

                        todayList = new ArrayList<Result>();
                        todayList = response.body().getResult();
                        Log.e("todayList size ==>", "" + todayList.size());

                        if (response.body().getUser().getIsUnlock().equalsIgnoreCase("1")) {
                            txtGreetings.setText("" + getResources().getString(R.string.level_complete));
                        } else {
                            txtGreetings.setText("" + getResources().getString(R.string.level_incomplete));
                        }
                        txtLevelNumber.setText("" + currentLevel + " / ");
                        txtTotalLevel.setText("" + totalLevel);
                        txtCurrentRank.setText("" + response.body().getUser().getRank());
                        txtTotalScore.setText("" + String.format("%.0f",
                                Double.parseDouble("" + response.body().getUser().getTotalScore())));
                        txtPointEarn.setText("" + String.format("%.0f",
                                Double.parseDouble("" + response.body().getUser().getScore())) + "+ " + getResources().getString(R.string.point_earned));

                        if (!TextUtils.isEmpty(response.body().getUser().getProfileImg())) {
                            Picasso.get().load(response.body().getUser().getProfileImg())
                                    .into(rivContestant);
                        }

                        Result();

                    }
                } catch (Exception e) {
                    Log.e("getTodayLeaderBoard API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<TodayLeaderBoardModel> call, @NonNull Throwable t) {
                Log.e("getTodayLeaderBoard : Failure", "That didn't work!!!" + t.getMessage());
                Utility.shimmerHide(shimmer);
            }
        });
    }

    private void Result() {
        levelResultAdapter = new LevelResultAdapter(LevelResult.this, todayList,
                "" + prefManager.getLoginId());
        rvContestantResult.setLayoutManager(new GridLayoutManager(LevelResult.this, 1,
                LinearLayoutManager.VERTICAL, false));
        rvContestantResult.setHasFixedSize(true);
        rvContestantResult.setAdapter(levelResultAdapter);
        rvContestantResult.setItemAnimator(new DefaultItemAnimator());
        levelResultAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        AdInit();
    }

    @Override
    public void onPause() {
        Utility.shimmerHide(shimmer);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Log.e("interstital_ad", "" + prefManager.getValue("interstital_ad"));
        if (prefManager.getValue("interstital_ad").equalsIgnoreCase("yes")) {
            mInterstitialAd.show(LevelResult.this);
        } else {
            startActivity(new Intent(LevelResult.this, LevelSelection.class));
            finish();
        }
    }

    private void ShareOnShare() {
        String shareMessage = "\n\n" + getResources().getString(R.string.let_me_recommend_you_this_application) + "\n"
                + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "";

        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "" + getResources().getString(R.string.my_scorecard));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "\n" + getResources().getString(R.string.name) + " " + todayList.get(0).getName() +
                "\n" + getResources().getString(R.string.level_rank) + " " + todayList.get(0).getRank() +
                "\n" + getResources().getString(R.string.level_number) + " " + currentLevel +
                "\n" + getResources().getString(R.string.level_score) + " " + todayList.get(0).getScore() + shareMessage);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        try {
            startActivity(Intent.createChooser(shareIntent, "" + getResources().getString(R.string.share_with)));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e("ActivityNotF", "" + ex.getMessage());
        }
    }

    //Showing ad by TYPE
    private void ShowAdByClick(String Type) {
        TYPE = Type;
        Log.e("=>TYPE", "" + TYPE);

        if (prefManager.getValue("reward_ad").equalsIgnoreCase("yes")) {
            if (mRewardedAd != null) {
                mRewardedAd.show(LevelResult.this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.e("RewardItem amount =>", "" + rewardItem.getAmount());
                    }
                });
            } else {
                Log.e("mRewardedAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                    startActivity(new Intent(LevelResult.this, LevelSelection.class));
                    finish();
                } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                    ShareOnShare();
                } else if (TYPE.equalsIgnoreCase("HOME")) {
                    startActivity(new Intent(LevelResult.this, MainActivity.class));
                    finish();
                }
            }

        } else if (prefManager.getValue("fb_rewardvideo_status").equalsIgnoreCase("on")) {
            if (fbRewardedVideoAd != null && fbRewardedVideoAd.isAdLoaded()) {
                fbRewardedVideoAd.show();
            } else {
                Log.e("fbRewardedVideoAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                    startActivity(new Intent(LevelResult.this, LevelSelection.class));
                    finish();
                } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                    ShareOnShare();
                } else if (TYPE.equalsIgnoreCase("HOME")) {
                    startActivity(new Intent(LevelResult.this, MainActivity.class));
                    finish();
                }
            }

        } else if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
            if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
                fbInterstitialAd.show();
            } else {
                Log.e("fbInterstitialAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                    startActivity(new Intent(LevelResult.this, LevelSelection.class));
                    finish();
                } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                    ShareOnShare();
                } else if (TYPE.equalsIgnoreCase("HOME")) {
                    startActivity(new Intent(LevelResult.this, MainActivity.class));
                    finish();
                }
            }

        } else {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(LevelResult.this);
            } else {
                Log.e("mInterstitialAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                    startActivity(new Intent(LevelResult.this, LevelSelection.class));
                    finish();
                } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                    ShareOnShare();
                } else if (TYPE.equalsIgnoreCase("HOME")) {
                    startActivity(new Intent(LevelResult.this, MainActivity.class));
                    finish();
                }
            }
        }
    }

    private void InterstitialAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Log.e("TAG", "InterstitialAd failed to show. " + adError.toString());
                            mInterstitialAd = null;
                            if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                                startActivity(new Intent(LevelResult.this, LevelSelection.class));
                                finish();
                            } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                                ShareOnShare();
                            } else if (TYPE.equalsIgnoreCase("HOME")) {
                                startActivity(new Intent(LevelResult.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            Log.e("TAG", "InterstitialAd was shown. ");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            Log.e("TAG", "InterstitialAd was dismissed. ");
                            mInterstitialAd = null;
                            if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                                startActivity(new Intent(LevelResult.this, LevelSelection.class));
                                finish();
                            } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                                ShareOnShare();
                            } else if (TYPE.equalsIgnoreCase("HOME")) {
                                startActivity(new Intent(LevelResult.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.e("TAG", "InterstitialAd onAdImpression. ");
                        }
                    };

            mInterstitialAd.load(this, "" + prefManager.getValue("interstital_adid"),
                    adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            Log.e("onAdLoaded", "");
                            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.e("loadAdError", "" + loadAdError.getMessage());
                            mInterstitialAd = null;
                        }
                    });
        } catch (Exception e) {
            Log.e("", "InterstitialAd Exception => " + e);
        }
    }

    private void FacebookInterstitialAd() {
        try {
            fbInterstitialAd = new com.facebook.ads.InterstitialAd(this,
                    "CAROUSEL_IMG_SQUARE_APP_INSTALL#" + prefManager.getValue("fb_interstiatial_id"));
            fbInterstitialAd.loadAd(fbInterstitialAd.buildLoadAdConfig()
                    .withAdListener(new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {
                            Log.e("fbInterstitialAd", "fb ad displayed.");
                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            fbInterstitialAd = null;
                            if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                                startActivity(new Intent(LevelResult.this, LevelSelection.class));
                                finish();
                            } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                                ShareOnShare();
                            } else if (TYPE.equalsIgnoreCase("HOME")) {
                                startActivity(new Intent(LevelResult.this, MainActivity.class));
                                finish();
                            }
                            Log.e("fbInterstitialAd", "fb ad dismissed.");
                        }

                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            Log.e("fbInterstitialAd", "fb ad failed to load : " + adError.getErrorMessage());
                            fbInterstitialAd = null;
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            Log.e("fbInterstitialAd", "fb ad is loaded and ready to be displayed!");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {
                            Log.e("fbInterstitialAd", "fb ad clicked!");
                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {
                            Log.e("fbInterstitialAd", "fb ad impression logged!");
                        }
                    })
                    .build());
        } catch (Exception e) {
            Log.e("fbInterstitialAd", "Exception =>" + e);
        }
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
                            if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                                startActivity(new Intent(LevelResult.this, LevelSelection.class));
                                finish();
                            } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                                ShareOnShare();
                            } else if (TYPE.equalsIgnoreCase("HOME")) {
                                startActivity(new Intent(LevelResult.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            Log.e("TAG", "Ad was shown.");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            mRewardedAd = null;
                            Log.e("TAG", "Ad was dismissed.");
                            if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                                startActivity(new Intent(LevelResult.this, LevelSelection.class));
                                finish();
                            } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                                ShareOnShare();
                            } else if (TYPE.equalsIgnoreCase("HOME")) {
                                startActivity(new Intent(LevelResult.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.e("TAG", "onAdImpression.");
                        }
                    };

            mRewardedAd.load(LevelResult.this, "" + prefManager.getValue("reward_adid"),
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
                            mRewardedAd = null;
                        }
                    });

        } catch (Exception e) {
            Log.e("RewardAd Exception =>", "" + e);
        }
    }

    private void FacebookRewardAd() {
        try {
            fbRewardedVideoAd = new RewardedVideoAd(LevelResult.this,
                    "VID_HD_16_9_15S_APP_INSTALL#" + prefManager.getValue("fb_rewardvideo_id"));

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
                    if (TYPE.equalsIgnoreCase("NEXTLEVEL")) {
                        startActivity(new Intent(LevelResult.this, LevelSelection.class));
                        finish();
                    } else if (TYPE.equalsIgnoreCase("SHARERESULT")) {
                        ShareOnShare();
                    } else if (TYPE.equalsIgnoreCase("HOME")) {
                        startActivity(new Intent(LevelResult.this, MainActivity.class));
                        finish();
                    }
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
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        if (mRewardedAd != null) {
            mRewardedAd = null;
        }
        if (fbRewardedVideoAd != null) {
            fbRewardedVideoAd.destroy();
            fbRewardedVideoAd = null;
        }
        if (fbInterstitialAd != null) {
            fbInterstitialAd.destroy();
            fbInterstitialAd = null;
        }
    }

}