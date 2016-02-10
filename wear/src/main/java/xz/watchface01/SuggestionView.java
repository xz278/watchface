package xz.watchface01;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.jar.Attributes;

/**
 * Created by Xian on 10/29/2015.
 * A custom view to display detail information for a given walk/practice suggestion
 */
public class SuggestionView extends View {


    private int type;


    private RectF mBounds;
    private RectF mPositionOval;

    private float mCenterX;
    private float mCenterY;

    private Paint mBackgroundPaint;
    private Paint mProcessRingPaint;
    private Paint mForegroundPaint;
    Paint mDetailWalkLeftNumberPaint;
    Paint mDetailWalkLeftUnitPaint;
    Paint mDetailWalkLocationPaint;
    Paint mDetailWalkSuggestionPaint;
    Paint mDetailWalkTimePaint;

    private Bitmap mMainIconBitmap;
    Bitmap mCalorieBitmap;
    Bitmap mLocationBitmap;

    private Resources mResources;
    private int Type;
    private float mProgress;

    // data
    private int mCalInt;
    private String mWalkLocationStr;
    private String mWalkSuggestionStr;
    private String mWalkTimeStr;
    private String mUnitStr;

    public SuggestionView(Context context, AttributeSet attrs){
        super(context, attrs);
        mResources = context.getResources();
        mPositionOval = new RectF();

        type = SuggestionActivity.SUGGESTION_WALK; // default type

        // background
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mResources.getColor(R.color.suggestion_walk_background_color));

        // ring
        mProcessRingPaint = new Paint();
        mProcessRingPaint.setARGB(130, 255, 255, 255);
        mProcessRingPaint.setStyle(Paint.Style.STROKE);
        mProcessRingPaint.setStrokeWidth(4);
        mProgress = 0.8f;

        // foreground
        mForegroundPaint = new Paint();
        mForegroundPaint.setARGB(255, 233, 233, 233);
        mForegroundPaint.setStyle(Paint.Style.FILL);

        // main icon bitmap
        mMainIconBitmap = BitmapFactory.decodeResource(mResources,R.drawable.walk_100);

        // location and calorie drawables
        mCalorieBitmap = BitmapFactory.decodeResource(mResources, R.drawable.wal_detail_cal_50);
        mLocationBitmap = BitmapFactory.decodeResource(mResources, R.drawable.wal_detail_loc_50);

        // details
        mDetailWalkLeftNumberPaint = new Paint();
        mDetailWalkLeftNumberPaint.setTextSize(mResources.getDimension(R.dimen.watch_face01_walk_detail_left_number));
        mDetailWalkLeftNumberPaint.setTextAlign(Paint.Align.RIGHT);
        mUnitStr = "c/walk";
        mDetailWalkLeftUnitPaint = new Paint();
        mDetailWalkLeftUnitPaint.setTextSize(mResources.getDimension(R.dimen.watch_face01_walk_detail_left_unit));
        mDetailWalkLeftUnitPaint.setTextAlign(Paint.Align.LEFT);
        mDetailWalkLocationPaint = new Paint();
        mDetailWalkLocationPaint.setTextSize(mResources.getDimension(R.dimen.watch_face01_walk_detail_location));
        mDetailWalkSuggestionPaint = new Paint();
        mDetailWalkSuggestionPaint.setTextSize(mResources.getDimension(R.dimen.watch_face01_walk_detail_suggestion));
        mDetailWalkTimePaint = new Paint();
        mDetailWalkTimePaint.setTextSize(mResources.getDimension(R.dimen.watch_face01_walk_detail_time));
        int DetailWalkColor =mResources.getColor(R.color.watch_face01_walk_detail_left_color);
        mDetailWalkLeftNumberPaint.setColor(DetailWalkColor);
        mDetailWalkLeftUnitPaint.setColor(DetailWalkColor);
        mDetailWalkSuggestionPaint.setColor(DetailWalkColor);
        mDetailWalkLocationPaint.setColor(DetailWalkColor);
        mDetailWalkTimePaint.setARGB(100,0,0,0);

        mDetailWalkLocationPaint.setTextAlign(Paint.Align.LEFT);
        mDetailWalkSuggestionPaint.setTextAlign(Paint.Align.LEFT);
        mDetailWalkTimePaint.setTextAlign(Paint.Align.LEFT);
    }


    @Override
    protected void onDraw(Canvas canvas){
        // background
        canvas.drawRect(mBounds, mBackgroundPaint);

        // foreground
        canvas.drawRect(new RectF(0, mBounds.centerY() * 0.78f, mBounds.width(), mBounds.height()), mForegroundPaint);

        // ring
        canvas.drawArc(new RectF(mBounds.width() * 0.04f, mBounds.height() * 0.04f,
                        mBounds.width() * 0.96f, mBounds.height() * 0.96f),
                -90, 360 * mProgress, false, mProcessRingPaint);

        // main icon drawable 100*100
        mPositionOval.set(mCenterX - 100 / 2, mCenterY - 29 - 100, mCenterX + 100 / 2, mCenterY - 29);
        canvas.drawBitmap(mMainIconBitmap, null, mPositionOval, null);

        // calorie and location drawable
        mPositionOval.set(mCenterX/2f-25,mCenterY-30,mCenterX/2f+25,mCenterY-30+50);
        canvas.drawBitmap(mCalorieBitmap, null, mPositionOval, null);
        mPositionOval.set(mCenterX + mCenterX / 2f - 25, mCenterY - 30, mCenterX + mCenterX / 2f + 25, mCenterY - 30 + 50);
        canvas.drawBitmap(mLocationBitmap, null, mPositionOval, null);

        // text on the left
        canvas.drawText("" + mCalInt, mCenterX / 2f + 5, mCenterY - 5 + 50 + 20, mDetailWalkLeftNumberPaint);
        canvas.drawText(mUnitStr, mCenterX / 2f + 5, mCenterY - 5 + 50 + 20, mDetailWalkLeftUnitPaint);
        // text on the right
        // location
        canvas.drawText(mWalkLocationStr, mCenterX + 20, mCenterY - 5 + 50, mDetailWalkLocationPaint);
        // suggestion
        canvas.drawText(mWalkSuggestionStr, mCenterX + 20, mCenterY - 5 + 50 + 20, mDetailWalkSuggestionPaint);
        // time
        canvas.drawText(mWalkTimeStr, mCenterX + 20, mCenterY - 5 + 50 + 40, mDetailWalkTimePaint);


    }

    // set suggestion detail
    public void setCalorie(int calorie){
        mCalInt = calorie;
    }

    public void setLocation(String location){
        mWalkLocationStr = location;
    }

    public void setWalkSuggestionStr(String suggestionStr){
        mWalkSuggestionStr = suggestionStr;
    }

    public void setmWalkTimeStr(String timeStr){
        mWalkTimeStr = timeStr;
    }

    public void setType(int t){
        type = t;
        if (type==SuggestionActivity.SUGGESTION_WALK) {
            mMainIconBitmap = BitmapFactory.decodeResource(mResources, R.drawable.walk_100);
            mBackgroundPaint.setColor(mResources.getColor(R.color.suggestion_walk_background_color));
            mUnitStr = "c/walk";
        } else {
            mMainIconBitmap = BitmapFactory.decodeResource(mResources, R.drawable.practice_100);
            mBackgroundPaint.setColor(mResources.getColor(R.color.suggestion_practice_background_color));
            mUnitStr = "cal";
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        mBounds = new RectF(0,0,w,h);
        mCenterX = w/2f;
        mCenterY = h/2f;
    }


}
