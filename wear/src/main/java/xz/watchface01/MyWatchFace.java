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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class MyWatchFace extends CanvasWatchFaceService {

    public static final String TAG = "MyWatchFace";
    private static final String TAG_TAP = "Tap";

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = 60000;

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
                watchFace01.updateTime(mTime);
            }
        };


        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(MyWatchFace.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        Suggestion mSuggestionData;
        int mWalkCount = 0;
        int mWorkoutCount = 0;


        boolean mRegisteredTimeZoneReceiver = false;

        WatchFace01 watchFace01;
        Time mTime;
        boolean mAmbient;
        int mSuggestionType;

        float mXOffset;
        float mYOffset;
        RectF mWalkSuggestionTouchArea;
        RectF mPracticeSuggestionTouchArea;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            Log.d("data","Watch face engine created");

            //mSuggestionData = "";
            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = MyWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);


            mTime = new Time();
            watchFace01 = new WatchFace01(getResources());



        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
            Log.d("data", "Watch face engine destroied");
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()){
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            watchFace01.updateTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                watchFace01.setmAmbient(inAmbientMode);
                if (mLowBitAmbient) {
                    //mTextPaint.setAntiAlias(!inAmbientMode);
                    watchFace01.updateTextAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height){
            //mCenterX = width/2f;
            //mCenterY = height/2f;
            //mDetail2TouchOval = new RectF(mCenterX - 40, mCenterY, mCenterX + 40, mCenterY + 110);
            watchFace01.setCenter(width / 2f, height / 2f);
            watchFace01.updateTouchArea();
            mWalkSuggestionTouchArea = watchFace01.getmWalkDetailTouchOval();
            mPracticeSuggestionTouchArea = watchFace01.getmPracticeDetailTouchOvl();
        }


        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Tap Command: " + tapType);
            }

            switch(tapType) {
                case TAP_TYPE_TOUCH:
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    break;
                case TAP_TYPE_TAP:
                    Log.d(TAG_TAP,"Tap detected");
                    // display detail when tapped
                    /*
                    if (!watchFace01.isInDetailMode() && watchFace01.validTouch(x,y)) {
                        invalidate();
                    }else{
                        watchFace01.setDefaultMode();
                        invalidate();
                    }
                    */
                    // start suggestion activity when tapped on walk icon
                    if (mWalkSuggestionTouchArea.contains(x,y) && mWalkCount>0){
                        startSuggestionActivity(SuggestionActivity.SUGGESTION_WALK);
                        break;
                    }
                    // start practice suggestion when tapped on practice icon
                    if (mPracticeSuggestionTouchArea.contains(x,y) && mWorkoutCount>0){
                        startSuggestionActivity(SuggestionActivity.SUGGESTION_PRACTICE);
                    }

                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }

            invalidate();
        }

        private void startSuggestionActivity(int type){
            Log.d("debug","startSuggestionActivity() called");
            Intent intent = new Intent(MyWatchFace.this,SuggestionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(SuggestionActivity.SUGGESTION_TYPE, type);
            bundle.putString(WatchFaceUtil.KEY_SUGGESTION_DATA, mSuggestionData.toString());
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d("debug", "startActivity() called");
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            mTime = watchFace01.draw(canvas,bounds);

        }



        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }


        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void updateConfigDataItemAndUiOnStartup() {
            WatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new WatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            // If the DataItem hasn't been created yet or some keys are missing,
                            // use the default values and upload the defualt to cloud nodes.
                            Log.d("data", "backgroundColor initial dataMap:" + startupConfig.toString());
                            setDefaultValuesForMissingConfigKeys(startupConfig);
                            WatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                            updateUiForConfigDataMap(startupConfig);

                        }
                    }
            );
        }



        private void setDefaultValuesForMissingConfigKeys(DataMap config) {
            addIntKeyIfMissing(config, WatchFaceUtil.KEY_BACKGROUND_COLOR,
                               getResources().getColor(R.color.default_background_color));
            //addIntKeyIfMissing(config, WatchFaceUtil.KEY_BACKGROUND_COLOR,
            //        WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);
            //addIntKeyIfMissing(config, WatchFaceUtil.KEY_HOURS_COLOR,
            //        WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS);
            //addIntKeyIfMissing(config, WatchFaceUtil.KEY_MINUTES_COLOR,
            //        WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
            //addIntKeyIfMissing(config, WatchFaceUtil.KEY_SECONDS_COLOR,
            //        WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_SECOND_DIGITS);
        }


        private void addIntKeyIfMissing(DataMap config, String key, int color) {
            if (!config.containsKey(key)) {
                Log.d(TAG,"addIntKeyIfMissing() called");
                config.putInt(key, color);
            }
        }

        @Override // DataApi.DataListener
        public void onDataChanged(DataEventBuffer dataEvents) {
            Log.d("data", "onDataChanged() called in MyWatchFace.engine");
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                    continue;
                }
                Log.d("data","onDateChanged(): event type is TYPE_CHANGED");

                DataItem dataItem = dataEvent.getDataItem();


                Log.d("data","onDataChanged() path: "+dataItem.getUri().toString());
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap dataMap = dataMapItem.getDataMap();
                Log.d("data","dataMap: "+dataMap.toString());

                if (dataItem.getUri().getPath().equals(WatchFaceUtil.PATH_SUGGESTION_DATA)){
                    if (dataMap.containsKey(WatchFaceUtil.KEY_SUGGESTION_DATA)) {
                        //mSuggestionData = dataMap.getString(WatchFaceUtil.KEY_SUGGESTION_DATA);
                        updateSuggestionData(dataMap);
                        Log.d("data", "dataChanged to: " + mSuggestionData);
                    }
                }


                if (!dataItem.getUri().getPath().equals(
                        WatchFaceUtil.PATH_WITH_FEATURE)) {
                    continue;
                }

                //DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                //DataMap config = dataMapItem.getDataMap();
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Config DataItem updated:" + dataMap);
                }
                updateUiForConfigDataMap(dataMap);
            }
        }

        private void updateUiForConfigDataMap(final DataMap config) {
            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }
                int color = config.getInt(configKey);
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Found watch face config key: " + configKey + " -> "
                            + Integer.toHexString(color));
                }
                if (updateUiForKey(configKey, color)) {
                    uiUpdated = true;
                }
            }
            if (uiUpdated) {
                invalidate();
            }
        }

        /**
         * Updates the color of a UI item according to the given {@code configKey}. Does nothing if
         * {@code configKey} isn't recognized.
         *
         * @return whether UI has been updated
         */
        private boolean updateUiForKey(String configKey, int color) {
            if (configKey.equals(WatchFaceUtil.KEY_BACKGROUND_COLOR)) {
                Log.d(MyWatchFaceConfigListenerService.TAG2,"background color updated");
                //setmBackgroundColor(color);
                watchFace01.updatemBackgroundColor(color);
            }  else {
                Log.w(TAG, "Ignoring unknown config key: " + configKey);
                return false;
            }
            return true;
        }

        /*

        private void setmBackgroundColor (int color){
            mBackgroundColor = color;
            Log.d(TAG, "color changed to: (" + Color.red(mBackgroundColor) + ", " + Color.green(mBackgroundColor) + ", " + Color.blue(mBackgroundColor) + ",)");
        }

        private void updatePaintIfInteractive(Paint paint, int interactiveColor) {
            if (!isInAmbientMode() && paint != null) {
                paint.setColor(interactiveColor);
            }
        }

        private void setInteractiveBackgroundColor(int color) {
            mInteractiveBackgroundColor = color;
            updatePaintIfInteractive(mBackgroundPaint, color);
        }

        */

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onConnected: " + connectionHint);
            }
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            updateConfigDataItemAndUiOnStartup();
            updateSuggestionDataItemOnStartup();
        }

        private void updateSuggestionDataItemOnStartup(){

            WatchFaceUtil.fetchSuggestionDataMap(mGoogleApiClient,
                    new WatchFaceUtil.FetchSuggestionDataMapCallback() {
                        @Override
                        public void onSuggestionDataMapFetched(DataMap suggestionDataMap) {
                            Log.d("WDT","dataMap fetched");
                            updateSuggestionData(suggestionDataMap);
                        }
                    }
            );


        }

        private void updateSuggestionData(DataMap dataMap){
            String str = dataMap.getString(WatchFaceUtil.KEY_SUGGESTION_DATA);
            if (str==null) {
                Log.d("WDT","null dataMap; watchface not updated");
            }else {
                mSuggestionData = new Suggestion(str);
                mWalkCount = mSuggestionData.getWalkCount();
                watchFace01.setWalkCount(mWalkCount);
                mWorkoutCount = mSuggestionData.getWorkoutCount();
                watchFace01.setPracticeCount(mWorkoutCount);
                Log.d("WDT", "watch face updated: " + mSuggestionData.toString());
                Log.d("WDT", "walkCount: "+mWalkCount+" workoutCount"+mWorkoutCount);
                invalidate();
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




    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }




}
