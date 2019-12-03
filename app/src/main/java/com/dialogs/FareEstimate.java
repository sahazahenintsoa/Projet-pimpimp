package com.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.MyProfileActivity;
import com.pimpimp.passenger.R;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 11-07-2016.
 */
public class FareEstimate  {

    Context mContext;
    Dialog dialogFareEstimate;
    GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;

    MTextView searchLocTxt;
    MTextView baseFareHTxt;
    MTextView baseFareVTxt;
    MTextView commisionHTxt;
    MTextView commisionVTxt;
    MTextView distanceTxt;
    MTextView distanceFareTxt;
    MTextView minuteTxt;
    MTextView minuteFareTxt;
    MTextView totalFareHTxt;
    MTextView totalFareVTxt;

    AVLoadingIndicatorView loaderView;
    LinearLayout container;
    LinearLayout reqBtnArea;
    LinearLayout mapArea;

    String userProfileJson;
    String currency_sign = "";

    SupportMapFragment map;
    ImageView locPinImg;
    MButton requestButton;
    Bundle bn;
    Activity actContext;
    GoogleMap gMapView;

    public FareEstimate(Context mContext,Bundle bn) {
        this.mContext = mContext;
        this.bn = bn;
        actContext=(MainActivity)mContext;
        generalFunc=new GeneralFunctions(mContext);
        displayData();
    }

  
    public void displayData() {

        dialogFareEstimate = new Dialog(mContext, R.style.Theme_Dialog);
        dialogFareEstimate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogFareEstimate.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialogFareEstimate.setContentView(R.layout.activity_fare_estimate);

        generalFunc = new GeneralFunctions(mContext);


        loaderView = (AVLoadingIndicatorView) dialogFareEstimate.findViewById(R.id.loaderView);
        container = (LinearLayout) dialogFareEstimate.findViewById(R.id.container);

        titleTxt = (MTextView) dialogFareEstimate.findViewById(R.id.titleTxt);
        backImgView = (ImageView) dialogFareEstimate.findViewById(R.id.backImgView);
        searchLocTxt = (MTextView) dialogFareEstimate.findViewById(R.id.searchLocTxt);
        baseFareHTxt = (MTextView) dialogFareEstimate.findViewById(R.id.baseFareHTxt);
        baseFareVTxt = (MTextView) dialogFareEstimate.findViewById(R.id.baseFareVTxt);
        commisionHTxt = (MTextView) dialogFareEstimate.findViewById(R.id.commisionHTxt);
        commisionVTxt = (MTextView) dialogFareEstimate.findViewById(R.id.commisionVTxt);
        distanceTxt = (MTextView) dialogFareEstimate.findViewById(R.id.distanceTxt);
        distanceFareTxt = (MTextView) dialogFareEstimate.findViewById(R.id.distanceFareTxt);
        minuteTxt = (MTextView) dialogFareEstimate.findViewById(R.id.minuteTxt);
        minuteFareTxt = (MTextView) dialogFareEstimate.findViewById(R.id.minuteFareTxt);
        totalFareHTxt = (MTextView) dialogFareEstimate.findViewById(R.id.totalFareHTxt);
        totalFareVTxt = (MTextView) dialogFareEstimate.findViewById(R.id.totalFareVTxt);
        locPinImg = (ImageView) dialogFareEstimate.findViewById(R.id.locPinImg);
        reqBtnArea = (LinearLayout) dialogFareEstimate.findViewById(R.id.reqBtnArea);
        mapArea = (LinearLayout) dialogFareEstimate.findViewById(R.id.mapArea);
        requestButton = ((MaterialRippleLayout) dialogFareEstimate.findViewById(R.id.requestButton)).getChildView();

        SupportMapFragment smap=new SupportMapFragment();
      /*  map = (SupportMapFragment) ((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.mapV2FareEstimate);
*/

        ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapArea,smap ).commit();
        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMapView = googleMap;
                gMapView.getUiSettings().setZoomControlsEnabled(true);
            }
        });

        userProfileJson = bn.getString("UserProfileJson");
        currency_sign = generalFunc.getJsonValue("CurrencySymbol", userProfileJson);


        if (bn.containsKey("RequestBtn"))
        {
            reqBtnArea.setVisibility(View.VISIBLE);
        }
        else
        {
            reqBtnArea.setVisibility(View.GONE);
        }
        backImgView.setOnClickListener(new setOnClickAct());
        searchLocTxt.setOnClickListener(new setOnClickAct());
        requestButton.setId(Utils.generateViewId());
        requestButton.setOnClickListener(new setOnClickAct());


        container.setVisibility(View.GONE);
        setLabels();

        locPinImg.setImageResource(R.mipmap.ic_search);

        if (bn.getString("isDestinationAdded").equals("true")) {
            searchLocTxt.setText(bn.getString("DestLocAddress"));
            findRoute(bn.getString("DestLocLatitude"), bn.getString("DestLocLongitude"));
        }

       if (bn.getString("CabReqType").equals(Utils.CabReqType_Later)) {
            requestButton.setText(generalFunc.retrieveLangLBl("", "LBL_CONFIRM_BOOKING"));
        } else {
            requestButton.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_REQUEST_PICKUP_TXT"));
        }
    }


    public class setOnClickAct implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == requestButton.getId()) {
                Bundle data = new Bundle();
                data.putString("Request", "true");
                (new StartActProcess(mContext)).setOkResult(data);
                backImgView.performClick();
            }

            switch (view.getId()) {
                case R.id.backImgView:
                   dismissDialog();
                    break;

                case R.id.searchLocTxt:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                .build((MainActivity)mContext);
                        actContext.startActivityForResult(intent, Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                        generalFunc.showMessage(generalFunc.getCurrentView((MainActivity)mContext),
                                generalFunc.retrieveLangLBl("", "LBL_SERVICE_NOT_AVAIL_TXT"));
                    }
                    break;

            }
        }
    }

    public void dismissDialog() {
        if (dialogFareEstimate != null) {
            dialogFareEstimate.dismiss();
        }
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FARE_ESTIMATE_TXT"));
        baseFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_BASE_FARE_SMALL_TXT"));
//        commisionHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PLATFORM_FEE_TXT"));
        totalFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MIN_FARE_TXT"));
        searchLocTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_PLACE_HINT_TXT"));
    }

    public void findRoute(String destLocLatitude, String destLocLongitude) {

        loaderView.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);

        String originLoc = bn.getString("PickUpLatitude") + "," + bn.getString("PickUpLongitude");
        String destLoc = destLocLatitude + "," + destLocLongitude;
        String serverKey = mContext.getResources().getString(R.string.google_api_get_address_from_location_serverApi);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey;

        Utils.printLog("url", "url:" + url);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext,url, true);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

