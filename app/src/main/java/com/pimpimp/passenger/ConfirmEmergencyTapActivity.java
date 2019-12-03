package com.pimpimp.passenger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MTextView;

import java.util.HashMap;

public class ConfirmEmergencyTapActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    String userProfileJson;
    String iTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_emergency_tap);

        generalFunc = new GeneralFunctions(getActContext());


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        userProfileJson = getIntent().getStringExtra("UserProfileJson");
        iTripId = getIntent().getStringExtra("TripId");

        setLabels();

        backImgView.setOnClickListener(new setOnClickList());
        (findViewById(R.id.policeContactArea)).setOnClickListener(new setOnClickList());
        (findViewById(R.id.emeContactArea)).setOnClickListener(new setOnClickList());

        ((ImageView) findViewById(R.id.phoneImgView)).setColorFilter(Color.parseColor("#DA5A61"));
        ;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT"));
        ((MTextView) findViewById(R.id.pageTitle)).setText(generalFunc.retrieveLangLBl("USE IN CASE OF EMERGENCY", "LBL_CONFIRM_EME_PAGE_TITLE"));
        ((MTextView) findViewById(R.id.callPoliceTxt)).setText(generalFunc.retrieveLangLBl("Call Police Control Room", "LBL_CALL_POLICE"));
        ((MTextView) findViewById(R.id.sendAlertTxt)).setText(generalFunc.retrieveLangLBl("Send message to your emergency contacts.",
                "LBL_SEND_ALERT_EME_CONTACT"));
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                ConfirmEmergencyTapActivity.super.onBackPressed();

            } else if (i == R.id.policeContactArea) {

                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + generalFunc.getJsonValue("SITE_POLICE_CONTROL_NUMBER", userProfileJson)));
                    startActivity(callIntent);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.emeContactArea) {
                sendAlertToEmeContacts();
            }
        }
    }

    public void sendAlertToEmeContacts() {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "sendAlertToEmergencyContacts");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iTripId", iTripId);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {
                    Utils.printLog("responseString", "responseString:" + responseString);

                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    } else {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return ConfirmEmergencyTapActivity.this;
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
