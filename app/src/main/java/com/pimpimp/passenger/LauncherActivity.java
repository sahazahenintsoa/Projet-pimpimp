package com.pimpimp.passenger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.OpenMainProfile;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.anim.loader.AVLoadingIndicatorView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class LauncherActivity extends AppCompatActivity implements GenerateAlertBox.HandleAlertBtnClick, GetLocationUpdates.LastLocationListner {

    AVLoadingIndicatorView loaderView;
    InternetConnection intCheck;
    GenerateAlertBox generateAlert;
    GeneralFunctions generalFunc;

    GetLocationUpdates getLastLocation;

    Location mLastLocation;

    String alertType = "";

    long autoLoginStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        intCheck = new InternetConnection(this);
        generalFunc = new GeneralFunctions(getActContext());
        generalFunc.storedata("isInLauncher","true");
        generalFunc.getHasKey(getActContext());
        generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setBtnClickList(this);
        setDefaultAlertBtn();
        generateAlert.setCancelable(false);

        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        Utils.runGC();
        checkConfigurations();
    }

    public void setDefaultAlertBtn() {
        generateAlert.resetBtn();
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));
    }

    public void checkConfigurations() {

        int status = (GoogleApiAvailability.getInstance()).isGooglePlayServicesAvailable(getActContext());

        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        } else if (status != ConnectionResult.SUCCESS) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        }

        if (generalFunc.isAllPermissionGranted() == false) {
            showError("", generalFunc.retrieveLangLBl("Application requires some permission to be granted to work. Please allow it.",
                    "LBL_ALLOW_PERMISSIONS_APP"));
            return;
        }
        if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {

            showNoInternetDialog();
        } else if (generalFunc.isLocationEnabled() == false) {
            showNoGPSDialog();
        } else {
            if (mLastLocation != null && mLastLocation.getLatitude() != 0.0 && mLastLocation.getLongitude() != 0.0) {
                continueProcess();
            } else if (getLastLocation == null) {
                getLastLocation = new GetLocationUpdates(getActContext(), 2);
                getLastLocation.setLastLocationListener(this);
            } else {
                showNoLocationDialog();
            }

            if (getLastLocation != null) {
                mLastLocation = getLastLocation.getLocation();
            }

        }

    }

    public void continueProcess() {

        showLoader();

        Utils.setAppLocal(getActContext());

        boolean isLanguageLabelsAvail = generalFunc.isLanguageLabelsAvail();
        Utils.printLog("isLanguageLabelsAvail", "::" + isLanguageLabelsAvail);
        Utils.printLog("isMemberAvail", "::" + generalFunc.isUserLoggedIn());
        Utils.printLog("memberId", "::" + generalFunc.getMemberId());

        if (generalFunc.isUserLoggedIn() == true) {
            autoLogin();
        } else {
            downloadGeneralData();
        }

    }

    public void downloadGeneralData() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "generalConfigData");
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("AppVersion", Utils.getAppVersion());
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                Utils.printLog("responseString", "::" + responseString);
                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        generalFunc.storedata(CommonUtilities.languageLabelsKey, generalFunc.getJsonValue("LanguageLabels", responseString));
                        generalFunc.storedata(CommonUtilities.FACEBOOK_APPID_KEY, generalFunc.getJsonValue("FACEBOOK_APP_ID", responseString));
                        generalFunc.storedata(CommonUtilities.LINK_FORGET_PASS_KEY, generalFunc.getJsonValue("LINK_FORGET_PASS_PAGE_PASSENGER", responseString));
                        generalFunc.storedata(CommonUtilities.APP_GCM_SENDER_ID_KEY, generalFunc.getJsonValue("GOOGLE_SENDER_ID", responseString));
                        generalFunc.storedata(CommonUtilities.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValue("MOBILE_VERIFICATION_ENABLE", responseString));
                        generalFunc.storedata(CommonUtilities.LANGUAGE_LIST_KEY, generalFunc.getJsonValue("LIST_LANGUAGES", responseString));
                        generalFunc.storedata(CommonUtilities.CURRENCY_LIST_KEY, generalFunc.getJsonValue("LIST_CURRENCY", responseString));
                        generalFunc.storedata(CommonUtilities.LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vCode", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
                        generalFunc.storedata(CommonUtilities.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("eType", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
                        generalFunc.storedata(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vGMapLangCode", generalFunc.getJsonValue("DefaultLanguageValues", responseString)));
                        generalFunc.storedata(CommonUtilities.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE",  responseString));
                        generalFunc.storedata(CommonUtilities.WALLET_ENABLE, generalFunc.getJsonValue("WALLET_ENABLE", responseString));
                        closeLoader();

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                new StartActProcess(getActContext()).startAct(AppLoginActivity.class);
                                ActivityCompat.finishAffinity(LauncherActivity.this);
                            }
                        }, 2000);

                    } else {
                        if (!generalFunc.getJsonValue("isAppUpdate", responseString).trim().equals("")
                                && generalFunc.getJsonValue("isAppUpdate", responseString).equals("true")) {

                            showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                            "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                    generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        } else {
                            showError();
                        }

                    }
                } else {
                    showError();
                }

            }
        });
        exeWebServer.execute();
    }

    public void autoLogin() {
        autoLoginStartTime = Calendar.getInstance().getTimeInMillis();

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("AppVersion", Utils.getAppVersion());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(final String responseString) {

                closeLoader();

                Utils.printLog("responseString", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                    if (message.equals("SESSION_OUT")) {
                        autoLoginStartTime = 0;
                        generalFunc.notifySessionTimeOut();
                        Utils.runGC();
                        return;
                    }

                    if (isDataAvail == true) {
                        if (Calendar.getInstance().getTimeInMillis() - autoLoginStartTime < 2000) {
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    new OpenMainProfile(getActContext(),
                                            generalFunc.getJsonValue(CommonUtilities.message_str, responseString), true, generalFunc).startProcess();
                                }
                            }, 2000);
                        } else {
                            new OpenMainProfile(getActContext(),
                                    generalFunc.getJsonValue(CommonUtilities.message_str, responseString), true, generalFunc).startProcess();
                        }


                    } else {
                        autoLoginStartTime = 0;
                        if (!generalFunc.getJsonValue("isAppUpdate", responseString).trim().equals("")
                                && generalFunc.getJsonValue("isAppUpdate", responseString).equals("true")) {

                            showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                            "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                    generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        } else {
                            showError(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                                    generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        }
                    }
                } else {
                    autoLoginStartTime = 0;
                    showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void showLoader() {
        loaderView.setVisibility(View.VISIBLE);
    }

    public void closeLoader() {
        loaderView.setVisibility(View.GONE);
    }

    public void showError() {
        alertType = "ERROR";
        setDefaultAlertBtn();
        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                generalFunc.retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT"));

        generateAlert.showAlertBox();
    }

    public void showError(String title, String contentMsg) {
        alertType = "ERROR";
        setDefaultAlertBtn();
        generateAlert.setContentMessage(title,
                contentMsg);

        generateAlert.showAlertBox();
    }

    public void showNoInternetDialog() {
        alertType = "NO_INTERNET";
        setDefaultAlertBtn();
        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));

        generateAlert.showAlertBox();

    }

    public void showNoGPSDialog() {
        alertType = "NO_GPS";
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Your GPS seems to be disabled, do you want to enable it?", "LBL_ENABLE_GPS"));

        generateAlert.resetBtn();
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));
        generateAlert.showAlertBox();

    }

    public void showErrorOnPlayServiceDialog(String content) {
        alertType = "NO_PLAY_SERVICE";
        generateAlert.setContentMessage("", content);

        generateAlert.resetBtn();
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
        generateAlert.showAlertBox();

    }

    public void showAppUpdateDialog(String content) {
        alertType = "APP_UPDATE";
        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("New update available", "LBL_NEW_UPDATE_AVAIL"), content);

        generateAlert.resetBtn();
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
        generateAlert.showAlertBox();

    }

    public void showNoLocationDialog() {
        alertType = "NO_LOCATION";
        setDefaultAlertBtn();
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_LOCATION_FOUND_TXT"));

        generateAlert.showAlertBox();

    }

    public Context getActContext() {
        return LauncherActivity.this;
    }

    @Override
    public void handleBtnClick(int btn_id) {

        if (btn_id == 0) {
            generateAlert.closeAlertBox();

            if (!alertType.equals("NO_PLAY_SERVICE") && !alertType.equals("APP_UPDATE")) {
                finish();
            } else {
                checkConfigurations();
            }


        } else {
            if (alertType.equals("NO_PLAY_SERVICE")) {

                boolean isSuccessfulOpen = new StartActProcess(getActContext()).openURL("market://details?id=com.google.android.gms");
                if (isSuccessfulOpen == false) {
                    new StartActProcess(getActContext()).openURL("http://play.google.com/store/apps/details?id=com.google.android.gms");
                }

                generateAlert.closeAlertBox();
                checkConfigurations();

            } else if (alertType.equals("APP_UPDATE")) {

                boolean isSuccessfulOpen = new StartActProcess(getActContext()).openURL("market://details?id=" + CommonUtilities.package_name);
                if (isSuccessfulOpen == false) {
                    new StartActProcess(getActContext()).openURL("http://play.google.com/store/apps/details?id=" + CommonUtilities.package_name);
                }

                generateAlert.closeAlertBox();
                checkConfigurations();

            } else if (!alertType.equals("NO_GPS")) {
                generateAlert.closeAlertBox();
                checkConfigurations();
            } else {
                new StartActProcess(getActContext()).
                        startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);
            }

        }
    }

    @Override
    public void handleLastLocationListnerCallback(Location mLastLocation) {

        this.mLastLocation = mLastLocation;
        Utils.printLog("Loc", "update:" + mLastLocation.getLatitude() + ":" + mLastLocation.getLongitude());
        checkConfigurations();
    }

    @Override
    public void handleLastLocationListnerNOVALUECallback(int id) {
        Utils.printLog("NO Loc", "update:");
        showNoLocationDialog();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (getLastLocation != null && getLastLocation.isApiConnected()) {
            getLastLocation.startLocationUpdates();
        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        System.gc();

        if (getLastLocation != null && getLastLocation.isApiConnected()) {
            getLastLocation.stopLocationUpdates();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.REQUEST_CODE_GPS_ON) {
            checkConfigurations();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        generalFunc.storedata("isInLauncher","false");
    }
}
