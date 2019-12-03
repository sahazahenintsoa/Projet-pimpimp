package com.general.files;

import android.content.Context;
import android.location.Location;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.R;
import com.utils.CommonUtilities;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Admin on 05-07-2016.
 */
public class LoadAvailableCab implements UpdateFrequentTask.OnTaskRunCalled {
    Context mContext;
    GeneralFunctions generalFunc;
    String selectedCabTypeId = "";
    Location pickUpLocation;
    GoogleMap gMapView;
    View parentView;
    ExecuteWebServerUrl currentWebTask;

    MainActivity mainAct;

    ArrayList<HashMap<String, String>> listOfDrivers;
    ArrayList<Marker> driverMarkerList;

    String userProfileJson;

    double RESTRICTION_KM_NEAREST_TAXI = 4;
    int ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL = 1 * 60 * 1000;
    int DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = 3;

    UpdateFrequentTask updateDriverListTask;

    boolean isTaskKilled = false;

    public LoadAvailableCab(Context mContext, GeneralFunctions generalFunc, String selectedCabTypeId, Location pickUpLocation, GoogleMap gMapView, String userProfileJson) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;
        this.selectedCabTypeId = selectedCabTypeId;
        this.pickUpLocation = pickUpLocation;
        this.gMapView = gMapView;
        this.userProfileJson = userProfileJson;

        if (mContext instanceof MainActivity) {
            mainAct = (MainActivity) mContext;
            parentView = generalFunc.getCurrentView(mainAct);
        }

        listOfDrivers = new ArrayList<>();
        driverMarkerList = new ArrayList<>();

