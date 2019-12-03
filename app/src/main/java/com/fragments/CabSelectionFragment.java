package com.fragments;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.adapter.files.CabTypeAdapter;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.pimpimp.passenger.FareEstimateActivity;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.R;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CabSelectionFragment extends Fragment implements CabTypeAdapter.OnItemClickList {

    public LinearLayout rideBtnContainer;
    public MButton ride_now_btn;
    public int currentPanelDefaultStateHeight = 100;
    public String currentCabGeneralType;
    View view = null;
    MainActivity mainAct;
    GeneralFunctions generalFunc;
    String userProfileJson = "";
    RecyclerView carTypeRecyclerView;
    CabTypeAdapter adapter;
    ArrayList<HashMap<String, String>> cabTypeList;
    ArrayList<HashMap<String, String>> cabCategoryList;
    MTextView fareEstimateTxt;
    MTextView personSizeVTxt;
    LinearLayout minFareArea;
    String currency_sign = "";
    MButton ride_later_btn;
    boolean isKilled = false;
    String app_type = "Ride";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.fragment_cab_selection, container, false);

        mainAct = (MainActivity) getActivity();
        carTypeRecyclerView = (RecyclerView) view.findViewById(R.id.carTypeRecyclerView);
        fareEstimateTxt = (MTextView) view.findViewById(R.id.fareEstimateTxt);
        personSizeVTxt = (MTextView) view.findViewById(R.id.personSizeVTxt);
        minFareArea = (LinearLayout) view.findViewById(R.id.minFareArea);
        rideBtnContainer = (LinearLayout) view.findViewById(R.id.rideBtnContainer);
        ride_later_btn = ((MaterialRippleLayout) view.findViewById(R.id.ride_later_btn)).getChildView();
        ride_now_btn = ((MaterialRippleLayout) view.findViewById(R.id.ride_now_btn)).getChildView();

        generalFunc = mainAct.generalFunc;

        userProfileJson = mainAct.userProfileJson;

        currency_sign = generalFunc.getJsonValue("CurrencySymbol", userProfileJson);
        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);

        isKilled = false;


        generateCarType();


        setLabels();

        ride_later_btn.setId(Utils.generateViewId());
        ride_now_btn.setId(Utils.generateViewId());

        fareEstimateTxt.setOnClickListener(new setOnClickList());
        minFareArea.setOnClickListener(new setOnClickList());
        ride_later_btn.setOnClickListener(new setOnClickList());
        ride_now_btn.setOnClickListener(new setOnClickList());

        setPersonSize();
        setMinFare();
        configRideLaterBtnArea(false);

        mainAct.sliding_layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                if (isKilled) {
                    return;
                }

                rideBtnContainer.setVisibility(View.GONE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if (isKilled) {
                    return;
                }
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    configRideLaterBtnArea(false);
                }
            }
        });

        return view;
    }

    public void setLabels() {
        ((MTextView) view.findViewById(R.id.etaHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ETA_TXT"));
        ((MTextView) view.findViewById(R.id.personSizeHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MAX_SIZE_TXT"));
        ((MTextView) view.findViewById(R.id.minFareHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MIN_FARE_TXT"));
        fareEstimateTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GET_FARE_EST_TXT"));

        ride_now_btn.setText(generalFunc.retrieveLangLBl("Ride Now", "LBL_RIDE_NOW"));
        ride_later_btn.setText(generalFunc.retrieveLangLBl("Ride Later", "LBL_RIDE_LATER"));
    }

    public void releaseResources() {
        isKilled = true;
    }

    public void changeCabGeneralType(String currentCabGeneralType) {
        if (!this.currentCabGeneralType.equals(currentCabGeneralType)) {
            this.currentCabGeneralType = currentCabGeneralType;
            generateCarType();

        }
    }

    public String getCurrentCabGeneralType() {
        return this.currentCabGeneralType;


    }

    public void configRideLaterBtnArea(boolean isGone) {
        if (isGone == true || app_type.equalsIgnoreCase("Ride-Delivery")) {
            rideBtnContainer.setVisibility(View.GONE);
            mainAct.setPanelHeight(100);
            if (!app_type.equalsIgnoreCase("Ride-Delivery")) {
                mainAct.setUserLocImgBtnMargin(105);
            }
            return;
        }
        if (!generalFunc.getJsonValue("RIIDE_LATER", userProfileJson).equalsIgnoreCase("YES") && !app_type.equalsIgnoreCase("Ride-Delivery")) {
            rideBtnContainer.setVisibility(View.GONE);
            mainAct.setUserLocImgBtnMargin(105);
            mainAct.setPanelHeight(100);
        }  else {
            rideBtnContainer.setVisibility(View.VISIBLE);
            mainAct.setPanelHeight(159);
            currentPanelDefaultStateHeight = 159;
            mainAct.setUserLocImgBtnMargin(164);
        }
    }

    public void setETA(String time) {
        if (view != null) {
            ((MTextView) view.findViewById(R.id.etaVTxt)).setText(time);
        }
    }

    public void setPersonSize() {
        personSizeVTxt.setText(generalFunc.getSelectedCarTypeData(mainAct.getSelectedCabTypeId(), "VehicleTypes", "iPersonSize", userProfileJson)
                + " " + generalFunc.retrieveLangLBl("", "LBL_PEOPLE_TXT"));
    }

    public void setMinFare() {
        ((MTextView) view.findViewById(R.id.minFareVTxt)).setText(currency_sign + " " +
                generalFunc.getSelectedCarTypeData(mainAct.getSelectedCabTypeId(), "VehicleTypes", "iMinFare", userProfileJson));
    }

    public void generateCarType() {

        if (cabTypeList == null) {
            cabTypeList = new ArrayList<>();
            adapter = new CabTypeAdapter(getActContext(), cabTypeList, generalFunc);
            carTypeRecyclerView.setAdapter(adapter);
            adapter.setOnItemClickList(this);
        } else {
            cabTypeList.clear();
        }

        JSONArray vehicleTypesArr = generalFunc.getJsonArray("VehicleTypes", userProfileJson);

        for (int i = 0; i < vehicleTypesArr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(vehicleTypesArr, i);

            HashMap<String, String> map = new HashMap<>();
            String iVehicleCategoryId = "";
            String iVehicleTypeId = generalFunc.getJsonValue("iVehicleTypeId", obj_temp.toString());

            String vVehicleType = generalFunc.getJsonValue("vVehicleType", obj_temp.toString());
            String fPricePerKM = generalFunc.getJsonValue("fPricePerKM", obj_temp.toString());
            String fPricePerMin = generalFunc.getJsonValue("fPricePerMin", obj_temp.toString());
            String iBaseFare = generalFunc.getJsonValue("iBaseFare", obj_temp.toString());
            String fCommision = generalFunc.getJsonValue("fCommision", obj_temp.toString());
            String iPersonSize = generalFunc.getJsonValue("iPersonSize", obj_temp.toString());
            String vLogo = generalFunc.getJsonValue("vLogo", obj_temp.toString());
            String eType = generalFunc.getJsonValue("eType", obj_temp.toString());
           if (!eType.equalsIgnoreCase(currentCabGeneralType)) {
                continue;
            }

            map.put("iVehicleTypeId", iVehicleTypeId);
            map.put("vVehicleType", vVehicleType);
            map.put("fPricePerKM", fPricePerKM);
            map.put("fPricePerMin", fPricePerMin);
            map.put("iBaseFare", iBaseFare);
            map.put("fCommision", fCommision);
            map.put("iPersonSize", iPersonSize);
            map.put("vLogo", vLogo);

            if (cabTypeList.size() == 0) {
                map.put("isHover", "true");
            } else {
                map.put("isHover", "false");
            }
            cabTypeList.add(map);
        }


        adapter.notifyDataSetChanged();
        mainAct.setCabTypeList(cabTypeList);


        if (cabTypeList.size() == 0) {
            fareEstimateTxt.setTextColor(Color.parseColor("#6B6B76"));
            ((MTextView) view.findViewById(R.id.etaVTxt)).setText("--");
            ((MTextView) view.findViewById(R.id.personSizeVTxt)).setText("--");
            ((MTextView) view.findViewById(R.id.minFareVTxt)).setText("--");
            fareEstimateTxt.setClickable(false);
            fareEstimateTxt.setFocusableInTouchMode(false);
            fareEstimateTxt.setFocusable(false);
        } else {
            fareEstimateTxt.setTextColor(Color.parseColor("#1C1C1C"));
            fareEstimateTxt.setClickable(true);
            fareEstimateTxt.setFocusableInTouchMode(true);
            fareEstimateTxt.setFocusable(true);
            adapter.clickOnItem(0);
        }

    }

    public void setShadow() {
        (view.findViewById(R.id.shadowView)).setVisibility(View.VISIBLE);
    }

    public Context getActContext() {
        return mainAct.getActContext();
    }


    @Override
    public void onItemClick(int position, String from) {
        if (from.equalsIgnoreCase("Type")) {
            ArrayList<HashMap<String, String>> tempList = new ArrayList<>();
            tempList.addAll(cabTypeList);
            cabTypeList.clear();

            for (int i = 0; i < tempList.size(); i++) {
                HashMap<String, String> map = tempList.get(i);

                if (i != position) {
                    map.put("isHover", "false");
                } else if (i == position) {
                    map.put("isHover", "true");
                }
                cabTypeList.add(map);
            }

            if (position > (cabTypeList.size() - 1)) {
                return;
            }

            mainAct.changeCabType(cabTypeList.get(position).get("iVehicleTypeId"));
            adapter = new CabTypeAdapter(getActContext(), cabTypeList, generalFunc);
            carTypeRecyclerView.setAdapter(adapter);
            carTypeRecyclerView.smoothScrollToPosition(position);
            adapter.notifyDataSetChanged();
            adapter.setOnItemClickList(this);

            setPersonSize();
            setMinFare();
        }
    }

    public void openFareEstimateDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.fare_detail_design, null);
        builder.setView(dialogView);

        ((MTextView) dialogView.findViewById(R.id.fareDetailHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_FARE_DETAIL_TXT"));
        ((MTextView) dialogView.findViewById(R.id.baseFareHTxt)).setText(" " + generalFunc.retrieveLangLBl("", "LBL_BASE_FARE_TXT"));
        ((MTextView) dialogView.findViewById(R.id.parMinHTxt)).setText(" / " + generalFunc.retrieveLangLBl("", "LBL_MIN_TXT"));
        ((MTextView) dialogView.findViewById(R.id.andTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_AND_TXT"));
        ((MTextView) dialogView.findViewById(R.id.parKmHTxt)).setText(" / " + generalFunc.retrieveLangLBl("", "LBL_KM_TXT"));

        ((MTextView) dialogView.findViewById(R.id.baseFareVTxt)).setText(currency_sign + " " +
                generalFunc.getSelectedCarTypeData(mainAct.getSelectedCabTypeId(), "VehicleTypes", "iBaseFare", userProfileJson));

        ((MTextView) dialogView.findViewById(R.id.parMinVTxt)).setText(currency_sign + " " +
                generalFunc.getSelectedCarTypeData(mainAct.getSelectedCabTypeId(), "VehicleTypes", "fPricePerMin", userProfileJson));

        ((MTextView) dialogView.findViewById(R.id.parKmVTxt)).setText(currency_sign + " " +
                generalFunc.getSelectedCarTypeData(mainAct.getSelectedCabTypeId(), "VehicleTypes", "fPricePerKM", userProfileJson));
        builder.show();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.fareEstimateTxt) {
                new StartActProcess(getActContext()).startActWithData(FareEstimateActivity.class, mainAct.getFareEstimateBundle("false"));
            } else if (i == R.id.minFareArea) {
                openFareEstimateDialog();
            } else if (i == ride_now_btn.getId()) {
                mainAct.setCabReqType(Utils.CabReqType_Now);
                mainAct.selectSourceLocArea.performClick();
            } else if (i == ride_later_btn.getId()) {
                mainAct.chooseDateTime();
            }
        }
    }
}
