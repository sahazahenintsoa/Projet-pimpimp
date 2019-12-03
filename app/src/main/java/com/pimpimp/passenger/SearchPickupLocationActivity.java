package com.pimpimp.passenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

public class SearchPickupLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GetAddressFromLocation.AddressFound {

    private String TAG = SearchPickupLocationActivity.class.getSimpleName();

    MTextView titleTxt;
    ImageView backImgView;
    ImageView changeMapTypImgView;

    GeneralFunctions generalFunc;
    MButton btn_type2;
    int btnId;

    MTextView placeTxtView;

    boolean isPlaceSelected = false;
    LatLng placeLocation;
    Marker placeMarker;

    SupportMapFragment map;
    GoogleMap gMap;

    GetAddressFromLocation getAddressFromLocation;
    LinearLayout placeArea;
    MTextView homePlaceTxt;
    MTextView workPlaceTxt;

    String userHomeLocationLatitude_str;
    String userHomeLocationLongitude_str;
    String userWorkLocationLatitude_str;
    String userWorkLocationLongitude_str;
    String home_address_str;
    String work_address_str;
    SharedPreferences mpref_place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_pickup_location);

        generalFunc = new GeneralFunctions(getActContext());


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        changeMapTypImgView = (ImageView) findViewById(R.id.changeMapTypImgView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        placeTxtView = (MTextView) findViewById(R.id.placeTxtView);

        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        workPlaceTxt = (MTextView) findViewById(R.id.workPlaceTxt);
        placeArea = (LinearLayout) findViewById(R.id.placeArea);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        setLabels();

        map.getMapAsync(SearchPickupLocationActivity.this);

        backImgView.setOnClickListener(new setOnClickAct());
        btnId = Utils.generateViewId();
        btn_type2.setId(btnId);

        btn_type2.setOnClickListener(new setOnClickAct());
        (findViewById(R.id.pickUpLocSearchArea)).setOnClickListener(new setOnClickAct());
        homePlaceTxt.setOnClickListener(new setOnClickAct());
        workPlaceTxt.setOnClickListener(new setOnClickAct());
        changeMapTypImgView.setOnClickListener(new setOnClickAct());

        generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this), generalFunc.retrieveLangLBl("", "LBL_LONG_TOUCH_CHANGE_LOC_TXT"));

        checkLocations();
    }

    public void setLabels() {
        if (getIntent().getStringExtra("isPickUpLoc") != null && getIntent().getStringExtra("isPickUpLoc").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SET_PICK_UP_LOCATION_TXT"));
        } else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_BIG_TXT"));
        } else if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_HEADER_TXT"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_DESTINATION_HEADER_TXT"));
        }

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_LOC"));
        placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_PLACE_HINT_TXT"));

    }


    public void checkLocations() {
        mpref_place = PreferenceManager.getDefaultSharedPreferences(getActContext());

        home_address_str = mpref_place.getString("userHomeLocationAddress", null);

        userHomeLocationLatitude_str = mpref_place.getString("userHomeLocationLatitude", null);
        userHomeLocationLongitude_str = mpref_place.getString("userHomeLocationLongitude", null);

        work_address_str = mpref_place.getString("userWorkLocationAddress", null);
        userWorkLocationLatitude_str = mpref_place.getString("userWorkLocationLatitude", null);
        userWorkLocationLongitude_str = mpref_place.getString("userWorkLocationLongitude", null);

        if (home_address_str != null && !home_address_str.equals("")) {
            homePlaceTxt.setVisibility(View.VISIBLE);
            placeArea.setVisibility(View.VISIBLE);
            (findViewById(R.id.seperationLine)).setVisibility(View.VISIBLE);
        }


        if (work_address_str != null && !work_address_str.equals("")) {
            workPlaceTxt.setVisibility(View.VISIBLE);
            placeArea.setVisibility(View.VISIBLE);
            (findViewById(R.id.seperationLine)).setVisibility(View.VISIBLE);
        }
    }

   /* @Override
    public void onAddressFound(String address, double latitude, double longitude,String cityName) {
        Utils.printLog("address", ":" + address);

            placeTxtView.setText(address);
            isPlaceSelected = true;
            this.placeLocation = new LatLng(latitude, longitude);

            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

            if (gMap != null) {
                gMap.clear();
                placeMarker = gMap.addMarker(new MarkerOptions().position(this.placeLocation).title(address));
                gMap.moveCamera(cu);
            }

    }*/


    @Override
    public void onAddressFound(String address, double latitude, double longitude,String cityName) {
        Utils.printLog("address", ":" + address+"cityName"+cityName);
        if(CommonUtilities.restrict_app.equalsIgnoreCase("Yes")) {
            if (!TextUtils.isEmpty(cityName) && (cityName.equalsIgnoreCase("Antananarivo") || cityName.equalsIgnoreCase("Home") || cityName.equalsIgnoreCase("Work")) ) {
                placeTxtView.setText(address);
                isPlaceSelected = true;
                this.placeLocation = new LatLng(latitude, longitude);

                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

                if (gMap != null) {
                    gMap.clear();
                    placeMarker = gMap.addMarker(new MarkerOptions().position(this.placeLocation).title(address));
                    gMap.moveCamera(cu);
                }
            } else {
                generalFunc.showMessage(generalFunc.getCurrentView(this),
                        generalFunc.retrieveLangLBl("", "LBL_SELECET_LOC_WITHIN_ANTANANARIVO_TXT"));
            }
        }
        else
        {
            placeTxtView.setText(address);
            isPlaceSelected = true;
            this.placeLocation = new LatLng(latitude, longitude);

            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

            if (gMap != null) {
                gMap.clear();
                placeMarker = gMap.addMarker(new MarkerOptions().position(this.placeLocation).title(address));
                gMap.moveCamera(cu);
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;
        getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if(CommonUtilities.restrict_app.equalsIgnoreCase("Yes")) {

            getMap().setLatLngBoundsForCameraTarget(Utils.ANTANANARIVO);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(Utils.ANTANAVARIVO1)
                    .zoom(5.5f)
                    .build();
            getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

       /* CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(GOLDEN_GATE_BRIDGE) // Sets the center of the map to
                        // Golden Gate Bridge
                .zoom(17)                   // Sets the zoom
                .bearing(90) // Sets the orientation of the camera to east
                .tilt(30)    // Sets the tilt of the camera to 30 degrees
                .build();    // Creates a CameraPosition from the builder*/



        if (getIntent().getStringExtra("isPickUpLoc") != null && getIntent().hasExtra("PickUpLatitude") &&  getIntent().hasExtra("PickUpLongitude")) {

            LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0,getIntent().getStringExtra("PickUpLatitude")),
                    generalFunc.parseDoubleValue(0.0,getIntent().getStringExtra("PickUpLongitude")));

            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14);

            gMap.moveCamera(cu);

        }
        else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true") && home_address_str != null && !home_address_str.equals("")) {
            if (mpref_place.getString("userHomeLocationLatitude", null) != null && mpref_place.getString("userHomeLocationLongitude", null) != null) {
                LatLng HomeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, mpref_place.getString("userHomeLocationLatitude", "0.0")),
                        generalFunc.parseDoubleValue(0.0, mpref_place.getString("userHomeLocationLongitude", "0.0")));
                if (HomeLocation.latitude != 0.0 && HomeLocation.longitude != 0.0) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(HomeLocation, 14.0f);
