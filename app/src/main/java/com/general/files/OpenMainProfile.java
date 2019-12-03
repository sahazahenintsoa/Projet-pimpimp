package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.RatingActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;

import org.json.JSONObject;

/**
 * Created by Admin on 29-06-2016.
 */
public class OpenMainProfile {
    Context mContext;
    String responseString;
    boolean isCloseOnError;
    GeneralFunctions generalFun;

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun) {
        this.mContext = mContext;
        this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
    }

    public void startProcess() {

        if (generalFun==null)
            return;

        generalFun.sendHeartBeat();

        generalFun.storedata(CommonUtilities.PUBNUB_PUB_KEY, generalFun.getJsonValue("PUBNUB_PUBLISH_KEY", responseString));
        generalFun.storedata(CommonUtilities.PUBNUB_SUB_KEY, generalFun.getJsonValue("PUBNUB_SUBSCRIBE_KEY", responseString));
        generalFun.storedata(CommonUtilities.PUBNUB_SEC_KEY, generalFun.getJsonValue("PUBNUB_SECRET_KEY", responseString));

        generalFun.storedata(CommonUtilities.SESSION_ID_KEY, generalFun.getJsonValue("tSessionId", responseString));
        generalFun.storedata(CommonUtilities.DEVICE_SESSION_ID_KEY, generalFun.getJsonValue("tDeviceSessionId", responseString));

        generalFun.storedata(CommonUtilities.CURRENCY_VAL_PAYPAL, generalFun.getJsonValue("CURRENCY_VAL_PAYPAL", responseString));
        generalFun.storedata(CommonUtilities.CONFIG_CLIENT_ID, generalFun.getJsonValue("CONFIG_CLIENT_ID", responseString));
        generalFun.storedata(CommonUtilities.CURRENCY_CODE_PAYPAL, generalFun.getJsonValue("CURRENCY_CODE_PAYPAL", responseString));


        String vTripStatus = generalFun.getJsonValue("vTripStatus", responseString);
        String PaymentStatus_From_Passenger_str = "";
        String Ratings_From_Passenger_str = "";
        String vTripPaymentMode_str = "";
        String eVerified_str = "";

        if (vTripStatus.equals("Not Active")) {
            String Last_trip_data = generalFun.getJsonValue("Last_trip_data", responseString);

            PaymentStatus_From_Passenger_str = generalFun.getJsonValue("PaymentStatus_From_Passenger", responseString);
            Ratings_From_Passenger_str = generalFun.getJsonValue("Ratings_From_Passenger", responseString);
            eVerified_str = generalFun.getJsonValue("eVerified", Last_trip_data);
            vTripPaymentMode_str = generalFun.getJsonValue("vTripPaymentMode", Last_trip_data);

            vTripPaymentMode_str = "Cash";// to remove paypal
            PaymentStatus_From_Passenger_str = "Approved"; // to remove paypal
        }

        Bundle bn = new Bundle();
        bn.putString("USER_PROFILE_JSON", responseString);

        if (!vTripStatus.equals("Not Active") || ((PaymentStatus_From_Passenger_str.equals("Approved")
                || vTripPaymentMode_str.equals("Cash")) && Ratings_From_Passenger_str.equals("Done")
                /*&& eVerified_str.equals("Verified")*/)) {
            new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
        }else{
            new StartActProcess(mContext).startActWithData(RatingActivity.class, bn);
        }

        ActivityCompat.finishAffinity((Activity) mContext);
    }
}
