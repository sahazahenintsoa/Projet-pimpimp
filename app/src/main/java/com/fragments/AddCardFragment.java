package com.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.pimpimp.passenger.CardPaymentActivity;
import com.pimpimp.passenger.R;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MButton;
import com.view.MaterialRippleLayout;
import com.view.MyProgressDialog;
import com.view.editBox.MaterialEditText;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCardFragment extends Fragment {

    GeneralFunctions generalFunc;
    View view;

    CardPaymentActivity cardPayAct;

    String userProfileJson;
    MButton btn_type2;
    MaterialEditText creditCardBox;
    MaterialEditText cvvBox;
    MaterialEditText mmBox;
    MaterialEditText yyBox;

    String required_str = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_card, container, false);

        cardPayAct = (CardPaymentActivity) getActivity();
        generalFunc = cardPayAct.generalFunc;
        userProfileJson = cardPayAct.userProfileJson;
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        creditCardBox = (MaterialEditText) view.findViewById(R.id.creditCardBox);
        cvvBox = (MaterialEditText) view.findViewById(R.id.cvvBox);
        mmBox = (MaterialEditText) view.findViewById(R.id.mmBox);
        yyBox = (MaterialEditText) view.findViewById(R.id.yyBox);

        if(getArguments().getString("PAGE_MODE").equals("ADD_CARD")){
            cardPayAct.changePageTitle(generalFunc.retrieveLangLBl("", "LBL_ADD_CARD"));
        }else{
            cardPayAct.changePageTitle(generalFunc.retrieveLangLBl("Change Card", "LBL_CHANGE_CARD"));
        }

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());

        setLabels();


        mmBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        yyBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        cvvBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        creditCardBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        return view;
    }

    public void setLabels() {
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_CARD"));
        creditCardBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HINT_TXT"));
        cvvBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CVV_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CVV_HINT_TXT"));
        mmBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"));
        yyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"));

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == btn_type2.getId()) {
                checkDetails();
            }
        }
    }

    public void checkDetails() {

        Card card = new Card(Utils.getText(creditCardBox), generalFunc.parseIntegerValue(0, Utils.getText(mmBox)),
                generalFunc.parseIntegerValue(0, Utils.getText(yyBox)), Utils.getText(cvvBox));

        Utils.printLog("Card No", ":" + card.validateNumber() + "::num::" + card.getNumber());
        boolean cardNoEntered = Utils.checkText(creditCardBox) ? (card.validateNumber() ? true :
                Utils.setErrorFields(creditCardBox, generalFunc.retrieveLangLBl("", "LBL_INVALID")))
                : Utils.setErrorFields(creditCardBox, required_str);
        boolean cvvEntered = Utils.checkText(cvvBox) ? (card.validateCVC() ? true :
                Utils.setErrorFields(cvvBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(cvvBox, required_str);
        boolean monthEntered = Utils.checkText(mmBox) ? (card.validateExpMonth() ? true :
                Utils.setErrorFields(mmBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(mmBox, required_str);
        boolean yearEntered = Utils.checkText(yyBox) ? (card.validateExpYear() ? true :
                Utils.setErrorFields(yyBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(yyBox, required_str);

        if (cardNoEntered == false || cvvEntered == false || monthEntered == false || yearEntered == false) {
            return;
        }

        Utils.printLog("Mask", Utils.maskCardNumber(card.getNumber()));
        if (card.validateCard()) {
            generateToken(card);
        }
    }

    public MyProgressDialog showLoader() {
        MyProgressDialog myPDialog = new MyProgressDialog(getActContext(), false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();

        return myPDialog;
    }

    public void generateToken(final Card card) {

        Utils.printLog("Pub Key", "::" + generalFunc.getJsonValue("STRIPE_PUBLISH_KEY", userProfileJson));
        final MyProgressDialog myPDialog = showLoader();

        String STRIPE_PUBLISH_KEY = generalFunc.getJsonValue("STRIPE_PUBLISH_KEY", userProfileJson);
        Stripe stripe = new Stripe();

        stripe.createToken(card, STRIPE_PUBLISH_KEY, new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
                myPDialog.close();
                Utils.printLog("Token", "::" + token.getId());
                CreateCustomer(card, token.getId());
            }

            public void onError(Exception error) {
                myPDialog.close();
                Utils.printLog("Error", "::" + error.toString());
                generalFunc.showError();
            }
        });
    }

    public void CreateCustomer(Card card, String vStripeToken) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateCustomer");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vStripeToken", vStripeToken);
        parameters.put("CardNo", Utils.maskCardNumber(card.getNumber()));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response--", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        cardPayAct.changeUserProfileJson(generalFunc.getJsonValue(CommonUtilities.message_str, responseString));
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
        return cardPayAct.getActContext();
    }
}
