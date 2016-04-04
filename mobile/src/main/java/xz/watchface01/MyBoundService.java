package xz.watchface01;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Xian on 4/2/2016.
 */
public class MyBoundService extends Service{

    public static boolean isRuning;
    private final IBinder myBinder=new MyLocalBinder();

    public MyBoundService(){};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        Log.d("Bound Service","onBind() called");
        return myBinder;
    }

    public class MyLocalBinder extends Binder{
        MyBoundService getService(){
            return MyBoundService.this;
        }
    }

    public String getCurrentTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
        return dateFormat.format(new Date());
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d("Bound Service","onUnbind() called");
        return true;
    }
}
