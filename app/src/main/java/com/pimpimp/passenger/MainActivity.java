package com.pimpimp.passenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.adapter.files.DrawerAdapter;
import com.datepicker.files.SlideDateTimeListener;
import com.datepicker.files.SlideDateTimePicker;
import com.dialogs.RequestNearestCab;
import com.fragments.CabSelectionFragment;
import com.fragments.DriverAssignedHeaderFragment;
import com.fragments.DriverDetailFragment;
import com.fragments.MainHeaderFragment;
import com.fragments.PickUpLocSelectedFragment;
import com.fragments.RequestPickUpFragment;
import com.general.files.ConfigPubNub;
import com.general.files.CreateAnimation;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GcmBroadCastReceiver;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.HashMapComparator;
import com.general.files.LoadAvailableCab;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;
import com.view.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GetLocationUpdates.LocationUpdates, AdapterView.OnItemClickListener {

    MTextView titleTxt;
    ImageView backImgView;

    MTextView pickUpLocLabelTxt;

    public GeneralFunctions generalFunc;

    SupportMapFragment map;

    GetLocationUpdates getLastLocation;

    GoogleMap gMap;

    ListView menuListView;
    DrawerAdapter drawerAdapter;
    ArrayList<String[]> list_menu_items;

    public DrawerLayout mDrawerLayout;

    public String userProfileJson = "";

    public boolean isFirstLocation = true;
    public SlidingUpPanelLayout sliding_layout;

    public ImageView userLocBtnImgView;
    public ImageView changeMapTypImgView;
    public ImageView pinImgView;
    public LinearLayout selectSourceLocArea;
    public RelativeLayout locationMarker;
    MTextView pickUpLocTimeTxt;

    RelativeLayout dragView;
    RelativeLayout mainArea;
    MainHeaderFragment mainHeaderFrag;
    CabSelectionFragment cabSelectionFrag;
    PickUpLocSelectedFragment pickUpLocSelectedFrag;
    RequestPickUpFragment reqPickUpFrag;

    DriverAssignedHeaderFragment driverAssignedHeaderFrag;
    DriverDetailFragment driverDetailFrag;

    ArrayList<HashMap<String, String>> cabTypeList;
    ArrayList<HashMap<String, String>> cabCategoryList;

    public Location userLocation;
    LoadAvailableCab loadAvailCabs;
    Location pickUpLocation;

    String selectedCabTypeId = "1";

    boolean isDestinationAdded = false;
    String destLocLatitude = "";
    String destLocLongitude = "";
    String destAddress = "";

    public static String paymentMode = "Cash";
    public static  Boolean isCashEnable = false;

    RequestNearestCab requestNearestCab;

    public ArrayList<HashMap<String, String>> currentLoadedDriverList;

    GcmBroadCastReceiver gcmMessageBroadCastReceiver;

    boolean isDriverAssigned = false;

    HashMap<String, String> driverAssignedData;

    String assignedDriverId = "";
    String assignedTripId = "";

    String DRIVER_REQUEST_METHOD = "All";

    GenerateAlertBox noCabAvailAlertBox;

    SelectableRoundedImageView driverImgView;
    UpdateFrequentTask allCabRequestTask;

    SendNotificationsToDriverByDist sendNotificationToDriverByDist;

    boolean isDestinationMode = false;

    public ImageView emeTapImgView;

    public String selectedDateTime = "";
    public String selectedDateTimeZone = "";

    String cabRquestType = ""; // Later OR Now

    String pickUpLocationAddress = "";
    String destLocationAddress = "";

    View rideArea;
    View deliverArea;

    AlertDialog pickUpTypeAlertBox = null;

    Intent deliveryData;

    String eTripType = "";

    String app_type = "Ride";

    AlertDialog alertDialog_surgeConfirm;

    public ConfigPubNub configPubNub;

    public ProgressBar loader;

    String required_str = "";
    ArrayList<LatLng> medagascarLatLngBounds = new ArrayList<>();

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generalFunc = new GeneralFunctions(getActContext());

        setContentView(R.layout.activity_main);

        userProfileJson = getIntent().getStringExtra("USER_PROFILE_JSON");
        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);

        Utils.printLog("Api","APP_PAYMENT_MODE"+generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson));
        Utils.printLog("Api","APP_PAYMENT_CASH_ENABLED"+generalFunc.getJsonValue("APP_PAYMENT_CASH_ENABLED", userProfileJson));
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        menuListView = (ListView) findViewById(R.id.menuListView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        pickUpLocTimeTxt = (MTextView) findViewById(R.id.pickUpLocTimeTxt);
        pickUpLocLabelTxt = (MTextView) findViewById(R.id.pickUpLocLabelTxt);
        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);
        changeMapTypImgView = (ImageView) findViewById(R.id.changeMapTypImgView);
        pinImgView = (ImageView) findViewById(R.id.pinImgView);
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        sliding_layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        selectSourceLocArea = (LinearLayout) findViewById(R.id.selectSourceLocArea);
        locationMarker = (RelativeLayout) findViewById(R.id.locationMarker);
        dragView = (RelativeLayout) findViewById(R.id.dragView);
        mainArea = (RelativeLayout) findViewById(R.id.mainArea);
        emeTapImgView = (ImageView) findViewById(R.id.emeTapImgView);
        rideArea = findViewById(R.id.rideArea);
        deliverArea = findViewById(R.id.deliverArea);
        loader=(ProgressBar)findViewById(R.id.LoadingMapProgressBar);
        gcmMessageBroadCastReceiver = new GcmBroadCastReceiver((MainActivity) getActContext());

        if (isPubNubEnabled()) {
            configPubNub = new ConfigPubNub(getActContext());
        }

        map.getMapAsync(MainActivity.this);

        initializeViews();


        getLastLocation = new GetLocationUpdates(getActContext(), 8);
        getLastLocation.setLocationUpdatesListener(this);

        setGeneralData();

        buildMenu();

        new CreateAnimation(selectSourceLocArea, getActContext(), R.anim.design_fab_in, 1000).startAnimation();
        new CreateAnimation(dragView, getActContext(), R.anim.design_bottom_sheet_slide_in, 1000, true).startAnimation();

        selectSourceLocArea.setOnClickListener(new setOnClickList());
        userLocBtnImgView.setOnClickListener(new setOnClickList());
        changeMapTypImgView.setOnClickListener(new setOnClickList());
        emeTapImgView.setOnClickListener(new setOnClickList());
        rideArea.setOnClickListener(new setOnClickList());
        deliverArea.setOnClickListener(new setOnClickList());


        setUserInfo();


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                releaseScheduleNotificationTask();
                generalFunc.restartApp();
            }
        }

        cabRquestType = "";
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void setLabels() {
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        ((MTextView) findViewById(R.id.rideTxt)).setText(generalFunc.retrieveLangLBl("Ride", "LBL_RIDE"));
        ((MTextView) findViewById(R.id.deliverTxt)).setText(generalFunc.retrieveLangLBl("Deliver", "LBL_DELIVER"));


    }


    public boolean isPubNubEnabled() {
        String ENABLE_PUBNUB = generalFunc.getJsonValue("ENABLE_PUBNUB", userProfileJson);

        return ENABLE_PUBNUB.equalsIgnoreCase("Yes");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }

    public void setUserInfo() {
        ((MTextView) findViewById(R.id.userNameTxt)).setText(generalFunc.getJsonValue("vName", userProfileJson) + " "
                + generalFunc.getJsonValue("vLastName", userProfileJson));

        generalFunc.checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userImgView), userProfileJson, "vImgName");
    }

    public void setGeneralData() {
        generalFunc.storedata(CommonUtilities.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValue("MOBILE_VERIFICATION_ENABLE", userProfileJson));
        String DRIVER_REQUEST_METHOD = generalFunc.getJsonValue("DRIVER_REQUEST_METHOD", userProfileJson);

        this.DRIVER_REQUEST_METHOD = DRIVER_REQUEST_METHOD.equals("") ? "All" : DRIVER_REQUEST_METHOD;

        Utils.printLog("DRIVER_REQUEST_METHOD", "DRIVER_REQUEST_METHOD::" + DRIVER_REQUEST_METHOD);

        // Set menu wallet n invite friend according status

        generalFunc.storedata(CommonUtilities.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", userProfileJson));
        generalFunc.storedata(CommonUtilities.WALLET_ENABLE, generalFunc.getJsonValue("WALLET_ENABLE", userProfileJson));

    }


    public void buildMenu() {
        if (list_menu_items == null) {
            list_menu_items = new ArrayList();
            drawerAdapter = new DrawerAdapter(list_menu_items, getActContext(), generalFunc);

            menuListView.setAdapter(drawerAdapter);
            menuListView.setOnItemClickListener(this);
        } else {
            list_menu_items.clear();
        }

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_profile, generalFunc.retrieveLangLBl("", "LBL_MY_PROFILE_HEADER_TXT"), "" + Utils.MENU_PROFILE});

        if (generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Card")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_card, generalFunc.retrieveLangLBl("Payment", "LBL_PAYMENT"), "" + Utils.MENU_PAYMENT});
        }


        if (generalFunc.isWalletEnable()) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_wallet, generalFunc.retrieveLangLBl("", "LBL_LEFT_MENU_WALLET"), "" + Utils.MENU_WALLET});

        }

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_history, generalFunc.retrieveLangLBl("", "LBL_RIDE_HISTORY"), "" + Utils.MENU_RIDE_HISTORY});

        if (generalFunc.getJsonValue("RIIDE_LATER", userProfileJson).equalsIgnoreCase("YES")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_bookings, generalFunc.retrieveLangLBl("My Bookings", "LBL_MY_BOOKINGS"), "" + Utils.MENU_BOOKINGS});
        }

        if (generalFunc.isReferralSchemeEnable()) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_invite, generalFunc.retrieveLangLBl("Invite Friends", "LBL_INVITE_FRIEND_TXT"), "" + Utils.MENU_INVITE_FRIEND});

        }
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_emergency, generalFunc.retrieveLangLBl("Emergency Contact", "LBL_EMERGENCY_CONTACT"), "" + Utils.MENU_EMERGENCY_CONTACT});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_about_us, generalFunc.retrieveLangLBl("", "LBL_ABOUT_US_TXT"), "" + Utils.MENU_ABOUT_US});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_contact_us, generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"), "" + Utils.MENU_CONTACT_US});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_help, generalFunc.retrieveLangLBl("", "LBL_HELP_TXT"), "" + Utils.MENU_HELP});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_privacy, generalFunc.retrieveLangLBl("", "LBL_PRIVACY_TXT"), "" + Utils.MENU_MY_PRIVACY});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_logout, generalFunc.retrieveLangLBl("", "LBL_SIGNOUT_TXT"), "" + Utils.MENU_SIGN_OUT});

        drawerAdapter.notifyDataSetChanged();

    }

    public MainHeaderFragment getMainHeaderFrag() {
        return mainHeaderFrag;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        (findViewById(R.id.LoadingMapProgressBar)).setVisibility(View.GONE);

        if (googleMap == null) {
            return;
        }

        this.gMap = googleMap;
        if (generalFunc.checkLocationPermission(true) == true) {
            getMap().setMyLocationEnabled(true);
            getMap().getUiSettings().setTiltGesturesEnabled(false);
            getMap().getUiSettings().setCompassEnabled(false);
            getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
            if(CommonUtilities.restrict_app.equalsIgnoreCase("Yes")) {

                getMap().setLatLngBoundsForCameraTarget(Utils.ANTANANARIVO);
            }
            getMap().getUiSettings().setMyLocationButtonEnabled(false);

            if (getPickUpLocation() != null) {

                Location placeLocation = getPickUpLocation();
                LatLng placeLatLng = new LatLng(placeLocation.getLatitude(), placeLocation.getLongitude());
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLatLng, 14.0f);
                gMap.moveCamera(cu);

            }
            if(CommonUtilities.restrict_app.equalsIgnoreCase("Yes")) {

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(Utils.ANTANAVARIVO1)
                        .zoom(5.5f)
                        .build();
                getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.hideInfoWindow();
                return true;
            }
        });
        getMap().setOnMapClickListener(this);

        if (mainHeaderFrag != null) {
            mainHeaderFrag.setGoogleMapInstance(this.gMap);
        }

        if (loadAvailCabs == null && userLocation != null) {
            initializeLoadCab();
        }

        if (driverAssignedHeaderFrag != null) {
            driverAssignedHeaderFrag.setGoogleMap(getMap());
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void setShadow() {
        if (cabSelectionFrag != null) {
            cabSelectionFrag.setShadow();
        }
    }

    public void setUserLocImgBtnMargin(int margin) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) (userLocBtnImgView).getLayoutParams();
        params.bottomMargin = Utils.dipToPixels(getActContext(), margin);

        userLocBtnImgView.setLayoutParams(params);
        changeMapTypImgView.setLayoutParams(params);
    }

    public void initializeLoadCab() {
        if (isDriverAssigned == true) {
            return;
        }
        loadAvailCabs = new LoadAvailableCab(getActContext(), generalFunc, selectedCabTypeId, userLocation,
                getMap(), userProfileJson);
        loadAvailCabs.checkAvailableCabs();
    }

    public void updateCabs() {
        if (loadAvailCabs != null) {
            loadAvailCabs.setPickUpLocation(pickUpLocation);
            loadAvailCabs.changeCabs();
        }
    }

    public void initializeViews() {
        String vTripStatus = generalFunc.getJsonValue("vTripStatus", userProfileJson);

        if (vTripStatus != null && (vTripStatus.equals("Active") || vTripStatus.equals("On Going Trip"))) {
            //Assign driver
            isDriverAssigned = true;
            if (driverAssignedHeaderFrag != null) {
                driverAssignedHeaderFrag = null;
            }
            configureAssignedDriver(true);

            configureDeliveryView(true);

        } else {
            //Set default view
            if (mainHeaderFrag == null) {
                mainHeaderFrag = new MainHeaderFragment();
            }

            if (cabSelectionFrag == null) {
                cabSelectionFrag = new CabSelectionFragment();
            }

            setCurrentType();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.headerContainer, mainHeaderFrag).commit();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dragView, cabSelectionFrag).commit();

            configureDeliveryView(false);
        }

        Utils.runGC();
    }

    private void setCurrentType() {
            cabSelectionFrag.currentCabGeneralType = Utils.CabGeneralType_Ride;
    }

    public void configureDeliveryView(boolean isHidden) {
        if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase("Ride-Delivery") && isHidden == false) {
            (findViewById(R.id.deliveryArea)).setVisibility(View.VISIBLE);
            setUserLocImgBtnMargin(190);
        } else {
            (findViewById(R.id.deliveryArea)).setVisibility(View.GONE);
            setUserLocImgBtnMargin(105);
        }

    }

    public void configDestinationMode(boolean isDestinationMode) {
        this.isDestinationMode = isDestinationMode;

        if (isDestinationMode == false) {
            pinImgView.setImageResource(R.drawable.pin_source_select);
            animateToLocation(getPickUpLocation().getLatitude(), getPickUpLocation().getLongitude());
        } else {
            pinImgView.setImageResource(R.drawable.pin_dest_select);

            if (isDestinationAdded == true && !getDestLocLatitude().trim().equals("") && !getDestLocLongitude().trim().equals("")) {
                animateToLocation(generalFunc.parseDoubleValue(0.0, getDestLocLatitude()), generalFunc.parseDoubleValue(0.0, getDestLocLongitude()));
            }
        }

        if (mainHeaderFrag != null) {
            mainHeaderFrag.configDestinationMode(isDestinationMode);
        }
    }

    public void animateToLocation(double latitude, double longitude) {
        if (latitude != 0.0 && longitude != 0.0) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude))
                    .zoom(gMap.getCameraPosition().zoom).build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

    }

    public void configureAssignedDriver(boolean isAppRestarted) {
        driverDetailFrag = new DriverDetailFragment();
        driverAssignedHeaderFrag = new DriverAssignedHeaderFragment();
        Bundle bn = new Bundle();
        bn.putString("isAppRestarted", "" + isAppRestarted);


        driverAssignedData = new HashMap<>();

        if (isAppRestarted == true) {

            String tripDetailJson = generalFunc.getJsonValue("TripDetails", userProfileJson);
            Utils.printLog("Api","TripDetails"+tripDetailJson.toString());
            String driverDetailJson = generalFunc.getJsonValue("DriverDetails", userProfileJson);
            String driverCarDetailJson = generalFunc.getJsonValue("DriverCarDetails", userProfileJson);

            String vTripPaymentMode = generalFunc.getJsonValue("vTripPaymentMode", tripDetailJson);
            String tEndLat = generalFunc.getJsonValue("tEndLat", tripDetailJson);
            String tEndLong = generalFunc.getJsonValue("tEndLong", tripDetailJson);
            String tDaddress = generalFunc.getJsonValue("tDaddress", tripDetailJson);

            paymentMode=vTripPaymentMode;
            assignedDriverId = generalFunc.getJsonValue("iDriverId", tripDetailJson);
            assignedTripId = generalFunc.getJsonValue("iTripId", tripDetailJson);
            eTripType = generalFunc.getJsonValue("eType", tripDetailJson);
            Log.d("Api", "eTripType" + eTripType);

            if (!tEndLat.equals("0.0") && !tEndLong.equals("0.0")
                    && !tDaddress.equals("Not Set") && !tEndLat.equals("") && !tEndLong.equals("")
                    && !tDaddress.equals("")) {
                isDestinationAdded = true;
                destAddress = tDaddress;
                destLocLatitude = tEndLat;
                destLocLongitude = tEndLong;


            }
            driverAssignedData.put("PickUpLatitude", generalFunc.getJsonValue("tStartLat", tripDetailJson));
            driverAssignedData.put("PickUpLongitude", generalFunc.getJsonValue("tStartLong", tripDetailJson));
            driverAssignedData.put("PickUpAddress", generalFunc.getJsonValue("tSaddress", tripDetailJson));
            driverAssignedData.put("vVehicleType", generalFunc.getJsonValue("vVehicleType", tripDetailJson));
            driverAssignedData.put("eType", generalFunc.getJsonValue("eType", tripDetailJson));
            driverAssignedData.put("carTypeName", generalFunc.getJsonValue("carTypeName", tripDetailJson));
            driverAssignedData.put("eIconType", generalFunc.getSelectedCarTypeData(generalFunc.getJsonValue("iVehicleTypeId", tripDetailJson), "VehicleTypes", "eIconType", userProfileJson));
            driverAssignedData.put("DriverPhone", generalFunc.getJsonValue("vPhone", driverDetailJson));
            driverAssignedData.put("DriverPhoneCode", generalFunc.getJsonValue("vCode", driverDetailJson));
            driverAssignedData.put("DriverRating", generalFunc.getJsonValue("vAvgRating", driverDetailJson));
            driverAssignedData.put("DriverAppVersion", generalFunc.getJsonValue("iAppVersion", driverDetailJson));
            driverAssignedData.put("DriverLatitude", generalFunc.getJsonValue("vLatitude", driverDetailJson));
            driverAssignedData.put("DriverLongitude", generalFunc.getJsonValue("vLongitude", driverDetailJson));
            driverAssignedData.put("DriverImage", generalFunc.getJsonValue("vImage", driverDetailJson));
            driverAssignedData.put("DriverName", generalFunc.getJsonValue("vName", driverDetailJson));
            driverAssignedData.put("DriverCarPlateNum", generalFunc.getJsonValue("vLicencePlate", driverCarDetailJson));
            driverAssignedData.put("DriverCarName", generalFunc.getJsonValue("make_title", driverCarDetailJson));
            driverAssignedData.put("DriverCarModelName", generalFunc.getJsonValue("model_title", driverCarDetailJson));


        } else {

            if (currentLoadedDriverList == null) {
                generalFunc.restartApp();
                return;
            }

            boolean isDriverIdMatch = false;
            for (int i = 0; i < currentLoadedDriverList.size(); i++) {
                HashMap<String, String> driverDataMap = currentLoadedDriverList.get(i);
                String iDriverId = driverDataMap.get("driver_id");

                if (iDriverId.equals(assignedDriverId)) {
                    isDriverIdMatch = true;

                    driverAssignedData.put("PickUpLatitude", "" + getPickUpLocation().getLatitude());
                    driverAssignedData.put("PickUpLongitude", "" + getPickUpLocation().getLongitude());

                    if (pickUpLocSelectedFrag != null) {
                        driverAssignedData.put("PickUpAddress", pickUpLocSelectedFrag.getPickUpAddress());
                    } else {
                        driverAssignedData.put("PickUpAddress", "");
                    }
                    driverAssignedData.put("carTypeName", generalFunc.getJsonValue("carTypeName", generalFunc.getSelectedCarTypeData(selectedCabTypeId, "VehicleTypes", "vVehicleType", userProfileJson)));
                    driverAssignedData.put("vVehicleType", generalFunc.getSelectedCarTypeData(selectedCabTypeId, "VehicleTypes", "vVehicleType", userProfileJson));
                    driverAssignedData.put("eIconType", generalFunc.getSelectedCarTypeData(selectedCabTypeId, "VehicleTypes", "eIconType", userProfileJson));
                    driverAssignedData.put("DriverPhone", driverDataMap.get("vPhone_driver"));
                    driverAssignedData.put("DriverPhoneCode", driverDataMap.get("vCode"));
                    driverAssignedData.put("DriverRating", driverDataMap.get("average_rating"));
                    driverAssignedData.put("DriverAppVersion", driverDataMap.get("iAppVersion"));
                    driverAssignedData.put("DriverLatitude", driverDataMap.get("Latitude"));
                    driverAssignedData.put("DriverLongitude", driverDataMap.get("Longitude"));
                    driverAssignedData.put("DriverImage", driverDataMap.get("driver_img"));
                    driverAssignedData.put("DriverName", driverDataMap.get("Name"));
                    driverAssignedData.put("DriverCarPlateNum", driverDataMap.get("vLicencePlate"));
                    driverAssignedData.put("DriverCarName", driverDataMap.get("make_title"));
                    driverAssignedData.put("DriverCarModelName", driverDataMap.get("model_title"));
                    driverAssignedData.put("eType", getCurrentCabGeneralType());

                    break;
                }
            }

            if (isDriverIdMatch == false) {
                generalFunc.restartApp();
                return;
            }
        }

        driverAssignedData.put("iDriverId", assignedDriverId);
        driverAssignedData.put("iTripId", assignedTripId);
        driverAssignedData.put("PassengerName", generalFunc.getJsonValue("vName", userProfileJson));
        driverAssignedData.put("PassengerImageName", generalFunc.getJsonValue("vImgName", userProfileJson));

        bn.putSerializable("TripData", driverAssignedData);
        driverAssignedHeaderFrag.setArguments(bn);
        driverDetailFrag.setArguments(bn);

        Location pickUpLoc = new Location("");
        pickUpLoc.setLatitude(generalFunc.parseDoubleValue(0.0, driverAssignedData.get("PickUpLatitude")));
        pickUpLoc.setLongitude(generalFunc.parseDoubleValue(0.0, driverAssignedData.get("PickUpLongitude")));
        this.pickUpLocation = pickUpLoc;

        if (mainHeaderFrag != null) {
            mainHeaderFrag.releaseResources();
            mainHeaderFrag = null;
        }

        if (cabSelectionFrag != null) {
            cabSelectionFrag.releaseResources();
            cabSelectionFrag = null;
        }

        Utils.runGC();

        locationMarker.setVisibility(View.GONE);

        setPanelHeight(100);

        try {
            super.onPostResume();
        } catch (Exception e) {

        }

        if (!isFinishing()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.headerContainer, driverAssignedHeaderFrag).commit();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dragView, driverDetailFrag).commit();

            setUserLocImgBtnMargin(105);
            new CreateAnimation(userLocBtnImgView, getActContext(), R.anim.design_bottom_sheet_slide_in, 600, true).startAnimation();
        } else {
            generalFunc.restartApp();
        }

    }

    @Override
    public void onLocationUpdate(Location location) {
        this.userLocation = location;

        CameraPosition cameraPosition = cameraForUserPosition();

        if (isFirstLocation == true) {
            if (cameraPosition != null)
                getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            isFirstLocation = false;
        }

        if (loadAvailCabs == null && getMap() != null) {
            initializeLoadCab();
        }
    }

    public CameraPosition cameraForUserPosition() {

        double currentZoomLevel = getMap() == null ? Utils.defaultZomLevel : getMap().getCameraPosition().zoom;

        if (Utils.defaultZomLevel > currentZoomLevel) {
            currentZoomLevel = Utils.defaultZomLevel;
        }
        if (userLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                    .zoom((float) currentZoomLevel).build();

            return cameraPosition;
        } else {
            return null;
        }
    }

    public CameraPosition cameraForDriverPosition() {

        double currentZoomLevel = getMap() == null ? Utils.defaultZomLevel : getMap().getCameraPosition().zoom;

        if (Utils.defaultZomLevel > currentZoomLevel) {
            currentZoomLevel = Utils.defaultZomLevel;
        }
        if (userLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(driverAssignedHeaderFrag.getDriverLoc())
                    .zoom((float) currentZoomLevel).build();

            return cameraPosition;
        } else {
            return null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        int itemId = generalFunc.parseIntegerValue(0, list_menu_items.get(position)[2]);
        Bundle bn = new Bundle();
        bn.putString("UserProfileJson", userProfileJson);
        switch (itemId) {
            case Utils.MENU_PROFILE:
                new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
                break;
            case Utils.MENU_RIDE_HISTORY:
                new StartActProcess(getActContext()).startActWithData(HistoryActivity.class, bn);
                break;
            case Utils.MENU_BOOKINGS:
                new StartActProcess(getActContext()).startActWithData(MyBookingsActivity.class, bn);
                break;
            case Utils.MENU_ABOUT_US:
                new StartActProcess(getActContext()).startAct(AboutUsActivity.class);
                break;
            case Utils.MENU_MY_PRIVACY:
                bn.putString("from","privacy");
                new StartActProcess(getActContext()).startActWithData(AboutUsActivity.class,bn);
                break;
            case Utils.MENU_PAYMENT:
                new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                break;

            case Utils.MENU_CONTACT_US:
                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                break;
            case Utils.MENU_HELP:
                new StartActProcess(getActContext()).startAct(HelpActivity.class);
                break;

            case Utils.MENU_EMERGENCY_CONTACT:
                new StartActProcess(getActContext()).startAct(EmergencyContactActivity.class);
                break;
            case Utils.MENU_WALLET:
                new StartActProcess(getActContext()).startActWithData(MyWalletActivity.class, bn);
                break;
            case Utils.MENU_INVITE_FRIEND:
                new StartActProcess(getActContext()).startActWithData(InviteFriendsActivity.class, bn);
                break;
            case Utils.MENU_SIGN_OUT:
                generalFunc.logOutUser();
                releaseScheduleNotificationTask();
                generalFunc.restartApp();
                break;
        }

        closeDrawer();
    }

    public void OpenCardPaymentAct() {
        Bundle bn = new Bundle();
        bn.putString("UserProfileJson", userProfileJson);
        new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
    }

    public void checkDrawerState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START) == true) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public boolean isPickUpLocationCorrect() {
        String pickUpLocAdd = mainHeaderFrag != null ? (mainHeaderFrag.getPickUpAddress().equals(
                generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT")) ? "" : mainHeaderFrag.getPickUpAddress()) : "";

        if (!pickUpLocAdd.equals("")) {
            return true;
        }

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.pimpimp.passenger/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.pimpimp.passenger/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == selectSourceLocArea.getId()) {
                String pickUpLocAdd = mainHeaderFrag != null ? (mainHeaderFrag.getPickUpAddress().equals(
                        generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT")) ? "" : mainHeaderFrag.getPickUpAddress()) : "";
                resetSelctedDatenTime();
                if (!pickUpLocAdd.equals("")) {

                    if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase("Ride-Delivery")) {

                        choosePickUpOption();
                    } else {
                        setCabReqType(Utils.CabReqType_Now);

                        checkSurgePrice("");

                    }

                }
            } else if (i == userLocBtnImgView.getId()) {
                if (driverAssignedHeaderFrag!=null && driverAssignedHeaderFrag.getDriverLoc()!=null)
                {
                    CameraPosition cameraPosition = cameraForDriverPosition();
                    if (cameraPosition != null)
                        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                else
                {
                    CameraPosition cameraPosition = cameraForUserPosition();
                    if (cameraPosition != null)
                        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            } else if (i == changeMapTypImgView.getId()) {
                getMap().setMapType(getMap().getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
            } else if (i == emeTapImgView.getId()) {
                Bundle bn = new Bundle();
                bn.putString("UserProfileJson", userProfileJson);
                bn.putString("TripId", assignedTripId);
                new StartActProcess(getActContext()).startActWithData(ConfirmEmergencyTapActivity.class, bn);
            } else if (i == rideArea.getId()) {
                ((ImageView) findViewById(R.id.rideImg)).setImageResource(R.mipmap.ride_on);
                ((ImageView) findViewById(R.id.deliverImg)).setImageResource(R.mipmap.delivery_off);

                ((MTextView) findViewById(R.id.rideTxt)).setTextColor(Color.parseColor("#FFFFFF"));
                ((MTextView) findViewById(R.id.deliverTxt)).setTextColor(Color.parseColor("#A2A2A2"));
                if (cabSelectionFrag != null) {
                    cabSelectionFrag.changeCabGeneralType(Utils.CabGeneralType_Ride);
                }

            } else if (i == deliverArea.getId()) {

                ((ImageView) findViewById(R.id.rideImg)).setImageResource(R.mipmap.ride_off);
                ((ImageView) findViewById(R.id.deliverImg)).setImageResource(R.mipmap.delivery_on);
                ((MTextView) findViewById(R.id.rideTxt)).setTextColor(Color.parseColor("#A2A2A2"));
                ((MTextView) findViewById(R.id.deliverTxt)).setTextColor(Color.parseColor("#FFFFFF"));

            }

        }
    }

    public String getCurrentCabGeneralType() {
        if (cabSelectionFrag != null) {
            return cabSelectionFrag.getCurrentCabGeneralType();
        } else if (!eTripType.trim().equals("")) {
            return eTripType;
        }

        return Utils.CabGeneralType_Ride;
    }


    public void chooseDateTime() {

        if (isPickUpLocationCorrect() == false) {
            return;
        }
        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {

                        Utils.printLog("Date select:", "::" + date.toString());
                        Utils.printLog("Time Zone:", "::" + Calendar.getInstance().getTimeZone().getID());
                        Utils.printLog("Converted Date select:", "::" + Utils.convertDateToFormat("yyyy-MM-dd H:mm", date));
                        Utils.printLog("Is valid Time", "::" + Utils.isValidTimeSelect(date, TimeUnit.HOURS.toMillis(1)));

                        selectedDateTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                        selectedDateTimeZone = Calendar.getInstance().getTimeZone().getID();

                        if (Utils.isValidTimeSelect(date, TimeUnit.HOURS.toMillis(1)) == false) {

                            generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Invalid pickup time", "LBL_INVALID_PICKUP_TIME"),
                                    generalFunc.retrieveLangLBl("Please make sure that pickup time is after atleast an hour from now.", "LBL_INVALID_PICKUP_NOTE_MSG"));
                            return;
                        }
                        setCabReqType(Utils.CabReqType_Later);
                        String selectedTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                        Utils.printLog("selectedTime", "::" + selectedTime);
                        checkSurgePrice(selectedTime);
                    }
                })
                .setInitialDate(new Date())
                .setMinDate(Calendar.getInstance().getTime())
                        //.setMaxDate(maxDate)
                .setIs24HourTime(true)
                        //.setTheme(SlideDateTimePicker.HOLO_DARK)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }

    public void setCabTypeList(ArrayList<HashMap<String, String>> cabTypeList) {
        this.cabTypeList = cabTypeList;
    }

    public void setCabCategoryList(ArrayList<HashMap<String, String>> cabCategoryList) {
        this.cabCategoryList = cabCategoryList;
    }

    public void changeCabType(String selectedCabTypeId) {
        this.selectedCabTypeId = selectedCabTypeId;

        if (loadAvailCabs != null) {
            loadAvailCabs.setCabTypeId(this.selectedCabTypeId);
        }
        updateCabs();
    }

    public String getSelectedCabTypeId() {
        return this.selectedCabTypeId;
    }

    public void choosePickUpOption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActContext());
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_pick_up_type_dialog, null);
        builder.setView(dialogView);

        LinearLayout pickUpLaterArea = (LinearLayout) dialogView.findViewById(R.id.pickUpLaterArea);
        LinearLayout pickUpNowArea = (LinearLayout) dialogView.findViewById(R.id.pickUpNowArea);
        ImageView pickUpLaterImgView = (ImageView) dialogView.findViewById(R.id.pickUpLaterImgView);
        ImageView pickUpNowImgView = (ImageView) dialogView.findViewById(R.id.pickUpNowImgView);
        MButton btn_type1 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type1)).getChildView();

        btn_type1.setText(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));

        ((MTextView) dialogView.findViewById(R.id.pickUpTypeHTxt)).setText(generalFunc.retrieveLangLBl("Select your PickUp type",
                "LBL_SELECT_PICKUP_TYPE_HEADER"));

            ((MTextView) dialogView.findViewById(R.id.rideNowTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_RIDE_NOW"));
            ((MTextView) dialogView.findViewById(R.id.rideLaterTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_RIDE_LATER"));

        pickUpLaterImgView.setColorFilter(Color.parseColor("#FFFFFF"));
        pickUpNowImgView.setColorFilter(Color.parseColor("#FFFFFF"));

        pickUpLaterArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePickUpTypeAlertBox();
                chooseDateTime();
            }
        });


        btn_type1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePickUpTypeAlertBox();
            }
        });

        pickUpNowArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCabReqType(Utils.CabReqType_Now);
                closePickUpTypeAlertBox();
