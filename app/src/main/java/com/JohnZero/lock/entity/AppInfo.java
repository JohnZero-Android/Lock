package com.JohnZero.lock.entity;

import android.graphics.drawable.Drawable;

/**
 * @author: JohnZero
 * @date: 2020-09-07
 **/
public class AppInfo {
    private boolean isSys=false;
    private boolean isLock = false;
    private boolean isIgnore = false;
    private String appName="Lock";
    private String packageName="";
    private Drawable icon=null;
    private int curTime = 0;
    private int maxTime = 1800;//秒

    public AppInfo() {
    }

    public AppInfo(Drawable icon, String appName, String packageName, Boolean isSys) {
        this.icon = icon;
        this.appName = appName;
        this.packageName = packageName;
        this.isSys = isSys;
    }

    public boolean isSys() {
        return isSys;
    }

    public void setSys(boolean sys) {
        isSys = sys;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    public boolean isIgnore() { return isIgnore; }

    public void setIgnore(boolean ignore) { isIgnore = ignore; }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getCurTime() {
        return curTime;
    }

    public void setCurTime(int curTime) {
        this.curTime = curTime;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }
}
