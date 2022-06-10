package com.divinetechs.quizapp.Activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.divinetechs.quizapp.PushNotification.Config;
import com.divinetechs.quizapp.PushNotification.NotificationUtils;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.facebook.login.LoginManager;
import com.facebook.ads.*;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    PrefManager prefManager;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    private InterstitialAd mInterstitialAd = null;
    private com.facebook.ads.InterstitialAd fbInterstitialAd = null;

    LayoutInflater inflater;

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    LinearLayout lyLeaderboard, lyInstruction, lyUserProfile, lySettings, lyLogout;
    TextView txtPlayquiz;

    String TYPE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PrefManager.forceRTLIfSupported(getWindow(), MainActivity.this);
        Utility.screenCapOff(MainActivity.this);

        prefManager = new PrefManager(MainActivity.this);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        Init();
        PushInit();
    }

    private void Init() {
        try {
            lyLeaderboard = findViewById(R.id.lyLeaderboard);
            lyInstruction = findViewById(R.id.lyInstruction);
            lyUserProfile = findViewById(R.id.lyUserProfile);
            lySettings = findViewById(R.id.lySettings);
            lyLogout = findViewById(R.id.lyLogout);
            txtPlayquiz = findViewById(R.id.txt_play_quiz);

            lyLeaderboard.setOnClickListener(this);
            lyInstruction.setOnClickListener(this);
            lyUserProfile.setOnClickListener(this);
            lySettings.setOnClickListener(this);
            lyLogout.setOnClickListener(this);
            txtPlayquiz.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("Init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        TYPE = "";

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyLeaderboard:
                if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
                    ShowAdByClick("LeaderBoard");
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
                break;

            case R.id.lyInstruction:
                ShowAdByClick("Instruction");
                break;

            case R.id.lyUserProfile:
                if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
                    ShowAdByClick("UserProfile");
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
                break;

            case R.id.lySettings:
                startActivity(new Intent(MainActivity.this, Settings.class));
                break;

            case R.id.lyLogout:
                if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
                    ShowAdByClick("LOGOUT");
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
                break;

            case R.id.txt_play_quiz:
                if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
                    startActivity(new Intent(MainActivity.this, Category.class));
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        txtDescription.setText("" + getResources().getString(R.string.do_you_want_to_exit));

        btnPositive.setText("" + getResources().getString(R.string.yes));
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                finishAffinity();
            }
        });

        btnNegative.setText("" + getResources().getString(R.string.no));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void logout() {
        inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                popupWindow.dismiss();
                prefManager.setLoginId("0");
                LoginManager.getInstance().logOut();
                mGoogleSignInClient.signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                MainActivity.this.finish();
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

    public void PushInit() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    displayFirebaseRegId();
                } else if (Objects.equals(intent.getAction(), Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                    Log.e("message ==>", "" + message);
//                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };
        displayFirebaseRegId();
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        Log.e(TAG, "Firebase reg id: " + regId);
        if (!TextUtils.isEmpty(regId)) {
            Log.e(TAG, "Firebase reg id: " + regId);
        } else {
            Log.e(TAG, "Firebase Reg Id is not received yet!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(getApplicationContext());

        AdInit();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    //Showing ad by TYPE
    private void ShowAdByClick(String Type) {
        TYPE = Type;
        Log.e("=>TYPE", "" + TYPE);

        if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
            if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
                fbInterstitialAd.show();
            } else {
                Log.e("fbInterstitialAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("LeaderBoard")) {
                    startActivity(new Intent(MainActivity.this, LeaderBoard.class));
                } else if (TYPE.equalsIgnoreCase("UserProfile")) {
                    startActivity(new Intent(MainActivity.this, UserProfile.class));
                } else if (TYPE.equalsIgnoreCase("Instruction")) {
                    Intent intentInstruction = new Intent(MainActivity.this, Instruction.class);
                    intentInstruction.putExtra("type", "Instruction");
                    startActivity(intentInstruction);
                } else if (TYPE.equalsIgnoreCase("LOGOUT")) {
                    logout();
                }
            }

        } else {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
            } else {
                Log.e("mInterstitialAd=>", "The ad wasn't ready yet.");
                if (TYPE.equalsIgnoreCase("LeaderBoard")) {
                    startActivity(new Intent(MainActivity.this, LeaderBoard.class));
                } else if (TYPE.equalsIgnoreCase("UserProfile")) {
                    startActivity(new Intent(MainActivity.this, UserProfile.class));
                } else if (TYPE.equalsIgnoreCase("Instruction")) {
                    Intent intentInstruction = new Intent(MainActivity.this, Instruction.class);
                    intentInstruction.putExtra("type", "Instruction");
                    startActivity(intentInstruction);
                } else if (TYPE.equalsIgnoreCase("LOGOUT")) {
                    logout();
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
                            mInterstitialAd = null;
                            Log.e("Ad failed to show.", "" + adError.toString());

                            if (TYPE.equalsIgnoreCase("LeaderBoard")) {
                                startActivity(new Intent(MainActivity.this, LeaderBoard.class));
                            } else if (TYPE.equalsIgnoreCase("UserProfile")) {
                                startActivity(new Intent(MainActivity.this, UserProfile.class));
                            } else if (TYPE.equalsIgnoreCase("Instruction")) {
                                Intent intentInstruction = new Intent(MainActivity.this, Instruction.class);
                                intentInstruction.putExtra("type", "Instruction");
                                startActivity(intentInstruction);
                            } else if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
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
                            mInterstitialAd = null;

                            if (TYPE.equalsIgnoreCase("LeaderBoard")) {
                                startActivity(new Intent(MainActivity.this, LeaderBoard.class));
                            } else if (TYPE.equalsIgnoreCase("UserProfile")) {
                                startActivity(new Intent(MainActivity.this, UserProfile.class));
                            } else if (TYPE.equalsIgnoreCase("Instruction")) {
                                Intent intentInstruction = new Intent(MainActivity.this, Instruction.class);
                                intentInstruction.putExtra("type", "Instruction");
                                startActivity(intentInstruction);
                            } else if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
                            }
                            Log.e("TAG", "Ad was dismissed.");
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.e("TAG", "onAdImpression.");
                        }
                    };

            mInterstitialAd.load(this, "" + prefManager.getValue("interstital_adid"),
                    adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            Log.e(TAG, "onAdLoaded");
                            mInterstitialAd = interstitialAd;
                            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e(TAG, "" + loadAdError.getMessage());
                            mInterstitialAd = null;
                        }
                    });

        } catch (Exception e) {
            Log.e("Interstial Exception =>", "" + e);
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
                            Log.e(TAG, "fb ad displayed.");
                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            fbInterstitialAd = null;

                            if (TYPE.equalsIgnoreCase("LeaderBoard")) {
                                startActivity(new Intent(MainActivity.this, LeaderBoard.class));
                            } else if (TYPE.equalsIgnoreCase("UserProfile")) {
                                startActivity(new Intent(MainActivity.this, UserProfile.class));
                            } else if (TYPE.equalsIgnoreCase("Instruction")) {
                                Intent intentInstruction = new Intent(MainActivity.this, Instruction.class);
                                intentInstruction.putExtra("type", "Instruction");
                                startActivity(intentInstruction);
                            } else if (TYPE.equalsIgnoreCase("LOGOUT")) {
                                logout();
                            }
                            Log.e(TAG, "fb ad dismissed.");
                        }

                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            Log.e(TAG, "fb ad failed to load : " + adError.getErrorMessage());
                            fbInterstitialAd = null;
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            Log.d(TAG, "fb ad is loaded and ready to be displayed!");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {
                            Log.d(TAG, "fb ad clicked!");
                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {
                            Log.d(TAG, "fb ad impression logged!");
                        }
                    })
                    .build());
        } catch (Exception e) {
            Log.e("fb Interstial", "Exception =>" + e);
        }
    }

    @Override
    protected void onDestroy() {
        if (fbInterstitialAd != null) {
            fbInterstitialAd.destroy();
            fbInterstitialAd = null;
        }
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        super.onDestroy();
    }

}