//                pickUpLocClicked();

                checkSurgePrice("");
            }
        });

        new CreateRoundedView(android.R.color.transparent, Utils.dipToPixels(getActContext(), 40),
                Utils.dipToPixels(getActContext(), 1), Color.parseColor("#FFFFFF"), (dialogView.findViewById(R.id.pickUpLaterImgView)), true);

        new CreateRoundedView(android.R.color.transparent, Utils.dipToPixels(getActContext(), 40),
                Utils.dipToPixels(getActContext(), 1), Color.parseColor("#FFFFFF"), (dialogView.findViewById(R.id.pickUpNowImgView)), true);


        pickUpTypeAlertBox = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(pickUpTypeAlertBox);
        }
        pickUpTypeAlertBox.show();
    }

    public void closePickUpTypeAlertBox() {
        if (pickUpTypeAlertBox != null) {
            pickUpTypeAlertBox.cancel();
        }
    }

    public void checkSurgePrice(final String selectedTime) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "checkSurgePrice");
        parameters.put("SelectedCarTypeID", "" + getSelectedCabTypeId());
        if (!selectedTime.trim().equals("")) {
            parameters.put("SelectedTime", selectedTime);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                Utils.printLog("responseString", "::" + responseString);
                if (responseString != null && !responseString.equals("")) {

                    generalFunc.sendHeartBeat();

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        pickUpLocClicked();
                    } else {
                        openSurgeConfirmDialog(responseString);
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void openSurgeConfirmDialog(String responseString) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.surge_confirm_design, null);
        builder.setView(dialogView);

        ((MTextView) dialogView.findViewById(R.id.headerMsgTxt)).setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
        ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.getJsonValue("SurgePrice", responseString));
        ((MTextView) dialogView.findViewById(R.id.payableTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PAYABLE_AMOUNT"));
        ((MTextView) dialogView.findViewById(R.id.tryLaterTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRY_LATER"));

        MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_SURGE"));
        btn_type2.setId(Utils.generateViewId());

        btn_type2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog_surgeConfirm.dismiss();

                pickUpLocClicked();
            }
        });
        (dialogView.findViewById(R.id.tryLaterTxt)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog_surgeConfirm.dismiss();
                closeRequestDialog(true);
            }
        });

        alertDialog_surgeConfirm = builder.create();
        alertDialog_surgeConfirm.setCancelable(false);
        alertDialog_surgeConfirm.setCanceledOnTouchOutside(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog_surgeConfirm);
        }

        alertDialog_surgeConfirm.show();
    }

    public void pickUpLocClicked() {

        if (reqPickUpFrag != null) {
            reqPickUpFrag = null;
            Utils.runGC();
        }
        if (pickUpLocSelectedFrag != null) {
            pickUpLocSelectedFrag = null;
            Utils.runGC();
        }

        Utils.runGC();
        pickUpLocSelectedFrag = new PickUpLocSelectedFragment();
        pickUpLocSelectedFrag.setPickUpLocSelectedFrag(pickUpLocSelectedFrag);
        pickUpLocSelectedFrag.setGoogleMap(getMap());
        pickUpLocSelectedFrag.setPickUpAddress(mainHeaderFrag.getPickUpAddress());


        reqPickUpFrag = new RequestPickUpFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.headerContainer, pickUpLocSelectedFrag).commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dragView, reqPickUpFrag).commit();

        new CreateAnimation(userLocBtnImgView, getActContext(), R.anim.design_bottom_sheet_slide_in, 600, true).startAnimation();
        new CreateAnimation(dragView, getActContext(), R.anim.design_bottom_sheet_slide_in, 600, true).startAnimation();

        selectSourceLocArea.setVisibility(View.INVISIBLE);

        configureDeliveryView(true);
    }

    public RequestPickUpFragment getReqPickUpFrag() {
        return this.reqPickUpFrag;
    }

    public void setDefaultView() {
        try {
            super.onPostResume();
        } catch (Exception e) {

        }

        setDestinationPoint("", "", "", false);
        cabRquestType = "";

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.headerContainer, mainHeaderFrag).commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dragView, cabSelectionFrag).commit();

        configDestinationMode(false);
        userLocBtnImgView.performClick();
        pickUpLocSelectedFrag = null;
        reqPickUpFrag = null;
        Utils.runGC();

        if (cabSelectionFrag != null) {
            setPanelHeight(cabSelectionFrag.currentPanelDefaultStateHeight);
        } else {
            setPanelHeight(100);
        }

        configureDeliveryView(false);

        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        if (cabSelectionFrag != null) {
            cabSelectionFrag.configRideLaterBtnArea(false);
            new CreateAnimation(userLocBtnImgView, getActContext(), R.anim.design_bottom_sheet_slide_in, 600, true).startAnimation();
        }
        new CreateAnimation(dragView, getActContext(), R.anim.design_bottom_sheet_slide_in, 600, true).startAnimation();

        selectSourceLocArea.setVisibility(View.VISIBLE);


        if (loadAvailCabs != null) {
            loadAvailCabs.setTaskKilledValue(false);
            loadAvailCabs.onResumeCalled();
        }


    }

    public void setPanelHeight(int value) {
        sliding_layout.setPanelHeight(Utils.dipToPixels(getActContext(), value));
    }

    public void onPickUpLocChanged(Location pickUpLocation) {
        this.pickUpLocation = pickUpLocation;

        updateCabs();
    }

    public Location getPickUpLocation() {
        return this.pickUpLocation;
    }

    public String getPickUpLocationAddress() {
        return this.pickUpLocationAddress;
    }

    public void setETA(String time) {
        if (cabSelectionFrag != null) {
            cabSelectionFrag.setETA(time);
        }
        pickUpLocTimeTxt.setText(time.replace(" ", "\n"));
    }

    public void notifyCarSearching() {
        selectSourceLocArea.setClickable(false);
        pickUpLocLabelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_CAR_WAIT_TXT"));

        setETA("--");

        if (cabSelectionFrag != null && cabSelectionFrag.ride_now_btn!=null) {
            cabSelectionFrag.ride_now_btn.setEnabled(false);
            cabSelectionFrag.ride_now_btn.setTextColor(Color.parseColor("#BABABA"));
        }
    }

    public void notifyNoCabs() {
        if (app_type.equalsIgnoreCase("Ride-Delivery")) {
            selectSourceLocArea.setClickable(true);
            pickUpLocLabelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SET_PICK_UP_LOCATION_TXT"));
        } else {
            selectSourceLocArea.setClickable(false);
            pickUpLocLabelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NO_CAR_AVAIL_TXT"));
        }

        setETA("--");
        setCurrentLoadedDriverList(new ArrayList<HashMap<String, String>>());

        if (noCabAvailAlertBox == null && generalFunc.getJsonValue("SITE_TYPE", userProfileJson).equalsIgnoreCase("Demo")) {
            String prefix_msg =  "LBL_NO_CARS_NOTE_1_TXT";
            String suffix_msg =  "LBL_NO_CARS_NOTE_2_TXT";

            buildNoCabMessage(generalFunc.retrieveLangLBl("", prefix_msg) + " " +
                            generalFunc.getSelectedCarTypeData(getSelectedCabTypeId(), "VehicleTypes", "vVehicleType", userProfileJson)
                            + ". " + generalFunc.retrieveLangLBl("", suffix_msg),
                    generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        }

        if (cabSelectionFrag!=null && cabSelectionFrag.ride_now_btn!=null) {
            cabSelectionFrag.ride_now_btn.setEnabled(false);
            cabSelectionFrag.ride_now_btn.setTextColor(Color.parseColor("#BABABA"));
        }
    }


    public void buildNoCabMessage(String message, String positiveBtn) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(true);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        generateAlert.showAlertBox();

        this.noCabAvailAlertBox = generateAlert;
    }

    public void notifyCabsAvailable() {
        selectSourceLocArea.setClickable(true);
        pickUpLocLabelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SET_PICK_UP_LOCATION_TXT"));

       if (cabSelectionFrag!=null && cabSelectionFrag.ride_now_btn!=null ) {
           cabSelectionFrag.ride_now_btn.setEnabled(true);
           cabSelectionFrag.ride_now_btn.setTextColor(getResources().getColor(R.color.btn_text_color_type2));
       }
       }

    public void onMapCameraChanged() {
        if (pickUpLocSelectedFrag != null) {
            if (isDestinationMode == true) {
                pickUpLocSelectedFrag.setDestinationAddress(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
            } else {
                pickUpLocSelectedFrag.setPickUpAddress(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
            }

        }
    }

    public void onAddressFound(String address) {
        if (pickUpLocSelectedFrag != null) {
            if (isDestinationMode == true) {
                pickUpLocSelectedFrag.setDestinationAddress(address);
            } else {
                pickUpLocSelectedFrag.setPickUpAddress(address);
            }
        }

    }

    public void setDestinationPoint(String destLocLatitude, String destLocLongitude, String destAddress, boolean isDestinationAdded) {
        this.isDestinationAdded = isDestinationAdded;
        this.destLocLatitude = destLocLatitude;
        this.destLocLongitude = destLocLongitude;
        this.destAddress = destAddress;
    }

    public boolean getDestinationStatus() {
        return this.isDestinationAdded;
    }

    public String getDestLocLatitude() {
        return this.destLocLatitude;
    }

    public String getDestLocLongitude() {
        return this.destLocLongitude;
    }

    public String getDestAddress() {
        return this.destAddress;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public void setCashModeEnable(Boolean isCashEnable) {
        this.isCashEnable = isCashEnable;
    }

    public void setCabReqType(String cabRquestType) {
        this.cabRquestType = cabRquestType;
    }

    public String getCabReqType() {
        return this.cabRquestType;
    }

    public Bundle getFareEstimateBundle(String isTrue) {
        Bundle bn = new Bundle();
        bn.putString("PickUpLatitude", "" + getPickUpLocation().getLatitude());
        bn.putString("PickUpLongitude", "" + getPickUpLocation().getLongitude());
        bn.putString("isDestinationAdded", "" + getDestinationStatus());
        bn.putString("DestLocLatitude", "" + getDestLocLatitude());
        bn.putString("DestLocLongitude", "" + getDestLocLongitude());
        bn.putString("DestLocAddress", "" + getDestAddress());
        if (Utils.checkText(selectedDateTime)) {
            bn.putString("selectedDateTime", "" + selectedDateTime);
        }
        if (Utils.checkText(selectedDateTimeZone)) {
            bn.putString("TimeZone", "" + selectedDateTimeZone);
        }
        bn.putString("SelectedCarId", "" + getSelectedCabTypeId());
        bn.putString("UserProfileJson", "" + userProfileJson);
        bn.putString("CabReqType", "" + getCabReqType());
        bn.putString("RequestBtn",isTrue);

        return bn;
    }

    public void setRideSchedule(String pickUpInstruction) {

        if (getDestinationStatus() == false) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ADD_DEST_MSG_BOOK_RIDE"));
        } else {
            bookRide(pickUpInstruction);
        }
    }

    private void resetSelctedDatenTime() {
        if (!getCabReqType().equals(Utils.CabReqType_Later)) {
            selectedDateTime="";
            selectedDateTimeZone="";
        }
    }

    public void bookRide(String pickUpInstruction) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "ScheduleARide");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("pickUpLocAdd", pickUpLocSelectedFrag != null ? pickUpLocSelectedFrag.getPickUpAddress() : "");
        parameters.put("pickUpLatitude", "" + getPickUpLocation().getLatitude());
        parameters.put("pickUpLongitude", "" + getPickUpLocation().getLongitude());
        parameters.put("destLocAdd", getDestAddress());
        parameters.put("destLatitude", getDestLocLatitude());
        parameters.put("destLongitude", getDestLocLongitude());
        parameters.put("scheduleDate", selectedDateTime);
        parameters.put("iVehicleTypeId", getSelectedCabTypeId());
        parameters.put("TimeZone", selectedDateTimeZone);
        parameters.put("vPaymentMode", "" + paymentMode);
        parameters.put("eType", "Ride");
        parameters.put("userName", generalFunc.getJsonValue("vName", userProfileJson) + " " + generalFunc.getJsonValue("vLastName", userProfileJson));
        parameters.put("PickUpIns", pickUpInstruction);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    String action = generalFunc.getJsonValue(CommonUtilities.action_str, responseString);
                    if (action.equals("1")) {
                        resetSelctedDatenTime();
                        setDefaultView();
                    }
                    showBookingAlert(generalFunc.retrieveLangLBl("",
                            generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void showBookingAlert(String message) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();

                if (btn_id == 0) {
                    Bundle bn = new Bundle();
                    bn.putString("UserProfileJson", userProfileJson);
                    new StartActProcess(getActContext()).startActWithData(MyBookingsActivity.class, bn);
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_VIEW_BOOKINGS"));

        generateAlert.showAlertBox();
    }


    public void scheduleDelivery(Intent data) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "ScheduleARide");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("pickUpLocAdd", pickUpLocSelectedFrag != null ? pickUpLocSelectedFrag.getPickUpAddress() : "");
        parameters.put("pickUpLatitude", "" + getPickUpLocation().getLatitude());
        parameters.put("pickUpLongitude", "" + getPickUpLocation().getLongitude());
        parameters.put("destLocAdd", getDestAddress());
        parameters.put("destLatitude", getDestLocLatitude());
        parameters.put("destLongitude", getDestLocLongitude());
        parameters.put("scheduleDate", selectedDateTime);
        parameters.put("iVehicleTypeId", getSelectedCabTypeId());
        parameters.put("TimeZone", selectedDateTimeZone);
        parameters.put("eType", "Deliver");
        parameters.put("vPaymentMode", "" + paymentMode);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    String action = generalFunc.getJsonValue(CommonUtilities.action_str, responseString);
                    if (action.equals("1")) {
                        setDefaultView();
                    }
                    showBookingAlert(generalFunc.retrieveLangLBl("",
                            generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();

    }

    public void deliverNow(Intent data) {

        this.deliveryData = data;

        requestPickUp();
    }

    public void requestPickUp() {

        if (pickUpLocSelectedFrag != null) {
            pickUpLocSelectedFrag.sourceLocSelectTxt.performClick();
        }
        locationMarker.setVisibility(View.VISIBLE);


        setLoadAvailCabTaskValue(true);
        requestNearestCab = new RequestNearestCab(getActContext(), generalFunc);
        requestNearestCab.run();

        String driverIds = getAvailableDriverIds();
        Utils.printLog("driverIdsList", "::" + driverIds);

        JSONObject cabRequestedJson = new JSONObject();
        try {
            cabRequestedJson.put("Message", "CabRequested");
            cabRequestedJson.put("sourceLatitude", "" + getPickUpLocation().getLatitude());
            cabRequestedJson.put("sourceLongitude", "" + getPickUpLocation().getLongitude());
            cabRequestedJson.put("PassengerId", generalFunc.getMemberId());
            cabRequestedJson.put("PName", generalFunc.getJsonValue("vName", userProfileJson) + " "
                    + generalFunc.getJsonValue("vLastName", userProfileJson));
            cabRequestedJson.put("PPicName", generalFunc.getJsonValue("vImgName", userProfileJson));
            cabRequestedJson.put("PFId", generalFunc.getJsonValue("vFbId", userProfileJson));
            cabRequestedJson.put("PRating", generalFunc.getJsonValue("vAvgRating", userProfileJson));
            cabRequestedJson.put("PPhone", generalFunc.getJsonValue("vPhone", userProfileJson));
            cabRequestedJson.put("PPhoneC", generalFunc.getJsonValue("vPhoneCode", userProfileJson));
            cabRequestedJson.put("REQUEST_TYPE", getCurrentCabGeneralType());


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Utils.printLog("Data:Drivers:", "::" + cabRequestedJson.toString());

        if (!generalFunc.getJsonValue("Message", cabRequestedJson.toString()).equals("")) {
            requestNearestCab.setRequestData(driverIds, cabRequestedJson.toString());

            if (DRIVER_REQUEST_METHOD.equals("All")) {
                sendReqToAll(driverIds, cabRequestedJson.toString());
            } else if (DRIVER_REQUEST_METHOD.equals("Distance") || DRIVER_REQUEST_METHOD.equals("Time")) {
                sendReqByDist(driverIds, cabRequestedJson.toString());
            } else {
                sendReqToAll(driverIds, cabRequestedJson.toString());
            }
        }

        if (pickUpLocSelectedFrag != null) {
            pickUpLocSelectedFrag.removeDestMarker();
        }

        unSubscribeCurrentDriverChannels();
    }

    public void sendReqToAll(String driverIds, String cabRequestedJson) {
        sendRequestToDrivers(driverIds, cabRequestedJson);

        if (allCabRequestTask != null) {
            allCabRequestTask.stopRepeatingTask();
            allCabRequestTask = null;
        }
        allCabRequestTask = new UpdateFrequentTask(35 * 1000);
        allCabRequestTask.startRepeatingTask();
        allCabRequestTask.setTaskRunListener(new UpdateFrequentTask.OnTaskRunCalled() {
            @Override
            public void onTaskRun() {
                setRetryReqBtn(true);
                allCabRequestTask.stopRepeatingTask();
            }
        });

    }

    public void sendReqByDist(String driverIds, String cabRequestedJson) {
        if (sendNotificationToDriverByDist == null) {
            sendNotificationToDriverByDist = new SendNotificationsToDriverByDist(driverIds, cabRequestedJson);
        } else {
            sendNotificationToDriverByDist.startRepeatingTask();
        }
    }

    public void setRetryReqBtn(boolean isVisible) {
        if (isVisible == true) {
            if (requestNearestCab != null) {
                requestNearestCab.setVisibilityOfRetryArea(View.VISIBLE);
            }
        } else {
            if (requestNearestCab != null) {
                requestNearestCab.setVisibilityOfRetryArea(View.GONE);
            }
        }
    }

    public void retryReqBtnPressed(String driverIds, String cabRequestedJson) {

        if (DRIVER_REQUEST_METHOD.equals("All")) {
            sendReqToAll(driverIds, cabRequestedJson.toString());
        } else if (DRIVER_REQUEST_METHOD.equals("Distance") || DRIVER_REQUEST_METHOD.equals("Time")) {
            sendReqByDist(driverIds, cabRequestedJson.toString());
        } else {
            sendReqToAll(driverIds, cabRequestedJson.toString());
        }

        setRetryReqBtn(false);
    }

    public class SendNotificationsToDriverByDist implements Runnable {

        String[] list_drivers_ids;
        String cabRequestedJson;
        private Handler mHandler_sendNotification;
        int mInterval = 35 * 1000;
        int current_position_driver_id = 0;

        public SendNotificationsToDriverByDist(String list_drivers_ids, String cabRequestedJson) {
            this.list_drivers_ids = list_drivers_ids.split(",");
            this.cabRequestedJson = cabRequestedJson;
            mHandler_sendNotification = new Handler();


            startRepeatingTask();
        }

        @Override
        public void run() {
            Log.d("Notification task", "called run");

            setRetryReqBtn(false);

            if ((current_position_driver_id + 1) <= list_drivers_ids.length) {

                sendRequestToDrivers(list_drivers_ids[current_position_driver_id], cabRequestedJson);
                current_position_driver_id = current_position_driver_id + 1;

                mHandler_sendNotification.postDelayed(this, mInterval);
            } else {

                setRetryReqBtn(true);

                stopRepeatingTask();
            }

        }


        public void stopRepeatingTask() {
            mHandler_sendNotification.removeCallbacks(this);
            mHandler_sendNotification.removeCallbacksAndMessages(null);
            current_position_driver_id = 0;
        }

        public void incTask() {
            mHandler_sendNotification.removeCallbacks(this);
            mHandler_sendNotification.removeCallbacksAndMessages(null);
            this.run();
        }

        public void startRepeatingTask() {
            stopRepeatingTask();

            this.run();
        }

    }

    public void setLoadAvailCabTaskValue(boolean value) {
        if (loadAvailCabs != null) {
            loadAvailCabs.setTaskKilledValue(value);
        }
    }

    public void setCurrentLoadedDriverList(ArrayList<HashMap<String, String>> currentLoadedDriverList) {
        this.currentLoadedDriverList = currentLoadedDriverList;
    }

    public ArrayList<String> getDriverLocationChannelList() {

        ArrayList<String> channels_update_loc = new ArrayList<>();

        if (currentLoadedDriverList != null) {

            for (int i = 0; i < currentLoadedDriverList.size(); i++) {
                channels_update_loc.add(Utils.pubNub_Update_Loc_Channel_Prefix + "" + (currentLoadedDriverList.get(i).get("driver_id")));

                Utils.printLog("Channels:", "::i:" + i + "::" + channels_update_loc.get(i));
            }

        }
        return channels_update_loc;
    }

    public ArrayList<String> getDriverLocationChannelList(ArrayList<HashMap<String, String>> listData) {

        ArrayList<String> channels_update_loc = new ArrayList<>();

        if (listData != null) {

            for (int i = 0; i < listData.size(); i++) {
                channels_update_loc.add(Utils.pubNub_Update_Loc_Channel_Prefix + "" + (listData.get(i).get("driver_id")));

                Utils.printLog("Channels:", "::i:" + i + "::" + channels_update_loc.get(i));
            }

        }
        return channels_update_loc;
    }

    public String getAvailableDriverIds() {
        String driverIds = "";

        ArrayList<HashMap<String, String>> finalLoadedDriverList = new ArrayList<HashMap<String, String>>();
        finalLoadedDriverList.addAll(currentLoadedDriverList);

        if (DRIVER_REQUEST_METHOD.equals("Distance")) {
            Collections.sort(finalLoadedDriverList, new HashMapComparator("DIST_TO_PICKUP"));
        }

        for (int i = 0; i < finalLoadedDriverList.size(); i++) {
            String iDriverId = finalLoadedDriverList.get(i).get("driver_id");

            driverIds = driverIds.equals("") ? iDriverId : (driverIds + "," + iDriverId);
        }

        return driverIds;
    }

    public void sendRequestToDrivers(String driverIds, String cabRequestedJson) {

        HashMap<String, String> requestCabData = new HashMap<String, String>();
        requestCabData.put("type", "sendRequestToDrivers");
        requestCabData.put("driverIds", driverIds);
        requestCabData.put("message", cabRequestedJson);
        requestCabData.put("userId", generalFunc.getMemberId());
        requestCabData.put("SelectedCarTypeID", "" + getSelectedCabTypeId());
        requestCabData.put("DestLatitude", getDestLocLatitude());
        requestCabData.put("DestLongitude", getDestLocLongitude());
        requestCabData.put("DestAddress", getDestAddress());
        requestCabData.put("vPaymentMode", "" + paymentMode);
        requestCabData.put("PickUpLatitude", "" + getPickUpLocation().getLatitude());
        requestCabData.put("PickUpLongitude", "" + getPickUpLocation().getLongitude());

        requestCabData.put("eType", getCurrentCabGeneralType());

        if (reqPickUpFrag != null) {
            requestCabData.put("PromoCode", reqPickUpFrag.getAppliedPromoCode());
        }
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),requestCabData);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                Utils.printLog("responseString", "::" + responseString);
                if (responseString != null && !responseString.equals("")) {

                    generalFunc.sendHeartBeat();

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == false) {

                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                        if (message.equals("SESSION_OUT")) {
                            closeRequestDialog(false);
                            generalFunc.notifySessionTimeOut();
                            Utils.runGC();
                            return;
                        }
                        if (message.equals("NO_CARS")) {
                            closeRequestDialog(true);
                            buildMessage(generalFunc.retrieveLangLBl("", "LBL_NO_CAR_AVAIL_TXT"), generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), false);
                        } else if (message.equals(CommonUtilities.GCM_FAILED_KEY) || message.equals(CommonUtilities.APNS_FAILED_KEY)) {
                            releaseScheduleNotificationTask();
                            generalFunc.restartApp();
                        } else {
                            closeRequestDialog(false);
                            buildMessage(generalFunc.retrieveLangLBl("", "LBL_REQUEST_FAILED_PROCESS"), generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), true);
                        }

                    }
                } else {
                    closeRequestDialog(true);
                    buildMessage(generalFunc.retrieveLangLBl("", "LBL_REQUEST_FAILED_PROCESS"), generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), false);
                }
            }
        });
        exeWebServer.execute();

        generalFunc.sendHeartBeat();
    }

    public void closeRequestDialog(boolean isSetDefault) {
        if (requestNearestCab != null) {
            requestNearestCab.dismissDialog();
        }

        releaseScheduleNotificationTask();

        if (isSetDefault == true) {
            setDefaultView();
        }

    }

    public void releaseScheduleNotificationTask() {
        if (allCabRequestTask != null) {
            allCabRequestTask.stopRepeatingTask();
            allCabRequestTask = null;
        }

        if (sendNotificationToDriverByDist != null) {
            sendNotificationToDriverByDist.stopRepeatingTask();
            sendNotificationToDriverByDist = null;
        }
    }

    public DriverDetailFragment getDriverDetailFragment() {
        return driverDetailFrag;
    }

    public void buildMessage(String message, String positiveBtn, final boolean isRestart) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();
                if (isRestart == true) {
                    generalFunc.restartApp();
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        generateAlert.showAlertBox();
    }

    public void buildTripEndMessage(String message, String positiveBtn) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();

                new StartActProcess(getActContext()).startAct(RatingActivity.class);

                stopReceivingPrivateMsg();
                ActivityCompat.finishAffinity(MainActivity.this);
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        generateAlert.showAlertBox();
    }


    public void onGcmMessageArrived(String message) {

        Utils.printLog("GCM", "::" + message);
        String driverMsg = generalFunc.getJsonValue("Message", message);

        if (driverMsg.equals("CabRequestAccepted")) {
            closeRequestDialog(false);
            configureDeliveryView(true);
            isDriverAssigned = true;

            assignedDriverId = generalFunc.getJsonValue("iDriverId", message);
            assignedTripId = generalFunc.getJsonValue("iTripId", message);

            if (generalFunc.isJSONkeyAvail("iCabBookingId", message) == true && !generalFunc.getJsonValue("iCabBookingId", message).trim().equals("")) {
                generalFunc.restartApp();
            } else {
                configureAssignedDriver(false);
            }

        } else if (driverMsg.equals("TripEnd")) {

                buildTripEndMessage(generalFunc.retrieveLangLBl("", "LBL_END_TRIP_DIALOG_TXT"),
                        generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));


            if (driverAssignedHeaderFrag != null) {
                driverAssignedHeaderFrag.setTaskKilledValue(true);
            }
        } else if (driverMsg.equals("TripStarted")) {
            buildMessage(generalFunc.retrieveLangLBl("", "LBL_START_TRIP_DIALOG_TXT"),
                    generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), false);

            if (driverAssignedHeaderFrag != null) {
                driverAssignedHeaderFrag.setTripStartValue(true);
                driverAssignedHeaderFrag.removeSourceTimeMarker();
            }

            if (driverDetailFrag != null) {
                driverDetailFrag.configTripStartView(true);
            }

        } else if (driverMsg.equals("DestinationAdded")) {

            String destAddedByDriver = generalFunc.retrieveLangLBl("Destination is added by driver.", "LBL_DEST_ADD_BY_DRIVER");
            generalFunc.generateNotification(MainActivity.this, destAddedByDriver);
            buildMessage(destAddedByDriver, generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), false);

            String destLatitude = generalFunc.getJsonValue("DLatitude", message);
            String destLongitude = generalFunc.getJsonValue("DLongitude", message);
            String destAddress = generalFunc.getJsonValue("DAddress", message);

            setDestinationPoint(destLatitude, destLongitude, destAddress, true);
            if (driverAssignedHeaderFrag != null) {
                driverAssignedHeaderFrag.setDestinationAddress();
                driverAssignedHeaderFrag.configDestinationView();
            }
        } else if (driverMsg.equals("TripCancelledByDriver")) {
            String reason = generalFunc.getJsonValue("Reason", message);
            String isTripStarted = generalFunc.getJsonValue("isTripStarted", message);

            if (isTripStarted.equals("true")) {
                generalFunc.generateNotification(MainActivity.this, generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_DRIVER") + " " + reason);

                buildTripEndMessage(generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_DRIVER") + " " + reason,
                        generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
            } else {
                generalFunc.generateNotification(MainActivity.this, generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_DRIVER") + " " + reason);

                buildMessage(generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_DRIVER") + " " + reason,
                        generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), true);
            }
            if (driverAssignedHeaderFrag != null) {
                driverAssignedHeaderFrag.setTaskKilledValue(true);
            }
        }
    }

    public DriverAssignedHeaderFragment getDriverAssignedHeaderFrag() {
        return driverAssignedHeaderFrag;
    }

    public void unSubscribeCurrentDriverChannels() {
        if (configPubNub != null && currentLoadedDriverList != null) {
            configPubNub.unSubscribeToChannels(getDriverLocationChannelList());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (loadAvailCabs != null) {
            loadAvailCabs.onPauseCalled();
        }

        if (driverAssignedHeaderFrag != null) {
            driverAssignedHeaderFrag.onPauseCalled();
        }

        unSubscribeCurrentDriverChannels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loadAvailCabs != null) {
            loadAvailCabs.onResumeCalled();
        }
        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);

        registerGcmMsgReceiver();


        if (driverAssignedHeaderFrag != null) {
            driverAssignedHeaderFrag.onResumeCalled();
        }

        if (configPubNub != null && currentLoadedDriverList != null) {
            configPubNub.subscribeToChannels(getDriverLocationChannelList());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unRegisterGcmReceiver();
        stopReceivingPrivateMsg();

        releaseScheduleNotificationTask();
    }

    public void registerGcmMsgReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonUtilities.driver_message_arrived_intent_action);

        registerReceiver(gcmMessageBroadCastReceiver, filter);
    }

    public void stopReceivingPrivateMsg() {
        if (configPubNub != null) {
            configPubNub.unSubscribeToPrivateChannel();
            configPubNub.releaseInstances();
            configPubNub = null;
            Utils.runGC();
        }
    }

    public void unRegisterGcmReceiver() {
        try {
            unregisterReceiver(gcmMessageBroadCastReceiver);
        } catch (Exception e) {

        }
    }

    public void setDriverImgView(SelectableRoundedImageView driverImgView) {
        this.driverImgView = driverImgView;
    }

    public Bitmap getDriverImg() {

        try {
            if (driverImgView != null) {
                driverImgView.buildDrawingCache();
                Bitmap driverBitmap = driverImgView.getDrawingCache();

                if (driverBitmap != null) {
                    return driverBitmap;
                } else {
                    return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_no_pic_user);
                }
            }

            return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_no_pic_user);
        } catch (Exception e) {
            return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_no_pic_user);
        }
    }

    public Bitmap getUserImg() {
        try {
            ((SelectableRoundedImageView) findViewById(R.id.userImgView)).buildDrawingCache();
            Bitmap userBitmap = ((SelectableRoundedImageView) findViewById(R.id.userImgView)).getDrawingCache();
            if (userBitmap != null) {
                return userBitmap;
            } else {
                return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_no_pic_user);
            }
        } catch (Exception e) {
            return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_no_pic_user);
        }
    }

    public void pubNubStatus(String status) {

    }

    public void pubNubMsgArrived(final String message) {

        Utils.printLog("message::OUT UI thread", "YES::" + generalFunc.getJsonValue("MsgType", message));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.printLog("message::IN UI thread", "YES");

                String msgType = generalFunc.getJsonValue("MsgType", message);

                Utils.printLog("msgType::IN UI thread", "msgType::" + msgType);
                if (msgType.equals("LocationUpdate")) {
                    if (loadAvailCabs == null) {
                        return;
                    }

                    String iDriverId = generalFunc.getJsonValue("iDriverId", message);
                    String vLatitude = generalFunc.getJsonValue("vLatitude", message);
                    String vLongitude = generalFunc.getJsonValue("vLongitude", message);

                    Marker driverMarker = getDriverMarkerOnPubNubMsg(iDriverId, false);

                    LatLng driverLocation_update = new LatLng(generalFunc.parseDoubleValue(0.0, vLatitude),
                            generalFunc.parseDoubleValue(0.0, vLongitude));
                    Utils.animateMarker(driverMarker, driverLocation_update, false, gMap);

                    Utils.printLog("message::iDriverId", "MainAct:iDriverId" + iDriverId);

                } else if (msgType.equals("TripRequestCancel")) {
                    if (DRIVER_REQUEST_METHOD.equals("Distance") || DRIVER_REQUEST_METHOD.equals("Time")) {
                        if (sendNotificationToDriverByDist != null) {
                            sendNotificationToDriverByDist.incTask();
                        }
                    }
                } else if (msgType.equals("LocationUpdateOnTrip")) {
                    if (driverAssignedHeaderFrag != null) {
                        driverAssignedHeaderFrag.updateDriverLocation(message);
                    }
                } else if (msgType.equals("DriverArrived")) {
                    driverAssignedHeaderFrag.isDriverArrived = true;
                    generalFunc.generateNotification(getActContext(), msgType);
                    if (driverAssignedHeaderFrag != null) {
                        driverAssignedHeaderFrag.setDriverStatusTitle(generalFunc.retrieveLangLBl("", "LBL_DRIVER_ARRIVED_TXT"));
                    }
                } else {
                    onGcmMessageArrived(message);
                }

            }
        });

        Utils.printLog("message::", "MainAct:" + message);
    }

    public Marker getDriverMarkerOnPubNubMsg(String iDriverId, boolean isRemoveFromList) {
        ArrayList<Marker> currentDriverMarkerList = loadAvailCabs.getDriverMarkerList();

        for (int i = 0; i < currentDriverMarkerList.size(); i++) {
            Marker marker = currentDriverMarkerList.get(i);

            String driver_id = marker.getTitle().replace("DriverId", "");

            if (driver_id.equals(iDriverId)) {

                if (isRemoveFromList) {
                    loadAvailCabs.getDriverMarkerList().remove(i);
                }

                return marker;
            }

        }

        return null;
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
            return;
        }

        if (pickUpLocSelectedFrag != null && isDriverAssigned == false) {
            setDefaultView();
            return;
        }

        super.onBackPressed();
    }

    public Context getActContext() {
        return MainActivity.this;
    }
    public Activity getActContext1() {
        return MainActivity.this;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, 1, 0, "" + generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        menu.add(0, 2, 0, "" + generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == 1) {

            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + driverDetailFrag.getDriverPhone()));
                startActivity(callIntent);
            } catch (Exception e) {
                // TODO: handle exception
            }

        } else if (item.getItemId() == 2) {

            try {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", "" + driverDetailFrag.getDriverPhone());
                startActivity(smsIntent);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.MY_PROFILE_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String userProfileJson = data.getStringExtra("UserProfileJson");
            this.userProfileJson = userProfileJson;
            setUserInfo();
        } else if (requestCode == Utils.CARD_PAYMENT_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String userProfileJson = data.getStringExtra("UserProfileJson");
            this.userProfileJson = userProfileJson;
        } else if (requestCode == Utils.DELIVERY_DETAILS_REQ_CODE && resultCode == RESULT_OK && data != null) {
            if (!getCabReqType().equals(Utils.CabReqType_Later)) {
                deliverNow(data);
            } else {
                scheduleDelivery(data);
            }
        }
    }
}
