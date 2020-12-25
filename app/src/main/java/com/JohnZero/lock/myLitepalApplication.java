package com.JohnZero.lock;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * @author: JohnZero
 * @date: 2020-09-23
 **/
public class myLitepalApplication extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
