package com.example.willwallis.demowatchdata;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class MainActivity extends AppCompatActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public final String LOG_TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private static final String DATA_PATH = "/weather";
    private static final String KEY_ONE = "high";
    private int counter = 0;
    private boolean useIntent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Button Clicked", Toast.LENGTH_SHORT).show();
                //      Using Activity Class
                //      OR Using IntentService
                if (useIntent) {
                    Intent sendIntent = new Intent(MainActivity.this, SendDataService.class);
                    sendIntent.putExtra("Counter", counter);
                    startService(sendIntent);
                    counter++;
                }
                else {
                    sendWatchData();
                }
            }
        });

        if(!useIntent) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    public void sendWatchData() {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DATA_PATH);

        String valueOne = "Testing Text: " + counter;
        counter++;
        putDataMapRequest.getDataMap().putLong("Time", System.currentTimeMillis()); // Added for testing
        putDataMapRequest.getDataMap().putString(KEY_ONE, valueOne);
        putDataMapRequest.setUrgent();

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();

        Log.v(LOG_TAG, "Generating DataItem: " + request);
        if (!mGoogleApiClient.isConnected()) {
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


    @Override
    public void onConnected(Bundle bundle) {
        Log.v(LOG_TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.v(LOG_TAG, "onDataChangedinMobile");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(LOG_TAG, "onConnectionFailed");
    }
}
