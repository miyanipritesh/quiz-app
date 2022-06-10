package com.divinetechs.quizapp.Activity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

public class AboutUs extends AppCompatActivity {

    private PrefManager prefManager;

    LinearLayout lyBack, lyToolbar, lyAdView, lyFbAdView;

    private ImageView ivAppicon;
    private TextView txtBack, txtToolbarTitle, txtAppname, txtCompanyname, txtEmail,
            txtWebsite, txtContactNo, txtAboutus;

    com.facebook.ads.AdView fbAdView = null;
    AdView mAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        PrefManager.forceRTLIfSupported(getWindow(), AboutUs.this);
        Utility.screenCapOff(AboutUs.this);

        init();
        AdInit();
        setDetails();

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutUs.this.finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(AboutUs.this);

            lyAdView = findViewById(R.id.lyAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            lyBack = findViewById(R.id.lyBack);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);

            ivAppicon = findViewById(R.id.ivAppicon);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtAppname = findViewById(R.id.txtAppname);
            txtCompanyname = findViewById(R.id.txtCompanyname);
            txtEmail = findViewById(R.id.txtEmail);
            txtWebsite = findViewById(R.id.txtWebsite);
            txtContactNo = findViewById(R.id.txtContactNo);
            txtAboutus = findViewById(R.id.txtAboutus);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            lyAdView.setVisibility(View.VISIBLE);
            Utility.Admob(AboutUs.this, mAdView, prefManager.getValue("banner_adid"), lyAdView);
        } else {
            lyAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utility.FacebookBannerAd(AboutUs.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    private void setDetails() {
        try {
            txtToolbarTitle.setTextColor(getResources().getColor(R.color.text_blue));
            txtBack.setBackgroundTintList(getResources().getColorStateList(R.color.text_blue));
            txtToolbarTitle.setText("" + getResources().getString(R.string.about_app));
            Picasso.get().load("" + prefManager.getValue("app_logo"))
                    .placeholder(R.drawable.app_icon)
                    .into(ivAppicon);
            txtAppname.setText("" + prefManager.getValue("app_name"));
            txtCompanyname.setText("" + prefManager.getValue("Author"));
            txtEmail.setText("" + prefManager.getValue("host_email"));
            txtWebsite.setText("" + prefManager.getValue("website"));
            txtContactNo.setText("" + prefManager.getValue("contact"));
            txtAboutus.setText("" + Html.fromHtml(prefManager.getValue("app_desripation")));
        } catch (Exception e) {
            Log.e("set_details", "Exception => " + e);
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
    }
}