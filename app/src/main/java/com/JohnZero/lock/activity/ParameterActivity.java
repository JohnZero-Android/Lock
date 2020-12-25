package com.JohnZero.lock.activity;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;

/**
 * @author: JohnZero
 * @date: 2020-09-11
 **/
public class ParameterActivity extends BaseActivity implements View.OnTouchListener,GestureDetector.OnGestureListener{
    Context mContext;
    GestureDetector mGestureDetector;
    LinearLayout layout_background;
    TimePicker timePicker;

    @Override
    protected int initLayout() {
        return R.layout.activity_parameter;
    }

    @Override
    protected void initData() {
        mContext=this;
        mGestureDetector=new GestureDetector(this,this);
    }

    @Override
    protected void initView() {
        layout_background=findViewById(R.id.layout_background);
        timePicker=findViewById(R.id.timePicker);
        timePicker.setHour(Utils.HOUR);
        timePicker.setMinute(Utils.MINUTE);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                Utils.updateTime(mContext,i,i1);
            }
        });
        timePicker.setOnTouchListener(this);
        timePicker.setLongClickable(true);
        layout_background.setOnTouchListener(this);
        layout_background.setLongClickable(true);
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