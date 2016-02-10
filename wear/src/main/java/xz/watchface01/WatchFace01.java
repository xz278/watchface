package xz.watchface01;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorRes;
import android.text.format.Time;

/**
 * Created by Xian on 10/25/2015.
 */
public class WatchFace01 {
    int mType;
    Resources resources;
    Paint mBackgroundPaint;
    Paint mInnerCirclePaint;
    Paint mOuterCirclePaint;
    Paint mInnerArcPaint;
    Paint mOuterArcPaint;
    Paint mTextPaint;
    Paint mCalPaint;
    Paint mCalUnitPaint;
    Paint mCountPaint;
    Paint mIconPaint;
    Paint mGraphPaint;
    Paint mForegroundPaint;
    Paint mWalkProgressPaint;
    Paint mDetailWalkLeftNumberPaint;
    Paint mDetailWalkLeftUnitPaint;
    Paint mDetailWalkLocationPaint;
    Paint mDetailWalkSuggestionPaint;
    Paint mDetailWalkTimePaint;

    int mDetailWalkColor;;
    int mBackgroundColor;
    RectF mIconOval;
    RectF mPracticeDetailTouchOvl;
    RectF mWalkDetailTouchOval;
    RectF mGoalDetailTouchOval;

    boolean mAmbient;
    boolean mTapped;
    boolean mDetailMode;
    int mDetailPos;

    Time mTime;

    float mCenterX;
    float mCenterY;
    Bitmap mPracticeIconBitmap;
    Bitmap mWalkIconBitmap;
    Bitmap mGoalIconBitmap;
    Bitmap mWalkDetailBitmap;
    Bitmap mWalkDetailCalBitmap;
    Bitmap mWalkDetailLocBitmap;
    Bitmap mWalkDetailLocAmbientBitmap;
    Bitmap mWalkDetailCalAmbientBitmap;

    // suggestion data counts
    int mWalkCount = 0;
    int mPracticeCount = 0;
    int mGoalCount = 0;


