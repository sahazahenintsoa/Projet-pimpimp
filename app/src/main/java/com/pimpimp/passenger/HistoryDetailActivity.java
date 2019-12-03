package com.pimpimp.passenger;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.fragments.CustomSupportMapFragment;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class HistoryDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    MTextView subTitleTxt;
    ImageView backImgView;
    CustomSupportMapFragment map;
    GoogleMap gMap;
    LinearLayout fareDetailDisplayArea;
    View convertView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        generalFunc = new GeneralFunctions(getActContext());


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        subTitleTxt = (MTextView) findViewById(R.id.subTitleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        fareDetailDisplayArea = (LinearLayout) findViewById(R.id.fareDetailDisplayArea);
        map = (CustomSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        setLabels();
        setData();

        map.getMapAsync(this);

        backImgView.setOnClickListener(new setOnClickList());
        subTitleTxt.setOnClickListener(new setOnClickList());

        map.setListener(new CustomSupportMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                ((ScrollView) findViewById(R.id.scrollContainer)).requestDisallowInterceptTouchEvent(true);
            }
        });

    }


    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RECEIPT_HEADER_TXT"));
        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GET_RECEIPT_TXT"));

        ((MTextView) findViewById(R.id.headerTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_THANKS_RIDING_TXT"));
        ((MTextView) findViewById(R.id.rideNoHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BOOKING_NO"));
        ((MTextView) findViewById(R.id.ratingHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
        ((MTextView) findViewById(R.id.driverHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DRIVER"));
        ((MTextView) findViewById(R.id.tripdateHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRIP_REQUEST_DATE_TXT"));
        ((MTextView) findViewById(R.id.pickUpHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_HEADER_TXT"));
        ((MTextView) findViewById(R.id.dropOffHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DROP_OFF_LOCATION_TXT"));
        ((MTextView) findViewById(R.id.chargesHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CHARGES_TXT"));

    }

    public void setData() {
        String tripData = getIntent().getStringExtra("TripData");

        ((MTextView) findViewById(R.id.rideNoVTxt)).setText(generalFunc.getJsonValue("vRideNo", tripData));
        ((MTextView) findViewById(R.id.nameDriverVTxt)).setText(generalFunc.getJsonValue("vName", generalFunc.getJsonValue("DriverDetails", tripData)) + " " +
                generalFunc.getJsonValue("vLastName", generalFunc.getJsonValue("DriverDetails", tripData)));
        ((MTextView) findViewById(R.id.tripdateVTxt)).setText(generalFunc.getJsonValue("tTripRequestDate", tripData));
        ((MTextView) findViewById(R.id.pickUpVTxt)).setText(generalFunc.getJsonValue("tSaddress", tripData));
        ((MTextView) findViewById(R.id.dropOffVTxt)).setText(generalFunc.getJsonValue("tDaddress", tripData));

        String trip_status_str = generalFunc.getJsonValue("iActive", tripData);

        if (trip_status_str.contains("Canceled")) {
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CANCELED_TRIP_TXT"));
            (findViewById(R.id.tripDetailArea)).setVisibility(View.GONE);
        } else if (trip_status_str.contains("Finished")) {

            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_FINISHED_TRIP_TXT"));

            (findViewById(R.id.tripDetailArea)).setVisibility(View.VISIBLE);
            subTitleTxt.setVisibility(View.VISIBLE);
        } else {
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(trip_status_str);

        }

        if (generalFunc.getJsonValue("vTripPaymentMode", tripData).equals("Cash")) {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CASH_PAYMENT_TXT"));
        }  else if (generalFunc.getJsonValue("vTripPaymentMode", tripData).equals("Card")){
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Card Payment", "LBL_CARD_PAYMENT"));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_card_tanspherent);
        }
        else
        {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Wallet Payment", "LBL_WALLET_PAYMENT"));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_card_tanspherent);
        }


        if (generalFunc.getJsonValue("eCancelled", tripData).equals("Yes")) {
            subTitleTxt.setVisibility(View.GONE);
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_DRIVER") + " " +
                    generalFunc.getJsonValue("vCancelReason", tripData));
        }

        ((RatingBar) findViewById(R.id.tripRating)).setRating(generalFunc.parseFloatValue(0, generalFunc.getJsonValue("TripRating", tripData)));

        String driverImageName = generalFunc.getJsonValue("vImage", generalFunc.getJsonValue("DriverDetails", tripData));
        if (driverImageName == null || driverImageName.equals("") || driverImageName.equals("NONE")) {
            ((SelectableRoundedImageView) findViewById(R.id.driverImgView)).setImageResource(R.mipmap.ic_no_pic_user);
        } else {
            String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Driver/" + generalFunc.getJsonValue("iDriverId", tripData) + "/"
                    + driverImageName;
            Picasso.with(getActContext())
                    .load(image_url)
                    .placeholder(R.mipmap.ic_no_pic_user)
                    .error(R.mipmap.ic_no_pic_user)
                    .into((SelectableRoundedImageView) findViewById(R.id.driverImgView));
        }

        if (Utils.checkText(generalFunc.getJsonValue("PPetId", generalFunc.getJsonValue("PetDetails", tripData))) && !generalFunc.getJsonValue("PPetId", generalFunc.getJsonValue("PetDetails", tripData)).equalsIgnoreCase("0"))

        {

            ((CardView) findViewById(R.id.petDetailCardView)).setVisibility(View.VISIBLE);

            // pet details

            ((MTextView) findViewById(R.id.petNameTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PET_NAME_TXT"));
            ((MTextView) findViewById(R.id.petTypeNameTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PET_TYPE"));


            ((MTextView) findViewById(R.id.petNameVTxt)).setText(generalFunc.getJsonValue("PetName", generalFunc.getJsonValue("PetDetails", tripData)));
            ((MTextView) findViewById(R.id.petTypeNameVTxt)).setText(generalFunc.getJsonValue("PetTypeName", generalFunc.getJsonValue("PetDetails", tripData)));

        }

        boolean FareDetailsArrNew = generalFunc.isJSONkeyAvail("HistoryFareDetailsNewArr", tripData);

        JSONArray FareDetailsArrNewObj = null;
        if (FareDetailsArrNew == true) {
            FareDetailsArrNewObj = generalFunc.getJsonArray("HistoryFareDetailsNewArr", tripData);
        }
        if (FareDetailsArrNewObj != null)
            addFareDetailLayout(FareDetailsArrNewObj);
    }


    private void addFareDetailLayout(JSONArray jobjArray) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                addFareDetailRow(jobject.names().getString(0), jobject.get(jobject.names().getString(0)).toString(), (jobjArray.length() - 1) == i ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.design_fare_deatil_row, null);
        TableRow FareDetailRow = (TableRow) convertView.findViewById(R.id.FareDetailRow);
        TableLayout fair_area_table_layout = (TableLayout) convertView.findViewById(R.id.fair_area);
        MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
        MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

        titleHTxt.setText(row_name);
        titleVTxt.setText(row_value);

        if (isLast == true) {
            TableLayout.LayoutParams tableRowParams =
                    new TableLayout.LayoutParams
                            (TableLayout.LayoutParams.FILL_PARENT, Utils.pxToDp(getActContext(), 40));
            tableRowParams.setMargins(0, 10, 0, 0);

            fair_area_table_layout.setLayoutParams(tableRowParams);
            FareDetailRow.setLayoutParams(tableRowParams);
            fair_area_table_layout.setBackgroundColor(Color.parseColor("#EBEBEB"));
            fair_area_table_layout.getChildAt(0).setPadding(5, 0, 5, 0);
        } else {
            titleHTxt.setTextColor(Color.parseColor("#303030"));
            titleVTxt.setTextColor(Color.parseColor("#111111"));
        }
        if (convertView != null)
            fareDetailDisplayArea.addView(convertView);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;

        String tripData = getIntent().getStringExtra("TripData");

        String tStartLat = generalFunc.getJsonValue("tStartLat", tripData);
        String tStartLong = generalFunc.getJsonValue("tStartLong", tripData);
        String tEndLat = generalFunc.getJsonValue("tEndLat", tripData);
        String tEndLong = generalFunc.getJsonValue("tEndLong", tripData);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker pickUpMarker = null;
        Marker destMarker = null;
        if (!tStartLat.equals("") && !tStartLat.equals("0.0") && !tStartLong.equals("") && !tStartLong.equals("0.0")) {
            LatLng pickUpLoc = new LatLng(generalFunc.parseDoubleValue(0.0, tStartLat), generalFunc.parseDoubleValue(0.0, tStartLong));
            MarkerOptions marker_opt = new MarkerOptions().position(pickUpLoc);
            marker_opt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker)).anchor(0.5f, 0.5f);
            pickUpMarker = this.gMap.addMarker(marker_opt);

//            builder.include(pickUpLoc);

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickUpLoc, 10));
        }

        if (generalFunc.getJsonValue("iActive", tripData).equals("Finished")) {
            if (!tEndLat.equals("") && !tEndLat.equals("0.0") && !tEndLong.equals("") && !tEndLong.equals("0.0")) {
                LatLng destLoc = new LatLng(generalFunc.parseDoubleValue(0.0, tEndLat), generalFunc.parseDoubleValue(0.0, tEndLong));
                MarkerOptions marker_opt = new MarkerOptions().position(destLoc);
                marker_opt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f, 0.5f);
                destMarker = this.gMap.addMarker(marker_opt);
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destLoc, 10));
            }
        }


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

//        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, Utils.dipToPixels(getActContext(), 200), 100));
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                // TODO Auto-generated method stub
                marker.hideInfoWindow();
                return true;
            }
        });

        if (pickUpMarker != null && destMarker != null) {
            drawRoute(pickUpMarker.getPosition(), destMarker.getPosition());
        }

    }

    public void drawRoute(LatLng pickUpLoc, LatLng destinationLoc) {
        String originLoc = pickUpLoc.latitude + "," + pickUpLoc.longitude;
        String destLoc = destinationLoc.latitude + "," + destinationLoc.longitude;
        String serverKey = getResources().getString(R.string.google_api_get_address_from_location_serverApi);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + generalFunc.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),url, true);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunc.getJsonValue("status", responseString);

                    if (status.equals("OK")) {

                        JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                        if (obj_routes != null && obj_routes.length() > 0) {

                            PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(responseString, Utils.dipToPixels(getActContext(), 4), Color.BLUE);

                            if (lineOptions != null) {
                                gMap.addPolyline(lineOptions);
                            }
                        }

                    }

                }
            }
        });
        exeWebServer.execute();
    }

    public void sendReceipt() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getReceipt");
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("iTripId", generalFunc.getJsonValue("iTripId", getIntent().getStringExtra("TripData")));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
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
        return HistoryDetailActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    HistoryDetailActivity.super.onBackPressed();
                    break;

                case R.id.subTitleTxt:
                    sendReceipt();
                    break;

            }
        }
    }
}
