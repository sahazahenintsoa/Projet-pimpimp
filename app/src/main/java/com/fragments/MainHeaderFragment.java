package com.fragments;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.StartActProcess;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.R;
import com.pimpimp.passenger.SearchPickupLocationActivity;
import com.utils.Utils;
import com.view.MTextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainHeaderFragment extends Fragment implements GetAddressFromLocation.AddressFound {


    MainActivity mainAct;
    GeneralFunctions generalFunc;
    GoogleMap gMap;

    ImageView menuImgView;

    View view;

    GetAddressFromLocation getAddressFromLocation;
    public MTextView sourceLocAddressTxt;
    LinearLayout searchPickUpLocArea;

    MainHeaderFragment mainHeaderFrag;
    String userProfileJson = "";
    boolean isDestinationMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_main_header, container, false);
        menuImgView = (ImageView) view.findViewById(R.id.menuImgView);
        sourceLocAddressTxt = (MTextView) view.findViewById(R.id.sourceLocAddressTxt);
        searchPickUpLocArea = (LinearLayout) view.findViewById(R.id.searchPickUpLocArea);

        mainAct = (MainActivity) getActivity();
        generalFunc = mainAct.generalFunc;
        mainHeaderFrag = mainAct.getMainHeaderFrag();

        userProfileJson = mainAct.userProfileJson;
        getAddressFromLocation = new GetAddressFromLocation(mainAct.getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        ((MTextView) view.findViewById(R.id.pickUpLocTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_HEADER_TXT"));
        menuImgView.setOnClickListener(new setOnClickList());
        searchPickUpLocArea.setOnClickListener(new setOnClickList());

        return view;
    }

    public void setGoogleMapInstance(GoogleMap gMap) {
        this.gMap = gMap;
        this.gMap.setOnCameraChangeListener(new onGoogleMapCameraChangeList());
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude,String cityName) {
        Utils.printLog("address", ":" + address);
        if (isDestinationMode == false) {
                sourceLocAddressTxt.setText(address);
        }
        mainAct.onAddressFound(address);
    }

    public String getPickUpAddress() {
        return sourceLocAddressTxt.getText().toString();
    }

    public void configDestinationMode(boolean isDestinationMode) {
        this.isDestinationMode = isDestinationMode;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == menuImgView.getId()) {
                mainAct.checkDrawerState();
            } else if (view.getId() == searchPickUpLocArea.getId()) {

                Bundle bn = new Bundle();
                bn.putString("isPickUpLoc", "true");
                if (mainAct.getPickUpLocation() !=null) {
                    bn.putString("PickUpLatitude", "" + mainAct.getPickUpLocation().getLatitude());
                    bn.putString("PickUpLongitude", "" + mainAct.getPickUpLocation().getLongitude());
                }
                new StartActProcess(mainAct.getActContext()).startActForResult(mainHeaderFrag, SearchPickupLocationActivity.class,
                        Utils.SEARCH_PICKUP_LOC_REQ_CODE, bn);
            }
        }
    }

    public class onGoogleMapCameraChangeList implements GoogleMap.OnCameraChangeListener {

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

            Utils.printLog("Camera", "changed");
            LatLng center = gMap.getCameraPosition().target;
            getAddressFromLocation.setLocation(center.latitude, center.longitude);
            getAddressFromLocation.execute();

            if (isDestinationMode == false) {
                sourceLocAddressTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));

                Location pickUpLoc = new Location("CameraChange");
                pickUpLoc.setLatitude(center.latitude);
                pickUpLoc.setLongitude(center.longitude);

                mainAct.onPickUpLocChanged(pickUpLoc);
            }


            mainAct.onMapCameraChanged();
        }
    }

    public void releaseResources() {
        this.gMap.setOnCameraChangeListener(null);
        this.gMap = null;
        getAddressFromLocation.setAddressList(null);
        getAddressFromLocation = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == mainAct.RESULT_OK && data != null && gMap != null) {

            Utils.printLog("Latitude", "::" + generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")));
            Utils.printLog("Longitude", "::" + generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude")));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")),
                            generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude"))))
                    .zoom(gMap.getCameraPosition().zoom).build();
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }
}
