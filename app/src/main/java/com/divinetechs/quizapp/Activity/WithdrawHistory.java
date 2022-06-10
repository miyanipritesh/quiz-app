package com.divinetechs.quizapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.divinetechs.quizapp.Adapter.WithdrawHistoryAdapter;
import com.divinetechs.quizapp.Model.ProfileModel.ProfileModel;
import com.divinetechs.quizapp.Model.WithdrawalModel.Result;
import com.divinetechs.quizapp.Model.WithdrawalModel.WithdrawalModel;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.divinetechs.quizapp.Webservice.AppAPI;
import com.divinetechs.quizapp.Webservice.BaseURL;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WithdrawHistory extends AppCompatActivity {

    PrefManager prefManager;
    Map<String, String> map;

    ShimmerFrameLayout shimmer;

    TextView txtPoints, txtMinPoints, txtPointChart, txtBack, txtToolbarTitle;

    LinearLayout lyWithdraw, lyToolbar, lyNoData, lyBack;

    RecyclerView rvHistory;
    WithdrawHistoryAdapter withdrawHistoryAdapter;
    List<Result> historyList;
    List<com.divinetechs.quizapp.Model.ProfileModel.Result> profileList;

    TemplateView nativeTemplate = null;
    NativeBannerAd fbNativeBannerAd = null;
    NativeAdLayout fbNativeTemplate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_history);
        PrefManager.forceRTLIfSupported(getWindow(), WithdrawHistory.this);

        init();
        AdInit();
        Profile();


        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lyWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("=>min_earning_point", "" + prefManager.getValue("min_earning_point"));
                if (Integer.parseInt(profileList.get(0).getTotalPoints()) >=
                        Integer.parseInt("" + prefManager.getValue("min_earning_point"))) {
                    startActivity(new Intent(WithdrawHistory.this, WithdrawRequest.class));

                } else {
                    Toasty.info(WithdrawHistory.this,
                            "" + getResources().getString(R.string.withdraw_request_warning),
                            Toasty.LENGTH_LONG).show();
                }
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(WithdrawHistory.this);
            map = new HashMap<>();
            map = Utility.GetMap(WithdrawHistory.this);

            shimmer = findViewById(R.id.shimmer);

            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            lyBack = findViewById(R.id.lyBack);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("" + getResources().getString(R.string.withdrawal_history));
            txtToolbarTitle.setTextColor(getResources().getColor(R.color.text_gray));
            txtBack.setBackgroundTintList(getResources().getColorStateList(R.color.text_gray));

            lyWithdraw = findViewById(R.id.lyWithdraw);
            nativeTemplate = findViewById(R.id.nativeTemplate);
            fbNativeTemplate = findViewById(R.id.fbNativeTemplate);

            txtPoints = findViewById(R.id.txtPoints);
            txtMinPoints = findViewById(R.id.txtMinPoints);
            txtPointChart = findViewById(R.id.txtPointChart);

            rvHistory = findViewById(R.id.rvHistory);
            lyNoData = findViewById(R.id.lyNoData);

            txtMinPoints.setText(getResources().getString(R.string.minimum)
                    + " " + prefManager.getValue("min_earning_point")
                    + " " + getResources().getString(R.string.minimum_points_required));

            txtPointChart.setText(" " + prefManager.getValue("earning_point")
                    + " Points = " + prefManager.getValue("earning_amount") + " " + prefManager.getValue("currency"));

        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("native_ad", "" + prefManager.getValue("native_ad"));
        if (prefManager.getValue("native_ad").equalsIgnoreCase("yes")) {
            nativeTemplate.setVisibility(View.VISIBLE);
            NativeAds();
        } else {
            nativeTemplate.setVisibility(View.GONE);
        }

        Log.e("fb_native_status", "" + prefManager.getValue("fb_native_status"));
        if (prefManager.getValue("fb_native_status").equalsIgnoreCase("on")) {
            fbNativeTemplate.setVisibility(View.VISIBLE);
            FacebookNativeBannerAd();
        } else {
            fbNativeTemplate.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WithdrawList();
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
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("profile API call : status", "" + response.body().getStatus());
                        if (response.body().getResult().size() > 0) {
                            profileList = new ArrayList<com.divinetechs.quizapp.Model.ProfileModel.Result>();
                            profileList = response.body().getResult();

                            txtPoints.setText("" + profileList.get(0).getTotalPoints());

                        }
                    } else {
                        Log.e("profile massage", "" + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("profile API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<ProfileModel> call, @NonNull Throwable t) {
                Utility.shimmerHide(shimmer);
                Log.e("profile API call : Failure", "That didn't work!!!" + t.getMessage());
            }
        });
    }

    //withdrawal_list API call
    private void WithdrawList() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<WithdrawalModel> call = bookNPlayAPI.withdrawal_list("" + prefManager.getLoginId());
        call.enqueue(new Callback<WithdrawalModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<WithdrawalModel> call, @NonNull Response<WithdrawalModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("withdrawal_list API call : status", "" + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            historyList = new ArrayList<Result>();
                            historyList = response.body().getResult();

                            withdrawHistoryAdapter = new WithdrawHistoryAdapter(WithdrawHistory.this,
                                    historyList);
                            rvHistory.setLayoutManager(new GridLayoutManager(WithdrawHistory.this, 1,
                                    LinearLayoutManager.VERTICAL, false));
                            rvHistory.setItemAnimator(new DefaultItemAnimator());
                            rvHistory.setAdapter(withdrawHistoryAdapter);
                            withdrawHistoryAdapter.notifyDataSetChanged();

                            lyNoData.setVisibility(View.GONE);
                            rvHistory.setVisibility(View.VISIBLE);
                        } else {
                            lyNoData.setVisibility(View.VISIBLE);
                            rvHistory.setVisibility(View.GONE);
                        }

                    } else {
                        Log.e("withdrawal_list massage", "" + response.body().getMessage());
                        lyNoData.setVisibility(View.VISIBLE);
                        rvHistory.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("withdrawal_list API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<WithdrawalModel> call, @NonNull Throwable t) {
                Utility.shimmerHide(shimmer);
                lyNoData.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
                Log.e("withdrawal_list API call : Failure", "That didn't work!!!" + t.getMessage());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Utility.shimmerHide(shimmer);
    }

    private void NativeAds() {
        try {
            Log.e("loginID =>", "" + prefManager.getLoginId());
            AdLoader adLoader = new AdLoader.Builder(WithdrawHistory.this, "" + prefManager.getValue("native_adid"))
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        private ColorDrawable background;

                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            Log.e("Advertiser =>", "" + nativeAd.getAdvertiser());
                            NativeTemplateStyle styles = new
                                    NativeTemplateStyle.Builder().withMainBackgroundColor(background).build();

                            nativeTemplate.setStyles(styles);
                            nativeTemplate.setNativeAd(nativeAd);
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            // Handle the failure by logging, altering the UI, and so on.
                            Log.e("NativeAd adError=>", "" + adError);
                        }

                        @Override
                        public void onAdClicked() {
                            // Log the click event or other custom behavior.
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder().build())
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
            Log.e("NativeAd Exception=>", "" + e);
        }
    }

    private void FacebookNativeBannerAd() {
        try {
            fbNativeBannerAd = new NativeBannerAd(WithdrawHistory.this,
                    "IMG_16_9_APP_INSTALL#" + prefManager.getValue("fb_native_id"));

            fbNativeBannerAd.loadAd(fbNativeBannerAd.buildLoadAdConfig().withAdListener(new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                    // Native ad finished downloading all assets
                    Log.e("TAG", "fbNative ad finished downloading all assets.");
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    // Native ad failed to load
                    Log.e("TAG", "fbNative ad failed to load: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    // Native ad is loaded and ready to be displayed
                    Log.d("TAG", "fbNative ad is loaded and ready to be displayed!");
                    if (fbNativeBannerAd == null || fbNativeBannerAd != ad) {
                        return;
                    }
                    // Inflate Native Banner Ad into Container
                    Utility.inflateFbSmallNativeAd(WithdrawHistory.this, fbNativeBannerAd, fbNativeTemplate);
                }

                @Override
                public void onAdClicked(Ad ad) {
                    // Native ad clicked
                    Log.d("TAG", "fbNative ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    // Native ad impression
                    Log.d("TAG", "fbNative ad impression logged!");
                }
            }).build());

        } catch (Exception e) {
            Log.e("AdView Exception=>", "" + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.shimmerHide(shimmer);
        Log.e("onDestroy", "Ads Destroyed");
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
    }

}