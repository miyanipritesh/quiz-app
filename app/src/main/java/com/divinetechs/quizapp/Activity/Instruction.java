package com.divinetechs.quizapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;
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

import java.util.ArrayList;
import java.util.List;

public class Instruction extends AppCompatActivity {

    PrefManager prefManager;

    TextView txtPolicyDesc, txtAppname, txtToolbarTitle, txtBack;
    LinearLayout lyBack, lyToolbar, lyAdView, lyFbAdView;

    TemplateView nativeTemplate = null;
    com.facebook.ads.AdView fbAdView = null;
    AdView mAdView = null;
    NativeBannerAd fbNativeBannerAd = null;
    NativeAdLayout fbNativeTemplate = null;

    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);
        PrefManager.forceRTLIfSupported(getWindow(), Instruction.this);
        Utility.screenCapOff(Instruction.this);

        init();
        AdInit();

        txtToolbarTitle.setTextColor(getResources().getColor(R.color.text_blue));
        txtBack.setBackgroundTintList(getResources().getColorStateList(R.color.text_blue));
        txtAppname.setText("" + Html.fromHtml(prefManager.getValue("app_name")));

        if (type.equalsIgnoreCase("Instruction")) {
            txtPolicyDesc.setText("" + Html.fromHtml(prefManager.getValue("instrucation")));
            txtToolbarTitle.setText("" + getString(R.string.Instruction));
        } else {
            txtPolicyDesc.setText("" + Html.fromHtml(prefManager.getValue("privacy_policy")));
            txtToolbarTitle.setText("" + getString(R.string.privacy_policy));
        }

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Instruction.this.finish();
            }
        });

    }

    private void init() {
        try {
            Intent intent = getIntent();
            if (intent.hasExtra("type")) {
                type = intent.getStringExtra("type");
                Log.e("type ==>", "" + type);
            }

            prefManager = new PrefManager(Instruction.this);

            lyAdView = findViewById(R.id.lyAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            nativeTemplate = findViewById(R.id.nativeTemplate);
            fbNativeTemplate = findViewById(R.id.fbNativeTemplate);
            lyBack = findViewById(R.id.lyBack);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtAppname = findViewById(R.id.txtAppname);
            txtPolicyDesc = findViewById(R.id.txtPolicyDesc);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            lyAdView.setVisibility(View.VISIBLE);
            Utility.Admob(Instruction.this, mAdView, prefManager.getValue("banner_adid"), lyAdView);
        } else {
            lyAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utility.FacebookBannerAd(Instruction.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }

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

    private void NativeAds() {
        try {
            Log.e("loginID =>", "" + prefManager.getLoginId());
            AdLoader adLoader = new AdLoader.Builder(Instruction.this, "" + prefManager.getValue("native_adid"))
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
            fbNativeBannerAd = new NativeBannerAd(Instruction.this,
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
                    Utility.inflateFbSmallNativeAd(Instruction.this, fbNativeBannerAd, fbNativeTemplate);
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
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
    }

}