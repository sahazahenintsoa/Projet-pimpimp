package com.general.files;

import android.content.Context;
import android.os.AsyncTask;

import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MyProgressDialog;

import java.util.HashMap;

/**
 * Created by Admin on 22-02-2016.
 */
public class ExecuteWebServerUrl extends AsyncTask<String, String, String> {

    SetDataResponse setDataRes;

    HashMap<String, String> parameters;

    GeneralFunctions generalFunc;

    String responseString = "";

    boolean directUrl_value = false;
    String directUrl = "";

    boolean isLoaderShown = false;
    Context mContext;

    MyProgressDialog myPDialog;

    boolean isGenerateDeviceToken = false;
    String key_DeviceToken_param;

    public ExecuteWebServerUrl(Context mContext, HashMap<String, String> parameters) {
        this.parameters = parameters;
        this.mContext = mContext;
    }

    public ExecuteWebServerUrl(Context mContext,String directUrl, boolean directUrl_value) {
        this.directUrl = directUrl;
        this.directUrl_value = directUrl_value;
        this.mContext = mContext;
    }

    public void setLoaderConfig(Context mContext, boolean isLoaderShown, GeneralFunctions generalFunc) {
        this.isLoaderShown = isLoaderShown;
        this.generalFunc = generalFunc;
        this.mContext = mContext;
    }

    public void setIsDeviceTokenGenerate(boolean isGenerateDeviceToken, String key_DeviceToken_param) {
        this.isGenerateDeviceToken = isGenerateDeviceToken;
        this.key_DeviceToken_param = key_DeviceToken_param;
    }

    public void setIsDeviceTokenGenerate(boolean isGenerateDeviceToken, String key_DeviceToken_param, GeneralFunctions generalFunc) {
        this.isGenerateDeviceToken = isGenerateDeviceToken;
        this.key_DeviceToken_param = key_DeviceToken_param;
        this.generalFunc = generalFunc;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (isLoaderShown == true) {
            myPDialog = new MyProgressDialog(mContext, true, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
            myPDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        if (isGenerateDeviceToken == true && generalFunc != null) {

            String vDeviceToken = generalFunc.generateDeviceToken();

            if (vDeviceToken.equals("")) {
                return "";
            }

            if (parameters != null) {
                parameters.put(key_DeviceToken_param, "" + vDeviceToken);
            }
        }


        if (parameters != null) {
            GeneralFunctions generalFunc = new GeneralFunctions(mContext);
            parameters.put("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(CommonUtilities.SESSION_ID_KEY));
            parameters.put("GeneralUserType", CommonUtilities.app_type);
            parameters.put("GeneralMemberId", generalFunc.getMemberId());
            parameters.put("GeneralDeviceType", "" + Utils.deviceType);
            parameters.put("GeneralAppVersion", Utils.getAppVersion());
        }


        if (directUrl_value == false) {
            responseString = OutputStreamReader.performPostCall(CommonUtilities.SERVER_URL_WEBSERVICE, parameters);
        } else {
            responseString = new ExecuteResponse().getResponse(directUrl);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (myPDialog != null) {
            myPDialog.close();
        }

        if (setDataRes != null) {
            setDataRes.setResponse(responseString);
        }
    }

    public interface SetDataResponse {
        void setResponse(String responseString);
    }

    public void setDataResponseListener(SetDataResponse setDataRes) {
        this.setDataRes = setDataRes;
    }
}
