package com.divinetechs.quizapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.divinetechs.quizapp.Model.SuccessModel.SuccessModel;
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
import com.facebook.login.LoginManager;
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

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WithdrawRequest extends AppCompatActivity {

    PrefManager prefManager;
    Map<String, String> map;

    TextView txtBack, txtToolbarTitle, txtSubmit;
    EditText etPaymentDetails;

    RadioGroup radioGroup;
    RadioButton rbOne, rbTwo, rbThree, rbFour, rbFive;

    LinearLayout lyToolbar, lyBack;
    TemplateView nativeTemplate = null;
    NativeBannerAd fbNativeBannerAd = null;
    NativeAdLayout fbNativeTemplate = null;

    String paymentType = "", paymentDetails = "";
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_request);
        PrefManager.forceRTLIfSupported(getWindow(), WithdrawRequest.this);

        init();
        AdInit();

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                if (rb == null) {
                    Toasty.warning(WithdrawRequest.this, "" + getResources().getString(R.string.please_select_payment_method),
                            Toasty.LENGTH_SHORT).show();
                    return;
                }
                paymentType = rb.getText().toString();
                Log.e("=>paymentType", "" + paymentType);
                paymentDetails = etPaymentDetails.getText().toString();

                if (TextUtils.isEmpty(paymentDetails)) {
                    Toasty.warning(WithdrawRequest.this, "" + getResources().getString(R.string.please_enter_payment_details),
                            Toasty.LENGTH_SHORT).show();
                    return;
                }

                WithdrawalRequest();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(WithdrawRequest.this);
            map = new HashMap<>();
            map = Utility.GetMap(WithdrawRequest.this);

            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            lyBack = findViewById(R.id.lyBack);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("" + getResources().getString(R.string.withdraw_request));
            txtToolbarTitle.setTextColor(getResources().getColor(R.color.text_gray));
            txtBack.setBackgroundTintList(getResources().getColorStateList(R.color.text_gray));

            fbNativeTemplate = findViewById(R.id.fbNativeTemplate);
            nativeTemplate = findViewById(R.id.nativeTemplate);
            radioGroup = findViewById(R.id.radioGroup);
            rbOne = findViewById(R.id.rbOne);
            rbTwo = findViewById(R.id.rbTwo);
            rbThree = findViewById(R.id.rbThree);
            rbFour = findViewById(R.id.rbFour);
            rbFive = findViewById(R.id.rbFive);
            txtSubmit = findViewById(R.id.txtSubmit);
            etPaymentDetails = findViewById(R.id.etPaymentDetails);

            if (prefManager.getValue("payment_status_1").equalsIgnoreCase("1")) {
                rbOne.setVisibility(View.VISIBLE);
                rbOne.setText("" + prefManager.getValue("payment_1"));
            } else {
                rbOne.setVisibility(View.GONE);
            }

            if (prefManager.getValue("payment_status_2").equalsIgnoreCase("1")) {
                rbTwo.setVisibility(View.VISIBLE);
                rbTwo.setText("" + prefManager.getValue("payment_2"));
            } else {
                rbTwo.setVisibility(View.GONE);
            }

            if (prefManager.getValue("payment_status_3").equalsIgnoreCase("1")) {
                rbThree.setVisibility(View.VISIBLE);
                rbThree.setText("" + prefManager.getValue("payment_3"));
            } else {
                rbThree.setVisibility(View.GONE);
            }

            if (prefManager.getValue("payment_status_4").equalsIgnoreCase("1")) {
                rbFour.setVisibility(View.VISIBLE);
                rbFour.setText("" + prefManager.getValue("payment_4"));
            } else {
                rbFour.setVisibility(View.GONE);
            }

            if (prefManager.getValue("payment_status_5").equalsIgnoreCase("1")) {
                rbFive.setVisibility(View.VISIBLE);
                rbFive.setText("" + prefManager.getValue("payment_5"));
            } else {
                rbFive.setVisibility(View.GONE);
            }
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

    private void WithdrawalRequest() {
        Utility.ProgressBarShow(WithdrawRequest.this);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<SuccessModel> call = bookNPlayAPI.withdrawal_request("" + prefManager.getLoginId(),
                "" + paymentDetails, "" + paymentType);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Log.e("Withdrawal_request", "" + response.body().getMessage());
                Utility.ProgressbarHide();
                if (response.code() == 200 && response.body().getStatus() == 200) {
                    flag = true;
                } else {
                    flag = false;
                }

                AlertDialog("" + response.body().getMessage());
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Utility.ProgressbarHide();
                flag = false;
                AlertDialog("" + t.getMessage());
            }
        });
    }

    private void AlertDialog(String message) {
        LayoutInflater inflater = (LayoutInflater) WithdrawRequest.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.alert_dialog, null);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setElevation(100);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        RoundedImageView rivDialog = popupView.findViewById(R.id.rivDialog);
        if (flag) {
            rivDialog.setImageResource(R.drawable.ic_success);
        } else {
            rivDialog.setImageResource(R.drawable.ic_warn);
        }

        TextView txtTitle = popupView.findViewById(R.id.txtTitle);
        TextView txtDescription = popupView.findViewById(R.id.txtDescription);
        Button btnNegative = popupView.findViewById(R.id.btnNegative);
        Button btnPositive = popupView.findViewById(R.id.btnPositive);

        txtTitle.setText(getResources().getString(R.string.app_name));
        txtDescription.setText("" + message);

        btnPositive.setText("" + getResources().getString(R.string.okay));
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                finish();
            }
        });

        btnNegative.setVisibility(View.GONE);
    }

    private void NativeAds() {
        try {
            Log.e("loginID =>", "" + prefManager.getLoginId());
            AdLoader adLoader = new AdLoader.Builder(WithdrawRequest.this, "" + prefManager.getValue("native_adid"))
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
                            Log.e("NativeAd adError=>", "" + adError.getCause());
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
            fbNativeBannerAd = new NativeBannerAd(WithdrawRequest.this,
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
                    Utility.inflateFbSmallNativeAd(WithdrawRequest.this, fbNativeBannerAd, fbNativeTemplate);
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
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
    }

}