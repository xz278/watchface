
/**
 * Created by Xian on 10/24/2015.
 */
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xz.watchface01;

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link WearableListenerService} listening for {@link MyWatchFace} config messages
 * and updating the config {@link com.google.android.gms.wearable.DataItem} accordingly.
 */
public class MyWatchFaceConfigListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "DigitalListenerService";
    public static final String TAG2 = "WatchFaceConfig";

    private GoogleApiClient mGoogleApiClient;

    @Override // WearableListenerService
    public void onMessageReceived(MessageEvent messageEvent) {
        if (!messageEvent.getPath().equals(WatchFaceUtil.PATH_WITH_FEATURE)) {
            return;
        }else {
            Log.d(TAG2,"MyWatchFaceConfigListenerService received message");
        }
        byte[] rawData = messageEvent.getData();
        // It's allowed that the message carries only some of the keys used in the config DataItem
        // and skips the ones that we don't want to change.
        DataMap configKeysToOverwrite = DataMap.fromByteArray(rawData);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Received watch face config message: " + configKeysToOverwrite);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(Wearable.API).build();
        }
        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult =
                    mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
                return;
            }
        }

        WatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }
    }

    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override  // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }

    @Override // Watch face listener
    public void onDataChanged(DataEventBuffer dataEvents){
        Log.d("data","Listener onDataChanged() called");
        for (DataEvent dataEvent:dataEvents){
            if (dataEvent.getType() != DataEvent.TYPE_CHANGED) continue;
            DataItem dataItem = dataEvent.getDataItem();
            if (!dataItem.getUri().getPath().equals(WatchFaceUtil.PATH_SUGGESTION_DATA)) continue;

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this).addApi(Wearable.API).build();
            }
            if (!mGoogleApiClient.isConnected()) {
                ConnectionResult connectionResult =
                        mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

                if (!connectionResult.isSuccess()) {
                    Log.e(TAG, "Failed to connect to GoogleApiClient.");
                    return;
                }
            }


            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap dataMap = dataMapItem.getDataMap();
            Log.d("data", "dataMap: "+dataMap.toString());
            WatchFaceUtil.overwriteSuggestionData(mGoogleApiClient,dataMap);

        }
;    }
}
