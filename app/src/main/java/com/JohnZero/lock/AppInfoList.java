package com.JohnZero.lock;

import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import com.JohnZero.lock.entity.AppInfo;
import com.JohnZero.lock.service.MyService;
import com.JohnZero.lock.util.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: JohnZero
 * @date: 2020-09-07
 **/
public class AppInfoList {
    public static int type;//1:已加密；2：未加密；3：已忽略
    public static int index;//-1:不在三个列表中
    public static ArrayList<AppInfo> appList1 = new ArrayList<>();//已加密
    public static ArrayList<AppInfo> appList2 = new ArrayList<>();//未加密
    public static ArrayList<AppInfo> appList3 = new ArrayList<>();//忽略名单
    public static ArrayList<String> childList = new ArrayList<>();//子应用
    public static ArrayList<String> usageList = new ArrayList<>();//使用日志
    public static boolean isChanged1=true;
    public static boolean isChanged2=true;
    public static boolean isChanged3=true;
    public static boolean isCount=false;

    //因为手机上的某些应用可能被卸载了或者更新了应用图标，所以需要调用该方法来更新这3个ArrayList
    public static void updateList(PackageManager packageManager) {
        boolean isFirst = appList1.isEmpty() && appList2.isEmpty() && appList3.isEmpty();
        AppInfo appInfo;
        ArrayList<AppInfo> appList_1 = new ArrayList<>();//已加密
        ArrayList<AppInfo> appList_2 = new ArrayList<>();//未加密
        ArrayList<AppInfo> appList_3 = new ArrayList<>();//已忽略
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfos.size(); i++) {
            PackageInfo packageInfo = packageInfos.get(i);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            Drawable icon = applicationInfo.loadIcon(packageManager);//图标
            String appName = (String) packageManager.getApplicationLabel(applicationInfo);//应用名
            String packageName = applicationInfo.packageName;//包名
            boolean isSys = (ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0;//是否为系统应用
            appInfo = new AppInfo(icon, appName, packageName, isSys);
            if ((isFirst && isSys) || (indexOfIgnore(appName) != 0)) {
                appInfo.setIgnore(true);
                appList_3.add(appInfo);
                continue;
            }
            int index = indexOf(appName);
            if (index > 0) {
                appInfo.setLock(true);
                appInfo.setCurTime(appList1.get(index - 1).getCurTime());
                appInfo.setMaxTime(appList1.get(index - 1).getMaxTime());
                appList_1.add(appInfo);
            }
            if (index <= 0) {
                appList_2.add(appInfo);
            }
        }
        appList1 = appList_1;
        appList2 = appList_2;
        appList3 = appList_3;
        if(appList3.isEmpty()) updateList3();
        sortAppList();
    }

    public static void updateList3(){
        int i=0;
        while(i<appList2.size()){
            if(appList2.get(i).isSys()){
                appList3.add(appList2.get(i));
                appList2.remove(i);
            }
            else i++;
        }
    }

    public static int indexOfIgnore(String appName) {
        //忽略名单
        for (int i = 0; i < appList3.size(); i++)
            if (appList3.get(i).getAppName().equals(appName))
                return i + 1;
        return 0;
    }

    public static int indexOfIgnore1(String packageName) {
        //忽略名单
        for (int i = 0; i < appList3.size(); i++)
            if (appList3.get(i).getPackageName().equals(packageName))
                return i + 1;
        return 0;
    }

    public static int indexOf(String appName) {
        //已加密
        for (int i = 0; i < appList1.size(); i++)
            if (appList1.get(i).getAppName().equals(appName))
                return i + 1;
        //未加密
        for (int i = 0; i < appList2.size(); i++)
            if (appList2.get(i).getAppName().equals(appName))
                return -i - 1;
        return 0;
    }

    public static int indexOf1(String packageName) {
        //已加密
        for (int i = 0; i < appList1.size(); i++)
            if (appList1.get(i).getPackageName().equals(packageName))
                return i + 1;
        //未加密
        for (int i = 0; i < appList2.size(); i++)
            if (appList2.get(i).getPackageName().equals(packageName))
                return -i - 1;
        return 0;
    }

