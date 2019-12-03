package com.general.files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pimpimp.passenger.MainActivity;
import com.utils.CommonUtilities;

/**
 * Created by Admin on 12-07-2016.
 */
public class GcmBroadCastReceiver extends BroadcastReceiver {
    MainActivity mainAct;

    public GcmBroadCastReceiver(MainActivity mainAct) {
        this.mainAct = mainAct;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(CommonUtilities.driver_message_arrived_intent_action)) {
            String message = intent.getExtras().getString(CommonUtilities.driver_message_arrived_intent_key);
            mainAct.onGcmMessageArrived(message);
        }
    }
}
