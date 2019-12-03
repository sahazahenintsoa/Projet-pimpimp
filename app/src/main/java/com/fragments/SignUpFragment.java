package com.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.OpenMainProfile;
import com.general.files.SetOnTouchList;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.pimpimp.passenger.AppLoginActivity;
import com.pimpimp.passenger.R;
import com.pimpimp.passenger.SelectCountryActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    View view;
    GenerateAlertBox generateAlert;
    AppLoginActivity appLoginAct;
    GeneralFunctions generalFunc;

    MaterialEditText fNameBox;
    MaterialEditText lNameBox;
    MaterialEditText emailBox;
    MaterialEditText passwordBox;
    MaterialEditText invitecodeBox;
    MaterialEditText countryBox;
    MaterialEditText mobileBox;

    MButton btn_type2;

    SignUpFragment signUpFrag;
    ImageView inviteQueryImg;

    LinearLayout inviteCodeArea;

    String vCountryCode = "";
    String vPhoneCode = "";
    boolean isCountrySelected = false;

    String required_str = "";
    String mobile_length_str = "";
    String error_email_str = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        appLoginAct = (AppLoginActivity) getActivity();
        generalFunc = appLoginAct.generalFunc;
        generateAlert = new GenerateAlertBox(getActContext());
        signUpFrag = appLoginAct.getSignUpFrag();

        fNameBox = (MaterialEditText) view.findViewById(R.id.fNameBox);
        lNameBox = (MaterialEditText) view.findViewById(R.id.lNameBox);
        emailBox = (MaterialEditText) view.findViewById(R.id.emailBox);
        countryBox = (MaterialEditText) view.findViewById(R.id.countryBox);
        mobileBox = (MaterialEditText) view.findViewById(R.id.mobileBox);
        passwordBox = (MaterialEditText) view.findViewById(R.id.passwordBox);
        invitecodeBox = (MaterialEditText) view.findViewById(R.id.invitecodeBox);

        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();

        inviteQueryImg = (ImageView) view.findViewById(R.id.inviteQueryImg);

        inviteCodeArea = (LinearLayout) view.findViewById(R.id.inviteCodeArea);

        inviteQueryImg.setColorFilter(Color.parseColor("#CECECE"));

        inviteQueryImg.setOnClickListener(new setOnClickList());

        inviteCodeArea.setVisibility(View.GONE);

        if (generalFunc.isReferralSchemeEnable())
        {
            inviteCodeArea.setVisibility(View.VISIBLE);
        }

        vCountryCode = "MG";
        vPhoneCode = "261";
        isCountrySelected = true;
        countryBox.setText(vPhoneCode);


        removeInput();
        setLabels();

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());

        passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mobileBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        fNameBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lNameBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        emailBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passwordBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mobileBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return view;
    }

    public void removeInput() {
        Utils.removeInput(countryBox);
    }

    public void setLabels() {

        fNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_FIRST_NAME_HEADER_TXT"));
        lNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LAST_NAME_HEADER_TXT"));
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        countryBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COUNTRY_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        passwordBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_PASSWORD_LBL_TXT"));

        if (generalFunc.retrieveValue(CommonUtilities.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes")) {
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_NEXT_TXT"));
        } else {
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_REGISTER_TXT"));
        }

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        mobile_length_str = generalFunc.retrieveLangLBl("", "LBL_MOBILE_LENGTH_REQUIRED_TXT")+" "+Utils.maxMobileDigits +" "+generalFunc.retrieveLangLBl("", "LBL_DIGIT_TXT");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");

        invitecodeBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_INVITE_CODE"), generalFunc.retrieveLangLBl("", "LBL_INVITE_CODE_HINT"));

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            if(!isAdded()) {
                return;
            }

            int i = view.getId();
            if (i == btn_type2.getId()) {
                checkData();
            } else if (i == R.id.countryBox) {
                new StartActProcess(getActContext()).startActForResult(signUpFrag, SelectCountryActivity.class, Utils.SELECT_COUNTRY_REQ_CODE);
            }
            else if(i==inviteQueryImg.getId())
            {
                generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Referral Scheme", "LBL_REFERAL_SCHEME_TXT"),
                        generalFunc.retrieveLangLBl("", "LBL_REFERAL_SCHEME"));


            }

        }
    }

    public void checkData() {
        Utils.hideKeyboard(getActContext());

        String noWhiteSpace = generalFunc.retrieveLangLBl("Password should not contain whitespace.", "LBL_ERROR_NO_SPACE_IN_PASS");
        String pass_length = generalFunc.retrieveLangLBl("Password must be", "LBL_ERROR_PASS_LENGTH_PREFIX")
                + " " + Utils.minPasswordLength + " " + generalFunc.retrieveLangLBl("or more character long.","LBL_ERROR_PASS_LENGTH_SUFFIX");

        boolean fNameEntered = Utils.checkText(fNameBox) ? true : Utils.setErrorFields(fNameBox, required_str);
        boolean lNameEntered = Utils.checkText(lNameBox) ? true : Utils.setErrorFields(lNameBox, required_str);
        boolean emailEntered = Utils.checkText(emailBox) ?
                (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                : Utils.setErrorFields(emailBox, required_str);
        boolean mobileEntered = Utils.checkText(mobileBox) ? true : Utils.setErrorFields(mobileBox, required_str);
        boolean mobileLengthEntered = Utils.checkText(mobileBox) ?(Utils.getText(mobileBox).length()==Utils.maxMobileDigits ? true : Utils.setErrorFields(mobileBox, mobile_length_str)) : Utils.setErrorFields(mobileBox, required_str);
        boolean countryEntered = isCountrySelected ? true : Utils.setErrorFields(countryBox, required_str);
        boolean passwordEntered = Utils.checkText(passwordBox) ?
                (Utils.getText(passwordBox).contains(" ") ? Utils.setErrorFields(passwordBox, noWhiteSpace)
                        : (Utils.getText(passwordBox).length() >= Utils.minPasswordLength ? true : Utils.setErrorFields(passwordBox, pass_length)))
                : Utils.setErrorFields(passwordBox, required_str);

        if (fNameEntered == false || lNameEntered == false || emailEntered == false || mobileEntered == false ||mobileLengthEntered==false
                || countryEntered == false || passwordEntered == false) {
            return;
        }

        if (generalFunc.retrieveValue(CommonUtilities.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes")) {
            checkUserExist();
        } else {
            registerUser();
        }
    }

    public void registerUser() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "signup");
        parameters.put("vFirstName", Utils.getText(fNameBox));
        parameters.put("vLastName", Utils.getText(lNameBox));
        parameters.put("vEmail", Utils.getText(emailBox));
        parameters.put("vPhone", Utils.getText(mobileBox));
        parameters.put("vPassword", Utils.getText(passwordBox));
        parameters.put("PhoneCode", vPhoneCode);
        parameters.put("CountryCode", vCountryCode);
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("vInviteCode", Utils.getText(invitecodeBox));


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken");
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        new SetUserData(responseString, generalFunc,getActContext());

                        new OpenMainProfile(getActContext(),
                                generalFunc.getJsonValue(CommonUtilities.message_str, responseString), false, generalFunc).startProcess();
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

    public void checkUserExist() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "isUserExist");
        parameters.put("Email", Utils.getText(emailBox));
        parameters.put("Phone", Utils.getText(mobileBox));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        notifyVerifyMobile();
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

    public void notifyVerifyMobile() {
        Bundle bn = new Bundle();
        bn.putString("MOBILE", vPhoneCode + Utils.getText(mobileBox));
        generalFunc.verifyMobile(bn, signUpFrag);
    }

    public Context getActContext() {
        return appLoginAct.getActContext();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == appLoginAct.RESULT_OK && data != null) {
            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;
            countryBox.setText("+" + vPhoneCode);
        } else if (requestCode == Utils.VERIFY_MOBILE_REQ_CODE && resultCode == appLoginAct.RESULT_OK) {
            registerUser();
        }
    }
}
