package com.general.files;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pimpimp.passenger.MainActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Admin on 05-10-2016.
 */
public class ConfigPubNub {
    Context mContext;
    PubNub pubnub;

    GeneralFunctions generalFunc;

    public ConfigPubNub(Context mContext) {
        this.mContext = mContext;
        generalFunc = new GeneralFunctions(mContext);

        PNConfiguration pnConfiguration = new PNConfiguration();
        Utils.printLog("SUB KEy:","::"+generalFunc.retrieveValue(CommonUtilities.PUBNUB_SUB_KEY));
        Utils.printLog("PUB KEy:","::"+generalFunc.retrieveValue(CommonUtilities.PUBNUB_PUB_KEY));
        Utils.printLog("SEC KEy:","::"+generalFunc.retrieveValue(CommonUtilities.PUBNUB_SEC_KEY));
        pnConfiguration.setUuid(generalFunc.retrieveValue(CommonUtilities.DEVICE_SESSION_ID_KEY).equals("") ? generalFunc.getMemberId() : generalFunc.retrieveValue(CommonUtilities.DEVICE_SESSION_ID_KEY));

        pnConfiguration.setSubscribeKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_SUB_KEY));
        pnConfiguration.setPublishKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_PUB_KEY));
        pnConfiguration.setSecretKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_SEC_KEY));
        pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);

        pubnub = new PubNub(pnConfiguration);
        pubnub.addListener(subscribeCallback);

        subscribeToPrivateChannel();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pubnub.reconnect();
            }
        }, 10000);
    }

    public void subscribeToPrivateChannel() {
        pubnub.subscribe()
                .channels(Arrays.asList("PASSENGER_" + generalFunc.getMemberId())) // subscribe to channels
                .execute();
    }

    public void unSubscribeToPrivateChannel() {
        pubnub.unsubscribe()
                .channels(Arrays.asList("PASSENGER_" + generalFunc.getMemberId())) // subscribe to channels
                .execute();
    }

    public void releaseInstances() {
        pubnub.removeListener(subscribeCallback);
    }

    public void subscribeToChannels(ArrayList<String> channels) {
        pubnub.subscribe()
                .channels(channels) // subscribe to channels
                .execute();
    }

    public void unSubscribeToChannels(ArrayList<String> channels) {
        pubnub.unsubscribe()
                .channels(channels)
                .execute();
    }

    SubscribeCallback subscribeCallback = new SubscribeCallback() {
        @Override
        public void status(final PubNub pubnub, final PNStatus status) {
            // the status object returned is always related to subscribe but could contain
            // information about subscribe, heartbeat, or errors
            // use the operationType to switch on different options
            if (status == null || status.getOperation() == null) {
                Log.e("status operation", ":::re connected::" + status.toString());
                pubnub.reconnect();
                return;
            }

            switch (status.getOperation()) {
                // let's combine unsubscribe and subscribe handling for ease of use
                case PNSubscribeOperation:
                case PNUnsubscribeOperation:
                    // note: subscribe statuses never have traditional
                    // errors, they just have categories to represent the
                    // different issues or successes that occur as part of subscribe
                    switch (status.getCategory()) {
                        case PNConnectedCategory:
                            // this is expected for a subscribe, this means there is no error or issue whatsoever
                            Log.e("PNConnectedCategory", ":::connected::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Connected);
                            }
                        case PNReconnectedCategory:
                            // this usually occurs if subscribe temporarily fails but reconnects. This means
                            // there was an error but there is no longer any issue
                            Log.e("PNReconnectedCategory", ":::re connected::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Connected);
                            }
                        case PNDisconnectedCategory:
                            // this is the expected category for an unsubscribe. This means there
                            // was no error in unsubscribing from everything
//                            Log.e("PNDisconnectedCategory", ":::dis connected::" + status.toString());
//                            if(mContext instanceof MainActivity){
//                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_DisConnected);
//                            }
                        case PNTimeoutCategory:
                        case PNUnexpectedDisconnectCategory:
                            // this is usually an issue with the internet connection, this is an error, handle appropriately
                            // retry will be called automatically
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pubnub.reconnect();
                                }
                            }, 1500);

                            Log.e("PNUnexpectedDisconnect", ":::dis unexpected::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_DisConnected);
                            }
                        case PNAccessDeniedCategory:
                            // this means that PAM does allow this client to subscribe to this
                            // channel and channel group configuration. This is another explicit error
//                            Log.e("AccessDenied", ":::denied::" + status.toString());
//                            if(mContext instanceof MainActivity){
//                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Denied);
//                            }
                        default:
                            // More errors can be directly specified by creating explicit cases for other
                            // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                            Log.e("Default", ":::default::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Error_Connection);
                            }
                    }

                case PNHeartbeatOperation:
                    // heartbeat operations can in fact have errors, so it is important to check first for an error.
                    // For more information on how to configure heartbeat notifications through the status
                    // PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
                    if (status.isError()) {
                        // There was an error with the heartbeat operation, handle here
                        Log.e("PNHeartbeatOperation", ":::failed::" + status.toString());
                    } else {
                        // heartbeat operation was successful
                        Log.e("PNHeartbeatOperation", ":::success::" + status.toString());
                    }
                default: {
                    // Encountered unknown status type
                    Log.e("unknown status", ":::unknown::" + status.toString());
                }
            }
        }

        @Override
        public void message(PubNub pubnub, PNMessageResult message) {
            // handle incoming messages
            Log.e("ON message", ":::got::" + message.getMessage().toString());
            if (mContext instanceof MainActivity) {
                // message.getMessage() Must be a json
                String msg = message.getMessage().toString().replace("\\\"", "\"");
                if (msg.length() > 2) {

                    if (isJsonObj( message.getMessage().toString())) {
                        ((MainActivity) mContext).pubNubMsgArrived( message.getMessage().toString());
                        return;
                    }

                    final String finalMsg = msg.substring(1, msg.length() - 1);

                    if (isJsonObj(finalMsg)) {
                        ((MainActivity) mContext).pubNubMsgArrived(finalMsg);
                    }

                }

            }
        }

        @Override
        public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            // handle incoming presence data
            Log.e("ON presence", ":::got::" + presence.toString());
        }
    };

    public boolean isJsonObj(String json) {

        try {
            JSONObject obj_check = new JSONObject(json);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void publishMsg(String channel, String message) {
        pubnub.publish()
                .message(message)
                .channel(channel)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                        Log.e("Publish Res", "::::" + result.getTimetoken());
                    }
                });
    }
}
