package com.general.files;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.drawRoute.DirectionsJSONParser;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pimpimp.passenger.LauncherActivity;
import com.pimpimp.passenger.MainActivity;
import com.pimpimp.passenger.R;
import com.pimpimp.passenger.VerifyMobileActivity;
import com.utils.CommonUtilities;
import com.utils.ScalingUtilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Admin on 27-06-2016.
 */
public class GeneralFunctions {

    Context mContext;
    public static final int MY_PERMISSIONS_REQUEST = 51;
    IntentFilter mIntentFilter;
    private int NOTIFICATION_ID = 11;

    public GeneralFunctions(Context context) {
        this.mContext = context;
        checkForRTL();
    }

    public void checkForRTL() {
        if (mContext instanceof Activity) {
            if (!retrieveValue(CommonUtilities.LANGUAGE_IS_RTL_KEY).equals("") && retrieveValue(CommonUtilities.LANGUAGE_IS_RTL_KEY).equals(CommonUtilities.DATABASE_RTL_STR)) {
                forceRTLIfSupported((Activity) mContext);
            }
        }
    }

    public void forceRTLIfSupported(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            act.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public void getHasKey(Context act) {
        PackageInfo info;
        try {
            info = act.getPackageManager().getPackageInfo(act.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

    public void forceRTLIfSupported(android.support.v7.app.AlertDialog alertDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            alertDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public String wrapHtml(Context context, String html) {
        return context.getString(R.string.html, html);
    }



    public void forceRTLIfSupported(Dialog alertDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            alertDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public boolean isRTLmode() {
        if (!retrieveValue(CommonUtilities.LANGUAGE_IS_RTL_KEY).equals("") && retrieveValue(CommonUtilities.LANGUAGE_IS_RTL_KEY).equals(CommonUtilities.DATABASE_RTL_STR)) {
            return true;
        }
        return false;
    }

    public String retrieveValue(String key) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String value_str = mPrefs.getString(key, "");

        return value_str;
    }

    public String retrieveLangLBl(String orig, String label) {

        if (isLanguageLabelsAvail() == true) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String languageLabels_str = mPrefs.getString(CommonUtilities.languageLabelsKey, "");

            if (getJsonValue(label, languageLabels_str).equals("")) {
                return orig;
            }

            return getJsonValue(label, languageLabels_str);
        }

        return orig;
    }

    public String generateDeviceToken() {
        if (checkPlayServices() == false) {
            return "";
        }
        InstanceID instanceID = InstanceID.getInstance(mContext);
        String GCMregistrationId = "";
        try {
            GCMregistrationId = instanceID.getToken(retrieveValue(CommonUtilities.APP_GCM_SENDER_ID_KEY), GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null);
        } catch (IOException e) {
            e.printStackTrace();
            GCMregistrationId = "";
        }

        return GCMregistrationId;
    }


    public void generateNotification(Context context, String message) {
        WakeLocker.acquire(context);

        int icon = R.mipmap.ic_launcher;
        String title = context.getString(R.string.app_name);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(icon)
                .setContentTitle(title).setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true);

        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationmanager.notify(NOTIFICATION_ID, mBuilder.build());

        WakeLocker.release();
    }

    public boolean checkPlayServices() {
        final GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        final int result = googleAPI.isGooglePlayServicesAvailable(mContext);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    public void run() {
                        googleAPI.getErrorDialog(((Activity) mContext), result,
                                Utils.PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    }
                });

            }

            return false;
        }

        return true;
    }

    public static boolean checkDataAvail(String key, String response) {
        try {
            JSONObject obj_temp = new JSONObject(response);

            String action_str = obj_temp.getString(key);

            if (!action_str.equals("") && !action_str.equals("0") && action_str.equals("1")) {
                return true;
            }

        } catch (JSONException e) {
            e.printStackTrace();

            return false;
        }

        return false;
    }

