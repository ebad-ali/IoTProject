package com.example.ebadali.iotproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class ServiceAutoStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ServiceAutoStarter", "Service Started Again");
        context.startService(new Intent(context, EmergencyService.class));
    }
}
