package com.divinetechs.quizapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.divinetechs.quizapp.Model.ProfileModel.ProfileModel;
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
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Statistics extends AppCompatActivity {

    PrefManager prefManager;
    Map<String, String> map;

    ShimmerFrameLayout shimmer;
    LinearLayout lyBack;

    RoundedImageView rivUser;

    TextView txtUserName, txtRank, txtCoins, txtScore, txtTotalQue, txtCorrectAns,
            txtIncorrectAns;

    TemplateView nativeTemplate = null;
    NativeBannerAd fbNativeBannerAd = null;
    NativeAdLayout fbNativeTemplate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        PrefManager.forceRTLIfSupported(getWindow(), Statistics.this);
        Utility.screenCapOff(Statistics.this);

        init();
        AdInit();
        GetProfile();

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(Statistics.this);
            map = new HashMap<>();
            map = Utility.GetMap(Statistics.this);

            shimmer = findViewById(R.id.shimmer);
            lyBack = findViewById(R.id.lyBack);
            nativeTemplate = findViewById(R.id.nativeTemplate);
            fbNativeTemplate = findViewById(R.id.fbNativeTemplate);

            rivUser = findViewById(R.id.rivUser);
            txtUserName = findViewById(R.id.txtUserName);
            txtRank = findViewById(R.id.txtRank);
            txtCoins = findViewById(R.id.txtCoins);
            txtScore = findViewById(R.id.txtScore);
            txtTotalQue = findViewById(R.id.txtTotalQue);
            txtCorrectAns = findViewById(R.id.txtCorrectAns);
            txtIncorrectAns = findViewById(R.id.txtIncorrectAns);
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

    //profile API call
    private void GetProfile() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<ProfileModel> call = bookNPlayAPI.profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<ProfileModel> call, @NonNull Response<ProfileModel> response) {
                try {
                    if (response.body().getStatus() == 200) {
                        Log.e("profile API call : status", "" + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            txtUserName.setText(getResources().getString(R.string.hello) + " " + response.body().getResult().get(0).getFirstName());
                            txtRank.setText("" + response.body().getResult().get(0).getRank());
                            txtCoins.setText("0");
                            txtScore.setText("" + String.format("%.0f",
                                    Double.parseDouble(response.body().getResult().get(0).getTotalScore())));

                            txtTotalQue.setText("" + response.body().getResult().get(0).getQuestionsAttended());
                            txtCorrectAns.setText("" + response.body().getResult().get(0).getCorrectAnswers());
                            txtIncorrectAns.setText("" + (Integer.parseInt(response.body().getResult().get(0).getQuestionsAttended())
                                    - Integer.parseInt(response.body().getResult().get(0).getCorrectAnswers())));

                            if (!response.body().getResult().get(0).getProfileImg().equalsIgnoreCase("")) {
                                Picasso.get().load(response.body().getResult().get(0).getProfileImg())
                                        .into(rivUser);
                            }
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

    @Override
    public void onPause() {
        Utility.shimmerHide(shimmer);
        super.onPause();
    }

    private void NativeAds() {
        try {
            AdLoader adLoader = new AdLoader.Builder(Statistics.this, "" + prefManager.getValue("native_adid"))
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
            fbNativeBannerAd = new NativeBannerAd(Statistics.this,
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
                    Utility.inflateFbSmallNativeAd(Statistics.this, fbNativeBannerAd, fbNativeTemplate);
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
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
    }

}