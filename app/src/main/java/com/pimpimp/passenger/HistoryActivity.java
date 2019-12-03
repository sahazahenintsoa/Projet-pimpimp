package com.pimpimp.passenger;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.adapter.files.ViewPagerAdapter;
import com.fragments.HistoryFragment;
import com.general.files.GeneralFunctions;
import com.utils.Utils;
import com.view.MTextView;
import com.view.MaterialTabs;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    public GeneralFunctions generalFunc;

    String userProfileJson;
    CharSequence[] titles;
    String app_type = "Ride";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        generalFunc = new GeneralFunctions(getActContext());

        userProfileJson = getIntent().getStringExtra("UserProfileJson");

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
        MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        ArrayList<Fragment> fragmentList = new ArrayList<>();


            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_RIDE")};
            fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Ride));
            material_tabs.setVisibility(View.GONE);


        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setViewPager(appLogin_view_pager);

    }

    public HistoryFragment generateHistoryFrag(String bookingType) {
        HistoryFragment frag = new HistoryFragment();
        Bundle bn = new Bundle();
        bn.putString("HISTORY_TYPE", bookingType);

        frag.setArguments(bn);

        return frag;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RIDE_HISTORY"));
    }


    @Override
    protected void onResume() {

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        super.onResume();

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    HistoryActivity.super.onBackPressed();
                    break;

            }
        }
    }


    public Context getActContext() {
        return HistoryActivity.this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
