package com.JohnZero.lock.service;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.JohnZero.lock.util.Utils;

/**
 * @author: JohnZero
 * @date: 2020-09-19
 **/
public class NotificationService extends NotificationListenerService {
    Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(sbn.getPackageName().equals("com.JohnZero.lock")&&sbn.getId()==2)
            Utils.sendNotification(mContext,"通知","服务正在运行",2);
    }
}
