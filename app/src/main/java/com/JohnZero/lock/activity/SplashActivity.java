package com.JohnZero.lock.activity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.JohnZero.lock.sqlite.MyDBOpenHelper;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;

/**
 * @author: JohnZero
 * @date: 2020-09-03
 **/
public class SplashActivity extends BaseActivity {
    Context mContext;
    SQLiteDatabase db;
    MyDBOpenHelper myDBOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread myThread = new Thread() {//创建子线程以显示splash，否则无效
            @Override
            public void run() {
                Utils.initData(mContext,db);//只能放在线程内处理，否则不会出现splash画面
                startActivity(new Intent(mContext, MainActivity.class));
                finish();//关闭当前活动
            }
        };
        myThread.start();//启动线程
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
    }

    @Override
    protected void initData() {
        mContext = this;
        myDBOpenHelper = new MyDBOpenHelper(mContext, "appList.db", null, 1);
        db = myDBOpenHelper.getWritableDatabase();
    }

    @Override
    protected void onDestroy() {
        Utils.log("SplashActiviy:onDestroy");
        Utils.sendNotification(mContext,"通知","程序已启动",2);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //修改后退按钮，使后退按钮失效。
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}