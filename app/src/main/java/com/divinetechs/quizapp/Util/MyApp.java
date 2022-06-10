package com.divinetechs.quizapp.Util;

import android.app.Application;
import android.content.Context;

import com.divinetechs.quizapp.R;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.onesignal.OneSignal;

public class MyApp extends Application {

    private static MyApp mInstance;
    PrefManager prefManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        prefManager = new PrefManager(this);

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext());
        // Initialize the Audience Network SDK (Facebook ads)
        AudienceNetworkAds.initialize(this);

        //OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        FirebaseApp.initializeApp(this);

    }

    public void initAppLanguage(Context context) {
        LocaleUtils.initialize(context, LocaleUtils.getSelectedLanguageId());
    }

    public Context getContext() {
        return mInstance.getContext();
    }

    public static synchronized MyApp getInstance() {
        if (mInstance == null) {
            mInstance = new MyApp();
        }
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}