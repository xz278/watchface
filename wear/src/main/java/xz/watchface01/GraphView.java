package xz.watchface01;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;

import java.util.LinkedList;

/**
 * Created by Xian on 10/30/2015.
 * An view object used to display the graph on watch app
 */
public class GraphView extends View {

    private int type;
    private float mProgress;

    private Resources mResources;


    // Graphics variable
    private RectF mBounds;
    private RectF mPositionOval;
    private float mCenterX;
    private float mCenterY;
    private Paint mBackgroundPaint;
    private Paint mProcessRingPaint;
    private Paint mForegroundPaint;
    private Float[] mDataList;
    private Float[] mBarHeight;
    private Float[] mBarXCoordinates;
    private Paint mGraphBarPaint;
    private float mBarWidth;
    private float mOriginX;
    private float mOriginY;
    private float mBarInterval;
    private float mBarMaxHeight;


    public GraphView(Context context, AttributeSet attrs){
        super(context,attrs);
        mResources = context.getResources();
        mPositionOval = new RectF();

        type = SuggestionActivity.SUGGESTION_WALK; // default type

        // background
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mResources.getColor(R.color.graph_background_color));

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

        // graph
        mGraphBarPaint = new Paint();
        mGraphBarPaint.setColor(mResources.getColor(R.color.graph_bar_gray));
        mGraphBarPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas){

        drawBackground(canvas);
        drawGraph(canvas);

    }

    private void drawGraph(Canvas canvas){
        for (int i=0;i<mDataList.length;i++){
            drawBar(canvas, mBarXCoordinates[i], mBarHeight[i]);
            //Log.d("debug","x: " + mBarXCoordinates[i] + ", " + mBarHeight[i]);
        }
    }

    private void drawBar(Canvas canvas, Float xCoordinate, Float height){
        mPositionOval.set(mOriginX+xCoordinate,mOriginY-height,mOriginX+xCoordinate+mBarWidth,mOriginY);
        Log.d("debug", (mOriginX + xCoordinate) + ", " + (mOriginY-height) + ", " + (mOriginX+xCoordinate+mBarWidth) + ", " + mOriginY);
        canvas.drawRect(mPositionOval, mGraphBarPaint);
    }

    private void drawBackground(Canvas canvas){
        // background
        canvas.drawRect(mBounds, mBackgroundPaint);

        // ring
        canvas.drawArc(new RectF(mBounds.width() * 0.04f, mBounds.height() * 0.04f,
                        mBounds.width() * 0.96f, mBounds.height() * 0.96f),
                -90, 360 * mProgress, false, mProcessRingPaint);

        // foreground
        mForegroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(0, mBounds.height() * 0.16f, mBounds.width(), mBounds.height() * 0.84f), mForegroundPaint);
        mForegroundPaint.setStyle(Paint.Style.STROKE);
        mForegroundPaint.setStrokeWidth(2);
        mForegroundPaint.setARGB(50, 0, 0, 0);
        canvas.drawRect(new RectF(0, mBounds.height()*0.16f-1, mBounds.width(), mBounds.height()*0.84f+1), mForegroundPaint);
        Log.d("debug", "before drawBar()");
    }

    private float getOriginX(){
        return mCenterX - (float)Math.sqrt( Math.pow(mBounds.width()/2f,2) - Math.pow(mCenterY-mBounds.height()*0.16f,2) );
    }

    private float getOriginY(){
        return mBounds.height() * 0.84f;
    }

    private float getBarWidth(){
        return mBounds.width()*0.85f*4f/7f/mDataList.length;
    }

    private float getBarInterval(){
        return mBarWidth*3f/4f;
    }

    private float getMaximunBarHeight(RectF bounds){
        return bounds.height()*0.68f*0.85f;
    }

    private Float[] getBarHeight(){
        mBarMaxHeight = getMaximunBarHeight(mBounds);
        int l = mDataList.length;
        Float[] heights = new Float[l];
        float max = 0;
        for (int i=0;i<l;i++){
            if (mDataList[i]>max) max = mDataList[i];
        }
        for (int i=0;i<l;i++){
            heights[i]=mBarMaxHeight*mDataList[i]/max;
        }
        return heights;
    }

    // x coordinates relative to x coordinate of Origin X
    private Float[] getBarXCoordinates(){
        Float[] coordinates = new Float[mDataList.length];
        for (int i=0;i<mDataList.length;i++){
            coordinates[i] = (mBarWidth+mBarInterval)*i;
        }


        return coordinates;
    }


    /*
    Add data to the graph view
     */
    public void setGraphData(Float[] data){
        mDataList = data;
    }


    private void setType(int t){
        type = t;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        mBounds = new RectF(0,0,w,h);
        mCenterX = w/2f;
        mCenterY = h/2f;


        mOriginX = getOriginX();
        mOriginY = getOriginY();
        mBarWidth = getBarWidth();
        mBarInterval = getBarInterval();
        Log.d("debug","width: " + mBarWidth + ", interval:" + mBarInterval);
        mBarHeight = getBarHeight();
        mBarXCoordinates = getBarXCoordinates();
    }


}
