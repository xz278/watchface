package xz.watchface01;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Random;
import java.util.zip.Inflater;

/**
 * Created by Xian on 10/28/2015.
 */
public class SuggestionActivity extends Activity
    implements DataApi.DataListener
    , GoogleApiClient.ConnectionCallbacks
    , GoogleApiClient.OnConnectionFailedListener{

    /*
    Key used to retrieve the type of the activity, whether walk suggestions or practice suggestions.
     */
    public final static String SUGGESTION_TYPE = "suggestion_type";
    public final static int SUGGESTION_WALK = 0;
    public final static int SUGGESTION_PRACTICE = 1;

    //data
    private int[] mCalorieCounts;
    private String[] mLocations;
    private String[] mSuggestions;
    private String[] mSuggestionTime;
    private BroadcastReceiver mTickReceiver;
    private GridPagerAdapter mAdapter;
    private int type;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        // retrieve the type of the suggestion to display in this activity
        type = bundle.getInt(SUGGESTION_TYPE);



        // retrieve the data to display
        //String suggestionStr = bundle.getString(WatchFaceUtil.KEY_SUGGESTION_DATA);
        //Log.d("data","suggestionActivity: data Changed to: "+suggestionStr);
        String data = bundle.getString(WatchFaceUtil.KEY_SUGGESTION_DATA);
        Log.d("debug","new suggestion activity created");
        Suggestion s = new Suggestion(data);

        if (type==SUGGESTION_WALK)  s.formWalkSuggestion();
        else s.formWorkoutSuggestion();

        int size = s.getCount();
        mCalorieCounts = s.getCalories();
        mSuggestionTime = s.getSuggestions();
        mLocations = s.getLocations();
        mSuggestions = s.getTypes();



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        // initiate data
        /*
        mCalorieCounts = new Integer[2];
        mCalorieCounts[0] = 21;
        mCalorieCounts[1] = 13;

        mSuggestions[0] = "Walk faster";
        mSuggestions[1] = "Walk faster";

        mLocations[0] = "Gates Hall";
        mLocations[1] = "Gates Hall";

        mSuggestionTime[0] = "Walk 5 min";
        mSuggestionTime[1] = "Walk 5 min";
        */

        // parse JSON input to strings
        /*
        if (suggestionStr!=null && suggestionStr.length()!=0) {
            try {
                JSONObject jsonObject = new JSONObject(suggestionStr);
                mLocations[0] = jsonObject.getString("location");
                Log.d("data","json location: "+mLocations[0]);
                mSuggestions[0] = jsonObject.getString("suggestion");
                Log.d("data","json suggestion: "+mSuggestions[0]);
                mCalorieCounts[0] = jsonObject.getInt("calorie");
                Log.d("data","json calorie: "+mCalorieCounts[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        */



        // generate random data to display in graph view
        Float[] list = new Float[13];
        Random random = new Random();
        for (int i=0;i<=12;i++){
            list[i]=random.nextFloat()*99f+1f;
        }

        // set view
        setContentView(R.layout.activity_suggestion);

        // set pager and indicator
        GridViewPager pager = (GridViewPager) findViewById(R.id.pager_suggestion);
        mAdapter =new SuggestionAdapter(this,mCalorieCounts,mLocations,
                                        mSuggestions,mSuggestionTime,type, list);
        pager.setAdapter(mAdapter);
        //pager.setAdapter(new SuggestionAdapter(this));

        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator)findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);
        Log.d("debug", "adapter set");
        mAdapter.notifyDataSetChanged();

        /*
        BroadcastReceiver used to update the display time every minute.
         */
        mTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK)==0){
                    mAdapter.notifyDataSetChanged();
                }
            }
        };

        registerReceiver(mTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mTickReceiver!=null) unregisterReceiver(mTickReceiver);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause(){
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient,this);
        mGoogleApiClient.disconnect();
    }

    @Override //GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint){
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }


    @Override //DataApi.DataListener
    public void onDataChanged(DataEventBuffer dataEvents){
        /*
        for (DataEvent dataEvent:dataEvents){
            if (dataEvent.getType() != DataEvent.TYPE_CHANGED) continue;

            DataItem dataItem = dataEvent.getDataItem();

            if (!dataItem.getUri().getPath().equals(WatchFaceUtil.PATH_SUGGESTION_DATA)) continue;

            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap dataMap = dataMapItem.getDataMap();

            if (dataMap.containsKey(WatchFaceUtil.KEY_SUGGESTION_DATA)){
                String data = dataMap.getString(WatchFaceUtil.KEY_SUGGESTION_DATA);

                if (data!=null && data.length()!=0) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        mLocations[0] = jsonObject.getString("location");
                        Log.d("data","json location: "+mLocations[0]);
                        mSuggestions[0] = jsonObject.getString("suggestion");
                        Log.d("data","json suggestion: "+mSuggestions[0]);
                        mCalorieCounts[0] = jsonObject.getInt("calorie");
                        Log.d("data","json calorie: "+mCalorieCounts[0]);
                        mAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        */
    }

    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
    }

    @Override  // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
    }


    /*
    Adapter for the gridPagerView.
     */
    private class SuggestionAdapter extends GridPagerAdapter{
        Context mContext;
        LayoutInflater mInflator;
        private int[] mCalorieCounts;
        private String[] mLocations;
        private String[] mSuggestions;
        private String[] mSuggestionTime;
        private int mSuggestionType;
        private Float[] mGraphData;

        public SuggestionAdapter(Context context){
            mContext = context;
            mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public SuggestionAdapter(Context context,int[] c, String[] l, String[] s, String[] t,int sType,
                                    Float[] data){
            mSuggestionType = sType;
            mContext = context;
            mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mCalorieCounts = c;
            mLocations = l;
            mSuggestions = s;
            mSuggestionTime = t;
            mGraphData = data;
        }

        @Override
        public int getRowCount(){
            return mCalorieCounts.length;
        }

        @Override
        public int getColumnCount(int i){
            return 2;
        }

        @Override
        public int getCurrentColumnForRow(int row, int currentColumn){
            return 0;
        }


        @Override
        /*
        Column 0 displays detail, Column 1 display the graph.
        Different rows display different suggestions.
         */
        public Object instantiateItem(ViewGroup viewGroup, int row, int col){

            FrameLayout view;
            Log.d("debug", "instantiateItem() called");
            if (col==0) { // show suggestion detail in SuggestionView at column 0
                view = (FrameLayout)mInflator.inflate(R.layout.page_view_suggestion,
                                                        viewGroup,
                                                        false);
                Log.d("debug", "custom layout inflated");
                SuggestionView suggestionView = (SuggestionView) view.findViewById(R.id.custom_suggestion_view);
                suggestionView.setType(mSuggestionType);
                suggestionView.setCalorie(mCalorieCounts[row]);
                suggestionView.setLocation(mLocations[row]);
                suggestionView.setWalkSuggestionStr(mSuggestions[row]);
                suggestionView.setmWalkTimeStr(mSuggestionTime[row]);
            }else{ // show data in graph view at column 1
                view = (FrameLayout)mInflator.inflate(R.layout.graph_suggestion,
                        viewGroup,
                        false);
                Log.d("debug", "custom layout inflated");
                GraphView graphView = (GraphView) view.findViewById(R.id.custom_graph_view);
                graphView.setGraphData(mGraphData);
                Log.d("debug","list length: "+mGraphData.length);
                //graphView.setType(mSuggestionType);


            }
            viewGroup.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup viewGroup, int i,int i2, Object o){
            viewGroup.removeView((View) o);
        }

        @Override
        public boolean isViewFromObject(View view, Object o){
            return view.equals(o);
        }


    }



}