//                    addMarker(HomeLocation);
                    gMap.addMarker(new MarkerOptions().position(HomeLocation).title(mpref_place.getString("userHomeLocationAddress", "")));

                    gMap.moveCamera(cu);
                }
            }
            placeTxtView.setText("" + mpref_place.getString("userHomeLocationAddress", ""));

        } else if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true") && work_address_str != null && !work_address_str.equals("")) {
            if (mpref_place.getString("userWorkLocationLatitude", null) != null && mpref_place.getString("userWorkLocationLongitude", null) != null) {
                LatLng WorkLocation = new LatLng(generalFunc.parseDoubleValue(0.0, mpref_place.getString("userWorkLocationLatitude", "0.0")),
                        generalFunc.parseDoubleValue(0.0, mpref_place.getString("userWorkLocationLongitude", "0.0")));
                if (WorkLocation.latitude != 0.0 && WorkLocation.longitude != 0.0) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(WorkLocation, 14.0f);
//                    addMarker(WorkLocation);
                    gMap.addMarker(new MarkerOptions().position(WorkLocation).title(mpref_place.getString("userWorkLocationAddress", "")));
                    gMap.moveCamera(cu);
                }
            }
            placeTxtView.setText("" + mpref_place.getString("userWorkLocationAddress", ""));

        }


        this.gMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                getAddressFromLocation.setLocation(latLng.latitude, latLng.longitude);
                getAddressFromLocation.setLoaderEnable(true);
                getAddressFromLocation.execute();
            }
        });

    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public class setOnClickAct implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                SearchPickupLocationActivity.super.onBackPressed();

            } else if (i == R.id.pickUpLocSearchArea) {
                try {

                    LatLngBounds bounds=null;

                    if (getIntent().hasExtra("PickUpLatitude") &&  getIntent().hasExtra("PickUpLongitude") ) {

                        LatLng pickupPlaceLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                                generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));
                        bounds=new LatLngBounds(pickupPlaceLocation,pickupPlaceLocation);
                    }

                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setBoundsBias(bounds)
                            .build(SearchPickupLocationActivity.this);
                    startActivityForResult(intent, Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);


                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                            generalFunc.retrieveLangLBl("", "LBL_SERVICE_NOT_AVAIL_TXT"));
                }
            } else if (i == btnId) {

                if (isPlaceSelected == false) {
                    generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                            "Please set location.");
                    return;
                }

                Bundle bn = new Bundle();
                bn.putString("Address", placeTxtView.getText().toString());
                bn.putString("Latitude", "" + placeLocation.latitude);
                bn.putString("Longitude", "" + placeLocation.longitude);

                Utils.printLog("Search Latitude", "::" + placeLocation.latitude);
                Utils.printLog("Search Longitude", "::" + placeLocation.longitude);

                new StartActProcess(getActContext()).setOkResult(bn);
                backImgView.performClick();
            } else if (i == homePlaceTxt.getId()) {
                onAddressFound(home_address_str, generalFunc.parseDoubleValue(0.0, userHomeLocationLatitude_str),
                        generalFunc.parseDoubleValue(0.0, userHomeLocationLongitude_str),"Home");
            } else if (i == workPlaceTxt.getId()) {
                onAddressFound(work_address_str, generalFunc.parseDoubleValue(0.0, userWorkLocationLatitude_str),
                        generalFunc.parseDoubleValue(0.0, userWorkLocationLongitude_str),"Work");
            }
            else if (i == changeMapTypImgView.getId()) {
                getMap().setMapType(getMap().getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place:" + place.toString());
                if(CommonUtilities.restrict_app.equalsIgnoreCase("Yes")) {

                    if (place.getName().equals("Antananarivo") || place.getAddress().toString().contains("Madagascar")) {
                        placeTxtView.setText(place.getAddress());
                        isPlaceSelected = true;
                        LatLng placeLocation = place.getLatLng();

                        this.placeLocation = placeLocation;

                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14.0f);

                        if (gMap != null) {
                            gMap.clear();
                            placeMarker = gMap.addMarker(new MarkerOptions().position(placeLocation).title("" + place.getAddress()));
                            gMap.moveCamera(cu);
                        }
                    } else {

                        generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                                generalFunc.retrieveLangLBl("", "LBL_SELECET_LOC_WITHIN_ANTANANARIVO_TXT"));
                    }
                }
                else {
                    placeTxtView.setText(place.getAddress());
                    isPlaceSelected = true;
                    LatLng placeLocation = place.getLatLng();

                    this.placeLocation = placeLocation;

                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14.0f);

                    if (gMap != null) {
                        gMap.clear();
                        placeMarker = gMap.addMarker(new MarkerOptions().position(placeLocation).title("" + place.getAddress()));
                        gMap.moveCamera(cu);
                    }
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());

                generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }

    public Context getActContext() {
        return SearchPickupLocationActivity.this;
    }
}
