package com.abhinav.CFM.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by clicklabs on 1/7/14.
 */


import com.abhinav.CFM.services.MyUSSDTest;

public class BootCompleteRecv extends BroadcastReceiver {
    private String TAG = BootCompleteRecv.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "rcvd boot event, launching service");
        Intent srvIntent = new Intent(context, MyUSSDTest.class);
        context.startService(srvIntent);
    }

}
