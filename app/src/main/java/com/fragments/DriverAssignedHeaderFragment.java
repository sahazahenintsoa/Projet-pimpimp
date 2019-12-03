package com.fragments;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.general.files.CancelNotificationReceiver;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.R;
import com.pimpimp.passenger.SearchPickupLocationActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverAssignedHeaderFragment extends Fragment implements UpdateFrequentTask.OnTaskRunCalled {

    MainActivity mainAct;
    GeneralFunctions generalFunc;
    String userProfileJson;
    View view;

    ImageView backImgView;
    MTextView titleTxt;

    DriverAssignedHeaderFragment driverAssignedHFrag;

    GoogleMap gMap;

    boolean isGooglemapSet = false;
    public boolean isDriverArrived = false;

    UpdateFrequentTask updateDriverLocTask;
    UpdateFrequentTask updateDestMarkerTask;

    int DRIVER_LOC_FETCH_TIME_INTERVAL;
    int DESTINATION_UPDATE_TIME_INTERVAL;

    boolean isTaskKilled = false;

    LatLng driverLocation;
    LatLng pickUpLocation;

    String iDriverId = "";

    Marker driverMarker;
    Marker sourceMarker;
    Marker destMarker;
    Marker time_driver_marker;
    Marker time_destination_marker;

    int notificationCount = 0;

    HashMap<String, String> driverData;

    long currentNotificationTime = 0;
    NotificationManager notificationmanager;

    boolean isTripStart = false;

    Polyline route_polyLine;

    MTextView pickUpLocTxt;
    MTextView sourceLocSelectTxt;
    MTextView destLocSelectTxt;
    MTextView destLocTxt;

    View area_source;
    View area2;

    Marker destinationPointMarker_temp;

    String eType;
    int DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = 3;

    String driverAppVersion = "1";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_driver_assigned_header, container, false);

        mainAct = (MainActivity) getActivity();
        generalFunc = mainAct.generalFunc;
        userProfileJson = mainAct.userProfileJson;
        driverAssignedHFrag = mainAct.getDriverAssignedHeaderFrag();
        gMap = mainAct.getMap();

        backImgView = (ImageView) view.findViewById(R.id.backImgView);
        titleTxt = (MTextView) view.findViewById(R.id.titleTxt);

        pickUpLocTxt = (MTextView) view.findViewById(R.id.pickUpLocTxt);
        sourceLocSelectTxt = (MTextView) view.findViewById(R.id.sourceLocSelectTxt);
        destLocSelectTxt = (MTextView) view.findViewById(R.id.destLocSelectTxt);
        destLocTxt = (MTextView) view.findViewById(R.id.destLocTxt);
        area_source = view.findViewById(R.id.area_source);
        area2 = view.findViewById(R.id.area2);

        backImgView.setImageResource(R.mipmap.ic_drawer_menu);

        backImgView.setOnClickListener(new setOnClickList());

        sourceLocSelectTxt.setOnClickListener(new setOnClickList());
        destLocSelectTxt.setOnClickListener(new setOnClickList());

        setDriverStatusTitle(generalFunc.retrieveLangLBl("", "LBL_EN_ROUTE_TXT"));
        setData();

        if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip")) {
            mainAct.isFirstLocation=false;
            setTripStartValue(true);
            removeSourceTimeMarker();

        }

        DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = generalFunc.parseIntegerValue(3, generalFunc.getJsonValue("DRIVER_ARRIVED_MIN_TIME_PER_MINUTE", userProfileJson));

        new CreateRoundedView(Color.parseColor("#CCe0e0e0"), Utils.dipToPixels(getActContext(), 5), Utils.dipToPixels(getActContext(), 1),
                Color.parseColor("#d2d2d2"), destLocSelectTxt);
        new CreateRoundedView(Color.parseColor("#CCe0e0e0"), Utils.dipToPixels(getActContext(), 5), Utils.dipToPixels(getActContext(), 1),
                Color.parseColor("#d2d2d2"), sourceLocSelectTxt);

        return view;
    }


    public void setData() {
        HashMap<String, String> driverData = (HashMap<String, String>) getArguments().getSerializable("TripData");
        this.driverData = driverData;
        iDriverId = driverData.get("iDriverId");
        driverAppVersion = driverData.get("DriverAppVersion");
        pickUpLocTxt.setText(driverData.get("PickUpAddress"));
        sourceLocSelectTxt.setText(driverData.get("PickUpAddress"));

        driverLocation = new LatLng(generalFunc.parseDoubleValue(0.0, driverData.get("DriverLatitude")),
                generalFunc.parseDoubleValue(0.0, driverData.get("DriverLongitude")));
        pickUpLocation = new LatLng(generalFunc.parseDoubleValue(0.0, driverData.get("PickUpLatitude")),
                generalFunc.parseDoubleValue(0.0, driverData.get("PickUpLongitude")));

        if (mainAct.getDestinationStatus()) {
            destLocTxt.setText(mainAct.getDestAddress());
            destLocSelectTxt.setText(mainAct.getDestAddress());
        } else {
            destLocTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
            destLocSelectTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
            destLocTxt.setOnClickListener(new setOnClickList());
        }

        if (gMap != null && isGooglemapSet == false) {
            isGooglemapSet = true;

            gMap.clear();
            configDriverLoc();
        }

        configDestinationView();
        eType = driverData.get("eType");
    }

    public void setGoogleMap(GoogleMap map) {
        this.gMap = map;
        if (isGooglemapSet == false) {
            gMap.clear();

            configDriverLoc();
        }

        configDestinationView();
    }

    public void configDriverLoc() {
        if (driverLocation == null) {
            setData();
            return;
        }
        MarkerOptions markerOptions_driver = new MarkerOptions();
        markerOptions_driver.position(driverLocation);

        int iconId = R.mipmap.car_driver;

        if (driverData.get("carTypeName").equalsIgnoreCase("Bike") || driverData.get("eIconType").equalsIgnoreCase("Bike")) {
            iconId = R.mipmap.car_driver_1;
        }

        markerOptions_driver.icon(BitmapDescriptorFactory.fromResource(iconId)).anchor(0.5f,
                0.5f).flat(true);

        driverMarker = gMap.addMarker(markerOptions_driver);

        MarkerOptions markerOptions_sourceLocation = new MarkerOptions();
        markerOptions_sourceLocation.position(pickUpLocation);
        markerOptions_sourceLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker)).anchor(0.5f,
                0.5f);
        sourceMarker = gMap.addMarker(markerOptions_sourceLocation);

        if (mainAct != null && (mainAct.isPubNubEnabled() == false || driverAppVersion.equals("1"))) {
            scheduleDriverLocUpdate();
        } else if (mainAct != null && mainAct.isPubNubEnabled()) {
            subscribeToDriverLocChannel();
        }

        notifyDriverArrivedTime("" + driverLocation.latitude, "" + driverLocation.longitude);
    }

    public void subscribeToDriverLocChannel() {

        if (mainAct != null && mainAct.configPubNub != null) {
            ArrayList<String> channelName = new ArrayList<>();
            channelName.add(Utils.pubNub_Update_Loc_Channel_Prefix + iDriverId);
            mainAct.configPubNub.subscribeToChannels(channelName);
        }

    }

    public void unSubscribeToDriverLocChannel() {
        if (mainAct != null && mainAct.configPubNub != null) {
            ArrayList<String> channelName = new ArrayList<>();
            channelName.add(Utils.pubNub_Update_Loc_Channel_Prefix + iDriverId);
            mainAct.configPubNub.unSubscribeToChannels(channelName);
        }
    }

    public void scheduleDriverLocUpdate() {

        DRIVER_LOC_FETCH_TIME_INTERVAL = (generalFunc.parseIntegerValue(1, generalFunc.getJsonValue("DRIVER_LOC_FETCH_TIME_INTERVAL", userProfileJson))) * 1 * 1000;

        if (updateDriverLocTask == null) {
            updateDriverLocTask = new UpdateFrequentTask(DRIVER_LOC_FETCH_TIME_INTERVAL);
            updateDriverLocTask.setTaskRunListener(this);
            onResumeCalled();
        }


    }

    public void setTaskKilledValue(boolean isTaskKilled) {
        this.isTaskKilled = isTaskKilled;

        if (isTaskKilled == true) {
            onPauseCalled();
        }
    }

    public void removeSourceTimeMarker() {

        if (time_driver_marker != null) {
            time_driver_marker.remove();
        }

    }

    public void setTripStartValue(boolean isTripStart) {
        this.isTripStart = isTripStart;

        if (isTripStart == true) {
            setDriverStatusTitle(generalFunc.retrieveLangLBl("", "LBL_EN_ROUTE_TXT"));
            configDestinationView();
        }

    }

    public void setDriverStatusTitle(String title) {
        ((MTextView) view.findViewById(R.id.titleTxt)).setText(title);
    }

    @Override
    public void onTaskRun() {
        updateDriverLocations();
    }

    public LatLng getDriverLoc()
    {
        return driverLocation;
    }

    public void updateDriverLocations() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDriverLocations");
        parameters.put("iDriverId", iDriverId);
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        String vLatitude = generalFunc.getJsonValue("vLatitude", responseString);
                        String vLongitude = generalFunc.getJsonValue("vLongitude", responseString);
                        String vTripStatus = generalFunc.getJsonValue("vTripStatus", responseString);

                        if (vTripStatus.equals("Arrived")) {
                            setDriverStatusTitle(generalFunc.retrieveLangLBl("", "LBL_DRIVER_ARRIVED_TXT"));
                            isDriverArrived = true;
                        }

                        LatLng driverLocation_update = new LatLng(generalFunc.parseDoubleValue(0.0, vLatitude),
                                generalFunc.parseDoubleValue(0.0, vLongitude));

                        Utils.animateMarker(driverMarker, driverLocation_update, false, gMap);
                        driverLocation = driverLocation_update;

                        if (vTripStatus.equals("Active")) {
                            updateDriverArrivedTime();
                        }
                    }
                } else {
                }
            }
        });
        exeWebServer.execute();
    }

    public void updateDriverLocation(String message) {
        if (message == null) {
            return;
        }
        String iDriverId = generalFunc.getJsonValue("iDriverId", message);
        String vLatitude = generalFunc.getJsonValue("vLatitude", message);
        String vLongitude = generalFunc.getJsonValue("vLongitude", message);

        LatLng driverLocation_update = new LatLng(generalFunc.parseDoubleValue(0.0, vLatitude),
                generalFunc.parseDoubleValue(0.0, vLongitude));

        Utils.animateMarker(driverMarker, driverLocation_update, false, gMap);
        driverLocation = driverLocation_update;

        notifyDriverArrivedTime(vLatitude, vLongitude);

    }

    public void notifyDriverArrivedTime(String vLatitude, String vLongitude) {
        if (isTripStart == false) {
//            updateDriverArrivedTime();
            double distance = generalFunc.CalculationByLocation(pickUpLocation.latitude, pickUpLocation.longitude,
                    generalFunc.parseDoubleValue(0.0, vLatitude), generalFunc.parseDoubleValue(0.0, vLongitude));
            int totalTimeInMinParKM = ((int) (distance * DRIVER_ARRIVED_MIN_TIME_PER_MINUTE));

            if (totalTimeInMinParKM < 1) {
                totalTimeInMinParKM = 1;
            }

            if (totalTimeInMinParKM < 3) {
                if (isDriverArrived == false) {
                    setDriverStatusTitle(generalFunc.retrieveLangLBl("", "LBL_ARRIVING_TXT"));
                }
            }

            if ((totalTimeInMinParKM == 1 || totalTimeInMinParKM == 3) && notificationCount < 3) {

                if (currentNotificationTime < 1 || (System.currentTimeMillis() - currentNotificationTime) > 1 * 60 * 1000) {
                    buildCustomNotification(totalTimeInMinParKM + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"),
                            generalFunc.retrieveLangLBl("", "LBL_DRIVER_ARRIVING_TXT"));
                }
            }

            Bitmap marker_time_ic = generalFunc.writeTextOnDrawable(getActContext(), R.drawable.driver_time_marker,
                    totalTimeInMinParKM + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));


            if (time_driver_marker != null) {
                time_driver_marker.remove();
            }
            time_driver_marker = gMap.addMarker(
                    new MarkerOptions().position(pickUpLocation)
                            .icon(BitmapDescriptorFactory.fromBitmap(marker_time_ic)));

        } else {
            if (time_driver_marker != null) {
                time_driver_marker.remove();
                time_driver_marker = null;
            }
        }
    }


    public void updateDriverArrivedTime() {
        if (mainAct == null) {
            return;
        }
        String originLoc = pickUpLocation.latitude + "," + pickUpLocation.longitude;
        String destLoc = driverLocation.latitude + "," + driverLocation.longitude;
        String serverKey = mainAct.getResources().getString(R.string.google_api_get_address_from_location_serverApi);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey;

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),url, true);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunc.getJsonValue("status", responseString);

                    if (status.equals("OK")) {

                        JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                        if (obj_routes != null && obj_routes.length() > 0) {

                            int duration = (int) Math.round((generalFunc.parseDoubleValue(0.0,
                                    generalFunc.getJsonValue("value", generalFunc.getJsonValue("duration",
                                            generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0).toString())))) / 60);


                            if (duration < 3) {
                                if (isDriverArrived == false) {
                                    setDriverStatusTitle(generalFunc.retrieveLangLBl("", "LBL_ARRIVING_TXT"));
                                }
                            }

                            if (duration < 1) {
                                duration = 1;
                            }

                            if ((duration == 1 || duration == 3) && notificationCount < 3) {

                                currentNotificationTime = System.currentTimeMillis();

                                if ((System.currentTimeMillis() - currentNotificationTime) < 60000) {
                                    buildCustomNotification(duration + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"),
                                            generalFunc.retrieveLangLBl("", "LBL_DRIVER_ARRIVING_TXT"));
                                }
                            }

                            Bitmap marker_time_ic = generalFunc.writeTextOnDrawable(getActContext(), R.drawable.driver_time_marker,
                                    duration + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));

                            if (isTripStart == false) {

                                if (time_driver_marker != null) {
                                    time_driver_marker.remove();
                                }
                                time_driver_marker = gMap.addMarker(
                                        new MarkerOptions().position(pickUpLocation)
                                                .icon(BitmapDescriptorFactory.fromBitmap(marker_time_ic)));
                            }

                        }

                    }

                }
            }
        });
        exeWebServer.execute();
    }


    public void buildCustomNotification(String time, String status_str) {
        currentNotificationTime = System.currentTimeMillis();

        notificationCount = notificationCount + 1;

        RemoteViews remoteViews = new RemoteViews(getActContext().getPackageName(),
                R.layout.notification_layout);

        Intent intent = new Intent(getActContext(), CancelNotificationReceiver.class);
        intent.putExtra("notificationId", 1);

        PendingIntent btPendingIntent = PendingIntent.getBroadcast(getActContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActContext());

        remoteViews.setImageViewBitmap(R.id.user_img, createRoundedBitmap(mainAct.getUserImg()));
        remoteViews.setImageViewBitmap(R.id.driver_img, createRoundedBitmap(mainAct.getDriverImg()));

        remoteViews.setTextViewText(R.id.txt_user_name, "" + generalFunc.getJsonValue("vName", userProfileJson));
        remoteViews.setTextViewText(R.id.txt_driver_name, "" + driverData.get("DriverName"));
        remoteViews.setTextViewText(R.id.car_number_txt, "" + driverData.get("DriverCarPlateNum"));
        remoteViews.setTextViewText(R.id.status_txt, "" + status_str);
        remoteViews.setTextViewText(R.id.time_txt, "" + time);

        Notification foregroundNote = builder.setSmallIcon(R.mipmap.ic_launcher)
                // Set Ticker Message
                .setTicker("Hello")
                        // Set Title
                .setContentTitle("Hello")
                        // Set Text
                .setContentText("Hello").setPriority(2)
                        // Dismiss Notification
                .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true).setContent(remoteViews)
                        // Set PendingIntent into Notification
                .setContentIntent(btPendingIntent).build();

        foregroundNote.priority = Notification.PRIORITY_HIGH;

        foregroundNote.bigContentView = remoteViews;

        if (notificationmanager != null) {
            notificationmanager = null;
        }

        notificationmanager = (NotificationManager) mainAct.getSystemService(mainAct.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(1, foregroundNote);
    }

    public Bitmap createRoundedBitmap(Bitmap bitmap) {

        int targetWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics());

        int targetHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics());

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        BitmapShader shader;
        shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);
        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2, ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth), ((float) targetHeight)) / 2), Path.Direction.CCW);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        paint.setStyle(Paint.Style.STROKE);
        canvas.clipPath(path);
        Bitmap sourceBitmap = bitmap;
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);

        return targetBitmap;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == backImgView.getId()) {
                mainAct.checkDrawerState();
            } else if (view.getId() == R.id.destLocTxt) {
                Bundle bn = new Bundle();
                bn.putString("isPickUpLoc", "false");
                if (mainAct.getPickUpLocation() != null) {
                    bn.putString("PickUpLatitude", "" + mainAct.getPickUpLocation().getLatitude());
                    bn.putString("PickUpLongitude", "" + mainAct.getPickUpLocation().getLongitude());
                }
                new StartActProcess(mainAct.getActContext()).startActForResult(driverAssignedHFrag, SearchPickupLocationActivity.class,
                        Utils.SEARCH_DEST_LOC_REQ_CODE, bn);
            } else if (view.getId() == R.id.sourceLocSelectTxt) {

                area_source.setVisibility(View.VISIBLE);
                area2.setVisibility(View.GONE);


                if (mainAct.getDestinationStatus() == true) {
                    destLocSelectTxt.setText(mainAct.getDestAddress());
                }

                mainAct.animateToLocation(pickUpLocation.latitude, pickUpLocation.longitude);

                if (destinationPointMarker_temp != null) {
                    destinationPointMarker_temp.remove();
                    destinationPointMarker_temp = null;
                }


            } else if (view.getId() == R.id.destLocSelectTxt) {
                area2.setVisibility(View.VISIBLE);
                area_source.setVisibility(View.GONE);


                if (mainAct.getDestinationStatus() == false) {


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            destLocTxt.performClick();
                        }
                    }, 250);

                } else {

                    if (isTripStart == false) {
                        destinationPointMarker_temp = gMap.addMarker(
                                new MarkerOptions().position(new LatLng(generalFunc.parseDoubleValue(0.0, mainAct.getDestLocLatitude()),
                                        generalFunc.parseDoubleValue(0.0, mainAct.getDestLocLongitude())))
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)));
                    }

                    mainAct.animateToLocation(generalFunc.parseDoubleValue(0.0, mainAct.getDestLocLatitude()),
                            generalFunc.parseDoubleValue(0.0, mainAct.getDestLocLongitude()));
                }

            }
        }
    }

    public void addDestination(final String latitude, final String longitude, final String address) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "addDestination");
        parameters.put("UserId", generalFunc.getMemberId());
        parameters.put("Latitude", latitude);
        parameters.put("Longitude", longitude);
        parameters.put("Address", address);
        parameters.put("iDriverId", iDriverId);
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(mainAct.getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        mainAct.setDestinationPoint(latitude, longitude, address, true);
                        setDestinationAddress();

                        if (isTripStart == true) {
                            configDestinationView();
                        }

                    } else {
                        String msg_str = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                        if (msg_str.equals(CommonUtilities.GCM_FAILED_KEY) || msg_str.equals(CommonUtilities.APNS_FAILED_KEY)) {
                            generalFunc.restartApp();
                        } else {
                            generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                                    generalFunc.retrieveLangLBl("", msg_str));
                        }

                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void setDestinationAddress() {
        destLocTxt.setText(mainAct.getDestAddress());
        destLocSelectTxt.setText(mainAct.getDestAddress());
        destLocTxt.setOnClickListener(null);
    }

    public void configDestinationView() {
        if (mainAct == null || mainAct.getDestinationStatus() == false || isTripStart == false) {
            return;
        }
        final String destLocLatitude = mainAct.getDestLocLatitude();
        final String destLocLongitude = mainAct.getDestLocLongitude();

        DESTINATION_UPDATE_TIME_INTERVAL = (generalFunc.parseIntegerValue(30, generalFunc.getJsonValue("DESTINATION_UPDATE_TIME_INTERVAL", userProfileJson))) * 60 * 1000;

        setDestinationAddress();

        if (updateDestMarkerTask != null) {
            updateDestMarkerTask.stopRepeatingTask();
            updateDestMarkerTask = null;
        }

        if (updateDestMarkerTask == null) {
            updateDestMarkerTask = new UpdateFrequentTask(DESTINATION_UPDATE_TIME_INTERVAL);
            updateDestMarkerTask.setTaskRunListener(new UpdateFrequentTask.OnTaskRunCalled() {
                @Override
                public void onTaskRun() {
                    if (gMap != null) {

                        if (destMarker == null) {
                            MarkerOptions markerOptions_destLocation = new MarkerOptions();
                            markerOptions_destLocation.position(new LatLng(generalFunc.parseDoubleValue(0.0, destLocLatitude),
                                    generalFunc.parseDoubleValue(0.0, destLocLongitude)));
                            markerOptions_destLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f,
                                    0.5f);
                            destMarker = gMap.addMarker(markerOptions_destLocation);
                        }

                        scheduleDestRoute(destLocLatitude, destLocLongitude);
                    }
                }
            });
            onResumeCalled();
        }
    }

    public void scheduleDestRoute(final String destLocLatitude, final String destLocLongitude) {

        String originLoc = "";

        if (driverLocation == null) {
            originLoc = pickUpLocation.latitude + "," + pickUpLocation.longitude;
        } else {
            originLoc = driverLocation.latitude + "," + driverLocation.longitude;
        }

        String destLoc = destLocLatitude + "," + destLocLongitude;
        String serverKey = getResources().getString(R.string.google_api_get_address_from_location_serverApi);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey;

        Utils.printLog("url destination", "url:" + url);
        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),url, true);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunc.getJsonValue("status", responseString);

                    if (status.equals("OK")) {

                        PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(responseString, Utils.dipToPixels(getActContext(), 4), Color.BLUE);

                        if (lineOptions != null) {
                            if (route_polyLine != null) {
                                route_polyLine.remove();
                            }
                            route_polyLine = gMap.addPolyline(lineOptions);
                        }

                        JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                        if (obj_routes != null && obj_routes.length() > 0) {
                            int duration = (int) Math.round((generalFunc.parseDoubleValue(0.0,
                                    generalFunc.getJsonValue("value", generalFunc.getJsonValue("duration",
                                            generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0).toString())))) / 60);

                            if (duration < 1) {
                                duration = 1;
                            }

                            Bitmap marker_time_ic = generalFunc.writeTextOnDrawable(getActContext(), R.drawable.driver_time_marker,
                                    duration + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));

                            if (isTripStart == true) {
                                if (time_destination_marker != null) {
                                    time_destination_marker.remove();
                                }
                                time_destination_marker = gMap.addMarker(
                                        new MarkerOptions().position(new LatLng(generalFunc.parseDoubleValue(0.0, destLocLatitude),
                                                generalFunc.parseDoubleValue(0.0, destLocLongitude)))
                                                .icon(BitmapDescriptorFactory.fromBitmap(marker_time_ic)));
                            }
                        }
                    } else {
                    }

                } else {
                }
            }
        });
        exeWebServer.execute();
    }

    public void onPauseCalled() {
        if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip"))
        {
            mainAct.isFirstLocation=false;
        }
        else
        {
            mainAct.isFirstLocation=true;
        }
        if (updateDriverLocTask != null) {
            updateDriverLocTask.stopRepeatingTask();
        }
        if (updateDestMarkerTask != null) {
            updateDestMarkerTask.stopRepeatingTask();
        }

        unSubscribeToDriverLocChannel();
    }

    public void onResumeCalled() {
        if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip"))
        {
            mainAct.isFirstLocation=false;
        }
        else
        {
            mainAct.isFirstLocation=true;
        }
        if (updateDriverLocTask != null && isTaskKilled == false) {
            updateDriverLocTask.startRepeatingTask();
        }

        if (updateDestMarkerTask != null && isTaskKilled == false) {
            updateDestMarkerTask.startRepeatingTask();
        }

        subscribeToDriverLocChannel();
    }

    public Context getActContext() {
        return mainAct.getActContext();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SEARCH_DEST_LOC_REQ_CODE && resultCode == mainAct.RESULT_OK && data != null) {
            addDestination(data.getStringExtra("Latitude"), data.getStringExtra("Longitude"), data.getStringExtra("Address"));
        }
    }
}