    public static ArrayList<AppInfo> search(String searchText, int type) {
        ArrayList<AppInfo> appList = new ArrayList<>();
        //已加密
        if (type == 1)
            for (int i = 0; i < appList1.size(); i++)
                if (appList1.get(i).getAppName().toLowerCase().contains(searchText.toLowerCase()))
                    appList.add(appList1.get(i));
        //未加密
        if (type == 2)
            for (int i = 0; i < appList2.size(); i++)
                if (appList2.get(i).getAppName().toLowerCase().contains(searchText.toLowerCase()))
                    appList.add(appList2.get(i));
        //已忽略
        if (type == 3)
            for (int i = 0; i < appList3.size(); i++)
                if (appList3.get(i).getAppName().toLowerCase().contains(searchText.toLowerCase()))
                    appList.add(appList3.get(i));
        return appList;
    }

    public static void findItem(String appName) {
        int index0 = indexOf(appName);
        if (index0 > 0) {
            type = 1;
            index = index0 - 1;
        }
        if (index0 < 0) {
            type = 2;
            index = -index0 - 1;
        }
        if (index0 == 0) {
            type = 3;
            index = indexOfIgnore(appName) - 1;
        }
    }

    public static void findItem1(String packageName) {
        int index0 = indexOf1(packageName);
        if (index0 > 0) {
            type = 1;
            index = index0 - 1;
        }
        if (index0 < 0) {
            type = 2;
            index = -index0 - 1;
        }
        if (index0 == 0) {
            type = 3;
            index = indexOfIgnore1(packageName) - 1;
        }
    }

    public static int isLockItem(String packageName) {
        isCount=false;
        for (int i = 0; i < appList1.size(); i++) {
            if (appList1.get(i).getPackageName().equals(packageName)) {
                if (appList1.get(i).getCurTime() < appList1.get(i).getMaxTime()&& MyService.isScreenOn) {
                    appList1.get(i).setCurTime(appList1.get(i).getCurTime() + 1);
                    isChanged1=true;
                    //Utils.log(packageName+":"+MyService.count);
                    isCount=true;
                }
                if (appList1.get(i).getCurTime() < appList1.get(i).getMaxTime()) return 1;//时间未到
                else return 2;//时间已到
            }
        }
        return 0;//不在已加密列表
    }

    public static void uploadAppList(SQLiteDatabase db) {
        if(isChanged1) {
            db.execSQL("drop table appList1");
            db.execSQL("CREATE TABLE appList1(appName VARCHAR(20),curTime INTEGER,maxTime INTEGER,isLock INTEGER,isIgnore INTEGER)");
        }
        if(isChanged2) {
            db.execSQL("drop table appList2");
            db.execSQL("CREATE TABLE appList2(appName VARCHAR(20),curTime INTEGER,maxTime INTEGER,isLock INTEGER,isIgnore INTEGER)");
        }
        if(isChanged3) {
            db.execSQL("drop table appList3");
            db.execSQL("CREATE TABLE appList3(appName VARCHAR(20),curTime INTEGER,maxTime INTEGER,isLock INTEGER,isIgnore INTEGER)");
        }

        ContentValues values;
        //已加密
        if(isChanged1)
        for (int i = 0; i < appList1.size(); i++) {
            values = new ContentValues();
            values.put("appName", appList1.get(i).getAppName());
            values.put("curTime", appList1.get(i).getCurTime());
            values.put("maxTime", appList1.get(i).getMaxTime());
            values.put("isLock", appList1.get(i).isLock());
            values.put("isIgnore", appList1.get(i).isIgnore());
            db.insert("appList1", null, values);
        }
        //未加密
        if(isChanged2)
        for (int i = 0; i < appList2.size(); i++) {
            values = new ContentValues();
            values.put("appName", appList2.get(i).getAppName());
            values.put("curTime", appList2.get(i).getCurTime());
            values.put("maxTime", appList2.get(i).getMaxTime());
            values.put("isLock", appList2.get(i).isLock());
            values.put("isIgnore", appList2.get(i).isIgnore());
            db.insert("appList2", null, values);
        }
        //已忽略
        if(isChanged3)
        for (int i = 0; i < appList3.size(); i++) {
            values = new ContentValues();
            values.put("appName", appList3.get(i).getAppName());
            values.put("curTime", appList3.get(i).getCurTime());
            values.put("maxTime", appList3.get(i).getMaxTime());
            values.put("isLock", appList3.get(i).isLock());
            values.put("isIgnore", appList3.get(i).isIgnore());
            db.insert("appList3", null, values);
        }
        isChanged1=isChanged2=isChanged3=false;
    }

