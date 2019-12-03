package com.utils;

import com.paypal.android.sdk.payments.PayPalConfiguration;

public class CommonUtilities {

    public static final String app_name = "PassengerApp";
    public static final String MINT_APP_ID = "452f0df7";
    public static final String package_name = "com.pimpimp.passenger";


     public static final String SERVER_URL = "https://www.pimpimp.com/";
    public static final String SERVER_URL_WEBSERVICE = SERVER_URL + "webservice_enc.php?";

    public static final String SERVER_URL_PHOTOS = SERVER_URL + "webimages/";
    public static final String PUBNUB_PUB_KEY = "PUBNUB_PUBLISH_KEY";
    public static final String PUBNUB_SUB_KEY = "PUBNUB_SUBSCRIBE_KEY";
    public static final String PUBNUB_SEC_KEY = "PUBNUB_SECRET_KEY";
    public static String app_type = "Passenger";
    public static String languageLabelsKey = "LanguageLabel";
    public static String APP_GCM_SENDER_ID_KEY = "APP_GCM_SENDER_ID";
    public static String MOBILE_VERIFICATION_ENABLE_KEY = "MOBILE_VERIFICATION_ENABLE";
    public static String FACEBOOK_APPID_KEY = "FACEBOOK_APPID";
    public static String LINK_FORGET_PASS_KEY = "LINK_FORGET_PASS_PAGE_PASSENGER";
    public static String LANGUAGE_LIST_KEY = "LANGUAGELIST";
    public static String CURRENCY_LIST_KEY = "CURRENCYLIST";
    public static String LANGUAGE_IS_RTL_KEY = "LANG_IS_RTL";
    public static String GOOGLE_MAP_LANGUAGE_CODE_KEY = "GOOGLE_MAP_LANG_CODE";
    public static String REFERRAL_SCHEME_ENABLE = "REFERRAL_SCHEME_ENABLE";
    public static String WALLET_ENABLE = "WALLET_ENABLE";
    public static String CURRENCY_VAL_PAYPAL = "CURRENCY_VAL_PAYPAL";
    public static String CURRENCY_CODE_PAYPAL = "CURRENCY_CODE_PAYPAL";
    public static String DATABASE_RTL_STR = "rtl";
    public static String LANGUAGE_CODE_KEY = "LANG_CODE";
    public static String isUserLogIn = "IsUserLoggedIn";
    public static String iMemberId_KEY = "iUserId";
    public static String action_str = "Action";
    public static String message_str = "message";
    public static String GCM_FAILED_KEY = "GCM_FAILED";
    public static String APNS_FAILED_KEY = "APNS_FAILED";

    // paypal configurations
    public static String driver_message_arrived_intent_action = package_name + ".driver.message.action";
    public static String driver_message_arrived_intent_key = "message";
    public static String CONFIG_CLIENT_ID = "CONFIG_CLIENT_ID";

    // Change When App goes live with card
    public static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    public static final String DISPLAY_TAG = "Pimpimp";

    /* Add/Remove  restriction to app.*/
    public static String restrict_app = "Yes";


    public static final String SESSION_ID_KEY = "SESSION_ID";
    public static String DEVICE_SESSION_ID_KEY = "DEVICE_SESSION_ID";

}
