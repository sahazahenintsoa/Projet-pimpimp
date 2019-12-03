package com.general.files;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.pimpimp.passenger.R;
import com.pimpimp.passenger.VerifyFbProfileActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Admin on 02-07-2016.
 */
public class GetAddressFromLocation {
    double latitude;
    double longitude;
    Context mContext;
    String serverKey;
    GeneralFunctions generalFunc;
    ExecuteWebServerUrl currentWebTask;

    AddressFound addressFound;
    boolean isLoaderEnable = false;

    String Address1 = "";
    String Address2 = "";
    String City = "";
    String State = "";
    String  Country = "";
    String County = "";
    String PIN = "";

    public GetAddressFromLocation(Context mContext, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;

        serverKey = mContext.getResources().getString(R.string.google_api_get_address_from_location_serverApi);
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLoaderEnable(boolean isLoaderEnable){

        this.isLoaderEnable=isLoaderEnable;
    }
    public void execute() {
        if (currentWebTask != null) {
            currentWebTask.cancel(true);
            currentWebTask = null;
        }
        String url_str = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude +"&key=" + serverKey;
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext,url_str, true);

        if(isLoaderEnable == true){
            exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        }
        this.currentWebTask = exeWebServer;
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunc.getJsonValue("status", responseString);

                    if (status.equals("OK")) {
                        String address_loc = "";

                        JSONArray arr = generalFunc.getJsonArray("results", responseString);

                        if (arr.length() > 0) {

                            JSONObject obj = generalFunc.getJsonObject(arr, 0);

                            String formatted_address = generalFunc.getJsonValue("formatted_address", obj.toString());

                            try {

                                JSONObject zero = arr.getJSONObject(0);
                                JSONArray address_components = zero.getJSONArray("address_components");

                                for (int i = 0; i < address_components.length(); i++) {
                                    JSONObject zero2 = address_components.getJSONObject(i);
                                    String long_name = zero2.getString("long_name");
                                    JSONArray mtypes = zero2.getJSONArray("types");
                                    String Type = mtypes.getString(0);

                                    if (TextUtils.isEmpty(long_name) == false || !long_name.equals(null) || long_name.length() > 0 || long_name != "") {
                                        if (Type.equalsIgnoreCase("street_number")) {
                                            Address1 = long_name + " ";
                                        } else if (Type.equalsIgnoreCase("route")) {
                                            Address1 = Address1 + long_name;
                                        } else if (Type.equalsIgnoreCase("sublocality")) {
                                            Address2 = long_name;
                                        } else if (Type.equalsIgnoreCase("locality")) {
                                            // Address2 = Address2 + long_name + ", ";
                                            City = long_name;
                                        } else if (Type.equalsIgnoreCase("administrative_area_level_2")) {
                                            County = long_name;
                                        } else if (Type.equalsIgnoreCase("administrative_area_level_1")) {
                                            State = long_name;
                                        } else if (Type.equalsIgnoreCase("country")) {
                                            Country = long_name;
                                        } else if (Type.equalsIgnoreCase("postal_code")) {
                                            PIN = long_name;
                                        }
                                    }
                                }
                            }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }

                            String[] addressArr = formatted_address.split(",");

                            boolean first_input = true;
                            for (int i = 0; i < addressArr.length; i++) {
                                if (!addressArr[i].contains("Unnamed") && !addressArr[i].contains("null")) {

                                    if (i == 0 && addressArr[0].matches("^[0-9]+$")) {
                                        continue;
                                    }
                                    if (first_input == true) {
                                        address_loc = addressArr[i];
                                        first_input = false;
                                    } else {
                                        address_loc = address_loc + "," + addressArr[i];
                                    }

                                }
                            }

                            if (Country.contains("Madagascar") && (TextUtils.isEmpty(City)))
                            {
                                City="Antananarivo";
                            }

                            Utils.printLog("Api","City"+City+"Country"+Country);

                            if (addressFound != null) {

                                    addressFound.onAddressFound(address_loc, latitude, longitude,City);

                            }
                        }

                    }

                }
            }
        });
        exeWebServer.execute();
    }

    public String getAddress1() {
        return Address1;

    }

    public String getAddress2() {
        return Address2;

    }

    public String getCity() {
        return City;

    }

    public String getState() {
        return State;

    }

    public String getCountry() {
        return Country;

    }

    public String getCounty() {
        return County;

    }

    public String getPIN() {
        return PIN;

    }
    public interface AddressFound {
        void onAddressFound(String address,double latitude,double longitude,String cityName);
    }

    public void setAddressList(AddressFound addressFound) {
        this.addressFound = addressFound;
    }
}