    //获取上一次保存在数据库中的已加密应用列表，未加密应用列表和忽略应用列表并保存在3个ArrayList
    public static void getAppList(SQLiteDatabase db) {
        appList1 = new ArrayList<>();
        appList2 = new ArrayList<>();
        appList3 = new ArrayList<>();
        AppInfo appInfo;
        Cursor cursor;
        //已加密
        cursor = db.query("appList1", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String appName = cursor.getString(cursor.getColumnIndex("appName"));
                int curTime = cursor.getInt(cursor.getColumnIndex("curTime"));
                int maxTime = cursor.getInt(cursor.getColumnIndex("maxTime"));
                boolean isLock = cursor.getInt(cursor.getColumnIndex("isLock")) == 1 ? true : false;
                boolean isIgnore = cursor.getInt(cursor.getColumnIndex("isIgnore")) == 1 ? true : false;
                appInfo = new AppInfo();
                appInfo.setAppName(appName);
                appInfo.setCurTime(curTime);
                appInfo.setMaxTime(maxTime);
                appInfo.setLock(isLock);
                appInfo.setIgnore(isIgnore);
                appList1.add(appInfo);
            } while (cursor.moveToNext());
        }
        //未加密
        cursor = db.query("appList2", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String appName = cursor.getString(cursor.getColumnIndex("appName"));
                int curTime = cursor.getInt(cursor.getColumnIndex("curTime"));
                int maxTime = cursor.getInt(cursor.getColumnIndex("maxTime"));
                boolean isLock = cursor.getInt(cursor.getColumnIndex("isLock")) == 1 ? true : false;
                boolean isIgnore = cursor.getInt(cursor.getColumnIndex("isIgnore")) == 1 ? true : false;
                appInfo = new AppInfo();
                appInfo.setAppName(appName);
                appInfo.setCurTime(curTime);
                appInfo.setMaxTime(maxTime);
                appInfo.setLock(isLock);
                appInfo.setIgnore(isIgnore);
                appList2.add(appInfo);
            } while (cursor.moveToNext());
        }
        //已忽略
        cursor = db.query("appList3", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String appName = cursor.getString(cursor.getColumnIndex("appName"));
                int curTime = cursor.getInt(cursor.getColumnIndex("curTime"));
                int maxTime = cursor.getInt(cursor.getColumnIndex("maxTime"));
                boolean isLock = cursor.getInt(cursor.getColumnIndex("isLock")) == 1 ? true : false;
                boolean isIgnore = cursor.getInt(cursor.getColumnIndex("isIgnore")) == 1 ? true : false;
                appInfo = new AppInfo();
                appInfo.setAppName(appName);
                appInfo.setCurTime(curTime);
                appInfo.setMaxTime(maxTime);
                appInfo.setLock(isLock);
                appInfo.setIgnore(isIgnore);
                appList3.add(appInfo);
            } while (cursor.moveToNext());
        }
    }

    public static void sortAppList() {
        new Utils().sortAppList(appList1);
        new Utils().sortAppList(appList2);
        new Utils().sortAppList(appList3);
    }
}
