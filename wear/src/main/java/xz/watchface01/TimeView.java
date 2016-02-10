package xz.watchface01;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Xian on 10/30/2015.
 */
public class TimeView extends View {

    private float mCenterX;
    private float mCenterY;
    private Paint mTimePaint;
    private String mTimeStr;
    private Resources mRes;
    private Paint mBackgroundPaint;
    private RectF mBounds;
    private Time mTime;

    public TimeView(Context context, AttributeSet attrs){
        super(context, attrs);
        mRes = context.getResources();
        mTimePaint = new Paint();
        mTimePaint.setTextAlign(Paint.Align.CENTER);
        mTimePaint.setColor(Color.WHITE);
        mTimePaint.setTextSize(mRes.getDimension(R.dimen.digital_text_size_round));
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setARGB(0, 0, 0, 0);
        mBounds = new RectF();
        mTime = new Time();
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();

    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        mBounds.set(0, 0, w, h);
        mCenterX = w/2f;
        mCenterY = h/2f;
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawRect(mBounds,mBackgroundPaint);
        mTime.setToNow();
        mTimeStr = String.format("%d:%02d", mTime.hour, mTime.minute);
        canvas.drawText(mTimeStr, mCenterX, mCenterY*0.25f, mTimePaint);
    }

}
