package com.JohnZero.lock.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.JohnZero.lock.AppInfoList;
import com.JohnZero.lock.activity.MainActivity;
import com.JohnZero.lock.entity.AppInfo;
import com.JohnZero.lock.entity.PasswordInfo;
import com.JohnZero.lock.service.DetectService;
import com.JohnZero.lock.service.MyService;
import com.JohnZero.lock.service.NotificationService;
import com.JohnZero.lock.sqlite.MyDBOpenHelper;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import static android.service.notification.NotificationListenerService.requestRebind;

/**
 * @author: JohnZero
 * @date: 2020-09-07
 **/
public class Utils {
    private static final String TAG = "江湖";
    public static int DAY = 0;
    public static int HOUR = 23;
    public static int MINUTE = 30;
    public static String PERIOD = "更改时段：" + HOUR + ":" + (MINUTE > 9 ? MINUTE : "0" + MINUTE) + "-00:00";
    public static int day = 0;
    public static int hour = 0;
    public static int minute = 0;
    public static int second = 0;
    public static long current = 0;
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    public static int mode=Context.MODE_MULTI_PROCESS;

    public static void log(String str) {
        Log.d(TAG, str);
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void toastSync(Context context, String msg) {
        Looper.prepare();
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    public static void getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
    }

    public static boolean compareTime() {
        if(MainActivity.isAdmin) return true;
        getTime();
        return hour > HOUR || (hour == HOUR && minute >= MINUTE);
    }

    public static void runtime() {
        long currentTime = System.currentTimeMillis();
        log(currentTime - current + "");
        current = currentTime;
    }

    public static String serviceTime() {
        getTime();
        if (hour < MyService.hour && hour == 0) {
            MyService.hour = 0;
            MyService.minute = 0;
            MyService.second = 0;
        } else {
            hour -= MyService.hour;
        }
        if (minute < MyService.minute) {
            hour--;
            minute += 60;
        }
        minute -= MyService.minute;
        if (second < MyService.second) {
            minute--;
            second += 60;
        }
        second -= MyService.second;
        String text = "已运行" + (hour > 0 ? hour + "时" : "") + (minute > 0 ? minute + "分" : "") + (second > 0 ? second + "秒" : "")+(AppInfoList.isCount?" 正在计时":"");
        return text;
    }

    Comparator<AppInfo> comparator = new Comparator<AppInfo>() {
        @Override
        public int compare(AppInfo a, AppInfo b) {
            if (a.getAppName().compareToIgnoreCase(b.getAppName()) > 0)
                return 1; //a排在b后面
            return -1; //b排在a后面
        }
    };

    public void sortAppList(ArrayList<AppInfo> appList) {
        Collections.sort(appList, comparator);
    }

    //初始化数据
    public static void initData(Context context, SQLiteDatabase db) {
        AppInfoList.getAppList(db);
        AppInfoList.updateList(context.getPackageManager());
        context.startService(new Intent(context, MyService.class));
        SharedPreferences sharedPreferences = context.getSharedPreferences("Lock", mode);
        int day = sharedPreferences.getInt("day", 0);
        int hour = sharedPreferences.getInt("hour", 23);
        int minute = sharedPreferences.getInt("minute", 30);
        DAY = day;
        HOUR = hour;
        MINUTE = minute;
        PERIOD = "更改时段：" + hour + ":" + (minute > 9 ? minute : "0" + minute) + "-00:00";
        updateChildList(context);
    }

    public static void clearData(Context context, SQLiteDatabase db) {
        LitePal.deleteAll(PasswordInfo.class);

        db.execSQL("drop table appList1");
        db.execSQL("drop table appList2");
        db.execSQL("drop table appList3");
//        db.execSQL("drop table childList");
        MyDBOpenHelper.createTable(db);
        AppInfoList.appList1.clear();
        AppInfoList.appList2.clear();
        AppInfoList.appList3.clear();
        AppInfoList.childList.clear();
        AppInfoList.updateList(context.getPackageManager());
        updateTime(context, 23, 30);
    }

    public static void updateTime(Context context, int hour0, int minute0) {
        HOUR = hour0;
        MINUTE = minute0;
        PERIOD = "更改时段：" + hour0 + ":" + (minute0 > 9 ? minute0 : "0" + minute0) + "-00:00";
        SharedPreferences sharedPreferences = context.getSharedPreferences("Lock", mode);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("hour", hour0);
        editor.putInt("minute", minute0);
        editor.commit();
    }

    public static void updateDay(Context context, int day0) {
        DAY = day0;
        SharedPreferences sharedPreferences = context.getSharedPreferences("Lock", mode);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("day", day0);
        editor.commit();
    }

    public static void updateChildList(Context context) {
        if (AppInfoList.childList.isEmpty()) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("Lock", mode);
            String child = sharedPreferences.getString("childList", null);
            if (child != null) {
                String[] childList = child.split("/");
                for (int i = 0; i < childList.length; i++) AppInfoList.childList.add(childList[i]);
            }
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences("Lock", mode);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String child = "";
            for (int i = 0; i < AppInfoList.childList.size(); i++)
                child += AppInfoList.childList.get(i) + "/";
            editor.putString("childList", child);
            editor.commit();
        }
    }

