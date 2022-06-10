package com.divinetechs.quizapp.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.divinetechs.quizapp.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAd;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class Utility {

    Context context;
    public static ProgressDialog pDialog;

    public Utility(Context context) {
        this.context = context;
    }

    //Facebook Small NativeAd layout
    public static void inflateFbSmallNativeAd(Activity activity, NativeBannerAd nativeBannerAd, NativeAdLayout nativeTemplate) {
        nativeBannerAd.unregisterView();

        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        View adView = LayoutInflater.from(activity).inflate(R.layout.fbnative_s_adview,
                nativeTemplate, false);
        nativeTemplate.addView(adView);

        // Add the AdChoices icon
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.adChoicesContainer);
        AdOptionsView adOptionsView = new AdOptionsView(activity, nativeBannerAd, nativeTemplate);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        TextView txtNativeTitle = adView.findViewById(R.id.txtNativeTitle);
        TextView txtNativeAdSocialContext = adView.findViewById(R.id.txtNativeAdSocialContext);
        TextView nativeAdSponsoredLabel = adView.findViewById(R.id.nativeAdSponsoredLabel);
        MediaView nativeMediaView = adView.findViewById(R.id.nativeMediaView);
        Button nativeAdCallToAction = adView.findViewById(R.id.nativeAdCallToAction);

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        txtNativeTitle.setText(nativeBannerAd.getAdvertiserName());
        txtNativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        nativeAdSponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(txtNativeTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(adView, nativeMediaView, clickableViews);
    }

    //Facebook Large NativeAd layout
    public static void inflateFbLargeNativeAd(Activity activity, NativeAd nativeAd, NativeAdLayout nativeTemplate) {

        nativeAd.unregisterView();
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        View adView = LayoutInflater.from(activity).inflate(R.layout.fbnative_l_adview, nativeTemplate, false);
        nativeTemplate.addView(adView);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = adView.findViewById(R.id.adChoicesContainer);
        AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeTemplate);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = adView.findViewById(R.id.nativeAdIcon);
        TextView txtNativeTitle = adView.findViewById(R.id.txtNativeTitle);
        MediaView nativeMediaView = adView.findViewById(R.id.nativeMediaView);
        TextView txtNativeAdSocialContext = adView.findViewById(R.id.txtNativeAdSocialContext);
        TextView txtNativeAdBody = adView.findViewById(R.id.txtNativeAdBody);
        TextView nativeAdSponsoredLabel = adView.findViewById(R.id.nativeAdSponsoredLabel);
        Button nativeAdCallToAction = adView.findViewById(R.id.nativeAdCallToAction);

        // Set the Text.
        txtNativeTitle.setText(nativeAd.getAdvertiserName());
        txtNativeAdBody.setText(nativeAd.getAdBodyText());
        txtNativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdSponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(txtNativeTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(adView, nativeMediaView, nativeAdIcon, clickableViews);
    }

    //Admob BannerAds
    public static void Admob(Activity activity, AdView mAdView, String bannerAdId, LinearLayout lyAdView) {
        try {
            mAdView = new AdView(activity);
            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.setAdUnitId("" + bannerAdId);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e("onAdFailedToLoad =>", "" + loadAdError.toString());
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }
            });
            mAdView.loadAd(adRequest);
            lyAdView.addView(mAdView);
        } catch (Exception e) {
            Log.e("Admob BannerAds", "Exception => " + e.getMessage());
        }
    }

    //Facebook BannerAds
    public static void FacebookBannerAd(Activity activity, com.facebook.ads.AdView fbAdView, String fbPlacementId, LinearLayout lyFbAdView) {
        try {
            fbAdView = new com.facebook.ads.AdView(activity, "IMG_16_9_APP_INSTALL#" + fbPlacementId,
                    com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            fbAdView.loadAd(fbAdView.buildLoadAdConfig().withAdListener(new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                    Log.e("fb Banner", "Error=> " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e("fb Banner", "Loaded=> " + ad.getPlacementId());
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.e("fb Banner", "AdClick=> " + ad.getPlacementId());
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.e("fb Banner", "LoggingImpression=> " + ad.getPlacementId());
                }
            }).build());

            lyFbAdView.addView(fbAdView);
        } catch (Exception e) {
            Log.e("Facebook BannerAds", "Exception => " + e.getMessage());
        }
    }


    //DateFormation :
    public static String DateFormat(String date) {
        String finaldate = "";
        try {
            @SuppressLint("SimpleDateFormat")
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date input = inputFormat.parse(date);
            DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            assert input != null;
            finaldate = outputFormat.format(input);
        } catch (Exception e) {
            Log.e("DateFormate", "Exception => " + e);
        }

        return finaldate;
    }

    //DateFormation :
    public static String DateFormat2(String date) {
        String finaldate = "";
        try {
            @SuppressLint("SimpleDateFormat")
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date input = inputFormat.parse(date);
            DateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            assert input != null;
            finaldate = outputFormat.format(input);
        } catch (Exception e) {
            Log.e("DateFormate2", "Exception => " + e);
        }

        return finaldate;
    }

    public static String covertTimeToText(String dataDate) {
        String convTime = null;
        String prefix = "";
        String suffix = "ago";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            Log.e("==>pastTime", "" + pasTime.getTime());
            Log.e("==>nowTime", "" + nowTime.getTime());

            long dateDiff = nowTime.getTime() - pasTime.getTime();
            Log.e("==>dateDiff", "" + (dateDiff / 1000));

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            Log.e("==>second", "" + (second));
            Log.e("==>minute", "" + (minute));
            Log.e("==>hour", "" + (hour));
            Log.e("==>day", "" + (day));

            if (second < 60 && second > 0) {
                if (second == 1) {
                    convTime = second + " second " + suffix;
                } else {
                    convTime = second + " seconds " + suffix;
                }
            } else if (minute < 60 && minute > 0) {
                if (minute == 1) {
                    convTime = minute + " minute " + suffix;
                } else {
                    convTime = minute + " minutes " + suffix;
                }
            } else if (hour < 24 && hour > 0) {
                if (hour == 1) {
                    convTime = hour + " hour " + suffix;
                } else {
                    convTime = hour + " hours " + suffix;
                }
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + " years " + suffix;
                } else if (day > 30) {
                    convTime = (day / 30) + " months " + suffix;
                } else {
                    convTime = (day / 7) + " week " + suffix;
                }
            } else if (day < 7) {
                if (day == 1) {
                    convTime = day + " day " + suffix;
                } else {
                    convTime = day + " days " + suffix;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvTimeE", "" + e.getMessage());
        }

        return convTime;
    }

    public static void shimmerShow(ShimmerFrameLayout shimmer) {
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }

    public static void shimmerHide(ShimmerFrameLayout shimmer) {
        shimmer.stopShimmer();
        shimmer.hideShimmer();
        shimmer.setVisibility(View.GONE);
    }

    public static void ProgressBarShow(Context mContext) {
        if (pDialog == null) {
            pDialog = new ProgressDialog(mContext, R.style.AlertDialogDanger);
            pDialog.setMessage("" + mContext.getResources().getString(R.string.please_wait));
            pDialog.setCanceledOnTouchOutside(false);
        }
        pDialog.show();
    }

    public static void ProgressbarHide() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    public static Map<String, String> GetMap(Context context) {
        PrefManager prefManager = new PrefManager(context);

        Map<String, String> map = new HashMap<>();
        map.put("general_token", prefManager.getValue("general_token"));
        map.put("key", prefManager.getValue("key"));
        map.put("device_token", prefManager.getValue("device_token"));
        map.put("auth_token", prefManager.getValue_return("auth_token"));

        return map;
    }

    //make fullscreen
    public static void fullScreen(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    //Screen capture ON/OFF
    public static void screenCapOff(Activity activity) {
        //activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
        //WindowManager.LayoutParams.FLAG_SECURE);
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fcm_token", "empty");
    }

}
