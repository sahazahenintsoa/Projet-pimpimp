package com.pimpimp.passenger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.general.files.GeneralFunctions;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

/**
 * Created by Admin on 03-11-2016.
 */
public class InviteFriendsActivity extends AppCompatActivity {

    private MButton btn_type3;
    MTextView titleTxt,shareTxtLbl,invitecodeTxt,shareTxt,detailTxt;
    ImageView backImgView;

    String userProfileJson="";
    String Refreal_code="";

    GeneralFunctions generalFunc;
    ImageView inviteQueryImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        init();
    }

    private void init() {


        generalFunc = new GeneralFunctions(getActContext());

        userProfileJson = getIntent().getStringExtra("UserProfileJson");

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        shareTxtLbl = (MTextView) findViewById(R.id.shareTxtLbl);
        invitecodeTxt = (MTextView) findViewById(R.id.invitecodeTxt);
        shareTxt = (MTextView) findViewById(R.id.shareTxt);
        detailTxt = (MTextView) findViewById(R.id.detailTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        inviteQueryImg = (ImageView) findViewById(R.id.inviteQueryImg);

        btn_type3 = ((MaterialRippleLayout) findViewById(R.id.btn_type3)).getChildView();
        btn_type3.setId(Utils.generateViewId());

        setLabels();

        btn_type3.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FREE_RIDE"));
        btn_type3.setText(generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_TXT"));
        detailTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DETAILS"));
        shareTxt.setText(generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_SHARE_TXT"));
        shareTxtLbl.setText(generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_SHARE"));

        Refreal_code =  generalFunc.getJsonValue("vRefCode", userProfileJson);


        invitecodeTxt.setText(Refreal_code.trim());


    }

    public Context getActContext() {
        return InviteFriendsActivity.this;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                InviteFriendsActivity.super.onBackPressed();

            } else if (i == btn_type3.getId()) {

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_TXT"));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, generalFunc.retrieveLangLBl("", "SHARE_CONTENT") + ". " + generalFunc.retrieveLangLBl("", "MY_REFERAL_CODE") + " : " + Refreal_code.trim());
                startActivity(Intent.createChooser(sharingIntent, "Share using"));
            }
        }
    }


}