//                Utils.printLog("Response", "::" + responseString);
                loaderView.setVisibility(View.GONE);

                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunc.getJsonValue("status", responseString);

                    if (status.equals("OK")) {

                        JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                        if (obj_routes != null && obj_routes.length() > 0) {
                            JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);

                            ((MTextView) dialogFareEstimate.findViewById(R.id.sourceLocTxt)).setText(generalFunc.getJsonValue("start_address", obj_legs.toString()));
                            ((MTextView) dialogFareEstimate.findViewById(R.id.destLocTxt)).setText(generalFunc.getJsonValue("end_address", obj_legs.toString()));

                            String distance = "" + (generalFunc.parseLongValue(0, generalFunc.getJsonValue("value",
                                    generalFunc.getJsonValue("distance", obj_legs.toString()).toString())) / 1000);

                            String time = "" + (generalFunc.parseLongValue(0, generalFunc.getJsonValue("value",
                                    generalFunc.getJsonValue("duration", obj_legs.toString()).toString())) / 60);

                            LatLng sourceLocation = new LatLng(generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("start_location", obj_legs.toString()))),
                                    generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("start_location", obj_legs.toString()))));

                            LatLng destLocation = new LatLng(generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("end_location", obj_legs.toString()))),
                                    generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("end_location", obj_legs.toString()))));

                            estimateFare(distance, time, responseString, sourceLocation, destLocation);
                        }

                    } else {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("", "LBL_ERROR_TXT"),
                                generalFunc.retrieveLangLBl("", "LBL_GOOGLE_DIR_NO_ROUTE"));
                    }

                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }


    public void estimateFare(final String distance, final String time, final String directionJSON, final LatLng sourceLocation, final LatLng destLocation) {
        loaderView.setVisibility(View.VISIBLE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "estimateFare");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("distance", distance);
        parameters.put("time", time);
        parameters.put("SelectedCar", bn.getString("SelectedCarId"));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext,parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        String total_fare = generalFunc.getJsonValue("total_fare", responseString);
                        String iBaseFare = generalFunc.getJsonValue("iBaseFare", responseString);
                        String fPricePerMin = generalFunc.getJsonValue("fPricePerMin", responseString);
                        String fPricePerKM = generalFunc.getJsonValue("fPricePerKM", responseString);
                        String fCommision = generalFunc.getJsonValue("fCommision", responseString);
                        String MinFareDiff = generalFunc.getJsonValue("MinFareDiff", responseString);

                        baseFareVTxt.setText(
                                generalFunc.getSelectedCarTypeData(bn.getString("SelectedCarId"), "VehicleTypes", "vVehicleType", userProfileJson)
                                        + " " + currency_sign + " " + iBaseFare);
                        distanceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DISTANCE_TXT") + "(" + distance + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + ")");
                        distanceFareTxt.setText(currency_sign + " " + fPricePerKM);
//                        commisionVTxt.setText(currency_sign + " " + fCommision);
                        minuteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TIME_TXT") + "(" + time + " " + generalFunc.retrieveLangLBl("", "LBL_MINUTES_TXT") + ")");
                        minuteFareTxt.setText(currency_sign + " " + fPricePerMin);
                        totalFareVTxt.setText(currency_sign + " " + total_fare);

                        if (!MinFareDiff.equals("") && !MinFareDiff.equals("0")) {

                            (dialogFareEstimate.findViewById(R.id.minFareRow)).setVisibility(View.VISIBLE);
                            ((MTextView) dialogFareEstimate.findViewById(R.id.minFareHTxt)).setText(currency_sign + "" + total_fare + " "
                                    + generalFunc.retrieveLangLBl("", "LBL_MINIMUM"));
                            ((MTextView) dialogFareEstimate.findViewById(R.id.minFareVTxt)).setText(currency_sign + " " + total_fare);
                        }

                        locPinImg.setImageResource(R.mipmap.ic_loc_pin_indicator);

                        loaderView.setVisibility(View.GONE);
                        container.setVisibility(View.VISIBLE);

                        if (gMapView != null) {

                            gMapView.clear();
                            PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(directionJSON, Utils.dipToPixels(mContext, 4), Color.BLUE);

                            if (lineOptions != null) {
                                gMapView.addPolyline(lineOptions);
                            }

                            MarkerOptions markerOptions_sourceLocation = new MarkerOptions();
                            markerOptions_sourceLocation.position(sourceLocation);
                            markerOptions_sourceLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker))
                                    .anchor(0.5f, 0.5f);

                            MarkerOptions markerOptions_destinationLocation = new MarkerOptions();
                            markerOptions_destinationLocation
                                    .position(destLocation);
                            markerOptions_destinationLocation
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f, 0.5f);

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(sourceLocation);
                            builder.include(destLocation);

                            LatLngBounds bounds = builder.build();

                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                                    Utils.dipToPixels(mContext, 280), Utils.dipToPixels(mContext, 280), 50);
                            gMapView.moveCamera(cu);

                            gMapView.addMarker(markerOptions_sourceLocation);
                            gMapView.addMarker(markerOptions_destinationLocation);

                        }


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


   /* @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMapView = googleMap;
        this.gMapView.getUiSettings().setZoomControlsEnabled(true);
    }*/
}
