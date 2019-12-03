package com.pimpimp.passenger;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Created by Admin on 05-04-2017.
 */
public class AddMoneyByVoucherActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    ProgressBar loading_voucher;
    MTextView viewTransactionsTxt;

    ErrorView errorView;
    String required_str = "";
    String error_money_str = "";
    String userProfileJson = "";
    private ScrollView scrollView;
    private MaterialEditText rechargeBox;
    private MTextView policyTxt;
    private MTextView termsTxt;
    private MButton  btn_type2;


    private MTextView addMoneyTagTxt;
    private MTextView addMoneyTxt;

    // paypal

    String payment_status = "";
    String paymentId_str = "";

    private static final String CONFIG_ENVIRONMENT = CommonUtilities.CONFIG_ENVIRONMENT;

    private static final int REQUEST_PAYPAL_PAYMENT = 1;

    private PayPalConfiguration config;


    android.support.v7.app.AlertDialog alert_paymentStatusError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbyvoucher);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        loading_voucher = (ProgressBar) findViewById(R.id.loading_voucher);
        viewTransactionsTxt = (MTextView) findViewById(R.id.viewTransactionsTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);

        addMoneyTxt = (MTextView) findViewById(R.id.addMoneyTxt);
        addMoneyTagTxt = (MTextView) findViewById(R.id.addMoneyTagTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);
        rechargeBox = (MaterialEditText) findViewById(R.id.rechargeBox);
        termsTxt = (MTextView) findViewById(R.id.termsTxt);
        policyTxt = (MTextView) findViewById(R.id.policyTxt);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        generalFunc = new GeneralFunctions(getActContext());
        userProfileJson = getIntent().getStringExtra("UserProfileJson");

        backImgView.setOnClickListener(new setOnClickList());
        viewTransactionsTxt.setOnClickListener(new setOnClickList());

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());

        termsTxt.setOnClickListener(new setOnClickList());

        termsTxt.setPaintFlags(termsTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loading_voucher.setVisibility(View.GONE);

        setLabels();

        ((LinearLayout) findViewById(R.id.addMoneyToWalletArea)).setVisibility(View.VISIBLE);
    }


    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("Redeem Voucher", "LBL_REDEEM_VOUCHER_TXT"));
        viewTransactionsTxt.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_TRANS_HISTORY"));

        rechargeBox.setBothText(generalFunc.retrieveLangLBl("Voucher Code", "LBL_VOUCHER_CODE_TXT"), generalFunc.retrieveLangLBl("Voucher Code", "LBL_VOUCHER_CODE_TXT"));
        rechargeBox.setInputType(InputType.TYPE_CLASS_TEXT);
        rechargeBox.getLabelFocusAnimator().start();


        addMoneyTxt.setText(generalFunc.retrieveLangLBl("Redeem Voucher", "LBL_REDEEM_VOUCHER_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("Redeem Voucher", "LBL_REDEEM_VOUCHER_TXT"));
        addMoneyTagTxt.setText(generalFunc.retrieveLangLBl("It's safe n secure", "LBL_ADD_MONEY_TXT1"));
        policyTxt.setText(generalFunc.retrieveLangLBl("By conducting you choose to accept Pimpimp's", "LBL_PRIVACY_POLICY"));
        termsTxt.setText(generalFunc.retrieveLangLBl("Terms & Privacy Policy", "LBL_PRIVACY_POLICY1"));

        required_str = generalFunc.retrieveLangLBl("REQUIRED", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_money_str = generalFunc.retrieveLangLBl("Please add correct details.", "LBL_ADD_CORRECT_DETAIL_TXT");


    }

    public void checkValues() {
        Utils.hideKeyboard(getActContext());

        boolean addMoneyAmountEntered = Utils.checkText(rechargeBox) ? true : Utils.setErrorFields(rechargeBox, required_str);


        if (addMoneyAmountEntered == false ) {
            return;
        }
           addMoneyToWallet();
     }


    private void addMoneyToWallet() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckVoucherCode");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("VoucherCode", Utils.getText(rechargeBox));
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        rechargeBox.setText("");

                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.retrieveLangLBl("Amount","LBL_AMOUNT")+ generalFunc.getJsonValue(CommonUtilities.message_str, responseString))+ generalFunc.retrieveLangLBl("has been credited to your wallet.","LBL_MONEY_CEREDITED_TXT"));
                        (new StartActProcess(getActContext())).setOkResult();
                        backImgView.performClick();

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

    public void closeLoader() {
        if (loading_voucher.getVisibility() == View.VISIBLE) {
            loading_voucher.setVisibility(View.GONE);
        }
    }



    public Context getActContext() {
        return AddMoneyByVoucherActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == btn_type2.getId()) {
                checkValues();
            }
            switch (view.getId()) {
                case R.id.backImgView:
                    AddMoneyByVoucherActivity.super.onBackPressed();
                    break;
                case R.id.viewTransactionsTxt:
                    new StartActProcess(getActContext()).startAct(MyWalletHistoryActivity.class);
                    break;
                case R.id.termsTxt:
                    (new StartActProcess(getActContext())).openURL(CommonUtilities.SERVER_URL + "terms-condition");
                    break;
            }
        }
    }
}
