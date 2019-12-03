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
public class AddMoneyByWalletActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    ProgressBar loading_wallet_history;
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

    private MTextView addMoneybtn1;
    private MTextView addMoneybtn2;
    private MTextView addMoneybtn3;
    private MTextView withDrawMoneyTxt;
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
        setContentView(R.layout.activity_addbywallet);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        loading_wallet_history = (ProgressBar) findViewById(R.id.loading_wallet_history);
        viewTransactionsTxt = (MTextView) findViewById(R.id.viewTransactionsTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);
        addMoneybtn1 = (MTextView) findViewById(R.id.addMoneybtn1);
        addMoneybtn2 = (MTextView) findViewById(R.id.addMoneybtn2);
        addMoneybtn3 = (MTextView) findViewById(R.id.addMoneybtn3);
        withDrawMoneyTxt = (MTextView) findViewById(R.id.withDrawMoneyTxt);
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
        addMoneybtn1.setOnClickListener(new setOnClickList());
        addMoneybtn2.setOnClickListener(new setOnClickList());
        addMoneybtn3.setOnClickListener(new setOnClickList());
        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());
        termsTxt.setOnClickListener(new setOnClickList());

        withDrawMoneyTxt.setMovementMethod(LinkMovementMethod.getInstance());
        termsTxt.setPaintFlags(termsTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        payPalConfigurations();

        setLabels();
        loading_wallet_history.setVisibility(View.GONE);
        withDrawMoneyTxt.setVisibility(View.GONE);

        ((LinearLayout) findViewById(R.id.addMoneyToWalletArea)).setVisibility(View.VISIBLE);
    }

    private void payPalConfigurations() {

        Utils.printLog("Api","CONFIG_CLIENT_ID"+generalFunc.retrieveValue(CommonUtilities.CONFIG_CLIENT_ID));

        config = new PayPalConfiguration().environment(CONFIG_ENVIRONMENT)
                .clientId(generalFunc.retrieveValue(CommonUtilities.CONFIG_CLIENT_ID))
                        // The following are only used in PayPalFuturePaymentActivity.
                .merchantName(CommonUtilities.DISPLAY_TAG)
                .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

    }

    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LEFT_MENU_WALLET"));
        viewTransactionsTxt.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_TRANS_HISTORY"));

        rechargeBox.setBothText(generalFunc.retrieveLangLBl("Recharge Amount", "LBL_RECHARGE_AMOUNT_TXT"), generalFunc.retrieveLangLBl("Recharge Amount", "LBL_RECHARGE_AMOUNT_TXT"));
        rechargeBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        rechargeBox.getLabelFocusAnimator().start();


        withDrawMoneyTxt.setText(generalFunc.retrieveLangLBl("With Draw Money", "LBL_WITHDRAW_MONEY_TXT"));
        addMoneyTxt.setText(generalFunc.retrieveLangLBl("Add Money", "LBL_ADD_MONEY_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("Add Money", "LBL_ADD_MONEY_TXT"));
        addMoneyTagTxt.setText(generalFunc.retrieveLangLBl("It's safe n secure", "LBL_ADD_MONEY_TXT1"));
        policyTxt.setText(generalFunc.retrieveLangLBl("By conducting you choose to accept Pimpimp's", "LBL_PRIVACY_POLICY"));
        termsTxt.setText(generalFunc.retrieveLangLBl("Terms & Privacy Policy", "LBL_PRIVACY_POLICY1"));

        required_str = generalFunc.retrieveLangLBl("REQUIRED", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_money_str = generalFunc.retrieveLangLBl("Please add correct details.", "LBL_ADD_CORRECT_DETAIL_TXT");

        addMoneybtn1.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_1", userProfileJson));
        addMoneybtn2.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_2", userProfileJson));
        addMoneybtn3.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_3", userProfileJson));
    }

    public void checkValues() {
        Utils.hideKeyboard(getActContext());

        int moneyAdded = 0;

        if (Utils.checkText(rechargeBox) == true) {

            moneyAdded = generalFunc.parseIntegerValue(0, Utils.getText(rechargeBox));
        }
        boolean addMoneyAmountEntered = Utils.checkText(rechargeBox) ? (moneyAdded > 0 ? true : Utils.setErrorFields(rechargeBox, error_money_str))
                : Utils.setErrorFields(rechargeBox, required_str);

        boolean addMoneyAmountEntered1= ((generalFunc.parseDoubleValue(1, generalFunc.retrieveValue(CommonUtilities.CURRENCY_VAL_PAYPAL))*generalFunc.parseDoubleValue(0, Utils.getText(rechargeBox)))) > 0 ? true : Utils.setErrorFields(rechargeBox, error_money_str);

        if (addMoneyAmountEntered == false ||addMoneyAmountEntered1==false) {
            return;
        }

        StartPaymentProcedure();
    }

    public void StartPaymentProcedure() {

        Utils.printLog("Api","CURRENCY_VAL_PAYPAL"+generalFunc.retrieveValue(CommonUtilities.CURRENCY_VAL_PAYPAL));
        Utils.printLog("Api","CURRENCY_CODE_PAYPAL"+generalFunc.retrieveValue(CommonUtilities.CURRENCY_CODE_PAYPAL));

        double moneyAdded =(generalFunc.parseDoubleValue(1, generalFunc.retrieveValue(CommonUtilities.CURRENCY_VAL_PAYPAL))*generalFunc.parseDoubleValue(0, Utils.getText(rechargeBox)));
        Utils.printLog("Api","moneyAddedStr"+moneyAdded);



        PayPalPayment payment = new PayPalPayment(new BigDecimal("" + moneyAdded), "" + generalFunc.retrieveValue(CommonUtilities.CURRENCY_CODE_PAYPAL),
                CommonUtilities.DISPLAY_TAG, PayPalPayment.PAYMENT_INTENT_SALE);


        Intent intent_start_paymentAct = new Intent(getActContext(), PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent_start_paymentAct.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent_start_paymentAct.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent_start_paymentAct, REQUEST_PAYPAL_PAYMENT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PAYPAL_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {

                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());

                        JSONObject obj_response = jsonObj.getJSONObject("response");

                        String status_str = obj_response.getString("state");
                        String payment_id_str = obj_response.getString("id");

                        if (status_str.trim().equals("approved")) {

                            paymentId_str = payment_id_str;
                            String payment_client = confirm.getPayment().toJSONObject().toString();
                            Utils.printLog("Api","payment_client"+payment_client.toString());
                            Toast.makeText(getApplicationContext(), payment_id_str, Toast.LENGTH_LONG).show();

                            addMoneyToWallet(payment_id_str);

                        } else {
                            buildPaymentErrorDialog();
                        }

                    } catch (JSONException e) {
                        // Log.e("paymentExample", "an extremely unlikely
                        // failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                // Log.i("paymentExample", "An invalid Payment was submitted.
                // Please see the docs.");
            }
        }

    }

    public void buildPaymentErrorDialog() {



        if (alert_paymentStatusError != null) {
            alert_paymentStatusError.dismiss();
        }

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage("" +generalFunc.retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT")).setCancelable(false)
                .setPositiveButton("" + generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        alert_paymentStatusError.dismiss();
                    }
                });
        alert_paymentStatusError = builder.create();
        alert_paymentStatusError.show();
    }
    private void addMoneyToWallet(String payment_id_str) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "addMoneyUserWallet");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("fAmount", Utils.getText(rechargeBox));
        parameters.put("UserType", CommonUtilities.app_type);

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
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        finish();

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
        if (loading_wallet_history.getVisibility() == View.VISIBLE) {
            loading_wallet_history.setVisibility(View.GONE);
        }
    }



    public Context getActContext() {
        return AddMoneyByWalletActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == btn_type2.getId()) {
                checkValues();
            }
            switch (view.getId()) {
                case R.id.backImgView:
                    AddMoneyByWalletActivity.super.onBackPressed();
                    break;
                case R.id.viewTransactionsTxt:
                    new StartActProcess(getActContext()).startAct(MyWalletHistoryActivity.class);
                    break;
                case R.id.addMoneybtn1:
                    rechargeBox.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_1", userProfileJson));
                    break;
                case R.id.addMoneybtn2:
                    rechargeBox.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_2", userProfileJson));
                    break;
                case R.id.addMoneybtn3:
                    rechargeBox.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_3", userProfileJson));
                    break;
                case R.id.termsTxt:
                    (new StartActProcess(getActContext())).openURL(CommonUtilities.SERVER_URL + "terms-condition");
                    break;
            }
        }
    }
}