    //判断服务是否在运行
    public static boolean isServiceRunning(Context context,String serviceName) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(1000);
        for (int i = 0; i < runningService.size(); i++)
            if (runningService.get(i).service.getClassName().toString().equals(serviceName))
                return true;
        return false;
    }

    public static void sendNotification(Context context, String title, String text, int id) {
        MainActivity.title = title;
        MainActivity.text = text;
        MainActivity.id = id;
        switch (id) {
            case 1:
                MainActivity.channelId = "Channel1";
                MainActivity.channelName = "服务";
                break;
            case 2:
                MainActivity.channelId = "Channel2";
                MainActivity.channelName = "通知";
                break;
        }
        context.sendBroadcast(new Intent("com.JohnZero.lock.Notification"));
    }

    //获取权限：加入电池优化白名单
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean requestIgnoreBatteryOptimizations(Context context) {
        boolean isOpen = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null)
            isOpen = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        if (!isOpen)
            try {
                //若已加入名单，就无法再弹出设置页面
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        return isOpen;
    }

    //获取权限：辅助功能判断手机当前前台显示的APP,适用于Android 8/9/10
    public static boolean requestAccessibility(Context context) {
        if (DetectService.isAccessibilitySettingsOn(context) == true) {
            DetectService detectService = DetectService.getInstance();
            String foreground = detectService.getForegroundPackage();
            MyService.str = foreground;
            return true;
        } else {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return false;
        }
    }

    //获取权限：显示其它应用上层
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean requestDrawOverLays(Context context) {
        if (Settings.canDrawOverlays(context)) return true;
        else {
            context.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName())));
            return false;
        }
    }

    //获取权限：允许发出通知
    public static boolean requestNotification(Context context) {
        if (Utils.isNotifyEnabled(context)) return true;
        Intent localIntent = new Intent();
        //直接跳转到应用通知设置的代码：
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0及以上
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0以上到8.0以下
            localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            localIntent.putExtra("app_package", context.getPackageName());
            localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {//4.4
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            localIntent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }
        }
        context.startActivity(localIntent);
        Utils.toast(context,"请授予通知权限");
        return false;
    }

    //调用该方法获取是否开启通知栏权限
    public static boolean isNotifyEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //8.0及以上通知权限判断
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Method sServiceField = notificationManager.getClass().getDeclaredMethod("getService");
                sServiceField.setAccessible(true);
                Object sService = sServiceField.invoke(notificationManager);
                Method method = sService.getClass().getDeclaredMethod("areNotificationsEnabledForPackage", String.class, Integer.TYPE);
                method.setAccessible(true);
                return (boolean) method.invoke(sService, pkg, uid);
            } catch (Exception e) {
                return true;
            }

        } else {
            //8.0以下判断 api19  4.4及以上判断
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            Class appOpsClass = null;
            try {
                appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
                int value = (Integer) opPostNotificationValue.get(Integer.class);
                return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    //获取权限：读取通知
    public static boolean requestNotificationListener(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if(string==null) string=" ";
        if (string.contains(NotificationService.class.getName())) {
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName(context, NotificationService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(context, NotificationService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ComponentName componentName = new ComponentName(context.getApplicationContext(), NotificationService.class);
                requestRebind(componentName);
            }
            return true;
        } else {
            context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            return false;
        }
    }

    //获取权限：后台弹出界面
    public static boolean requestBackgroundStart(Context context) {
        boolean isOpen=false;
        String phoneFirm = checkPhoneFirm();
        switch (phoneFirm) {
            case PhoneConstant.IS_VIVO:
                isOpen = false;
                String packageName = context.getPackageName();
                Uri uri = Uri.parse("content://com.vivo.permissionmanager.provider.permission/start_bg_activity");
                String selection = "pkgname = ?";
                String[] selectionArgs = new String[]{packageName};
                try {
                    Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            int currentmode = cursor.getInt(cursor.getColumnIndex("currentstate"));
                            cursor.close();
                            if (currentmode == 0) isOpen = true;
                            if (currentmode == 1) isOpen = false;
                        } else {
                            cursor.close();
                            isOpen = false;
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                if (!isOpen){
                    toAppDetailPage(context);
                    Utils.toast(context,"请授予后台弹出界面权限");
                }
                break;
            case PhoneConstant.IS_XIAOMI:
                //OP_BACKGROUND_START_ACTIVITY = 10021 后台弹出界面
                isOpen = checkOp(context,10021);
                if (!isOpen){
                    toAppDetailPage(context);
                    Utils.toast(context,"请授予后台弹出界面权限");
                }
                break;
            default:
                isOpen=true;
                break;
        }
        return isOpen;
    }

    //跳转到应用程序信息页面
    public static void toAppDetailPage(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(intent);
    }

    //判断手机厂商
    public static String checkPhoneFirm() {
        String phoneState = Build.BRAND.toLowerCase(); //获取手机厂商
        if (phoneState.equals("huawei") || phoneState.equals("honor"))
            return PhoneConstant.IS_HUAWEI;
        else if (phoneState.equals("xiaomi") && Build.BRAND != null)
            return PhoneConstant.IS_XIAOMI;
        else if (phoneState.equals("oppo") && Build.BRAND != null)
            return PhoneConstant.IS_OPPO;
        else if (phoneState.equals("vivo") && Build.BRAND != null)
            return PhoneConstant.IS_VIVO;
        else if (phoneState.equals("meizu") && Build.BRAND != null)
            return PhoneConstant.IS_MEIZU;
        else if (phoneState.equals("samsung") && Build.BRAND != null)
            return PhoneConstant.IS_SAMSUNG;
        else if (phoneState.equals("letv") && Build.BRAND != null)
            return PhoneConstant.IS_LETV;
        else if (phoneState.equals("smartisan") && Build.BRAND != null)
            return PhoneConstant.IS_SMARTISAN;
        return "";
    }

    public class PhoneConstant {
        final public static String IS_HUAWEI = "isHuawei"; //华为
        final public static String IS_XIAOMI = "isXiaomi"; //小米
        final public static String IS_OPPO = "isOppo";  //oppo
        final public static String IS_VIVO = "isVivo"; //vivo
        final public static String IS_MEIZU = "isMeizu"; //魅族
        final public static String IS_SAMSUNG = "isSamsung"; //三星
        final public static String IS_LETV = "isLetv"; //乐视
        final public static String IS_SMARTISAN = "isSmartisan"; //锤子
    }

    public static void getAllOpsField(Context context){
        AppOpsManager manager= (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try{
            Class appOpsClass =Class.forName(AppOpsManager.class.getName());
            Field[]fields=appOpsClass.getFields();
            String strOps="";
            for(Field f : fields){
                if(f.getType()== Integer.TYPE&& Modifier.isStatic(f.getModifiers())){
                    f.setAccessible(true);
                    strOps+=f.getName()+":"+f.get(null)+";";
                }
            }
            Utils.log(strOps);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean checkOp(Context context,int op){
        AppOpsManager manager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            //sdk>=23
            Method method = manager.getClass().getMethod("checkOpNoThrow", new Class[]{int.class, int.class, String.class});
            Integer result = (Integer) method.invoke(manager, op, android.os.Process.myUid(), context.getPackageName());
            return result == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
        }
        return false;
    }

    //移动程序至前台
    public static void toTaskTop(Context context){
        ActivityManager myActivityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list_task = myActivityManager.getRunningTasks(100);
        //遍历当前运行task，找到本程序所在task，移至前台
        for (ActivityManager.RunningTaskInfo i : list_task) {
            if (i.topActivity.getPackageName().equals(context.getPackageName())) {
                myActivityManager.moveTaskToFront(i.id, 0);
            }
        }
    }

    public static String prefix(int num){
        return num<10?"0"+num:""+num;
    }

    public static String parseString(Calendar calendar){
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return  year+"-"+prefix(month)+"-"+prefix(day)+" "+prefix(hour)+":"+prefix(minute)+":"+prefix(second);
    }

    public static void passwordIn(Context context){
        FileInputStream in = null;
        BufferedReader reader = null;
        try {
            //in = context.openFileInput("Password Book");
            in=new FileInputStream(new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"Password Book.txt"));//文件不存在会自动创建
            reader = new BufferedReader(new InputStreamReader(in));
            LitePal.deleteAll(PasswordInfo.class);
            String line = "";
            int count=0;
            PasswordInfo info=null;
            while ((line = reader.readLine()) != null) {
                switch (count){
                    case 0:
                        info=new PasswordInfo();
                        info.setTitle(CipherUtil.Decrypt(line.replace("\n","")));
                        break;
                    case 1:
                        info.setPassword(CipherUtil.Decrypt(line.replace("\n","")));
                        break;
                    case 2:
                        info.setNote(CipherUtil.Decrypt(line.replace("\n","")));
                        break;
                    case 3:
                        info.setAccessTime(CipherUtil.Decrypt(line.replace("\n","")));
                        break;
                    case 4:
                        info.setModifyTime(CipherUtil.Decrypt(line.replace("\n","")));
                        info.save();
                        count=-1;
                        break;
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void passwordOut(Context context) throws Exception {
        List<PasswordInfo>infos= LitePal.findAll(PasswordInfo.class);
        String data="";
        for(PasswordInfo info:infos){
            data+=CipherUtil.Encrypt(info.getTitle())+"\n";
            data+=CipherUtil.Encrypt(info.getPassword())+"\n";
            data+=CipherUtil.Encrypt(info.getNote())+"\n";
            data+=CipherUtil.Encrypt(info.getAccessTime())+"\n";
            data+=CipherUtil.Encrypt(info.getModifyTime())+"\n";
        }
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            //out = context.openFileOutput("Password Book", Context.MODE_PRIVATE); //内部存储
            out=new FileOutputStream(new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"Password Book.txt")); //主外部存储；卸载软件时该文件也会被删除；覆盖模式
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
