package com.divinetechs.quizapp.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.divinetechs.quizapp.BuildConfig;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.LocaleUtils;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.facebook.login.LoginManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    PrefManager prefManager;

    ShimmerFrameLayout shimmer;
    LayoutInflater inflater;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    SwitchCompat switchSound, switchVibration, switchPush;
    Spinner spinnerLanguage;

    LinearLayout lyBack, lyToolbar, lyAbout, lyTermCondition, lyLogin, lyRateApp, lyShareApp;
    TextView txtToolbarTitle, txtBack, txtLogin;

    TemplateView nativeTemplate = null;
    NativeBannerAd fbNativeBannerAd = null;
    com.facebook.ads.NativeAd fbNativeAd = null;
    NativeAdLayout fbNativeTemplate = null;
    private InterstitialAd mInterstitialAd = null;
    private com.facebook.ads.InterstitialAd fbInterstitialAd = null;
    private RewardedAd mRewardedAd = null;
    private RewardedVideoAd fbRewardedVideoAd = null;

    String currentLanguage = "en", TYPE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        PrefManager.forceRTLIfSupported(getWindow(), Settings.this);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(Settings.this, gso);

        init();

        txtToolbarTitle.setText("" + getString(R.string.Settings));
        txtToolbarTitle.setTextColor(getResources().getColor(R.color.text_blue));
        txtBack.setBackgroundTintList(getResources().getColorStateList(R.color.text_blue));

        if (prefManager.getBool("PUSH")) {
            switchPush.setChecked(true);
        } else {
            switchPush.setChecked(false);
        }

        if (prefManager.getBool("SOUND")) {
            switchSound.setChecked(true);
        } else {
            switchSound.setChecked(false);
        }

        if (prefManager.getBool("VIBRATION")) {
            switchVibration.setChecked(true);
        } else {
            switchVibration.setChecked(false);
        }

        if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
            txtLogin.setText(getResources().getString(R.string.logout));
        } else {
            txtLogin.setText(getResources().getString(R.string.log_in));
        }

        spinnerOnClick();
        currentLanguage = prefManager.getValue("select_language");
        Log.e("lan_currentLan", "" + currentLanguage);

        currentLanguage = LocaleUtils.getSelectedLanguageId();
        Log.e("currentLanguage", "" + currentLanguage);
    }

    private void init() {
        try {
            prefManager = new PrefManager(Settings.this);
            shimmer = findViewById(R.id.shimmer);
            switchVibration = findViewById(R.id.switchVibration);
            switchSound = findViewById(R.id.switchSound);
            switchPush = findViewById(R.id.switchPush);
            spinnerLanguage = findViewById(R.id.spinnerLanguage);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            lyBack = findViewById(R.id.lyBack);
            lyAbout = findViewById(R.id.lyAbout);
            lyTermCondition = findViewById(R.id.lyTermCondition);
            lyRateApp = findViewById(R.id.lyRateApp);
            lyShareApp = findViewById(R.id.lyShareApp);
            lyLogin = findViewById(R.id.lyLogin);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtBack = findViewById(R.id.txtBack);
            txtLogin = findViewById(R.id.txtLogin);
            nativeTemplate = findViewById(R.id.nativeTemplate);
            fbNativeTemplate = findViewById(R.id.fbNativeTemplate);

            switchSound.setOnCheckedChangeListener(this);
            switchVibration.setOnCheckedChangeListener(this);
            switchPush.setOnCheckedChangeListener(this);
            lyBack.setOnClickListener(this);
            lyAbout.setOnClickListener(this);
            lyTermCondition.setOnClickListener(this);
            lyShareApp.setOnClickListener(this);
            lyRateApp.setOnClickListener(this);
            lyLogin.setOnClickListener(this);
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

        Log.e("fb_native_full_status", "" + prefManager.getValue("fb_native_full_status"));
        if (prefManager.getValue("fb_native_full_status").equalsIgnoreCase("on")) {
            fbNativeTemplate.setVisibility(View.VISIBLE);
            FacebookNativeAd();
        } else {
            fbNativeTemplate.setVisibility(View.GONE);
        }

        TYPE = "";
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

        Log.e("fb_interstiatial_status", "" + prefManager.getValue("fb_interstiatial_status"));
        if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
            fbInterstitialAd = null;
            FacebookInterstitialAd();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchSound:
                Log.e("Sound ==>", "" + isChecked);
                if (isChecked) {
                    prefManager.setBool("SOUND", true);
                } else {
                    prefManager.setBool("SOUND", false);
                }
                break;

            case R.id.switchVibration:
                Log.e("Vibration ==>", "" + isChecked);
                if (isChecked) {
                    prefManager.setBool("VIBRATION", true);
                } else {
                    prefManager.setBool("VIBRATION", false);
                }
                break;

            case R.id.switchPush:
                Log.e("Push ==>", "" + isChecked);
                if (isChecked) {
                    OneSignal.setSubscription(true);
                } else {
                    OneSignal.setSubscription(false);
                }
                prefManager.setBool("PUSH", isChecked);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AdInit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyBack:
                finish();
                break;

            case R.id.lyAbout:
                ShowAdByClick("ABOUTUS");
                break;

            case R.id.lyTermCondition:
                ShowAdByClick("PRIVACY");
                break;

            case R.id.lyShareApp:
                ShowAdByClick("SHAREAPP");
                break;

            case R.id.lyRateApp:
                ShowAdByClick("RATEAPP");
                break;

            case R.id.lyLogin:
                if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
                    ShowAdByClick("LOGOUT");
                } else {
                    startActivity(new Intent(Settings.this, Login.class));
                }
                break;
        }
    }

    private void spinnerOnClick() {
        List<String> list = new ArrayList<String>();
        list.add("English");
        list.add("عربى");
        list.add("हिंदी");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Settings.this, R.layout.spinner_item, list);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        if (LocaleUtils.getSelectedLanguageId().equalsIgnoreCase("en")) {
            Log.e("selected_eng", " English");
            spinnerLanguage.setSelection(0);
        }
        if (LocaleUtils.getSelectedLanguageId().equalsIgnoreCase("ar")) {
            Log.e("select_Arabic", " Arabic");
            spinnerLanguage.setSelection(1);
        }
        if (LocaleUtils.getSelectedLanguageId().equalsIgnoreCase("hi")) {
            Log.e("select_Hindi", " Hindi");
            spinnerLanguage.setSelection(2);
        }

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Log.e("pos", "" + position);
                switch (position) {
                    case 0:
                        setLocale("en");
                        break;
                    case 1:
                        setLocale("ar");
                        break;
                    case 2:
                        setLocale("hi");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setLocale(String localeName) {
        try {
            Log.e("=>lan_name", "" + localeName);
            Log.e("=>currentLanguage", "" + currentLanguage);
            if (!localeName.equals(currentLanguage)) {
                LocaleUtils.setSelectedLanguageId(localeName);
                Intent i = Settings.this.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(Settings.this.getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } else {
//                Toasty.info(Settings.this, "" + getResources().getString(R.string.language_already_selected),
//                        Toasty.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("error_msg", "" + e.getMessage());
        }
    }

    private void logout() {
        inflater = (LayoutInflater) Settings.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.alert_dialog, null);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setElevation(100);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        RoundedImageView rivDialog = popupView.findViewById(R.id.rivDialog);
        rivDialog.setImageResource(R.drawable.app_icon2);

        TextView txtTitle = popupView.findViewById(R.id.txtTitle);
        TextView txtDescription = popupView.findViewById(R.id.txtDescription);
        Button btnNegative = popupView.findViewById(R.id.btnNegative);
        Button btnPositive = popupView.findViewById(R.id.btnPositive);

        txtTitle.setText(getResources().getString(R.string.app_name));
        txtDescription.setText("" + getResources().getString(R.string.are_you_sure_you_want_to_logout));

        btnPositive.setText("" + getResources().getString(R.string.logout));
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefManager.setLoginId("0");
                LoginManager.getInstance().logOut();
                mGoogleSignInClient.signOut();
                Intent intent = new Intent(Settings.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
//                getActivity().finish();
                txtLogin.setText(getResources().getString(R.string.log_in));
                popupWindow.dismiss();
            }
        });

        btnNegative.setText("" + getResources().getString(R.string.cancel));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void rateMe() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + Settings.this.getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + Settings.this.getPackageName())));
        }
    }

    private void ShareMe() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "" + getResources().getString(R.string.app_name));
            String shareMessage = "\n" + getResources().getString(R.string.let_me_recommend_you_this_application) + "\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "" + getResources().getString(R.string.share_with)));
        } catch (Exception e) {
            //e.toString();
        }
    }

    //Showing ad by TYPE
    private void ShowAdByClick(String Type) {
        TYPE = Type;
        Log.e("=>TYPE", "" + TYPE);

        if (prefManager.getValue("reward_ad").equalsIgnoreCase("yes")) {
            if (mRewardedAd != null) {
                mRewardedAd.show(Settings.this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.e("RewardItem amount =>", "" + rewardItem.getAmount());
                    }
                });
            } else {
                Log.e("mRewardedAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("LOGOUT")) {
                    logout();
                } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                    ShareMe();
                } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                    rateMe();
                } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                    Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                    intentPrivacy.putExtra("type", "Privacy Policy");
                    startActivity(intentPrivacy);
                } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                    startActivity(new Intent(Settings.this, AboutUs.class));
                }
            }

        } else if (prefManager.getValue("fb_rewardvideo_status").equalsIgnoreCase("on")) {
            if (fbRewardedVideoAd != null && fbRewardedVideoAd.isAdLoaded()) {
                fbRewardedVideoAd.show();
            } else {
                Log.e("fbRewardedVideoAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("LOGOUT")) {
                    logout();
                } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                    ShareMe();
                } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                    rateMe();
                } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                    Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                    intentPrivacy.putExtra("type", "Privacy Policy");
                    startActivity(intentPrivacy);
                } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                    startActivity(new Intent(Settings.this, AboutUs.class));
                }
            }

        } else if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
            if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
                fbInterstitialAd.show();
            } else {
                Log.e("fbInterstitialAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("LOGOUT")) {
                    logout();
                } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                    ShareMe();
                } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                    rateMe();
                } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                    Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                    intentPrivacy.putExtra("type", "Privacy Policy");
                    startActivity(intentPrivacy);
                } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                    startActivity(new Intent(Settings.this, AboutUs.class));
                }
            }

        } else {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(Settings.this);
            } else {
                Log.e("mInterstitialAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("LOGOUT")) {
                    logout();
                } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                    ShareMe();
                } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                    rateMe();
                } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                    Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                    intentPrivacy.putExtra("type", "Privacy Policy");
                    startActivity(intentPrivacy);
                } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                    startActivity(new Intent(Settings.this, AboutUs.class));
                }
            }
        }
    }

    private void NativeAds() {
        try {
            Log.e("loginID =>", "" + prefManager.getLoginId());
            AdLoader adLoader = new AdLoader.Builder(Settings.this, "" + prefManager.getValue("native_adid"))
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

    private void FacebookNativeAd() {
        try {
            fbNativeAd = new com.facebook.ads.NativeAd(Settings.this,
                    "IMG_16_9_APP_INSTALL#" + prefManager.getValue("fb_native_full_id"));

            fbNativeAd.loadAd(fbNativeAd.buildLoadAdConfig().withAdListener(new NativeAdListener() {
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
                    if (fbNativeAd == null || fbNativeAd != ad) {
                        return;
                    }
                    // Inflate Native Banner Ad into Container
                    Utility.inflateFbLargeNativeAd(Settings.this, fbNativeAd, fbNativeTemplate);
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
            Log.e("fbNative", "Exception => " + e.getMessage());
        }
    }

    private void InterstitialAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Log.e("InterstitialAd", "failed to show. => " + adError.toString());
                            mInterstitialAd = null;
                            if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
                            } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                                ShareMe();
                            } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                                rateMe();
                            } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                                Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                                intentPrivacy.putExtra("type", "Privacy Policy");
                                startActivity(intentPrivacy);
                            } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                                startActivity(new Intent(Settings.this, AboutUs.class));
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            Log.e("InterstitialAd", "ShowedFullScreen");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            Log.e("InterstitialAd", "DismissedFullScreen");
                            mInterstitialAd = null;
                            if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
                            } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                                ShareMe();
                            } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                                rateMe();
                            } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                                Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                                intentPrivacy.putExtra("type", "Privacy Policy");
                                startActivity(intentPrivacy);
                            } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                                startActivity(new Intent(Settings.this, AboutUs.class));
                            }
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.e("InterstitialAd", "onAdImpression.");
                        }
                    };

            mInterstitialAd.load(Settings.this, "" + prefManager.getValue("interstital_adid"),
                    adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            Log.e("InterstitialAd", "onAdLoaded");
                            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.e("InterstitialAd", "loadAdError => " + loadAdError.getMessage());
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
                            if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
                            } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                                ShareMe();
                            } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                                rateMe();
                            } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                                Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                                intentPrivacy.putExtra("type", "Privacy Policy");
                                startActivity(intentPrivacy);
                            } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                                startActivity(new Intent(Settings.this, AboutUs.class));
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
                            Log.e("TAG", "fb ad clicked!");
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
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Log.e("RewardedAd", "Ad failed to show. => " + adError.toString());
                            mRewardedAd = null;
                            if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
                            } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                                ShareMe();
                            } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                                rateMe();
                            } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                                Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                                intentPrivacy.putExtra("type", "Privacy Policy");
                                startActivity(intentPrivacy);
                            } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                                startActivity(new Intent(Settings.this, AboutUs.class));
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            Log.e("RewardedAd", "Ad was shown.");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            mRewardedAd = null;
                            Log.e("RewardedAd", "Ad was dismissed.");
                            if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
                            } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                                ShareMe();
                            } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                                rateMe();
                            } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                                Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                                intentPrivacy.putExtra("type", "Privacy Policy");
                                startActivity(intentPrivacy);
                            } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                                startActivity(new Intent(Settings.this, AboutUs.class));
                            }
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.e("RewardedAd", "onAdImpression.");
                        }
                    };
            //TEST reward_adid ==>  ca-app-pub-3940256099942544/5224354917 ==OR== + prefManager.getValue("reward_adid")
            mRewardedAd.load(Settings.this, "" + prefManager.getValue("reward_adid"),
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            super.onAdLoaded(rewardedAd);
                            Log.e("RewardedAd", "onAdLoaded");
                            mRewardedAd = rewardedAd;
                            mRewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            Log.e("RewardedAd", "onAdFailedToLoad");
                            mRewardedAd = null;
                        }
                    });

        } catch (Exception e) {
            Log.e("RewardAd Exception =>", "" + e);
        }
    }

    private void FacebookRewardAd() {
        try {
            fbRewardedVideoAd = new RewardedVideoAd(Settings.this,
                    "VID_HD_16_9_15S_APP_INSTALL#" + prefManager.getValue("fb_rewardvideo_id"));

            fbRewardedVideoAd.loadAd(fbRewardedVideoAd.buildLoadAdConfig().withAdListener(new RewardedVideoAdListener() {
                @Override
                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                    Log.e("TAG", "Rewarded video adError => " + adError.getErrorMessage());
                    fbRewardedVideoAd.destroy();
                    fbRewardedVideoAd = null;
                    if (TYPE.equalsIgnoreCase("LOGOUT")) {
                        logout();
                    } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                        ShareMe();
                    } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                        rateMe();
                    } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                        Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                        intentPrivacy.putExtra("type", "Privacy Policy");
                        startActivity(intentPrivacy);
                    } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                        startActivity(new Intent(Settings.this, AboutUs.class));
                    }
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
                    if (TYPE.equalsIgnoreCase("LOGOUT")) {
                        logout();
                    } else if (TYPE.equalsIgnoreCase("SHAREAPP")) {
                        ShareMe();
                    } else if (TYPE.equalsIgnoreCase("RATEAPP")) {
                        rateMe();
                    } else if (TYPE.equalsIgnoreCase("PRIVACY")) {
                        Intent intentPrivacy = new Intent(Settings.this, Instruction.class);
                        intentPrivacy.putExtra("type", "Privacy Policy");
                        startActivity(intentPrivacy);
                    } else if (TYPE.equalsIgnoreCase("ABOUTUS")) {
                        startActivity(new Intent(Settings.this, AboutUs.class));
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
        Utility.shimmerHide(shimmer);
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
        if (fbNativeAd != null) {
            fbNativeAd.destroy();
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