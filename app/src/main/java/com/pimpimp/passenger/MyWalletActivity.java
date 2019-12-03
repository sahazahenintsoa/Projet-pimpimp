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
 * Created by Admin on 04-11-2016.
 */
public class MyWalletActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    ProgressBar loading_wallet_history;
    MTextView viewTransactionsTxt;
    MTextView paymentMthdTxt;

    ErrorView errorView;
    String required_str = "";
    String error_money_str = "";
    String userProfileJson = "";
    boolean mIsLoading = false;
    String next_page_str = "0";

    private MTextView yourBalTxt;
    private MButton btn_type1,payBycard,payByVoucher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        loading_wallet_history = (ProgressBar) findViewById(R.id.loading_wallet_history);
        viewTransactionsTxt = (MTextView) findViewById(R.id.viewTransactionsTxt);
        paymentMthdTxt = (MTextView) findViewById(R.id.paymentMthdTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);


        yourBalTxt = (MTextView) findViewById(R.id.yourBalTxt);

        btn_type1 = ((MaterialRippleLayout) findViewById(R.id.btn_type1)).getChildView();
        payBycard = ((MaterialRippleLayout) findViewById(R.id.btn_card)).getChildView();
        payByVoucher = ((MaterialRippleLayout) findViewById(R.id.btn_voucher)).getChildView();

        generalFunc = new GeneralFunctions(getActContext());
        userProfileJson = getIntent().getStringExtra("UserProfileJson");

        backImgView.setOnClickListener(new setOnClickList());
        viewTransactionsTxt.setOnClickListener(new setOnClickList());
        btn_type1.setId(Utils.generateViewId());

        payBycard.setVisibility(View.GONE);
        payByVoucher.setVisibility(View.VISIBLE);

        Utils.printLog("Api", "cash enabled" + generalFunc.getJsonValue("APP_PAYMENT_CASH_ENABLED", userProfileJson));
        Utils.printLog("Api", "APP_PAYMENT_MODE enabled" + generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson));
        Utils.printLog("Api", "PAYMENT_ENABLED enabled" + generalFunc.getJsonValue("PAYMENT_ENABLED", userProfileJson));
        Utils.printLog("Api", "userProfileJson " + userProfileJson.toString());

         if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card")) {
           cardOptionVisible(true);
        }
         else if (generalFunc.getJsonValue("PAYMENT_ENABLED", userProfileJson).equalsIgnoreCase("No"))
         {
           cardOptionVisible(false);

         }
      /*  else if (generalFunc.getJsonValue("APP_PAYMENT_CASH_ENABLED", userProfileJson).equalsIgnoreCase("Yes") || generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash"))
         {
             payBycard.setVisibility(View.GONE);

         }*/
         else
         {
             cardOptionVisible(true);
         }

        payByVoucher.setId(Utils.generateViewId());
        payByVoucher.setBackgroundColor(getResources().getColor(R.color.appThemeColor_1));
        payByVoucher.setOnClickListener(new setOnClickList());

        btn_type1.setOnClickListener(new setOnClickList());


        setLabels();

        getTransactionHistory(false);
    }

    private void cardOptionVisible(boolean b) {
        if (b)
        {
            payBycard.setVisibility(View.VISIBLE);
            payBycard.setId(Utils.generateViewId());
            payBycard.setBackgroundColor(getResources().getColor(R.color.appThemeColor_1));
            payBycard.setOnClickListener(new setOnClickList());
        }
        else {
            payBycard.setVisibility(View.GONE);
        }
    }


    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LEFT_MENU_WALLET"));
        yourBalTxt.setText(generalFunc.retrieveLangLBl("", "LBL_USER_BALANCE"));
        paymentMthdTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_PAYMENT_METHOD_TXT"));
        viewTransactionsTxt.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_TRANS_HISTORY"));
        btn_type1.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_TRANS_HISTORY"));
        payBycard.setText(generalFunc.retrieveLangLBl("", "LBL_PAY_VIA_CARD_TXT"));
        payByVoucher.setText(generalFunc.retrieveLangLBl("", "LBL_PAY_VIA_VOUCHER_TXT"));

        required_str = generalFunc.retrieveLangLBl("REQUIRED", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_money_str = generalFunc.retrieveLangLBl("Please add correct details.", "LBL_ADD_CORRECT_DETAIL_TXT");

    }


    public void closeLoader() {
        if (loading_wallet_history.getVisibility() == View.VISIBLE) {
            loading_wallet_history.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();
        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getTransactionHistory(false);
            }
        });
    }

    public void getTransactionHistory(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_wallet_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_wallet_history.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTransactionHistory");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("page", next_page_str);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    closeLoader();

                    String LBL_BALANCE = generalFunc.getJsonValue("user_available_balance", responseString);

                    ((MTextView) findViewById(R.id.yourBalTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_USER_BALANCE"));
                    ((MTextView) findViewById(R.id.walletamountTxt)).setText(LBL_BALANCE);


                } else {
                    if (isLoadMore == false) {
                        generateErrorView();
                    }
                }

                mIsLoading = false;
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return MyWalletActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Bundle bn = new Bundle();
            bn.putString("UserProfileJson", userProfileJson);

            if (view.getId() == btn_type1.getId()) {
                new StartActProcess(getActContext()).startAct(MyWalletHistoryActivity.class);
            }if (view.getId() == payBycard.getId()) {
                new StartActProcess(getActContext()).startActForResult(AddMoneyByWalletActivity.class, bn, Utils.WALLET_CREDIT_CODE);
            }if (view.getId() == payByVoucher.getId()) {
                new StartActProcess(getActContext()).startActForResult(AddMoneyByVoucherActivity.class, bn, Utils.VOUCHER_CREDIT_CODE);
            }
            switch (view.getId()) {

                case R.id.backImgView:
                    MyWalletActivity.super.onBackPressed();
                    break;
                case R.id.viewTransactionsTxt:
                    new StartActProcess(getActContext()).startAct(MyWalletHistoryActivity.class);
                    break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.WALLET_CREDIT_CODE && resultCode == RESULT_OK) {
           getTransactionHistory(false);
        } else if (requestCode == Utils.VOUCHER_CREDIT_CODE && resultCode == RESULT_OK) {
            getTransactionHistory(false);
        }
    }

}