        RESTRICTION_KM_NEAREST_TAXI = generalFunc.parseFloatValue(4, generalFunc.getJsonValue("RESTRICTION_KM_NEAREST_TAXI", userProfileJson));
        ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL = (generalFunc.parseIntegerValue(1, generalFunc.getJsonValue("ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL", userProfileJson))) * 60 * 1000;
        DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = generalFunc.parseIntegerValue(3, generalFunc.getJsonValue("DRIVER_ARRIVED_MIN_TIME_PER_MINUTE", userProfileJson));
    }

    public void setPickUpLocation(Location pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public void setCabTypeId(String selectedCabTypeId) {
        this.selectedCabTypeId = selectedCabTypeId;
    }

    public void changeCabs() {

        if (driverMarkerList.size() > 0) {
            filterDrivers(true);
        } else {
            checkAvailableCabs();
        }
    }

    public void checkAvailableCabs() {

        if (pickUpLocation == null) {
            return;
        }
        if (updateDriverListTask == null) {
            updateDriverListTask = new UpdateFrequentTask(ONLINE_DRIVER_LIST_UPDATE_TIME_INTERVAL);
            onResumeCalled();
            updateDriverListTask.setTaskRunListener(this);
        }
        if (currentWebTask != null) {
            currentWebTask.cancel(true);
            currentWebTask = null;
        }

        if (mainAct != null) {
            mainAct.notifyCarSearching();
        }

        if (listOfDrivers.size() > 0) {
            listOfDrivers.clear();
        }


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadAvailableCab");
        parameters.put("PassengerLat", "" + pickUpLocation.getLatitude());
        parameters.put("PassengerLon", "" + pickUpLocation.getLongitude());
        parameters.put("iUserId", generalFunc.getMemberId());


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext,parameters);
        this.currentWebTask = exeWebServer;
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                    if (message.equals("SESSION_OUT")) {
                        generalFunc.notifySessionTimeOut();
                        Utils.runGC();
                        return;
                    }

                    JSONArray availCabArr = generalFunc.getJsonArray("AvailableCabList", responseString);

                    if (availCabArr!=null) {
                        for (int i = 0; i < availCabArr.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(availCabArr, i);

                            String carDetailsJson = generalFunc.getJsonValue("DriverCarDetails", obj_temp.toString());
                            HashMap<String, String> driverDataMap = new HashMap<String, String>();
                            driverDataMap.put("driver_id", generalFunc.getJsonValue("iDriverId", obj_temp.toString()));
                            driverDataMap.put("Name", generalFunc.getJsonValue("vName", obj_temp.toString()));
                            driverDataMap.put("Latitude", generalFunc.getJsonValue("vLatitude", obj_temp.toString()));
                            driverDataMap.put("Longitude", generalFunc.getJsonValue("vLongitude", obj_temp.toString()));
                            driverDataMap.put("GCMID", generalFunc.getJsonValue("iGcmRegId", obj_temp.toString()));
                            driverDataMap.put("iAppVersion", generalFunc.getJsonValue("iAppVersion", obj_temp.toString()));
                            driverDataMap.put("driver_img", generalFunc.getJsonValue("vImage", obj_temp.toString()));
                            driverDataMap.put("average_rating", generalFunc.getJsonValue("vAvgRating", obj_temp.toString()));
                            driverDataMap.put("vPhone_driver", generalFunc.getJsonValue("vPhone", obj_temp.toString()));
                            driverDataMap.put("vCode", generalFunc.getJsonValue("vCode", obj_temp.toString()));

                            driverDataMap.put("vCarType", generalFunc.getJsonValue("vCarType", carDetailsJson));
                            driverDataMap.put("vLicencePlate", generalFunc.getJsonValue("vLicencePlate", carDetailsJson));
                            driverDataMap.put("make_title", generalFunc.getJsonValue("make_title", carDetailsJson));
                            driverDataMap.put("model_title", generalFunc.getJsonValue("model_title", carDetailsJson));

                            listOfDrivers.add(driverDataMap);
                        }

                        if (availCabArr.length() == 0) {
                            removeDriversFromMap(true);
                            if (mainAct != null) {
                                mainAct.notifyNoCabs();
                            }
                        } else {
                            filterDrivers(false);
                        }
                    }
                    else
                    {
                        removeDriversFromMap(true);
                        if (mainAct != null) {
                            mainAct.notifyNoCabs();
                        }
                    }


                } else {
                    removeDriversFromMap(true);
                    if (parentView != null) {
                        generalFunc.showMessage(parentView, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }

                    if (mainAct != null) {
                        mainAct.notifyNoCabs();
                    }
                }
            }
        });
        exeWebServer.execute();
    }

    public void setTaskKilledValue(boolean isTaskKilled) {
        this.isTaskKilled = isTaskKilled;

        if (isTaskKilled == true) {
            onPauseCalled();
        }
    }

    public void removeDriversFromMap(boolean isUnSubscribeAll) {
        if (driverMarkerList.size() > 0) {
            for (int i = 0; i < driverMarkerList.size(); i++) {
                Marker marker_temp = driverMarkerList.get(0);
                marker_temp.remove();
                driverMarkerList.remove(0);
            }
        }

        // Remove listener of channels (unsuscribe) of drivers from pubnub


        if (mainAct != null && mainAct.configPubNub != null && isUnSubscribeAll == true) {
            mainAct.configPubNub.unSubscribeToChannels(mainAct.getDriverLocationChannelList());
        }
    }

    public ArrayList<Marker> getDriverMarkerList() {
        return this.driverMarkerList;
    }


    public void filterDrivers(boolean isCheckAgain) {

        if (pickUpLocation == null) {
            generalFunc.restartApp();
            return;
        }
        
        double lowestKM = 0.0;
        boolean isFirst_lowestKM = true;

        ArrayList<HashMap<String, String>> currentLoadedDrivers = new ArrayList<>();
//        removeDriversFromMap(false);

        ArrayList<Marker> driverMarkerList_temp = new ArrayList<>();

        for (int i = 0; i < listOfDrivers.size(); i++) {
            HashMap<String, String> driverData = listOfDrivers.get(i);

            String driverName = driverData.get("Name");
            String[] vCarType = driverData.get("vCarType").split(",");

            boolean isCarSelected = Arrays.asList(vCarType).contains(selectedCabTypeId);

            if (isCarSelected == false) {
                continue;
            }
            double driverLocLatitude = generalFunc.parseDoubleValue(0.0, driverData.get("Latitude"));
            double driverLocLongitude = generalFunc.parseDoubleValue(0.0, driverData.get("Longitude"));

            double distance = generalFunc.CalculationByLocation(pickUpLocation.getLatitude(), pickUpLocation.getLongitude(), driverLocLatitude, driverLocLongitude);

            if (isFirst_lowestKM == true) {
                lowestKM = distance;
                isFirst_lowestKM = false;
            } else {
                if (distance < lowestKM) {
                    lowestKM = distance;
                }
            }

            if (distance <= RESTRICTION_KM_NEAREST_TAXI) {
                driverData.put("DIST_TO_PICKUP", "" + distance);
                currentLoadedDrivers.add(driverData);

                Marker driverMarker_temp = mainAct.getDriverMarkerOnPubNubMsg(driverData.get("driver_id"), true);
                if (driverMarker_temp == null) {
                    Marker driverMarker = drawMarker(new LatLng(driverLocLatitude, driverLocLongitude), driverName, driverData);
                    driverMarkerList_temp.add(driverMarker);
                } else {

                    String eIconType = generalFunc.getSelectedCarTypeData(selectedCabTypeId, "VehicleTypes", "eIconType", userProfileJson);

                    int iconId = R.mipmap.car_driver;
                    if (eIconType.equalsIgnoreCase("Bike")) {
                        iconId = R.mipmap.car_driver_1;
                    } else if (eIconType.equalsIgnoreCase("Cycle")) {
                        iconId = R.mipmap.car_driver_2;
                    }
                    driverMarker_temp.setIcon(BitmapDescriptorFactory.fromResource(iconId));
                    driverMarkerList_temp.add(driverMarker_temp);
                }

            }
        }
        removeDriversFromMap(false);
        driverMarkerList.addAll(driverMarkerList_temp);

        if (mainAct != null) {
            int lowestTime = ((int) (lowestKM * DRIVER_ARRIVED_MIN_TIME_PER_MINUTE));
            if (lowestTime < DRIVER_ARRIVED_MIN_TIME_PER_MINUTE) {
                mainAct.setETA("" + DRIVER_ARRIVED_MIN_TIME_PER_MINUTE + " " + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));
            } else {
                mainAct.setETA("" + lowestTime + " " + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));
            }

        }
        if (mainAct != null) {

            ArrayList<String> unSubscribeChannelList = new ArrayList<>();
            ArrayList<String> subscribeChannelList = new ArrayList<>();

            ArrayList<String> currentDriverChannelsList = mainAct.getDriverLocationChannelList();
            ArrayList<String> newDriverChannelsList = mainAct.getDriverLocationChannelList(currentLoadedDrivers);


            for (int i = 0; i < currentDriverChannelsList.size(); i++) {
                String channel_name = currentDriverChannelsList.get(i);
                if (!newDriverChannelsList.contains(channel_name)) {
                    unSubscribeChannelList.add(channel_name);
                }
            }

            for (int i = 0; i < newDriverChannelsList.size(); i++) {
                String channel_name = newDriverChannelsList.get(i);
                if (!currentDriverChannelsList.contains(channel_name)) {
                    subscribeChannelList.add(channel_name);
                }
            }

            mainAct.setCurrentLoadedDriverList(currentLoadedDrivers);

            if (mainAct.configPubNub != null) {
                mainAct.configPubNub.subscribeToChannels(subscribeChannelList);
            }
            if (mainAct.configPubNub != null) {
                mainAct.configPubNub.unSubscribeToChannels(unSubscribeChannelList);
            }
        }
        if (currentLoadedDrivers.size() == 0) {
            if (mainAct != null) {
                mainAct.notifyNoCabs();
            }

            if (isCheckAgain == true) {
                checkAvailableCabs();
            }
        } else {

            if (mainAct != null) {
                mainAct.notifyCabsAvailable();
            }
        }
    }

    public Marker drawMarker(LatLng point, String Name, HashMap<String, String> driverData) {

        try {
            finalize();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        String eIconType = generalFunc.getSelectedCarTypeData(selectedCabTypeId, "VehicleTypes", "eIconType", userProfileJson);

        int iconId = R.mipmap.car_driver;
        if (eIconType.equals("Bike")) {
            iconId = R.mipmap.car_driver_1;
        } else if (eIconType.equals("Cycle")) {
            iconId = R.mipmap.car_driver_2;
        }
        // Setting latitude and longitude for the marker
        markerOptions.position(point).title("DriverId" + driverData.get("driver_id")).icon(BitmapDescriptorFactory.fromResource(iconId))
                .anchor(0.5f, 0.5f).flat(true);

        // Adding marker on the Google Map
        Marker marker = gMapView.addMarker(markerOptions);
        marker.setRotation(0);
        marker.setVisible(true);
        return marker;
    }

    public void onPauseCalled() {

        if (updateDriverListTask != null) {
            updateDriverListTask.stopRepeatingTask();
        }
    }

    public void onResumeCalled() {
        if (updateDriverListTask != null && isTaskKilled == false) {
            updateDriverListTask.startRepeatingTask();
        }
    }

    @Override
    public void onTaskRun() {
        checkAvailableCabs();
    }
}
