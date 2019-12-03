package com.pimpimp.passenger;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.HashMap;

public class VerifyMobileActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;
    ProgressBar loading;
    MaterialEditText codeBox;

    GeneralFunctions generalFunc;

    MButton okBtn;
    MButton resendBtn;
    MButton editBtn;

    String verificationCode = "";
    String required_str = "";
    String error_verification_code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile);

        generalFunc = new GeneralFunctions(getActContext());
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        codeBox = (MaterialEditText) findViewById(R.id.codeBox);
        okBtn = ((MaterialRippleLayout) findViewById(R.id.okBtn)).getChildView();
        resendBtn = ((MaterialRippleLayout) findViewById(R.id.resendBtn)).getChildView();
        editBtn = ((MaterialRippleLayout) findViewById(R.id.editBtn)).getChildView();

        backImgView.setOnClickListener(new setOnClickList());

        okBtn.setId(Utils.generateViewId());
        okBtn.setOnClickListener(new setOnClickList());

        resendBtn.setId(Utils.generateViewId());
        resendBtn.setOnClickListener(new setOnClickList());

        editBtn.setId(Utils.generateViewId());
        editBtn.setOnClickListener(new setOnClickList());

        setLabels();

        sendVerificationSMS();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                VerifyMobileActivity.super.onBackPressed();
            } else if (i == okBtn.getId()) {
                boolean isCodeEntered = Utils.checkText(codeBox) ?
                        (verificationCode.equals(Utils.getText(codeBox)) ? true : Utils.setErrorFields(codeBox, error_verification_code))
                        : Utils.setErrorFields(codeBox, required_str);
                if (isCodeEntered) {
                    new StartActProcess(getActContext()).setOkResult();
                    backImgView.performClick();
                }
            } else if (i == resendBtn.getId()) {
                openWaitingDialog();
            } else if (i == editBtn.getId()) {
                VerifyMobileActivity.super.onBackPressed();
            }
        }
    }

    public Context getActContext() {
        return VerifyMobileActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_VERIFy_TXT"));
        ((MTextView) findViewById(R.id.smsTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ENTER_VERIFICATION_CODE_TXT"));
        ((MTextView) findViewById(R.id.smsSubTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_SMS_SENT_TO") + ": ");
        okBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        resendBtn.setText(generalFunc.retrieveLangLBl("", "LBL_RESEND_SMS"));
        editBtn.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT_MOBILE"));

        error_verification_code = generalFunc.retrieveLangLBl("", "LBL_VERIFICATION_CODE_INVALID");
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");

        ((MTextView) findViewById(R.id.phoneTxt)).setText("+" + getIntent().getStringExtra("MOBILE"));
    }

    public void sendVerificationSMS() {
        loading.setVisibility(View.VISIBLE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        /*parameters.put("type", "sendVerificationSMS");
        parameters.put("MobileNo", getIntent().getStringExtra("MOBILE"));*/
        parameters.put("type", "sendVerificationSMS");
        parameters.put("MobileNo", getIntent().getStringExtra("MOBILE"));
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);



        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);
                loading.setVisibility(View.GONE);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        verificationCode = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
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

    public void openWaitingDialog() {
        final android.support.v7.app.AlertDialog alertDialog;

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.wait_verify_mobile, null);

        final MTextView countDownTxt = (MTextView) dialogView.findViewById(R.id.countDownTxt);
        builder.setView(dialogView);


        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        CountDownTimer countDnTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long milliseconds) {

                int seconds = (int) (milliseconds / 1000) % 60;
                int minutes = (int) ((milliseconds / (1000 * 60)) % 60);

                countDownTxt.setText(twoDigitString(minutes) + ":" + twoDigitString(seconds) + " / " + "01:00");
            }

            @Override
            public void onFinish() {
                // this function will be called when the timecount is finished
                countDownTxt.setText("01:00");

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                sendVerificationSMS();
            }

        }.start();
    }

    private String twoDigitString(long number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
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
