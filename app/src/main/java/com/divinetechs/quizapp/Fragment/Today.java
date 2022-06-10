package com.divinetechs.quizapp.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.divinetechs.quizapp.Activity.Instruction;
import com.divinetechs.quizapp.Activity.Statistics;
import com.divinetechs.quizapp.Adapter.TopContestantAdapter;
import com.divinetechs.quizapp.Model.LeaderBoardModel.LeaderBoardModel;
import com.divinetechs.quizapp.Model.LeaderBoardModel.Result;
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
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Today extends Fragment {

    PrefManager prefManager;
    Map<String, String> map;

    private View root;

    LinearLayout lyRecycler, lyUserPos, lyNoData;

    ShimmerFrameLayout shimmer;

    RoundedImageView rivFirst, rivSecond, rivThird, rivUser;

    TextView txtPointsFirst, txtPointsSecond, txtPointsThird,
            txtFirstName, txtSecondName, txtThirdName, txtUserRank, txtUserName,
            txtUserPoints;

    private RecyclerView rvTodayContestant;
    private List<Result> topContenstantList;
    private TopContestantAdapter topContestantAdapter;

    TemplateView nativeTemplate = null;
    NativeBannerAd fbNativeBannerAd = null;
    NativeAdLayout fbNativeTemplate = null;

    public Today() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.subfragment_today, container, false);

        init();
        AdInit();
        TopContestatnt();

        return root;
    }

    private void init() {
        try {
            map = new HashMap<>();
            map = Utility.GetMap(getActivity());
            prefManager = new PrefManager(getActivity());

            shimmer = root.findViewById(R.id.shimmer);
            lyNoData = root.findViewById(R.id.lyNoData);
            lyRecycler = root.findViewById(R.id.lyRecycler);
            lyUserPos = root.findViewById(R.id.lyUserPos);
            nativeTemplate = root.findViewById(R.id.nativeTemplate);
            fbNativeTemplate = root.findViewById(R.id.fbNativeTemplate);

            rvTodayContestant = root.findViewById(R.id.rvTodayContestant);

            rivFirst = root.findViewById(R.id.rivFirst);
            rivSecond = root.findViewById(R.id.rivSecond);
            rivThird = root.findViewById(R.id.rivThird);
            rivUser = root.findViewById(R.id.rivUser);

            txtUserRank = root.findViewById(R.id.txtUserRank);
            txtUserName = root.findViewById(R.id.txtUserName);
            txtUserPoints = root.findViewById(R.id.txtUserPoints);
            txtFirstName = root.findViewById(R.id.txtFirstName);
            txtSecondName = root.findViewById(R.id.txtSecondName);
            txtThirdName = root.findViewById(R.id.txtThirdName);
            txtPointsFirst = root.findViewById(R.id.txtPointsFirst);
            txtPointsSecond = root.findViewById(R.id.txtPointsSecond);
            txtPointsThird = root.findViewById(R.id.txtPointsThird);
        } catch (Exception e) {
            Log.e("TODAY init Exception =>", "" + e);
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

    //getLeaderBoard API call
    private void TopContestatnt() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<LeaderBoardModel> call = bookNPlayAPI.getLeaderBoard("" + prefManager.getLoginId(), "today");
        call.enqueue(new Callback<LeaderBoardModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<LeaderBoardModel> call, @NonNull Response<LeaderBoardModel> response) {
                try {
                    Log.e("TODAY API call : status", "" + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getUser() != null) {
                            txtUserRank.setText("" + response.body().getUser().getRank());
                            txtUserName.setText("" + response.body().getUser().getFullname());
                            txtUserPoints.setText("" + String.format("%.0f",
                                    Double.parseDouble(response.body().getUser().getTotalScore())));
                            if (!TextUtils.isEmpty(response.body().getUser().getProfileImg())) {
                                Picasso.get().load(response.body().getUser().getProfileImg())
                                        .into(rivUser);
                            }
                        } else {
                            lyUserPos.setVisibility(View.GONE);
                        }

                        if (response.body().getResult().size() > 0) {
                            lyNoData.setVisibility(View.GONE);

                            topContenstantList = new ArrayList<Result>();
                            topContenstantList = response.body().getResult();
                            Log.e("TODAY size", "" + topContenstantList.size());

                            if (topContenstantList.size() == 1) {
                                txtFirstName.setText("" + topContenstantList.get(0).getName());
                                txtPointsFirst.setText("" + String.format("%.0f",
                                        Double.parseDouble(topContenstantList.get(0).getScore())));
                                if (!TextUtils.isEmpty(topContenstantList.get(0).getProfileImg())) {
                                    Picasso.get().load(topContenstantList.get(0).getProfileImg())
                                            .into(rivFirst);
                                }

                            } else if (topContenstantList.size() == 2) {
                                txtFirstName.setText("" + topContenstantList.get(0).getName());
                                txtPointsFirst.setText("" + String.format("%.0f",
                                        Double.parseDouble(topContenstantList.get(0).getScore())));
                                if (!TextUtils.isEmpty(topContenstantList.get(0).getProfileImg())) {
                                    Picasso.get().load(topContenstantList.get(0).getProfileImg())
                                            .into(rivFirst);
                                }

                                txtSecondName.setText("" + topContenstantList.get(1).getName());
                                txtPointsSecond.setText("" + String.format("%.0f",
                                        Double.parseDouble(topContenstantList.get(1).getScore())));
                                if (!TextUtils.isEmpty(topContenstantList.get(1).getProfileImg())) {
                                    Picasso.get().load(topContenstantList.get(1).getProfileImg())
                                            .into(rivSecond);
                                }

                            } else {
                                txtFirstName.setText("" + topContenstantList.get(0).getName());
                                txtPointsFirst.setText("" + String.format("%.0f",
                                        Double.parseDouble(topContenstantList.get(0).getScore())));
                                if (!TextUtils.isEmpty(topContenstantList.get(0).getProfileImg())) {
                                    Picasso.get().load(topContenstantList.get(0).getProfileImg())
                                            .into(rivFirst);
                                }

                                txtSecondName.setText("" + topContenstantList.get(1).getName());
                                txtPointsSecond.setText("" + String.format("%.0f",
                                        Double.parseDouble(topContenstantList.get(1).getScore())));
                                if (!TextUtils.isEmpty(topContenstantList.get(1).getProfileImg())) {
                                    Picasso.get().load(topContenstantList.get(1).getProfileImg())
                                            .into(rivSecond);
                                }

                                txtThirdName.setText("" + topContenstantList.get(2).getName());
                                txtPointsThird.setText("" + String.format("%.0f",
                                        Double.parseDouble(topContenstantList.get(2).getScore())));
                                if (!TextUtils.isEmpty(topContenstantList.get(2).getProfileImg())) {
                                    Picasso.get().load(topContenstantList.get(2).getProfileImg())
                                            .into(rivThird);
                                }
                            }

                            if (topContenstantList.size() > 3) {
                                topContestantAdapter = new TopContestantAdapter(getActivity(), topContenstantList);
                                GridLayoutManager gridLayoutManager =
                                        new GridLayoutManager(getActivity(), 1,
                                                LinearLayoutManager.VERTICAL, false);
                                rvTodayContestant.setLayoutManager(gridLayoutManager);
                                rvTodayContestant.setHasFixedSize(true);
                                rvTodayContestant.setAdapter(topContestantAdapter);
                                rvTodayContestant.setItemAnimator(new DefaultItemAnimator());
                                topContestantAdapter.notifyDataSetChanged();
                            } else {
                                rvTodayContestant.setVisibility(View.GONE);
                            }

                        } else {
                            lyNoData.setVisibility(View.VISIBLE);
                            lyUserPos.setVisibility(View.GONE);
                        }

                    } else {
                        Log.e("TODAY massage", "" + response.body().getMessage());
                        lyUserPos.setVisibility(View.GONE);
                        lyNoData.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e("TODAY API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<LeaderBoardModel> call, @NonNull Throwable t) {
                Log.e("TODAY API call : Failure", "That didn't work!!!" + t.getMessage());
                lyNoData.setVisibility(View.VISIBLE);
                lyUserPos.setVisibility(View.GONE);
                Utility.shimmerHide(shimmer);
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
            Log.e("loginID =>", "" + prefManager.getLoginId());
            AdLoader adLoader = new AdLoader.Builder(getActivity(), "" + prefManager.getValue("native_adid"))
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
            fbNativeBannerAd = new NativeBannerAd(getActivity(),
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
                    Utility.inflateFbSmallNativeAd(getActivity(), fbNativeBannerAd, fbNativeTemplate);
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
