package com.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.adapter.files.RequestPickUpAdapter;
import com.dialogs.FareEstimate;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.pimpimp.passenger.FareEstimateActivity;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.MyWalletActivity;
import com.pimpimp.passenger.R;
import com.pimpimp.passenger.SelectCountryActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestPickUpFragment extends Fragment implements RequestPickUpAdapter.OnItemClickList {

    public final int BOTTOM_MENU_CASH = 1;
    public final int BOTTOM_MENU_CARD = 2;
    public final int BOTTOM_MENU_WALLET = 3;
    public final int BOTTOM_MENU_FARE_ESTIMATE = 3;
    public final int BOTTOM_MENU_PROMO = 4;

    View view;
    GeneralFunctions generalFunc;
    MainActivity mainAct;

    RecyclerView reqPickUpRecyclerView;
    RequestPickUpAdapter adapter;
    ArrayList<HashMap<String, String>> list_item;
    MButton requestPickUpBtn;

    String userProfileJson = "";
    String appliedPromoCode = "";
    int requestPickUpBtnId;

    boolean isCardValidated = false;
    boolean isProcessOnClicked = true;

    View payTypeSelectArea;
    MTextView payTypeTxt;

    RadioButton cashRadioBtn;
    RadioButton cardRadioBtn;
    RadioButton walletRadioBtn;
    ImageView payImgView;
    LinearLayout reqBtnArea;
    LinearLayout cardArea;
     String user_avail_bal="";
    private String required_str;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_request_pick_up, container, false);

        mainAct = (MainActivity) getActivity();
        generalFunc = mainAct.generalFunc;
        userProfileJson = mainAct.userProfileJson;


        reqPickUpRecyclerView = (RecyclerView) view.findViewById(R.id.reqPickUpRecyclerView);
        cardArea = (LinearLayout) view.findViewById(R.id.cardArea);
        reqBtnArea = (LinearLayout) view.findViewById(R.id.reqBtnArea);
        reqPickUpRecyclerView = (RecyclerView) view.findViewById(R.id.reqPickUpRecyclerView);
        payTypeTxt = (MTextView) view.findViewById(R.id.payTypeTxt);
        requestPickUpBtn = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        payTypeSelectArea = view.findViewById(R.id.payTypeSelectArea);
        cashRadioBtn = (RadioButton) view.findViewById(R.id.cashRadioBtn);
        walletRadioBtn = (RadioButton) view.findViewById(R.id.walletRadioBtn);
        cardRadioBtn = (RadioButton) view.findViewById(R.id.cardRadioBtn);
        payImgView = (ImageView) view.findViewById(R.id.payImgView);

        (view.findViewById(R.id.shadowView)).setVisibility(View.GONE);


        generateData();

        requestPickUpBtnId = Utils.generateViewId();
        requestPickUpBtn.setId(requestPickUpBtnId);

        requestPickUpBtn.setOnClickListener(new setOnClickList());
        (view.findViewById(R.id.paymentArea)).setOnClickListener(new setOnClickList());
        (view.findViewById(R.id.fareEstArea)).setOnClickListener(new setOnClickList());
        (view.findViewById(R.id.promoArea)).setOnClickListener(new setOnClickList());
        mainAct.setPanelHeight(135);
        mainAct.setUserLocImgBtnMargin(140);

        new CreateRoundedView(Color.parseColor("#FFFFFF"), 0, Utils.dipToPixels(mainAct.getActContext(), 1),
                Color.parseColor("#DDDDDD"), cashRadioBtn);
        new CreateRoundedView(Color.parseColor("#FFFFFF"), 0, Utils.dipToPixels(mainAct.getActContext(), 1),
                Color.parseColor("#DDDDDD"), cardRadioBtn);
        new CreateRoundedView(Color.parseColor("#FFFFFF"), 0, Utils.dipToPixels(mainAct.getActContext(), 1),
                Color.parseColor("#DDDDDD"), walletRadioBtn);
        setLabels();
        ((RadioGroup) view.findViewById(R.id.paymentTypeRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Utils.printLog("ID btn", "::" + i);
                hidePayTypeSelectionArea();

                if (radioGroup.getCheckedRadioButtonId() == R.id.cashRadioBtn) {
                    setCashSelection();
                }
                else if (radioGroup.getCheckedRadioButtonId() == R.id.walletRadioBtn){
                    setWalletSelection();
                }
                else if (isCardValidated == false && generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card") ) {
                    setCashSelection();
                    checkCardConfig();
                }
            }
        });

        if (!mainAct.getCabReqType().equals(Utils.CabReqType_Later)) {
            mainAct.selectedDateTime="";
            mainAct.selectedDateTimeZone="";
        }
        if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash")) {
            cashRadioBtn.setVisibility(View.VISIBLE);
            cardRadioBtn.setVisibility(View.GONE);
            walletRadioBtn.setVisibility(View.GONE);
            cardRadioBtn.setChecked(false);
            cashRadioBtn.setChecked(true);
            walletRadioBtn.setChecked(false);
            mainAct.setPaymentMode("Cash");
        } else if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card")) {
            cashRadioBtn.setVisibility(View.GONE);
            walletRadioBtn.setVisibility(View.GONE);
            cardRadioBtn.setVisibility(View.VISIBLE);
            cashRadioBtn.setChecked(false);
            walletRadioBtn.setChecked(false);
            setCardSelection();
            isCardValidated = false;
        }
        else if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Wallet"))
        {
            cashRadioBtn.setVisibility(View.GONE);
            cardRadioBtn.setVisibility(View.GONE);
            walletRadioBtn.setVisibility(View.VISIBLE);
            setWalletSelection();
            payTypeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WALLET"));
            mainAct.setPaymentMode("Wallet");
        }

        if (generalFunc.getJsonValue("APP_PAYMENT_CASH_ENABLED", userProfileJson).equalsIgnoreCase("Yes"))
        {
            if (!generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card")) {
                cashRadioBtn.setVisibility(View.VISIBLE);
                mainAct.setCashModeEnable(true);
            }
        }
        else
        {
            if (!generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash")) {
                cashRadioBtn.setVisibility(View.GONE);
            }
            mainAct.setCashModeEnable(false);
        }

        Utils.printLog("Data", "PayPalConfiguration" + generalFunc.getJsonValue("PayPalConfiguration", userProfileJson));

        if (generalFunc.getJsonValue("PROMO_CODE", userProfileJson).equalsIgnoreCase("YES")
                && !mainAct.getCabReqType().equals(Utils.CabReqType_Later)) {
            (view.findViewById(R.id.promoArea)).setVisibility(View.VISIBLE);
            (view.findViewById(R.id.promoSeperationLine)).setVisibility(View.VISIBLE);
        } else {
            (view.findViewById(R.id.promoArea)).setVisibility(View.GONE);
            (view.findViewById(R.id.promoSeperationLine)).setVisibility(View.GONE);
        }

        return view;
    }

    public void setLabels() {
        Utils.printLog("ReqType", "::" + mainAct.getCabReqType());
        requestPickUpBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_NEXT_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");

        cashRadioBtn.setText(generalFunc.retrieveLangLBl("", "LBL_CASH_TXT"));
        payTypeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WALLET"));
        cardRadioBtn.setText(generalFunc.retrieveLangLBl("", "LBL_CARD"));
        ((MTextView) view.findViewById(R.id.fareEstTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_FARE_ESTIMATE_TXT"));
        ((MTextView) view.findViewById(R.id.promoTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PRMO_TXT"));
    }

    public void generateData() {

        if (list_item == null) {
            list_item = new ArrayList<>();
            adapter = new RequestPickUpAdapter(getActContext(), list_item, generalFunc);

            reqPickUpRecyclerView.setAdapter(adapter);
            adapter.setOnItemClickList(this);
        } else {
            list_item.clear();
        }

        if (mainAct.getCurrentCabGeneralType().equals(Utils.CabGeneralType_Ride)) {
            list_item.add(buildMap(generalFunc.retrieveLangLBl("", "LBL_CASH_TXT"), R.mipmap.ic_cash, R.mipmap.ic_cash_hover, BOTTOM_MENU_CASH));
        }

        if (!generalFunc.getJsonValue("APP_PAYMENT_CASH_ENABLED", userProfileJson).equalsIgnoreCase("Yes") &&
                !generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card")) {
            list_item.add(buildMap(generalFunc.retrieveLangLBl("", "LBL_CASH_TXT"), R.mipmap.ic_cash, R.mipmap.ic_cash_hover, BOTTOM_MENU_CASH));
        }

        if (generalFunc.getJsonValue("PayPalConfiguration", userProfileJson).equalsIgnoreCase("Yes")
                && !mainAct.getCabReqType().equals(Utils.CabReqType_Later)) {
            list_item.add(buildMap(generalFunc.retrieveLangLBl("Card", "LBL_CARD"), R.mipmap.ic_card, R.mipmap.ic_card_hover, BOTTOM_MENU_CARD));
        }

        list_item.add(buildMap(generalFunc.retrieveLangLBl("", "LBL_FARE_ESTIMATE_TXT"), R.mipmap.ic_fare_estimate, R.mipmap.ic_fare_estimate_hover, BOTTOM_MENU_FARE_ESTIMATE));

        if (generalFunc.getJsonValue("PROMO_CODE", userProfileJson).equalsIgnoreCase("YES")) {
            list_item.add(buildMap(generalFunc.retrieveLangLBl("", "LBL_PRMO_TXT"), R.mipmap.ic_fare_estimate, R.mipmap.ic_fare_estimate_hover, BOTTOM_MENU_PROMO));
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == requestPickUpBtnId) {

                if (mainAct.getDestinationStatus() == false) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please add your destination location ", "LBL_ADD_DEST_MSG_ADD_ITEM"));
                    return;
                }

                Bundle bn=mainAct.getFareEstimateBundle("true");


                new StartActProcess(getActContext()).startActForResult(mainAct.getReqPickUpFrag(),
                        FareEstimateActivity.class, Utils.FARE_ESTIMATE_CODE,bn);

            } else if (i == R.id.paymentArea) {

                if (payTypeSelectArea.getVisibility() == View.VISIBLE) {
                    hidePayTypeSelectionArea();
                } else {
                    payTypeSelectArea.setVisibility(View.VISIBLE);

                    if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash") ||
                            generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card") || generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Wallet")) {
                        mainAct.setPanelHeight(202);
                        mainAct.setUserLocImgBtnMargin(215);
                    }
                    if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Wallet") && generalFunc.getJsonValue("APP_PAYMENT_CASH_ENABLED", userProfileJson).equalsIgnoreCase("Yes"))
                    {
                        mainAct.setUserLocImgBtnMargin(270);

                        mainAct.setPanelHeight(260);
                    }

                    else {
                        mainAct.setUserLocImgBtnMargin(280);
                        mainAct.setPanelHeight(260);
                    }
                }

            } else if (i == R.id.fareEstArea) {

                if (mainAct.getDestinationStatus() == false) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please add your destination location ", "LBL_ADD_DEST_MSG_ADD_ITEM"));

                    return;
                }

                Bundle bn=mainAct.getFareEstimateBundle("false");
                new StartActProcess(getActContext()).startActWithData(FareEstimateActivity.class, bn);

            } else if (i == R.id.promoArea) {
                showPromoBox();
            }
        }
    }

    public void hidePayTypeSelectionArea() {
        payTypeSelectArea.setVisibility(View.GONE);

        mainAct.setPanelHeight(135);
        mainAct.setUserLocImgBtnMargin(140);
    }

    public void setCashSelection() {
        payTypeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CASH_TXT"));

        isCardValidated = false;
        mainAct.setPaymentMode("Cash");
        cashRadioBtn.setChecked(true);
        cardRadioBtn.setChecked(false);
        walletRadioBtn.setChecked(false);

        payImgView.setImageResource(R.mipmap.ic_cash_new);
    }

    public void setCardSelection() {
        payTypeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CARD"));

        isCardValidated = true;
        mainAct.setPaymentMode("Card");
        cardRadioBtn.setChecked(true);
        cashRadioBtn.setChecked(false);
        walletRadioBtn.setChecked(false);

        payImgView.setImageResource(R.mipmap.ic_card_new);
    }

    public void setWalletSelection() {
        payTypeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WALLET"));

        isCardValidated = false;
        mainAct.setPaymentMode("Wallet");
        cardRadioBtn.setChecked(false);
        cashRadioBtn.setChecked(false);
        walletRadioBtn.setChecked(true);

        payImgView.setImageResource(R.mipmap.ic_card_new);
    }

    public HashMap<String, String> buildMap(String labelValue, int icon, int icon_hover, int position) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Title", labelValue);
        map.put("Icon", "" + icon);
        map.put("IconHover", "" + icon_hover);
        map.put("Position", "" + position);
        if (position == BOTTOM_MENU_FARE_ESTIMATE) {
            map.put("isDivider", "true");
        } else {
            map.put("isDivider", "false");
        }

        return map;
    }

    public Context getActContext() {
        return mainAct.getActContext();
    }

    public void setUserProfileJson() {
        userProfileJson = mainAct.userProfileJson;
    }

    @Override
    public void onItemClick(int position) {

        HashMap<String, String> map = list_item.get(position);
        int clickID = generalFunc.parseIntegerValue(0, map.get("Position"));

        for (int i = 0; i < list_item.size(); i++) {
            RequestPickUpAdapter.ViewHolder cabTypeViewHolder = (RequestPickUpAdapter.ViewHolder) reqPickUpRecyclerView.findViewHolderForAdapterPosition(i);

            if (clickID == BOTTOM_MENU_CASH || (clickID == BOTTOM_MENU_CARD && isCardValidated == true)) {
                if (i != position) {
                    adapter.setData(cabTypeViewHolder, i, false);
                } else if (i == position) {
                    adapter.setData(cabTypeViewHolder, i, true);

                }
            }
        }

        if (isProcessOnClicked == false) {
            isProcessOnClicked = true;
            return;
        }

        switch (clickID) {
            case BOTTOM_MENU_CASH:
                isCardValidated = false;
                mainAct.setPaymentMode("Cash");
                break;
            case BOTTOM_MENU_CARD:
                checkCardConfig();
                break;
            case BOTTOM_MENU_FARE_ESTIMATE:

                if (mainAct.getDestinationStatus() == false) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please add your destination location " +
                            "to deliver your package.", "LBL_ADD_DEST_MSG_DELIVER_ITEM"));
                    return;
                }
                Bundle bn=mainAct.getFareEstimateBundle("false");

                new StartActProcess(getActContext()).startActForResult(mainAct.getReqPickUpFrag(),
                        FareEstimateActivity.class, Utils.FARE_ESTIMATE_CODE,bn);

                break;
            case BOTTOM_MENU_PROMO:
                showPromoBox();
                break;
        }
    }

    public void checkCardConfig() {
        setUserProfileJson();

        String vStripeCusId = generalFunc.getJsonValue("vStripeCusId", userProfileJson);

        if (vStripeCusId.equals("")) {
            // Open CardPaymentActivity
            mainAct.OpenCardPaymentAct();
        } else {
            showPaymentBox();
        }
    }

    public void showPromoBox() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_PROMO_CODE_ENTER_TITLE"));

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);
        builder.setView(dialogView);

        final MaterialEditText input = (MaterialEditText) dialogView.findViewById(R.id.editBox);

        if (!appliedPromoCode.equals("")) {
            input.setText(appliedPromoCode);
        }

        builder.setPositiveButton(generalFunc.retrieveLangLBl("OK", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().trim().equals("") && appliedPromoCode.equals("")) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ENTER_PROMO"));
                } else if (input.getText().toString().trim().equals("") && !appliedPromoCode.equals("")) {
                    appliedPromoCode = "";
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_REMOVED"));
                } else if (input.getText().toString().contains(" ")) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_INVALIED"));
                } else {
                    checkPromoCode(input.getText().toString().trim());
                }
            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        android.support.v7.app.AlertDialog alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.show();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Utils.hideKeyboard(mainAct);
            }
        });
    }

    public void showPaymentBox() {
        android.support.v7.app.AlertDialog alertDialog;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);
        builder.setView(dialogView);

        final MaterialEditText input = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        final MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);

        Utils.removeInput(input);

        subTitleTxt.setVisibility(View.VISIBLE);
        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TITLE_PAYMENT_ALERT"));
        input.setText(generalFunc.getJsonValue("vCreditCard", userProfileJson));

        builder.setPositiveButton(generalFunc.retrieveLangLBl("Confirm", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                checkPaymentCard();
            }
        });
        builder.setNeutralButton(generalFunc.retrieveLangLBl("Change", "LBL_CHANGE"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mainAct.OpenCardPaymentAct();
            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void checkPaymentCard() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckCard");
        parameters.put("iUserId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    String action = generalFunc.getJsonValue(CommonUtilities.action_str, responseString);
                    if (action.equals("1")) {

                        setCardSelection();
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }
    public String getAppliedPromoCode() {
        return this.appliedPromoCode;
    }

    public void checkPromoCode(final String promoCode) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckPromoCode");
        parameters.put("PromoCode", promoCode);
        parameters.put("iUserId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    String action = generalFunc.getJsonValue(CommonUtilities.action_str, responseString);
                    if (action.equals("1")) {
                        appliedPromoCode = promoCode;
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_APPLIED"));
                    } else if (action.equals("01")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_USED"));
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PROMO_INVALIED"));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.printLog("Api", "requestCode " + requestCode + "resultCode " + resultCode + "data " + data);
        if (requestCode==Utils.FARE_ESTIMATE_CODE && resultCode==mainAct.RESULT_OK && data != null)
        {

            String responseString="";
            if (data.hasExtra("responseString")) {
                responseString = data.getStringExtra("responseString");
            }

            if (mainAct.getDestinationStatus()) {
                String destLocAdd = mainAct != null ? (mainAct.getDestAddress().equals(
                        generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT")) ? "" : mainAct.getDestAddress()) : "";
                if (destLocAdd.equals("")) {
                    return;
                }
            }

            if (isCardValidated == false && generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card")) {
                checkCardConfig();
                return;
            }

            if (mainAct.paymentMode.equalsIgnoreCase("Wallet") && cashRadioBtn.isChecked()==true) {
                isCardValidated = false;
                mainAct.setPaymentMode("Cash");
            }



            if (mainAct.paymentMode.equalsIgnoreCase("Wallet") && cashRadioBtn.isChecked()==false) {
                double user_avail_bal=generalFunc.parseDoubleValue(0, generalFunc.getJsonValue("user_avail_bal", responseString).toString().replace(",", ""));
                double  total_fare = generalFunc.parseDoubleValue(0, generalFunc.getJsonValue("total_fare", responseString).toString().replace(",", ""));
                getWalletAmount(data.getStringExtra("SelectedCarId"),""+total_fare);
                Utils.printLog("Api", "fareEstimatePrice" + user_avail_bal + "getWalletAmount()" + total_fare);
            }
            else
            {
                if (!mainAct.getCabReqType().equals(Utils.CabReqType_Later)) {
                    mainAct.requestPickUp();
                } else {

                    addPickUpInstruction();

                }
            }

        }
    }

    private void addPickUpInstruction() {

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
            builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_PICKUP_INST_ENTER_TITLE"));

            LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.input_box_view, null);
            builder.setView(dialogView);

            final MaterialEditText input = (MaterialEditText) dialogView.findViewById(R.id.editBox);

           input.setBothText("",generalFunc.retrieveLangLBl("Enter pickup instruction", "LBL_ADD_PICKUP_INST_TXT"));

            builder.setPositiveButton(generalFunc.retrieveLangLBl("OK", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean pickupInstEntered = Utils.checkText(input) ? true : Utils.setErrorFields(input, required_str);

                    if (pickupInstEntered==false)
                    {
                        return;
                    }
                    else {
                        mainAct.setRideSchedule("" + Utils.getText(input));
                    }
                }
            });
            builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    mainAct.setRideSchedule("");
                }
            });

            android.support.v7.app.AlertDialog alertDialog = builder.create();
            if (generalFunc.isRTLmode() == true) {
                generalFunc.forceRTLIfSupported(alertDialog);
            }
            alertDialog.show();
            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Utils.hideKeyboard(mainAct);
                }
            });

    }

    public void buildWarningMessage(String message, String positiveBtn, String negativeBtn, final boolean isCancelTripWarning) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(mainAct.getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();

                if (btn_id == 1) {

                    Bundle bn = new Bundle();
                    bn.putString("UserProfileJson", userProfileJson);
                    new StartActProcess(getActContext()).startActWithData(MyWalletActivity.class, bn);
                 }

            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);

        generateAlert.showAlertBox();
    }

    private void getWalletAmount(String selectedCarId,String totalFare) {
            if (mainAct.loader.getVisibility() != View.VISIBLE) {
                mainAct.loader.setVisibility(View.VISIBLE);
            }
            final HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "getRiderWalletDetail");
            parameters.put("iUserId", generalFunc.getMemberId());
            parameters.put("iVehicleTypeId", selectedCarId);
            parameters.put("total_fare", totalFare);

            final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
            exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
                @Override
                public void setResponse(String responseString) {

                    if (responseString != null && !responseString.equals("")) {

                        mainAct.loader.setVisibility(View.GONE);

                        Utils.printLog("Api","action_str"+generalFunc.checkDataAvail(CommonUtilities.action_str, responseString));
                        if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {
                            if (!mainAct.getCabReqType().equals(Utils.CabReqType_Later)) {
                                mainAct.requestPickUp();
                            } else {
                                addPickUpInstruction();
                            }


                        }
                        else {

                            buildWarningMessage(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)),
                                    generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), "", false);
                        }

                    }
                    else {
                        generalFunc.showError();
                    }

                }
            });
            exeWebServer.execute();

    }
}
