package com.JohnZero.lock.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.JohnZero.lock.entity.AppInfo;
import com.JohnZero.lock.AppInfoList;
import com.JohnZero.lock.sqlite.MyDBOpenHelper;
import com.JohnZero.lock.util.Utils;
import com.JohnZero.lock.activity.MainActivity;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author: JohnZero
 * @date: 2020-09-11
 **/
public class MyService extends Service {
    public static String saveName = "com.JohnZero.lock";
    public static String str = null;
    public static int count = 0;
    public static int hour=0;  //服务启动时间
    public static int minute=0;
    public static int second=0;
    public static boolean isScreenOn = true;
    Context mContext;
    SQLiteDatabase db;
    MyDBOpenHelper myDBOpenHelper;
    private PowerManager.WakeLock mWakeLock = null;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        mContext = this;
        myDBOpenHelper = new MyDBOpenHelper(mContext, "appList.db", null, 1);
        db = myDBOpenHelper.getWritableDatabase();

        Utils.getTime();
        hour=Utils.hour;minute=Utils.minute;second=Utils.second;
        new Timer().schedule(new TimerTask() {
            @SuppressLint("MissingPermission")
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                Utils.sendNotification(mContext,"服务",Utils.serviceTime(),1);
                boolean isAccessible = Utils.requestAccessibility(mContext);
                if (str != null) {
                    //Utils.log("当前应用：" + str);
                    int isLock = AppInfoList.isLockItem(str);
                    if (!str.equals(saveName)) {
                        int isLock1 = 0;
                        if (isLock != 2 && isChild(saveName, str))
                            isLock1 = AppInfoList.isLockItem(saveName);
                        if (isLock == 2 || isLock1 == 2)
                            sendBroadcast(new Intent("com.JohnZero.lock.Lock"));
                        if (isLock1 != 1) saveName = str;
                        //使用日志
                        AppInfo appInfo = findItem(saveName);
                        if (appInfo != null) {
                            if (AppInfoList.usageList.size() == 0)
                                AppInfoList.usageList.add(appInfo.getAppName());
                            else {
                                String appName = appInfo.getAppName();
                                if (!AppInfoList.usageList.get(0).equals(appName)) {
                                    if (AppInfoList.usageList.size() == 50)
                                        AppInfoList.usageList.remove(49);
                                    AppInfoList.usageList.add(0, appName);
                                }
                            }
                        }
                    } else if (isLock == 2) sendBroadcast(new Intent("com.JohnZero.lock.Lock"));
                } else {
                    if(!MainActivity.onDestroy){
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        Looper.prepare();
                        Toast.makeText(mContext, "请重启辅助功能", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
                count++;
                sendBroadcast(new Intent("com.JohnZero.lock.service.MyService"));
            }
        }, 0, 1000);

    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("tag", "MyService Destroy");
        Utils.sendNotification(mContext,"Lock","服务已停止",2);
        sendBroadcast(new Intent("com.JohnZero.lock.MyServiceDestroy"));
        stopForeground(true);
        super.onDestroy();
    }

    AppInfo findItem(String packageName) {
        AppInfoList.findItem1(packageName);
        if (AppInfoList.index == -1) return null;
        AppInfo appInfo = new AppInfo();
        if (AppInfoList.type == 1) appInfo = AppInfoList.appList1.get(AppInfoList.index);
        if (AppInfoList.type == 2) appInfo = AppInfoList.appList2.get(AppInfoList.index);
        if (AppInfoList.type == 3) appInfo = AppInfoList.appList3.get(AppInfoList.index);
        return appInfo;
    }

    boolean isChild(String save, String str) {
        boolean isLock = false;
        boolean isChild = false;
        AppInfoList.findItem1(save);
        if (AppInfoList.type == 1) isLock = true;
        AppInfo appInfo = findItem(str);//appInfo!=null
        for (int i = 0; i < AppInfoList.childList.size(); i++) {
            if (AppInfoList.childList.get(i).equals(appInfo.getAppName())) {
                isChild = true;
                break;
            }
        }
        return isLock && isChild;
    }

}