    public WatchFace01(Resources res){
        mType = 1;
        resources = res;

        //default background color
        mBackgroundColor = resources.getColor(R.color.default_background_color);

        mBackgroundPaint = new Paint();
        mForegroundPaint = new Paint();
        mForegroundPaint.setARGB(255, 233, 233, 233);
        mForegroundPaint.setStyle(Paint.Style.FILL);

        mWalkProgressPaint = new Paint();
        mWalkProgressPaint.setARGB(130, 255, 255, 255);
        mWalkProgressPaint.setStyle(Paint.Style.STROKE);
        mWalkProgressPaint.setStrokeWidth(4);
        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setARGB(180, 255, 255, 255);
        mInnerCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint = new Paint();
        mOuterCirclePaint.setARGB(100, 255, 255, 255);
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mInnerArcPaint = new Paint();
        mInnerArcPaint.setARGB(150, 255, 255, 255);
        mInnerArcPaint.setStyle(Paint.Style.FILL);
        mOuterArcPaint = new Paint();
        mOuterArcPaint.setARGB(200, 255, 255, 255);
        mOuterArcPaint.setStyle(Paint.Style.FILL);
        mDetailWalkLeftNumberPaint = new Paint();
        mDetailWalkLeftNumberPaint.setTextSize(resources.getDimension(R.dimen.watch_face01_walk_detail_left_number));
        mDetailWalkLeftNumberPaint.setTextAlign(Paint.Align.RIGHT);
        mDetailWalkLeftUnitPaint = new Paint();
        mDetailWalkLeftUnitPaint.setTextSize(resources.getDimension(R.dimen.watch_face01_walk_detail_left_unit));
        mDetailWalkLeftUnitPaint.setTextAlign(Paint.Align.LEFT);
        mDetailWalkLocationPaint = new Paint();
        mDetailWalkLocationPaint.setTextSize(resources.getDimension(R.dimen.watch_face01_walk_detail_location));
        mDetailWalkSuggestionPaint = new Paint();
        mDetailWalkSuggestionPaint.setTextSize(resources.getDimension(R.dimen.watch_face01_walk_detail_suggestion));
        mDetailWalkTimePaint = new Paint();
        mDetailWalkTimePaint.setTextSize(resources.getDimension(R.dimen.watch_face01_walk_detail_time));
        mDetailWalkColor =resources.getColor(R.color.watch_face01_walk_detail_left_color);
        /*
        mDetailWalkLeftNumberPaint.setColor(mDetailWalkColor);
        mDetailWalkLeftUnitPaint.setColor(mDetailWalkColor);
        mDetailWalkSuggestionPaint.setColor(mDetailWalkColor);
        mDetailWalkLocationPaint.setColor(mDetailWalkColor);
        mDetailWalkTimePaint.setColor(mDetailWalkColor);
        */
        mDetailWalkLocationPaint.setTextAlign(Paint.Align.LEFT);
        mDetailWalkSuggestionPaint.setTextAlign(Paint.Align.LEFT);
        mDetailWalkTimePaint.setTextAlign(Paint.Align.LEFT);



        mIconPaint = new Paint();
        mIconPaint.setARGB(140, 255, 255, 255);
        mIconOval = new RectF();

        mGraphPaint = new Paint();
        mGraphPaint.setColor(Color.parseColor("white"));


        mTextPaint = new Paint();
        //mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));
        mTextPaint.setARGB(255, 255, 255, 255);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mCalPaint = new Paint();
        mCalPaint.setARGB(255, 255, 255, 255);
        mCalPaint.setTextAlign(Paint.Align.CENTER);
        mCalPaint.setTextSize(resources.getDimension(R.dimen.cal));

        mCalUnitPaint = new Paint();
        mCalUnitPaint.setARGB(255, 255, 255, 255);
        mCalUnitPaint.setTextAlign(Paint.Align.LEFT);
        mCalUnitPaint.setTextSize(resources.getDimension(R.dimen.cal_unit));

        mPracticeIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_practice_54);
        mWalkIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_walk_54);
        mGoalIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_goal_54);
        mWalkDetailBitmap = BitmapFactory.decodeResource(resources, R.drawable.walk_78);
        mWalkDetailCalBitmap = BitmapFactory.decodeResource(resources, R.drawable.wal_detail_cal_50);
        mWalkDetailLocBitmap = BitmapFactory.decodeResource(resources, R.drawable.wal_detail_loc_50);
        mWalkDetailLocAmbientBitmap = BitmapFactory.decodeResource(resources, R.drawable.wal_detail_loc_50_ambient);
        mWalkDetailCalAmbientBitmap = BitmapFactory.decodeResource(resources, R.drawable.wal_detail_cal_50_ambient);

        mCountPaint = new Paint();
        mCountPaint.setARGB(255, 255, 255, 255);
        mCountPaint.setTextSize(resources.getDimension(R.dimen.count));
        mCountPaint.setTextAlign(Paint.Align.CENTER);

        mDetailMode = false;
        mDetailPos = 0;
        mTime = new Time();
    }

    public void setWalkCount(int count){
        mWalkCount = count;
    }

    public void setPracticeCount(int count){
        mPracticeCount = count;
    }

    public void updatemBackgroundColor(int color){
        mBackgroundColor = color;
    }

    public void setCenter(float x, float y){
        mCenterX = x;
        mCenterY = y;
    }

    public void updateTouchArea(){
        float margin = mCenterX/4f;
        if (mType==1) {
            mWalkDetailTouchOval = new RectF(mCenterX/2f-margin,mCenterY,mCenterX/2f+margin,mCenterY+110);
            mPracticeDetailTouchOvl = new RectF(mCenterX - margin, mCenterY, mCenterX + margin, mCenterY + 110);
            mGoalDetailTouchOval = new RectF(mCenterX+mCenterX/2f-margin,mCenterX,mCenterX+mCenterX/2f+margin,mCenterY+110);
        }
    }

    public RectF getmWalkDetailTouchOval(){
        return mWalkDetailTouchOval;
    }

    public RectF getmPracticeDetailTouchOvl(){ return mPracticeDetailTouchOvl;}


    public void setmAmbient(boolean ambient){
        mAmbient = ambient;
    }

    public void updateTextSize(float textSize){
        mTextPaint.setTextSize(textSize);
    }

    public void updateTextAntiAlias(boolean antiAlias){
        mTextPaint.setAntiAlias(antiAlias);
    }

    public boolean isInDetailMode(){
        return mDetailMode;
    }

    public void setDefaultMode(){
        mDetailMode = false;
        mDetailPos = 0;
    }

    public boolean validTouch(int x, int y){
        if (mType==1) {
            if (mWalkDetailTouchOval.contains(x, y)) {
                mDetailPos = 1;
                mDetailMode = true;
            }
            if (mPracticeDetailTouchOvl.contains(x, y)) {
                mDetailPos = 2;
                mDetailMode = true;
            }
            if (mGoalDetailTouchOval.contains(x, y)) {
                mDetailPos = 3;
                mDetailMode = true;
            }
        }
        return mDetailMode;
    }

    public void updateTime(Time time){
        mTime = time;
    }


    public Time draw(Canvas canvas, Rect bounds){

        //if (!mDetailMode){
            // Custom round background
            RectF[] sizes = drawBackground(canvas, bounds);

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            drawTextTime(canvas, bounds);
            int hour = mTime.hour>12 ? mTime.hour-12+1 : mTime.hour+1;
            int minute = mTime.minute % 5 == 0 ? mTime.minute / 5 : mTime.minute/5+1;
            drawArcShape(canvas, hour, 1, sizes);
            drawArcShape(canvas, minute, 0, sizes);
            //draw Calorie
            drawCal(canvas, bounds);

            // draw event counts
            drawWalkCount(canvas,bounds);
            drawPracticeCount(canvas, bounds);
            drawGoalCount(canvas,bounds);

        /*
        }else {
            switch (mDetailPos){
                case 1:
                    drawDetailSuggestion(canvas,bounds);



                    break;
                case 2:
                    drawDetailGraph(canvas,bounds);
                    break;
                case 3:
                    drawDetailGraph(canvas,bounds);
                    break;
                default:
                    break;
            }
        }*/
        return mTime;
    }

    private void drawDetailSuggestion(Canvas canvas, Rect bounds){
        if (mAmbient) {
            mBackgroundPaint.setARGB(255, 0, 0, 0); //black background in ambient mode
        } else {
            mBackgroundPaint.setColor(mBackgroundColor);
        }
        // paint background
        canvas.drawRect(new Rect(0, 0, bounds.width(), bounds.height()), mBackgroundPaint);
        // paint text time
        drawTextTime(canvas, bounds);
        // paint foreground
        if (!mAmbient) {
            canvas.drawRect(new RectF(0, bounds.centerY() * 0.78f, bounds.width(), bounds.height()), mForegroundPaint);
        }
        // ring
        // ring
        int minute = mTime.minute;
        float position = (minute)/60f;
        canvas.drawArc(new RectF(bounds.width() * 0.04f, bounds.height() * 0.04f, bounds.width() * 0.96f, bounds.height() * 0.96f),
                -90, 360 * position, false, mWalkProgressPaint);
        //walk icon: 78*78
        mIconOval.set(mCenterX-78/2,mCenterY-40-78,mCenterX+78/2,mCenterY-40);
        canvas.drawBitmap(mWalkDetailBitmap, null, mIconOval, null);


        // left icon: 50*50
        mIconOval.set(mCenterX/2f-25,mCenterY-30,mCenterX/2f+25,mCenterY-30+50);
        if (!mAmbient) {
            canvas.drawBitmap(mWalkDetailCalBitmap, null, mIconOval, null);
        }else{
            canvas.drawBitmap(mWalkDetailCalAmbientBitmap, null, mIconOval, null);
        }
        // right icon: 50*50
        mIconOval.set(mCenterX + mCenterX / 2f - 25, mCenterY - 30, mCenterX + mCenterX / 2f + 25, mCenterY - 30 + 50);
        if (!mAmbient) {
            canvas.drawBitmap(mWalkDetailLocBitmap, null, mIconOval, null);
        }else{
            canvas.drawBitmap(mWalkDetailLocAmbientBitmap, null, mIconOval, null);
        }

        if (mAmbient){
            mDetailWalkLeftNumberPaint.setColor(Color.WHITE);
            mDetailWalkLeftUnitPaint.setColor(Color.WHITE);
            mDetailWalkSuggestionPaint.setColor(Color.WHITE);
            mDetailWalkLocationPaint.setColor(Color.WHITE);
            mDetailWalkTimePaint.setColor(Color.WHITE);
        }else{
            mDetailWalkLeftNumberPaint.setColor(mDetailWalkColor);
            mDetailWalkLeftUnitPaint.setColor(mDetailWalkColor);
            mDetailWalkSuggestionPaint.setColor(mDetailWalkColor);
            mDetailWalkLocationPaint.setColor(mDetailWalkColor);
            mDetailWalkTimePaint.setColor(mDetailWalkColor);
        }
        // text on the left
        canvas.drawText(""+21,mCenterX/2f+5,mCenterY-5+50+20,mDetailWalkLeftNumberPaint);
        canvas.drawText("c/walk",mCenterX/2f+5,mCenterY-5+50+20,mDetailWalkLeftUnitPaint);
        // text on the right
        // location
        canvas.drawText("Graham Rd",mCenterX+20,mCenterY-5+50,mDetailWalkLocationPaint);
        // suggestion
        canvas.drawText("Walk Faster",mCenterX+20,mCenterY-5+50+20,mDetailWalkSuggestionPaint);
        // time
        canvas.drawText("Each walk 3 min",mCenterX+20,mCenterY-5+50+40,mDetailWalkTimePaint);
    }

    private void drawDetailGraph(Canvas canvas, Rect bounds){
        drawBackgoundDetial(canvas, bounds);
        drawGraph(canvas, bounds);
        //draw icon2
        mIconOval.set(mCenterX - 27, mCenterY + 50, mCenterX + 27, mCenterY + 104);
        canvas.drawBitmap(mPracticeIconBitmap, null, mIconOval, null); // width of the icon is 54

        mIconOval.set(mCenterX - 14, mCenterY + 64 + 50, mCenterX + 14, mCenterY + 64 + 28 + 50); // width:28
        drawCountBackground(canvas, mIconOval);

        canvas.drawText("" + mPracticeCount, mCenterX, mCenterY + 64 + 28 - 6 + 50, mCountPaint);
    }

    private void drawCountBackground(Canvas canvas,RectF oval){
        // Fill (in interactive mode)
        if(!mAmbient) {
            mIconPaint.setStrokeWidth(1);
            mIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawOval(oval, mIconPaint);
        }

        // Stroke
        mIconPaint.setStyle(Paint.Style.STROKE);
        mIconPaint.setStrokeWidth(2);
        canvas.drawOval(oval, mIconPaint);
    }

    // draw walk icon
    private void drawWalkCount(Canvas canvas, Rect bounds){
        // bitmap size: 54*54
        mIconOval.set(mCenterX / 2f - 27, mCenterY, mCenterX / 2 + 27, mCenterY + 54);

        canvas.drawBitmap(mWalkIconBitmap, null, mIconOval, null);

        // size: 28
        mIconOval.set(mCenterX / 2f - 14, mCenterY + 64, mCenterX / 2 + 14, mCenterY + 64 + 28); // width:32
        // background
        drawCountBackground(canvas, mIconOval);
        // text
        canvas.drawText("" + mWalkCount, mCenterX / 2f, mCenterY + 64 + 28 - 6, mCountPaint);

    }

    // draw event count2
    private void drawPracticeCount(Canvas canvas, Rect bounds){
        //draw icon2
        mIconOval.set(mCenterX - 27, mCenterY, mCenterX + 27, mCenterY + 54);
        canvas.drawBitmap(mPracticeIconBitmap, null, mIconOval, null); // width of the icon is 64

        mIconOval.set(mCenterX - 14, mCenterY + 64, mCenterX + 14, mCenterY + 64 + 28); // width:32
        drawCountBackground(canvas, mIconOval);

        canvas.drawText(""+mPracticeCount,mCenterX,mCenterY+64+28-6,mCountPaint);
    }

    private void drawGoalCount(Canvas canvas, Rect bounds) {
        //draw icon
        mIconOval.set(mCenterX + mCenterX / 2f - 27, mCenterY, mCenterX + mCenterX / 2f + 27, mCenterY + 54);
        canvas.drawBitmap(mGoalIconBitmap, null, mIconOval, null); // width of the icon is 64

        canvas.drawText(mGoalCount+" cal",mCenterX+mCenterX/2f,mCenterY+64+28-6,mCountPaint);
    }

    // draw the calorie count
    private void drawCal(Canvas canvas, Rect bounds){
        String cal = Integer.toString(246);
        canvas.drawText(cal, mCenterX, mCenterY - 20, mCalPaint);
        canvas.drawText("cal", mCenterX + 60, mCenterY - 20, mCalUnitPaint);
    }

    private void drawTextTime(Canvas canvas, Rect bounds){
        mTime.setToNow();
        //String text = mAmbient
        //        ? String.format("%d:%02d", mTime.hour, mTime.minute)
        //        : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
        //canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
        String textTime = String.format("%d:%02d", mTime.hour, mTime.minute);
        canvas.drawText(textTime, mCenterX, mCenterY*0.25f, mTextPaint);
    }

    private void drawGraph(Canvas canvas, Rect bounds){
        float r = (float) mCenterX * 0.9f;
        float xA = (float)Math.cos(Math.PI*5/6);
        float x = xA*r+mCenterX;
        float yA = -(float)Math.sin(Math.PI*5/6);
        float y = yA*r+mCenterY;
        float w = Math.abs(mCenterX-x)*2;
        float h = Math.abs(mCenterY-y)*2;
        float[] xx = new float[6];
        xx[0] = x;
        xx[1] = x+w*0.2f;
        xx[2] = x+w*0.4f;
        xx[3] = x+w*0.6f;
        xx[4] = x+w*0.8f;
        xx[5] = x+w;
        float[] yy = new float[6];
        yy[0] = y+h;
        yy[1] = y+h*0.3f;
        yy[2] = y+h*0.6f;
        yy[3] = y;
        yy[4] = y+h*0.8f;
        yy[5] = y+h*0.1f;
        Path path = new Path();
        path.moveTo(xx[0], yy[0]);
        for (int i=1;i<6;i++){
            path.lineTo(xx[i],yy[i]);
        }
        mGraphPaint.setStrokeWidth(2);
        mGraphPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path,mGraphPaint);


    }


    private void drawBackgoundDetial(Canvas canvas, Rect bounds){
        if (mAmbient){
            mBackgroundPaint.setARGB(255, 0, 0, 0);
        } else{
            mBackgroundPaint.setColor(mBackgroundColor);
        }
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
    }

    private RectF[] drawBackground(Canvas canvas, Rect bounds){
        // Draw the background.
        if (mAmbient) {
            mBackgroundPaint.setARGB(255, 0, 0, 0); //black background in ambient mode
        } else {
            mBackgroundPaint.setColor(mBackgroundColor);
            //Log.d(TAG,"current mBackgroundColor: (" + Color.red(mBackgroundColor) + ", " + Color.green(mBackgroundColor) + ", " + Color.blue(mBackgroundColor) + ")");
        }
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

        //if (!mAmbient){
        mCenterX = (float)bounds.centerX();
        mCenterY = (float)bounds.centerY();
        //inner circle
        float r1 = bounds.width()*0.92f/2f;
        float m1 = mCenterX - r1;
        RectF oval1 = new RectF(m1,m1,m1+2f*r1,m1+2f*r1);
        canvas.drawOval(oval1, mInnerCirclePaint);

        //outer circle
        float r2 = bounds.width()*0.96f/2f;
        float m2 = mCenterX - r2;
        RectF oval2 = new RectF(m2,m2,m2+2f*r2,m2+2f*r2);
        canvas.drawOval(oval2,mOuterCirclePaint);

        //add ticks
        float innerTickRadius = r1;
        float outerTickRadius = mCenterX;
        for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
            float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
            float innerX = (float) Math.sin(tickRot) * innerTickRadius;
            float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
            float outerX = (float) Math.sin(tickRot) * outerTickRadius;
            float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
            canvas.drawLine(mCenterX + innerX, mCenterY + innerY,
                    mCenterX + outerX, mCenterY + outerY, mInnerCirclePaint);
        }
        //}
        RectF[] rectFs = new RectF[3];
        rectFs[0] = oval1;
        rectFs[1] = oval2;
        rectFs[2] = new RectF(0,0,bounds.width(),bounds.height());
        return rectFs;
    }

    // position: 0 for inner circle, 1 for outer circle
    private void drawArcShape(Canvas canvas,int position, int type, RectF[] sizes){
        position = position-3>0 ? position-3 : position-3+12;
        RectF innerSize;
        RectF outerSize;
        Path path = new Path();
        float startAngle = (position-1)*30;
        float sweepAngle = 30;
        if (type==0){
            innerSize = sizes[0];
            outerSize = sizes[1];
            path.arcTo(innerSize,startAngle,sweepAngle);
            path.arcTo(outerSize,startAngle + sweepAngle,-sweepAngle);
            path.close();
            canvas.drawPath(path,mInnerArcPaint);
        } else {
            innerSize = sizes[1];
            outerSize = sizes[2];
            path.arcTo(innerSize,startAngle,sweepAngle);
            path.arcTo(outerSize,startAngle + sweepAngle,-sweepAngle);
            path.close();
            canvas.drawPath(path, mOuterArcPaint);
        }
    }


    private RectF getPosition(float centerX, float centerY, float width, float height){
        return new RectF(centerX-width/2f, centerY-height/2f,
                         centerX+width/2f, centerY+height/2f);
    }



}
