package com.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.squareup.picasso.Picasso;
import com.pimpimp.passenger.ContactUsActivity;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.R;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;

public class DriverDetailFragment extends Fragment implements UpdateFrequentTask.OnTaskRunCalled {

    int PICK_CONTACT = 2121;
    View view;
    MainActivity mainAct;
    GeneralFunctions generalFunc;
    String driverPhoneNum = "";
    DriverDetailFragment driverDetailFragment;
    String userProfileJson;
    UpdateFrequentTask updateTaxiMeterFrequentTask;
    int UPDATE_TIME_INTERVAL =  1 * 60 * 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_driver_detail, container, false);

        mainAct = (MainActivity) getActivity();
        userProfileJson = mainAct.userProfileJson;
        generalFunc = mainAct.generalFunc;

        setLabels();
        setData();
        driverDetailFragment = mainAct.getDriverDetailFragment();
        mainAct.setDriverImgView(((SelectableRoundedImageView) view.findViewById(R.id.driverImgView)));

        if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip")) {
            configTripStartView(true);
        }



        mainAct.sliding_layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip")) {
                    configTripStartView(false);
                }

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if (newState != SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip")) {
                        configTripStartView(false);
                    }
                }
            }
        });

        new CreateRoundedView(Color.parseColor("#535353"), Utils.dipToPixels(mainAct.getActContext(), 5), 2,
                mainAct.getActContext().getResources().getColor(android.R.color.transparent), (view.findViewById(R.id.numberPlateArea)));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip")) {
            if (updateTaxiMeterFrequentTask==null)
            {
                configTripStartView(true);
            }
            else
            {
                configTripStartView(false);
            }

        }
    }

    public void setLabels() {
        ((MTextView) view.findViewById(R.id.slideUpForDetailTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_SLIDE_UP_DETAIL"));
        ((MTextView) view.findViewById(R.id.contact_btn)).setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_TXT"));
        ((MTextView) view.findViewById(R.id.btn_share_txt)).setText(generalFunc.retrieveLangLBl("", "LBL_SHARE_BTN_TXT"));
        ((MTextView) view.findViewById(R.id.btn_cancle_trip)).setText(generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TRIP_TXT"));
        ((MTextView) view.findViewById(R.id.btn_support_trip)).setText(generalFunc.retrieveLangLBl("", "LBL_SUPPORT_HEADER_TXT"));
        ((MTextView) view.findViewById(R.id.current_fare_txt)).setText(generalFunc.retrieveLangLBl("", "LBL_LOADING_HINT_TXT"));
        ((MTextView) view.findViewById(R.id.current_fare_hint_txt)).setText(generalFunc.retrieveLangLBl("", "LBL_CURRENT_FARE_TXT"));
    }

    public void setData() {
        HashMap<String, String> tripDataMap = (HashMap<String, String>) getArguments().getSerializable("TripData");
        ((MTextView) view.findViewById(R.id.driver_name)).setText(tripDataMap.get("DriverName"));
        ((MTextView) view.findViewById(R.id.driver_car_type)).setText(tripDataMap.get("vVehicleType"));
        ((MTextView) view.findViewById(R.id.txt_rating)).setText(tripDataMap.get("DriverRating"));
        ((MTextView) view.findViewById(R.id.driver_car_name)).setText(tripDataMap.get("DriverCarName"));
        ((MTextView) view.findViewById(R.id.driver_car_model)).setText(tripDataMap.get("DriverCarModelName"));
        ((MTextView) view.findViewById(R.id.numberPlate_txt)).setText(tripDataMap.get("DriverCarPlateNum"));

        driverPhoneNum = "+"+tripDataMap.get("DriverPhoneCode")+" "+tripDataMap.get("DriverPhone");
        String driverImageName = tripDataMap.get("DriverImage");
        if (driverImageName == null || driverImageName.equals("") || driverImageName.equals("NONE")) {
            ((SelectableRoundedImageView) view.findViewById(R.id.driverImgView)).setImageResource(R.mipmap.ic_no_pic_user);
        } else {
            String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Driver/" + tripDataMap.get("iDriverId") + "/"
                    + tripDataMap.get("DriverImage");
            Picasso.with(mainAct.getActContext())
                    .load(image_url)
                    .placeholder(R.mipmap.ic_no_pic_user)
                    .error(R.mipmap.ic_no_pic_user)
                    .into(((SelectableRoundedImageView) view.findViewById(R.id.driverImgView)));
        }

        mainAct.registerForContextMenu(view.findViewById(R.id.contact_btn));
        (view.findViewById(R.id.contact_btn)).setOnClickListener(new setOnClickList());
        (view.findViewById(R.id.btn_share_txt)).setOnClickListener(new setOnClickList());
        (view.findViewById(R.id.btn_cancle_trip)).setOnClickListener(new setOnClickList());
        (view.findViewById(R.id.btn_support_trip)).setOnClickListener(new setOnClickList());

    }
    public String getDriverPhone() {
        return driverPhoneNum;
    }

    public void configTripStartView(boolean b) {
        (view.findViewById(R.id.contact_btn)).setVisibility(View.GONE);
        (view.findViewById(R.id.btn_cancle_trip)).setVisibility(View.GONE);
        (view.findViewById(R.id.btn_current_fare_container)).setVisibility(View.VISIBLE);
        if (b) {
            getCurrentFareOfTrip();
            updateTaxiMeterFrequentTask = new UpdateFrequentTask(UPDATE_TIME_INTERVAL);
            updateTaxiMeterFrequentTask.setTaskRunListener(this);
            updateTaxiMeterFrequentTask.startRepeatingTask();
        }
        else
        {
            getCurrentFareOfTrip();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopFreqTask();
    }
    public void stopFreqTask() {
        if (updateTaxiMeterFrequentTask != null) {
            updateTaxiMeterFrequentTask.stopRepeatingTask();
        }
    }

    @Override
    public void onTaskRun() {
        getCurrentFareOfTrip();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.contact_btn:
                    Utils.printLog("click", "perform");
                    mainAct.openContextMenu(view);
                    break;
                case R.id.btn_share_txt:
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_CONTACT);
                    break;

                case R.id.btn_cancle_trip:
                    buildWarningMessage(generalFunc.retrieveLangLBl("", "LBL_TRIP_CANCEL_TXT"),
                            generalFunc.retrieveLangLBl("", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"),
                            generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TRIP_TXT"), true);
                    break;

                case R.id.btn_support_trip:
                    new StartActProcess(mainAct.getActContext()).startAct(ContactUsActivity.class);
                    break;
            }
        }
    }

    public void buildWarningMessage(String message, String positiveBtn, String negativeBtn, final boolean isCancelTripWarning) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(mainAct.getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();

                if (btn_id == 1) {
                    if (isCancelTripWarning == true) {
                        cancelTrip();
                    } else {
                        generalFunc.restartApp();
                    }
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        if (!negativeBtn.equals("")) {
            generateAlert.setNegativeBtn(negativeBtn);
        }

        generateAlert.showAlertBox();
    }


    public void getCurrentFareOfTrip() {
        if (((MTextView) view.findViewById(R.id.current_fare_txt)).getText().equals(generalFunc.retrieveLangLBl("", "LBL_LOADING_HINT_TXT"))) {
            (view.findViewById(R.id.reqLoaderView)).setVisibility(View.VISIBLE);
        }
        HashMap<String, String> tripDataMap = (HashMap<String, String>) getArguments().getSerializable("TripData");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCurrentTripFare");
        parameters.put("TripId", tripDataMap.get("iTripId"));
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(final String responseString) {

                Utils.printLog("Api", " getCurrentTripFare response ::" + responseString);
                (view.findViewById(R.id.reqLoaderView)).setVisibility(View.GONE);

                if (responseString != null && !responseString.equals("")) {

                    final boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    mainAct.getActContext1().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //TODO: update your UI
                            if (isDataAvail == true) {

                                ((MTextView) view.findViewById(R.id.current_fare_txt)).setText("" + generalFunc.getJsonValue(CommonUtilities.message_str, responseString));

                            } else {
                                ((MTextView) view.findViewById(R.id.current_fare_txt)).setText(generalFunc.retrieveLangLBl("", "LBL_LOADING_HINT_TXT"));

                            }
                        }

                    });


                } else {
                    ((MTextView) view.findViewById(R.id.current_fare_txt)).setText(generalFunc.retrieveLangLBl("", "LBL_LOADING_HINT_TXT"));

                }



            }
        });
        exeWebServer.execute();
    }

    public void cancelTrip() {
        HashMap<String, String> tripDataMap = (HashMap<String, String>) getArguments().getSerializable("TripData");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelTrip");
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iDriverId", tripDataMap.get("iDriverId"));
        parameters.put("iTripId", tripDataMap.get("iTripId"));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getContext(),parameters);
        exeWebServer.setLoaderConfig(mainAct.getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        generalFunc.restartApp();
                    } else {
                        buildWarningMessage(generalFunc.retrieveLangLBl("", "LBL_REQUEST_FAILED_PROCESS"),
                                generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), "", false);
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

        if (requestCode == PICK_CONTACT && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = mainAct.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE}, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);

                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        smsIntent.putExtra("address", "" + number);

                        String link_location = "http://maps.google.com/?q=" + mainAct.userLocation.getLatitude() + "," + mainAct.userLocation.getLongitude();
                        smsIntent.putExtra("sms_body", generalFunc.retrieveLangLBl("", "LBL_SEND_STATUS_CONTENT_TXT") + " " + link_location);
                        startActivity(smsIntent);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }
}
