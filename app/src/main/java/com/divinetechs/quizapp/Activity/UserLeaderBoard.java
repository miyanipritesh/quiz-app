package com.divinetechs.quizapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.divinetechs.quizapp.Fragment.All;
import com.divinetechs.quizapp.Fragment.Month;
import com.divinetechs.quizapp.Fragment.Today;
import com.divinetechs.quizapp.R;
import com.divinetechs.quizapp.Util.PrefManager;
import com.divinetechs.quizapp.Util.Utility;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLeaderBoard extends AppCompatActivity implements View.OnClickListener {

    Map<String, String> map;

    TabLayout tabLayout;
    ViewPager tabViewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_leaderboard);
        PrefManager.forceRTLIfSupported(getWindow(), UserLeaderBoard.this);
        Utility.screenCapOff(UserLeaderBoard.this);

        init();
    }

    public void init() {
        try {
            map = new HashMap<>();
            map = Utility.GetMap(UserLeaderBoard.this);

            tabLayout = findViewById(R.id.tabLayout);
            tabViewpager = findViewById(R.id.tabViewpager);
            tabViewpager.setOffscreenPageLimit(2);
            setupViewPager(tabViewpager);
            tabLayout.setupWithViewPager(tabViewpager);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    //Tab With ViewPager
    private void setupViewPager(ViewPager viewPager) {
        TestPagerAdapter adapter = new TestPagerAdapter(UserLeaderBoard.this.getSupportFragmentManager());
        adapter.addFragment(new Today(), "" + getResources().getString(R.string.today));
        adapter.addFragment(new Month(), "" + getResources().getString(R.string.month));
        adapter.addFragment(new All(), "" + getResources().getString(R.string.all));
        viewPager.setAdapter(adapter);
    }

    static class TestPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public TestPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}