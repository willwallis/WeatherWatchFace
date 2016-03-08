package com.example.android.sunshine.app;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class SendDataService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public final String LOG_TAG = SendDataService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private static final String DATA_PATH = "/weather";
    private static final String KEY_ONE = "weatherId";
    private static final String KEY_TWO = "high";
    private static final String KEY_THREE = "low";
    private int weatherId = 0;
    private double high = 0;
    private double low = 0;

    public SendDataService() {
        super("SendDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        weatherId = intent.getIntExtra(KEY_ONE, 0);
        high = intent.getDoubleExtra(KEY_TWO, 7);
        low = intent.getDoubleExtra(KEY_THREE, 8);
        mGoogleApiClient = new GoogleApiClient.Builder(SendDataService.this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendWatchData();
        Log.v(LOG_TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(LOG_TAG, "onConnectionFailed");
    }

    // Send the Message
    public void sendWatchData() {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DATA_PATH);
        putDataMapRequest.getDataMap().putLong("Time", System.currentTimeMillis()); // Added for testing
        putDataMapRequest.getDataMap().putInt(KEY_ONE, weatherId);
        putDataMapRequest.getDataMap().putDouble(KEY_TWO, high);
        putDataMapRequest.getDataMap().putDouble(KEY_THREE, low);
        putDataMapRequest.setUrgent();

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();

        Log.v(LOG_TAG, "Generating DataItem: " + request);
        if (!mGoogleApiClient.isConnected()) {
            Log.v(LOG_TAG, "Api client not connected");
            return;
        }
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e(LOG_TAG, "Failed to send data to wearable");
                        } else {
                            Log.d(LOG_TAG, "Success in sending data to wearable");
                        }
                    }
                });
    }
}
