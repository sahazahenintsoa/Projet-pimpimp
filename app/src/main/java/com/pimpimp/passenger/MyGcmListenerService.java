package com.pimpimp.passenger;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.utils.CommonUtilities;

public class MyGcmListenerService extends GcmListenerService {

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs. For
     *             Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        Intent intent_broad = new Intent(CommonUtilities.driver_message_arrived_intent_action);
        intent_broad.putExtra(CommonUtilities.driver_message_arrived_intent_key, message);
        this.sendBroadcast(intent_broad);


    }


}
