package com.divinetechs.quizapp.Fragment;

import android.annotation.SuppressLint;
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
import com.divinetechs.quizapp.Activity.LevelSelection;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Month extends Fragment {

    PrefManager prefManager;
    Map<String, String> map;

    private View root;

    LinearLayout lyRecycler, lyUserPos, lyNoData, lyAdView, lyFbAdView;
    ShimmerFrameLayout shimmer;

    RoundedImageView rivFirst, rivSecond, rivThird, rivUser;

    TextView txtPointsFirst, txtPointsSecond, txtPointsThird,
            txtFirstName, txtSecondName, txtThirdName, txtUserName, txtUserRank, txtUserPoints;

    private RecyclerView rvMonthContestant;
    private List<Result> topContenstantList;
    private TopContestantAdapter topContestantAdapter;

    AdView mAdView = null;
    com.facebook.ads.AdView fbAdView = null;

    public Month() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.subfragment_month, container, false);

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
            lyUserPos = root.findViewById(R.id.lyUserPos);
            lyRecycler = root.findViewById(R.id.lyRecycler);
            lyAdView = root.findViewById(R.id.lyAdView);
            lyFbAdView = root.findViewById(R.id.lyFbAdView);

            rvMonthContestant = root.findViewById(R.id.rv_month_contestant);

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
            Log.e("MONTH init Exception =>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            lyAdView.setVisibility(View.VISIBLE);
            Utility.Admob(getActivity(), mAdView, prefManager.getValue("banner_adid"), lyAdView);
        } else {
            lyAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utility.FacebookBannerAd(getActivity(), fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    //getLeaderBoard API call
    private void TopContestatnt() {
        Utility.shimmerShow(shimmer);

        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<LeaderBoardModel> call = bookNPlayAPI.getLeaderBoard("" + prefManager.getLoginId(), "month");
        call.enqueue(new Callback<LeaderBoardModel>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(@NonNull Call<LeaderBoardModel> call, @NonNull Response<LeaderBoardModel> response) {
                try {
                    Log.e("MONTH API call : status", "" + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        txtUserRank.setText("" + response.body().getUser().getRank());
                        txtUserName.setText("" + response.body().getUser().getFullname());
                        txtUserPoints.setText("" + String.format("%.0f",
                                Double.parseDouble(response.body().getUser().getTotalScore())));
                        if (!TextUtils.isEmpty(response.body().getUser().getProfileImg())) {
                            Picasso.get().load(response.body().getUser().getProfileImg())
                                    .into(rivUser);
                        }

                        if (response.body().getResult().size() > 0) {
                            lyNoData.setVisibility(View.GONE);

                            topContenstantList = new ArrayList<Result>();
                            topContenstantList = response.body().getResult();
                            Log.e("MONTH size", "" + topContenstantList.size());

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
                                rvMonthContestant.setLayoutManager(gridLayoutManager);
                                rvMonthContestant.setHasFixedSize(true);
                                rvMonthContestant.setAdapter(topContestantAdapter);
                                rvMonthContestant.setItemAnimator(new DefaultItemAnimator());
                                topContestantAdapter.notifyDataSetChanged();
                            } else {
                                rvMonthContestant.setVisibility(View.GONE);
                            }

                        } else {
                            lyNoData.setVisibility(View.VISIBLE);
                            lyUserPos.setVisibility(View.GONE);
                        }

                    } else {
                        Log.e("MONTH massage", "" + response.body().getMessage());
                        lyNoData.setVisibility(View.VISIBLE);
                        lyUserPos.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("MONTH API error==>", "" + e);
                }
                Utility.shimmerHide(shimmer);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Call<LeaderBoardModel> call, @NonNull Throwable t) {
                Log.e("MONTH API call : Failure", "That didn't work!!!" + t.getMessage());
                lyUserPos.setVisibility(View.GONE);
                lyNoData.setVisibility(View.VISIBLE);
                Utility.shimmerHide(shimmer);
            }
        });
    }

    @Override
    public void onPause() {
        Utility.shimmerHide(shimmer);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Utility.shimmerHide(shimmer);
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
    }

}
