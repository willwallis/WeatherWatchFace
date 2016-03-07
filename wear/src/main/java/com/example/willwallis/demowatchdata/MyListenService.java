package com.example.willwallis.demowatchdata;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class MyListenService extends WearableListenerService {
    public final String LOG_TAG = MyListenService.class.getSimpleName();
    private static final String DATA_PATH = "/weather";
    private static final String KEY_ONE = "high";


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v(LOG_TAG,"Wearable onDataChanged");
        for(DataEvent dataEvent : dataEvents){
            Log.v(LOG_TAG,"In for Loop");
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED){
                Log.v(LOG_TAG,"Data is TYPE_CHANGED");
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
                Log.v(LOG_TAG,"The path is: " + path);
                if (path.equals(DATA_PATH)){
                    String theMessage = dataMap.getString(KEY_ONE);
                    Log.v(LOG_TAG, theMessage);
                    // Send data to UI
                    //Intent intent = new Intent("update-main-activity");
                    Intent intent = new Intent("update-watch-face");
                    intent.putExtra("message", theMessage);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            }
        }
    }
}