    public void removeValue(String key) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(key);
        editor.commit();
    }

    public void storedata(String key, String data) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public String addSemiColonToPrice(String value) {
        return NumberFormat.getNumberInstance(Locale.US).format(parseIntegerValue(0, value));
    }

    public void storeUserData(String memberId) {
        storedata(CommonUtilities.iMemberId_KEY, memberId);
        storedata(CommonUtilities.isUserLogIn, "1");
    }

    public String getMemberId() {
        if (isUserLoggedIn() == true) {
            return retrieveValue(CommonUtilities.iMemberId_KEY);
        } else {
            return "";
        }
    }

    public boolean isWalletEnable() {
        if (!retrieveValue(CommonUtilities.WALLET_ENABLE).equals("") && retrieveValue(CommonUtilities.WALLET_ENABLE).equalsIgnoreCase("Yes")) {
            return true;
        }
        return false;
    }

    public boolean isReferralSchemeEnable() {
        if (!retrieveValue(CommonUtilities.REFERRAL_SCHEME_ENABLE).equals("") && retrieveValue(CommonUtilities.REFERRAL_SCHEME_ENABLE).equalsIgnoreCase("Yes")) {
            return true;
        }
        return false;
    }

    public void logOutUser() {
        removeValue(CommonUtilities.iMemberId_KEY);
        removeValue(CommonUtilities.isUserLogIn);
        removeValue(CommonUtilities.languageLabelsKey);
    }

    public boolean isUserLoggedIn() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String isUserLoggedIn_str = mPrefs.getString(CommonUtilities.isUserLogIn, "");

        if (!isUserLoggedIn_str.equals("") && isUserLoggedIn_str.equals("1")) {
            return true;
        }

        return false;
    }

    public String getJsonValue(String key, String response) {

        try {
            JSONObject obj_temp = new JSONObject(response);

            String value_str = Html.fromHtml(obj_temp.getString(key)).toString();

            if (value_str != null && !value_str.equals("null") && !value_str.equals("")) {
                return value_str;
            }

        } catch (JSONException e) {
            e.printStackTrace();

            return "";
        }

        return "";
    }

    public boolean isLanguageLabelsAvail() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String languageLabels_str = mPrefs.getString(CommonUtilities.languageLabelsKey, null);

        if (languageLabels_str != null && !languageLabels_str.equals("")) {
            return true;
        }

        return false;
    }

    public JSONArray getJsonArray(String key, String response) {
        try {
            JSONObject obj_temp = new JSONObject(response);
            JSONArray obj_temp_arr = obj_temp.getJSONArray(key);

            return obj_temp_arr;

        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }

    }

    public JSONArray getJsonArray(String response) {
        try {
            JSONArray obj_temp_arr = new JSONArray(response);

            return obj_temp_arr;

        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }

    }

    public JSONObject getJsonObject(JSONArray arr, int position) {
        try {
            JSONObject obj_temp = arr.getJSONObject(position);

            return obj_temp;

        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }

    }

    public boolean isJSONkeyAvail(String key, String response) {
        try {
            JSONObject json_obj = new JSONObject(response);

            if (json_obj.has(key) && !json_obj.isNull(key)) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean isJSONArrKeyAvail(String key, String response) {
        try {
            JSONObject json_obj = new JSONObject(response);

            if (json_obj.optJSONArray(key) != null) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public Float parseFloatValue(float defaultValue, String strValue) {

        try {
            float value = Float.parseFloat(strValue);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Double parseDoubleValue(double defaultValue, String strValue) {

        try {
            double value = Double.parseDouble(strValue);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int parseIntegerValue(int defaultValue, String strValue) {

        try {
            int value = Integer.parseInt(strValue);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public long parseLongValue(long defaultValue, String strValue) {

        try {
            long value = Long.parseLong(strValue);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void sendHeartBeat() {
        mContext.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        mContext.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
    }

    public boolean isEmailValid(String email) {
        boolean isValid = false;
        String expression = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public void generateErrorView(ErrorView errorView, String title, String subTitle) {
        errorView.setConfig(ErrorView.Config.create()
                .title(retrieveLangLBl("Error", title))
                .titleColor(mContext.getResources().getColor(android.R.color.black))
                .subtitle(retrieveLangLBl("", subTitle))
                .retryText(retrieveLangLBl("Retry", "LBL_RETRY_TXT"))
                .retryTextColor(mContext.getResources().getColor(R.color.error_view_retry_btn_txt_color))
                .build());
    }

    public void showError() {
        GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setContentMessage(retrieveLangLBl("Error", "LBL_ERROR_TXT"), retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT"));
        generateAlert.setPositiveBtn(retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    public void showGeneralMessage(String title, String message) {
        GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setContentMessage(title, message);
        generateAlert.setPositiveBtn(retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (locationMode != Settings.Secure.LOCATION_MODE_OFF && statusOfGPS == true) {
                return true;
            }

            return false;

        } else {
            locationProviders = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            return !TextUtils.isEmpty(locationProviders);
        }

    }

    public boolean checkLocationPermission(boolean isPermissionDialogShown) {
        int permissionCheck_fine = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck_coarse = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck_fine != PackageManager.PERMISSION_GRANTED || permissionCheck_coarse != PackageManager.PERMISSION_GRANTED) {

            if (isPermissionDialogShown == false) {
                ActivityCompat.requestPermissions((Activity) mContext,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST);
            }
            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            return false;
        }
        return true;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                if (mContext instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
                }
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {

                return true;
            } else {

                if (mContext instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST);
                }

                return false;
            }
        } else {
            return true;
        }
    }

    public boolean isAllPermissionGranted() {
        int permissionCheck_fine = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck_coarse = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_storage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck_camera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);

        if (permissionCheck_fine != PackageManager.PERMISSION_GRANTED || permissionCheck_coarse != PackageManager.PERMISSION_GRANTED
                || permissionCheck_storage != PackageManager.PERMISSION_GRANTED || permissionCheck_camera != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST);

            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            return false;
        }

        return true;
    }

    public void logOUTFrmFB() {
        LoginManager.getInstance().logOut();
    }

    public GenerateAlertBox notifyRestartApp() {
        GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setContentMessage(retrieveLangLBl("", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"),
                retrieveLangLBl("In order to apply changes restarting app is required. Please wait.", "LBL_NOTIFY_RESTART_APP_TO_CHANGE"));
        generateAlert.setPositiveBtn(retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();

        return generateAlert;
    }

    public void notifySessionTimeOut() {
        logOutUser();

        GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setContentMessage(retrieveLangLBl("", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"),
                retrieveLangLBl("Your session is expired. Please login again.", "LBL_SESSION_TIME_OUT"));
        generateAlert.setPositiveBtn(retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {

                if (btn_id == 1) {
                    restartApp();
                }
            }
        });
        generateAlert.showAlertBox();

    }



    public void restartApp() {
        new StartActProcess(mContext).startAct(LauncherActivity.class);
        ((Activity) mContext).setResult(Activity.RESULT_CANCELED);
        ActivityCompat.finishAffinity((Activity) mContext);
        System.gc();
    }

    public View getCurrentView(Activity act) {
        View view = act.findViewById(android.R.id.content);
        return view;
    }

    public void showMessage(View view, String message) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public double CalculationByLocation(double lat1, double lon1, double lat2, double lon2) {
        int Radius = 6371;// radius of earth in Km
        double lat1_s = lat1;
        double lat2_d = lat2;
        double lon1_s = lon1;
        double lon2_d = lon2;
        double dLat = Math.toRadians(lat2_d - lat1_s);
        double dLon = Math.toRadians(lon2_d - lon1_s);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1_s))
                * Math.cos(Math.toRadians(lat2_d)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        // Log.i("Radius Value", "" + valueResult + " KM " + kmInDec
        // + " Meter " + meterInDec);

        return Radius * c;
    }

    public String getSelectedCarTypeData(String selectedCarTypeId, String jsonArrKey, String dataKey, String json) {
        JSONArray arr = getJsonArray(jsonArrKey, json);

        for (int i = 0; i < arr.length(); i++) {
            JSONObject tempObj = getJsonObject(arr, i);

            String iVehicleTypeId = getJsonValue("iVehicleTypeId", tempObj.toString());

            if (iVehicleTypeId.equals(selectedCarTypeId)) {
                String dataValue = getJsonValue(dataKey, tempObj.toString());

                return dataValue;
            }
        }

        return "";
    }

    public PolylineOptions getGoogleRouteOptions(String directionJson, int width, int color) {
        PolylineOptions lineOptions = new PolylineOptions();

        try {
            DirectionsJSONParser parser = new DirectionsJSONParser();
            List<List<HashMap<String, String>>> routes_list = parser.parse(new JSONObject(directionJson));

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            if (routes_list.size() > 0) {
                // Fetching i-th route
                List<HashMap<String, String>> path = routes_list.get(0);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                }

                lineOptions.addAll(points);
                lineOptions.width(width);
                lineOptions.color(color);

                return lineOptions;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, String userProfileJson, String imageKey) {
        String vImgName_str = getJsonValue(imageKey, userProfileJson);

        if (vImgName_str == null || vImgName_str.equals("") || vImgName_str.equals("NONE")) {
            userProfileImgView.setImageResource(R.mipmap.ic_no_pic_user);
        } else {
            new DownloadProfileImg(mContext, userProfileImgView,
                    CommonUtilities.SERVER_URL_PHOTOS + "upload/Passenger/" + getMemberId() + "/" + vImgName_str,
                    vImgName_str).execute();
        }
    }

    public void verifyMobile(final Bundle bn, final Fragment fragment) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();

                if (btn_id == 0) {
                    return;
                }
                if (fragment == null) {
                    new StartActProcess(mContext).startActForResult(VerifyMobileActivity.class, bn, Utils.VERIFY_MOBILE_REQ_CODE);
                } else {
                    new StartActProcess(mContext).startActForResult(fragment, VerifyMobileActivity.class, Utils.VERIFY_MOBILE_REQ_CODE, bn);
                }

            }
        });
        generateAlert.setContentMessage("", retrieveLangLBl("", "LBL_VERIFY_MOBILE_CONFIRM_MSG"));
        generateAlert.setPositiveBtn(retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(retrieveLangLBl("", "LBL_CANCEL_TXT"));
        generateAlert.showAlertBox();
    }

    public String decodeFile(String path, int DESIREDWIDTH, int DESIREDHEIGHT, String tempImgName) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.CROP);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.CROP);
            } else {
                if (unscaledBitmap.getWidth() > unscaledBitmap.getHeight()) {
                    scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, unscaledBitmap.getHeight(), unscaledBitmap.getHeight(), ScalingUtilities.ScalingLogic.CROP);
                } else {
                    scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, unscaledBitmap.getWidth(), unscaledBitmap.getWidth(), ScalingUtilities.ScalingLogic.CROP);
                }
            }

            // Store to tmp file

            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/" + Utils.TempImageFolderPath);
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

//            String s = "tmp.png";

            File f = new File(mFolder.getAbsolutePath(), tempImgName);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }

    public Bitmap convertDrawableToBitmap(Context mContext, int resId) {
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
                resId);

        return icon;
    }

    public Bitmap writeTextOnDrawable(Context mContext, int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.createFromAsset(mContext.getAssets(), mContext.getResources().getString(R.string.defaultFont));

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Utils.dipToPixels(mContext, 14));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        // If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))
            paint.setTextSize(Utils.dipToPixels(mContext, 7));

        int xPos = (canvas.getWidth() / 2) - 2; // -2 is for regulating the x
        // position offset

        // baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 4) - ((paint.descent() + paint.ascent()) / 2));

        // canvas.save();

        for (String line : text.split("\n")) {
            canvas.drawText(line, xPos, yPos, paint);
            paint.setTextSize(Utils.dipToPixels(mContext, 14));
            yPos += paint.descent() - paint.ascent();
        }

        return bm;
    }
}
