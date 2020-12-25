package com.JohnZero.lock.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.RequiresApi;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;

/**
 * @author: JohnZero
 * @date: 2020-09-11
 **/
public class PermissionActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener,GestureDetector.OnGestureListener {
    Context mContext;
    GestureDetector mGestureDetector;
    LinearLayout layout_background;
    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
    Button btn5;
    Button btn6;

    @Override
    protected int initLayout() {
        return R.layout.activity_permission;
    }

    @Override
    protected void initView() {
        layout_background=findViewById(R.id.layout_background);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3=findViewById(R.id.btn3);
        btn4=findViewById(R.id.btn4);
        btn5=findViewById(R.id.btn5);
        btn6=findViewById(R.id.btn6);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        layout_background.setOnTouchListener(this);
        layout_background.setLongClickable(true);
    }

    @Override
    protected void initData() {
        mContext=this;
        mGestureDetector=new GestureDetector(this,this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                if (Utils.requestIgnoreBatteryOptimizations(mContext)) Utils.toast(mContext,"已加入白名单");
                break;
            case R.id.btn2:
                if(MainActivity.isAdmin){
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                }
                if(Utils.requestAccessibility(mContext)) Utils.toast(mContext,"已开启辅助功能");
                break;
            case R.id.btn3:
                if(MainActivity.isAdmin){
                    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
                    break;
                }
                if(Utils.requestDrawOverLays(mContext)) Utils.toast(mContext,"已获得显示在其它应用上层的权限");
                break;
            case R.id.btn4:
                if(MainActivity.isAdmin){
                    Utils.toAppDetailPage(mContext);
                    break;
                }
                if(Utils.requestNotification(mContext)) Utils.toast(mContext,"已获得通知权限");
                break;
            case R.id.btn5:
                if(MainActivity.isAdmin){
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    break;
                }
                if(Utils.requestNotificationListener(mContext)) Utils.toast(mContext,"已获得通知使用权限");
                break;
            case R.id.btn6:
                if(MainActivity.isAdmin){
                    Utils.toAppDetailPage(mContext);
                    break;
                }
                if(Utils.requestBackgroundStart(mContext)) Utils.toast(mContext,"已获得后台弹出界面权限");
                break;
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) { }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) { return false; }

    @Override
    public void onLongPress(MotionEvent motionEvent) { }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if(endX-beginX>MainActivity.minMove&&Math.abs(endY-beginY)<MainActivity.maxMove&&Math.abs(velocityX)>MainActivity.minVelocity){   //右滑
            finish();
        }
        return false;
    }

    //不能省略
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
    }

}