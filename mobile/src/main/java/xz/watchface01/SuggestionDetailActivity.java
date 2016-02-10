package xz.watchface01;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by xz278 on 1/27/16.
 */
public class SuggestionDetailActivity extends Activity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private static final String PATH_SUGGESTION_DATA = "/wear_data";
    private static final String KEY_SUGGESTION_DATA = "suggestion_data";
    private String detail;
    private UUID uuid;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion_detail);

        TextView cal = (TextView)findViewById(R.id.suggestion_detail_calorie);
        TextView loc = (TextView)findViewById(R.id.suggestion_detail_location);
        TextView suggestion = (TextView)findViewById(R.id.suggestion_detail_suggestion);
        Intent intent = getIntent();
        detail = intent.getStringExtra(ListViewActivity.SUGGESTION_DETAIL);
        try{
            JSONObject jsonObject = new JSONObject(detail);
            uuid = UUID.fromString(jsonObject.getString("id"));
            cal.setText((String)jsonObject.getString("calorie"));
            loc.setText((String)jsonObject.getString("location"));
            suggestion.setText((String)jsonObject.getString("suggestion"));
        }catch (JSONException e){
            e.printStackTrace();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        Button sendBtn = (Button)findViewById(R.id.button_send_data);
        Button removeBtn = (Button)findViewById(R.id.button_remove_data);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DT","add-btn clicked");
                fetchSuggestionDataMap(mGoogleApiClient,
                        new FetchSuggestionDataMapCallback() {
                            @Override
                            public void onSuggestionDataMapFetched(DataMap dataMap) {
                                String str = dataMap.getString(KEY_SUGGESTION_DATA);
                                Log.d("DT","DataMap fetched");
                                if (str == null) {
                                    Suggestion data = new Suggestion();
                                    data.add(detail);
                                    uploadData(data.toString());
                                    Log.d("DT","first data uploaded "+data.toString());
                                } else {
                                    Suggestion data = new Suggestion(str);
                                    if (!data.contains(uuid)) {
                                        data.add(detail);
                                        uploadData(data.toString());
                                        Log.d("DT", "data uploaded " + data.toString());
                                    }else {
                                        Log.d("DT", "data repeated " + data.toString());
                                    }
                                }
                            }
                        });
            }
        });
        removeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                fetchSuggestionDataMap(mGoogleApiClient,
                        new FetchSuggestionDataMapCallback() {
                            @Override
                            public void onSuggestionDataMapFetched(DataMap dataMap) {
                                String str = dataMap.getString(KEY_SUGGESTION_DATA);
                                if (str==null) return;
                                Suggestion data = new Suggestion(str);
                                if (data.contains(uuid)) {
                                    data.remove(detail);
                                    uploadData(data.toString());
                                }
                            }
                        });
            }
        });
    }


    public static void fetchSuggestionDataMap(final GoogleApiClient client,
                                              final FetchSuggestionDataMapCallback callback){
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        String localNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .path(PATH_SUGGESTION_DATA)
                                .authority(localNode)
                                .build();
                        Wearable.DataApi.getDataItem(client,uri)
                                .setResultCallback(new SuggestionDataItemResultCallback(callback));
                    }
                }
        );
    }

    private static class SuggestionDataItemResultCallback implements
            ResultCallback<DataApi.DataItemResult>{

        private final FetchSuggestionDataMapCallback mCallback;

        public SuggestionDataItemResultCallback(FetchSuggestionDataMapCallback callback){
            mCallback = callback;
        }

        @Override
        public void onResult(DataApi.DataItemResult dataItemResult){
            if (dataItemResult.getStatus().isSuccess()){
                if (dataItemResult.getDataItem() != null){
                    DataItem suggestionDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(suggestionDataItem);
                    DataMap data = dataMapItem.getDataMap();
                    mCallback.onSuggestionDataMapFetched(data);
                }else{
                    mCallback.onSuggestionDataMapFetched(new DataMap());
                }
            }
        }
    }

    public interface FetchSuggestionDataMapCallback{
        void onSuggestionDataMapFetched(DataMap suggestionDataMap);
    }

    private void uploadData(String dataToUpload){
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SuggestionDetailActivity.PATH_SUGGESTION_DATA);
        putDataMapRequest.getDataMap().putString(KEY_SUGGESTION_DATA,dataToUpload);
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();

        PendingResult<NodeApi.GetConnectedNodesResult> pendingResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        pendingResult.setResultCallback(new updateDataItemCallBack(mGoogleApiClient,putDataRequest));
    }


    /*
Callback method to upload data when get connected nodes got called.
 */
    private static class updateDataItemCallBack
            implements ResultCallback<NodeApi.GetConnectedNodesResult> {
        private final PutDataRequest putDataRequest;
        private final GoogleApiClient googleApiClient;

        public updateDataItemCallBack(GoogleApiClient googleApiClient, PutDataRequest putDataRequest){
            this.putDataRequest = putDataRequest;
            this.googleApiClient = googleApiClient;
        }

        @Override
        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
            if (getConnectedNodesResult.getNodes().size()==0){
                //Log.d("data", "no existing nodes");
                return;
            }
            for (final Node node:getConnectedNodesResult.getNodes()) {
                Wearable.DataApi.putDataItem(googleApiClient, putDataRequest)
                        .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                //if (dataItemResult.getStatus().isSuccess()) {
                                //    Log.d("data", "data sent to node: " + node.getDisplayName());
                                //    Log.d("data","MyActivity/putItemDataResult: "+dataItemResult.getStatus());
                                //} else {
                                //    Log.d("data", "data not sent");
                                //}
                            }
                        });
            }
        }
    }

    @Override
    protected  void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
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



}
