package com.general.files;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;

import com.splunk.mint.Mint;
import com.pimpimp.passenger.LauncherActivity;
import com.pimpimp.passenger.R;
import com.utils.CommonUtilities;
import com.utils.Utils;

/**
 * Created by Admin on 28-06-2016.
 */
public class MyApp extends Application {
    GeneralFunctions generalFun;
    private Activity mCurrentActivity = new LauncherActivity();
    private GpsReceiver mGpsReceiver;
    protected MyApp mMyApp;

    @Override
    public void onCreate() {
        super.onCreate();

        setScreenOrientation();
        mMyApp = (MyApp) this.getApplicationContext();
        Mint.initAndStartSession(this, CommonUtilities.MINT_APP_ID);

        generalFun = new GeneralFunctions(this);

        if (mGpsReceiver == null)
            registerReceiver();

        if (generalFun != null && generalFun.getMemberId() != null) {
            Mint.addExtraData("iMemberId", "" + generalFun.getMemberId());
        }
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mGpsReceiver != null)
            this.unregisterReceiver(mGpsReceiver);
        this.mGpsReceiver = null;
    }

    private void registerReceiver() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

            this.mGpsReceiver = new GpsReceiver();
            this.registerReceiver(this.mGpsReceiver, mIntentFilter);
        }
    }

    public void setScreenOrientation() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                // new activity created; force its orientation to portrait
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                activity.setTitle(getResources().getString(R.string.app_name));
                mMyApp.setCurrentActivity(activity);

                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mMyApp.setCurrentActivity(activity);

            }

            @Override
            public void onActivityResumed(Activity activity) {
                mMyApp.setCurrentActivity(activity);
                if (!activity.getLocalClassName().equalsIgnoreCase("LauncherActivity") && generalFun.isLocationEnabled() == false && generalFun.retrieveValue("isInLauncher").equalsIgnoreCase("false")) {
                    new GeneralFunctions(activity).restartApp();
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                clearReferences();
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                clearReferences();

            }


        });
    }
    private void clearReferences() {
        Activity currActivity = mMyApp.getCurrentActivity();
        if (this.equals(currActivity))
            mMyApp.setCurrentActivity(null);
    }



}
