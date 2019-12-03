package com.pimpimp.passenger;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fragments.RequestPickUpFragment;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class FareEstimateActivity extends AppCompatActivity implements OnMapReadyCallback {

    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    MTextView searchLocTxt;
    MTextView baseFareHTxt;
    MTextView rentalFareHTxt;
    MTextView baseFareVTxt;
    MTextView rentalFareVTxt;
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
    String userProfileJson;
    String currency_sign = "";
    SupportMapFragment map;
    GoogleMap gMapView;
    ImageView locPinImg;
    MButton requestButton;
    ImageView changeMapTypImgView;
    String fareDetailResponseString = "";
    private String TAG = FareEstimateActivity.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_estimate);


        generalFunc = new GeneralFunctions(getActContext());


        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        container = (LinearLayout) findViewById(R.id.container);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        searchLocTxt = (MTextView) findViewById(R.id.searchLocTxt);
        baseFareHTxt = (MTextView) findViewById(R.id.baseFareHTxt);
        rentalFareHTxt = (MTextView) findViewById(R.id.rentalFareHTxt);
        rentalFareVTxt = (MTextView) findViewById(R.id.rentalFareVTxt);
        baseFareVTxt = (MTextView) findViewById(R.id.baseFareVTxt);
        commisionHTxt = (MTextView) findViewById(R.id.commisionHTxt);
        commisionVTxt = (MTextView) findViewById(R.id.commisionVTxt);
        distanceTxt = (MTextView) findViewById(R.id.distanceTxt);
        distanceFareTxt = (MTextView) findViewById(R.id.distanceFareTxt);
        minuteTxt = (MTextView) findViewById(R.id.minuteTxt);
        minuteFareTxt = (MTextView) findViewById(R.id.minuteFareTxt);
        totalFareHTxt = (MTextView) findViewById(R.id.totalFareHTxt);
        totalFareVTxt = (MTextView) findViewById(R.id.totalFareVTxt);
        locPinImg = (ImageView) findViewById(R.id.locPinImg);
        reqBtnArea = (LinearLayout) findViewById(R.id.reqBtnArea);
        changeMapTypImgView = (ImageView) findViewById(R.id.changeMapTypImgView);
        requestButton = ((MaterialRippleLayout) findViewById(R.id.requestButton)).getChildView();


        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2FareEstimate);
        map.getMapAsync(FareEstimateActivity.this);

        userProfileJson = getIntent().getStringExtra("UserProfileJson");
        currency_sign = generalFunc.getJsonValue("CurrencySymbol", userProfileJson);


        if (getIntent().hasExtra("RequestBtn") && getIntent().getStringExtra("RequestBtn").equalsIgnoreCase("true")) {
            reqBtnArea.setVisibility(View.VISIBLE);
        } else {
            reqBtnArea.setVisibility(View.GONE);
        }

        backImgView.setOnClickListener(new setOnClickAct());
        searchLocTxt.setOnClickListener(new setOnClickAct());
        requestButton.setId(Utils.generateViewId());
        requestButton.setOnClickListener(new setOnClickAct());
        changeMapTypImgView.setOnClickListener(new setOnClickAct());


        container.setVisibility(View.GONE);
        setLabels();

        locPinImg.setImageResource(R.mipmap.ic_search);

        if (getIntent().getStringExtra("isDestinationAdded").equals("true")) {
            searchLocTxt.setText(getIntent().getStringExtra("DestLocAddress"));

            findRoute(getIntent().getStringExtra("DestLocLatitude"), getIntent().getStringExtra("DestLocLongitude"));
        }

        if (getIntent().hasExtra("RequestBtn") && getIntent().getStringExtra("RequestBtn").equalsIgnoreCase("true") && getIntent().hasExtra("CabReqType")) {
            reqBtnArea.setVisibility(View.VISIBLE);
           if (getIntent().getStringExtra("CabReqType").equals(Utils.CabReqType_Later)) {
                requestButton.setText(generalFunc.retrieveLangLBl("", "LBL_CONFIRM_BOOKING"));
            } else {
                requestButton.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_REQUEST_PICKUP_TXT"));
            }
        } else

        {
            reqBtnArea.setVisibility(View.GONE);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMapView = googleMap;
        this.gMapView.getUiSettings().setZoomControlsEnabled(true);
        this.gMapView.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public Context getActContext() {
        return FareEstimateActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FARE_ESTIMATE_TXT"));
        baseFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_BASE_FARE_SMALL_TXT"));
        rentalFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RENTAL_FARE_SMALL_TXT"));
        totalFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MIN_FARE_TXT"));
        searchLocTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_PLACE_HINT_TXT"));
    }

    public void findRoute(String destLocLatitude, String destLocLongitude) {

        loaderView.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);

        String originLoc = getIntent().getStringExtra("PickUpLatitude") + "," + getIntent().getStringExtra("PickUpLongitude");
        String destLoc = destLocLatitude + "," + destLocLongitude;
        String serverKey = getResources().getString(R.string.google_api_get_address_from_location_serverApi);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey;

        Utils.printLog("url", "url:" + url);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),url, true);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                loaderView.setVisibility(View.GONE);
                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunc.getJsonValue("status", responseString);

                    if (status.equals("OK")) {

                        JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                        if (obj_routes != null && obj_routes.length() > 0) {
                            JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);

                            ((MTextView) findViewById(R.id.sourceLocTxt)).setText(generalFunc.getJsonValue("start_address", obj_legs.toString()));
                            ((MTextView) findViewById(R.id.destLocTxt)).setText(generalFunc.getJsonValue("end_address", obj_legs.toString()));

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
        if (getIntent().hasExtra("selectedDateTime")) {
            parameters.put("selectedDateTime", getIntent().getStringExtra("selectedDateTime"));
        }
        parameters.put("SelectedCar", getIntent().getStringExtra("SelectedCarId"));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);
                fareDetailResponseString = responseString;

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        String total_fare = generalFunc.getJsonValue("total_fare", responseString);
                        String iBaseFare = generalFunc.getJsonValue("iBaseFare", responseString);
                        String fPricePerMin = generalFunc.getJsonValue("fPricePerMin", responseString);
                        String fPricePerKM = generalFunc.getJsonValue("fPricePerKM", responseString);
                        String fCommision = generalFunc.getJsonValue("fCommision", responseString);
                        String MinFareDiff = generalFunc.getJsonValue("MinFareDiff", responseString);
                        String iMinDist = generalFunc.getJsonValue("iMinDist", responseString);
                        String iMinTime = generalFunc.getJsonValue("iMinTime", responseString);
                        String iRentalFare = generalFunc.getJsonValue("iRentalFare", responseString);
                        String multiplyedTime = generalFunc.getJsonValue("multiplied_time", responseString);
                        String SurgeType = generalFunc.getJsonValue("SurgeType", responseString);
                        String SurgeFactor = generalFunc.getJsonValue("SurgeFactor", responseString);

                        baseFareVTxt.setText(
                                generalFunc.getSelectedCarTypeData(getIntent().getStringExtra("SelectedCarId"), "VehicleTypes", "vVehicleType", userProfileJson)
                                        + " " + currency_sign + " " + iBaseFare);
                        distanceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DISTANCE_TXT") + "(" + distance + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + ")");
                        distanceFareTxt.setText(currency_sign + " " + fPricePerKM);
                        rentalFareVTxt.setText(currency_sign + " " + iRentalFare);
                        minuteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TIME_TXT") + "(" + multiplyedTime + " " + generalFunc.retrieveLangLBl("", "LBL_MINUTES_TXT") + ")");
                        minuteFareTxt.setText(currency_sign + " " + fPricePerMin);
                        totalFareVTxt.setText(currency_sign + " " + total_fare);

                        if (!MinFareDiff.equals("") && !MinFareDiff.equals("0")) {

                            (findViewById(R.id.minFareRow)).setVisibility(View.VISIBLE);
                            ((MTextView) findViewById(R.id.minFareHTxt)).setText(currency_sign + "" + total_fare + " "
                                    + generalFunc.retrieveLangLBl("", "LBL_MINIMUM"));
                            ((MTextView) findViewById(R.id.minFareVTxt)).setText(currency_sign + " " + total_fare);
                        }
                        if (Utils.checkText(SurgeFactor)) {
                            (findViewById(R.id.nightChargeRow)).setVisibility(View.VISIBLE);
                            ((MTextView) findViewById(R.id.nightChargeHTxt)).setText(SurgeType);
                            ((MTextView) findViewById(R.id.nightChargeVTxt)).setText(SurgeFactor);
                        }

                        locPinImg.setImageResource(R.mipmap.ic_loc_pin_indicator);

                        loaderView.setVisibility(View.GONE);
                        container.setVisibility(View.VISIBLE);

                        if (gMapView != null) {

                            gMapView.clear();
                            PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(directionJSON, Utils.dipToPixels(getActContext(), 4), Color.BLUE);

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
                                    Utils.dipToPixels(getActContext(), 280), Utils.dipToPixels(getActContext(), 280), 50);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (CommonUtilities.restrict_app.equalsIgnoreCase("Yes")) {

                    if (place.getName().equals("Antananarivo") || place.getAddress().toString().contains("Madagascar")) {
                        Log.i(TAG, "Place:" + place.toString());

                        searchLocTxt.setText(place.getAddress());
                        LatLng placeLocation = place.getLatLng();

                        findRoute("" + placeLocation.latitude, "" + placeLocation.longitude);
                    } else {

                        generalFunc.showMessage(generalFunc.getCurrentView(FareEstimateActivity.this),
                                generalFunc.retrieveLangLBl("", "LBL_SELECET_LOC_WITHIN_ANTANANARIVO_TXT"));
                    }
                } else {
                    Log.i(TAG, "Place:" + place.toString());

                    searchLocTxt.setText(place.getAddress());
                    LatLng placeLocation = place.getLatLng();

                    findRoute("" + placeLocation.latitude, "" + placeLocation.longitude);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());

                generalFunc.showMessage(generalFunc.getCurrentView(FareEstimateActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }

    public class setOnClickAct implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == requestButton.getId()) {
                Bundle data = new Bundle();
                data.putString("Request", "true");
                data.putString("SelectedCarId",getIntent().getStringExtra("SelectedCarId"));
                data.putString("responseString", fareDetailResponseString);
                (new StartActProcess(getActContext())).setOkResult(data);
                backImgView.performClick();
            } else if (i == changeMapTypImgView.getId()) {
                gMapView.setMapType(gMapView.getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
            }

            switch (view.getId()) {
                case R.id.backImgView:
                    FareEstimateActivity.super.onBackPressed();
                    break;

                case R.id.searchLocTxt:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                .build(FareEstimateActivity.this);
                        startActivityForResult(intent, Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                        generalFunc.showMessage(generalFunc.getCurrentView(FareEstimateActivity.this),
                                generalFunc.retrieveLangLBl("", "LBL_SERVICE_NOT_AVAIL_TXT"));
                    }
                    break;

            }
        }
    }
}
