package com.JohnZero.lock.activity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.JohnZero.lock.R;

/**
 * @author: JohnZero
 * @date: 2020-09-03
 **/
public class LockActivity extends BaseActivity implements View.OnTouchListener, GestureDetector.OnGestureListener{
    public static boolean screenTag=false; //锁屏界面是否正在显示
    GestureDetector mGestureDetector;
    LinearLayout layout_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_lock;
    }

    @Override
    protected void initView() {
        layout_background = findViewById(R.id.layout_background);
        layout_background.setOnTouchListener(this);
        layout_background.setLongClickable(true);
    }

    @Override
    protected void initData() {
        mGestureDetector=new GestureDetector(this,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenTag=false;
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
        float minMove = 120;         //最小滑动距离
        float minVelocity = 0;      //最小滑动速度
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if(endX-beginX>MainActivity.minMove&&Math.abs(endY-beginY)<MainActivity.maxMove&&Math.abs(velocityX)>MainActivity.minVelocity){   //右滑
            screenTag=false;
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