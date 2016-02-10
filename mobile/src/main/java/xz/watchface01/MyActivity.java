package xz.watchface01;

import android.app.Activity;
import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xian on 10/27/2015.
 * Handheld app that is used to send custom suggestions to wearable app.
 */
public class MyActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = "SendDataActivity";
    private static final String PATH_SUGGESTION_DATA = "/wear_data";
    private static final String KEY_SUGGESTION_DATA = "suggestion_data";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        /*
        // Send notification
        Button mSendNotificationButton = (Button) findViewById(R.id.send_notification_button);
        mSendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Notification notification = new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Hello World")
                        .setContentText("Hello world")
                        .extend(
                                new  NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                        .build();

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplication());

                int notificationId = 1;
                notificationManagerCompat.notify(notificationId, notification);
            }
        });
        */

        final EditText mEditTextCalorie = (EditText)findViewById(R.id.data_calorie_data);
        final EditText mEditTextLocation = (EditText)findViewById(R.id.data_location);
        final EditText mEditTextSuggestion = (EditText)findViewById(R.id.data_suggestion);

        /* Send user input data to wearable
         */
        Button mSendDataBtn = (Button)findViewById(R.id.button_send_data);
        mSendDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dataToSend = generateDataToSend(mEditTextCalorie,mEditTextLocation,mEditTextSuggestion);
                if (dataToSend.length()!=0) {
                    uploadData(dataToSend);
                    mEditTextCalorie.setText("");
                    mEditTextLocation.setText("");
                    mEditTextSuggestion.setText("");
                }
                //Log.d("data", "onClicked called; data: " + dataToSend);
            }
        });
    } // onCreate()

    /*
    Upload user input data to wearable node API
     */
    public void uploadData(String dataStr){
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(MyActivity.PATH_SUGGESTION_DATA);
        putDataMapRequest.getDataMap().putString(KEY_SUGGESTION_DATA,dataStr);
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();

        PendingResult<NodeApi.GetConnectedNodesResult> pendingResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        pendingResult.setResultCallback(new updateDataItemCallBack(mGoogleApiClient,putDataRequest));

        /*NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        for (Node node:nodes.getNodes()) {
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (dataItemResult.getStatus().isSuccess()) {
                                Log.d("data", "data sent");
                            } else {
                                Log.d("data", "data not sent");
                            }
                        }
                    });
        }*/
    }

    /*
    Parse user input data into JSON format
     */
    private String generateDataToSend(EditText calorie, EditText location, EditText suggestion){
        int c = Integer.parseInt(calorie.getText().toString());
        String l = location.getText().toString();
        String s = suggestion.getText().toString();
        String dataToSend = "";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("calorie",c);
            jsonObject.put("location",l);
            jsonObject.put("suggestion",s);
            dataToSend = jsonObject.toString();
            Log.d("data",dataToSend);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return dataToSend;
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint){
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause){
    }

    @Override // GoogleApi.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result){

    }

    /*
    Callback method to upload data when get connected nodes got called.
     */
    private static class updateDataItemCallBack
            implements ResultCallback<NodeApi.GetConnectedNodesResult>{
        private final PutDataRequest putDataRequest;
        private final GoogleApiClient googleApiClient;

        public updateDataItemCallBack(GoogleApiClient googleApiClient, PutDataRequest putDataRequest){
            this.putDataRequest = putDataRequest;
            this.googleApiClient = googleApiClient;
        }

        @Override
        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
            if (getConnectedNodesResult.getNodes().size()==0){
                Log.d("data","no existing nodes");
                return;
            }
            for (final Node node:getConnectedNodesResult.getNodes()) {
                Wearable.DataApi.putDataItem(googleApiClient, putDataRequest)
                        .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                if (dataItemResult.getStatus().isSuccess()) {
                                    Log.d("data", "data sent to node: " + node.getDisplayName());
                                    Log.d("data","MyActivity/putItemDataResult: "+dataItemResult.getStatus());
                                } else {
                                    Log.d("data", "data not sent");
                                }
                            }
                        });
            }
        }
    }

}
