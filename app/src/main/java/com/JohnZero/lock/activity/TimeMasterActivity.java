package com.JohnZero.lock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.JohnZero.lock.AppInfoList;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;

/**
 * @author: JohnZero
 * @date: 2020-12-21
 **/
public class TimeMasterActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener{
    boolean isClear=false;
    float rate=10;
    boolean redOn=false;
    boolean blueOn=false;
    int[][]time0=new int[3][3]; //旧 时分秒
    int[][]time=new int[3][3];  //新
    String text1,text2,text3;
    EditText et_rate;
    Button btn_rate;
    LinearLayout layout_background;
    LinearLayout layout_red;
    LinearLayout layout_blue;
    TextView tv1;
    TextView tv2;
    TextView tv3;
    GestureDetector mGestureDetector;
    MyReceiver myReceiver;
    private boolean mReceiverTag = false;   //广播接受者标识

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_time_master;
    }

    @Override
    protected void initData() {
        mGestureDetector=new GestureDetector(this,this);
        update();
        text1=toString(time0[1]);
        text2=toString(time0[2]);
        time[2]=toHour((int)(toSecond(time0[1])/rate));
        text3=toString(time[2]);
        registerReceiver();
    }

    @Override
    protected void initView() {
        et_rate = findViewById(R.id.et_rate);
        btn_rate = findViewById(R.id.btn_rate);
        layout_background=(LinearLayout)findViewById(R.id.layout_background);
        layout_red=(LinearLayout)findViewById(R.id.layout_red);
        layout_blue=(LinearLayout)findViewById(R.id.layout_blue);
        tv1=(TextView)findViewById(R.id.tv1);
        tv2=(TextView)findViewById(R.id.tv2);
        tv3=(TextView)findViewById(R.id.tv3);
        btn_rate.setOnClickListener(this);
        layout_red.setOnClickListener(this);
        layout_blue.setOnClickListener(this);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
        layout_background.setOnTouchListener(this);
        layout_background.setLongClickable(true);
        layout_red.setOnTouchListener(this);
        layout_red.setLongClickable(true);
        layout_blue.setOnTouchListener(this);
        layout_blue.setLongClickable(true);
        tv1.setOnTouchListener(this);
        tv1.setLongClickable(true);
        tv2.setOnTouchListener(this);
        tv2.setLongClickable(true);
        tv3.setOnTouchListener(this);
        tv3.setLongClickable(true);

        et_rate.setHint("时间汇率："+rate);
        tv1.setText(text1);
        tv2.setText(text2);
        tv3.setText(text3);
        lock();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rate:
                String str=et_rate.getText().toString();
                if(str.isEmpty()) break;
                float r=Float.parseFloat(str);
                if(r>0){
                    rate = r;
                    SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("rate",rate);
                    editor.commit();
                    et_rate.setHint("时间汇率："+rate);
                }
                break;
            case R.id.tv1:
                layout_red.performClick();
                break;
            case R.id.tv2:
            case R.id.tv3:
                layout_blue.performClick();
                break;
            case R.id.layout_red:
                if(redOn){
                    redOn=false;
                    SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for(int i=0;i<3;i++){
                        time0[1][i]=time[0][i];
                        editor.putInt("time0[1]["+i+"]", time0[1][i]);
                    }
                    editor.putBoolean("redOn",redOn);
                    editor.commit();
                }else{
                    redOn=true;
                    if(blueOn) layout_blue.performClick();
                    Utils.getTime();
                    time0[0][0]=Utils.hour;time0[0][1]=Utils.minute;time0[0][2]=Utils.second;
                    SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for(int i=0;i<3;i++) editor.putInt("time0[0]["+i+"]", time0[0][i]);
                    editor.putBoolean("redOn",redOn);
                    editor.putBoolean("blueOn",blueOn);
                    editor.commit();
                }
                break;
            case R.id.layout_blue:
                if(blueOn){
                    blueOn=false;
                    SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for(int i=0;i<3;i++){
                        time0[2][i]=time[1][i];
                        editor.putInt("time0[2]["+i+"]", time0[2][i]);
                    }
                    editor.putBoolean("blueOn",blueOn);
                    editor.commit();
                }else{
                    blueOn=true;
                    if(redOn) layout_red.performClick();
                    Utils.getTime();
                    time0[0][0]=Utils.hour;time0[0][1]=Utils.minute;time0[0][2]=Utils.second;
                    SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for(int i=0;i<3;i++) editor.putInt("time0[0]["+i+"]", time0[0][i]);
                    editor.putBoolean("redOn",redOn);
                    editor.putBoolean("blueOn",blueOn);
                    editor.commit();
                    lock();
                }
                break;
        }
    }

    //MyReceiver接受广播
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "com.JohnZero.lock.service.MyService":
                    if(isClear) clear();
                    if(redOn){
                        Utils.getTime();
                        int hour=Utils.hour;int minute=Utils.minute;int second=Utils.second;
                        int base=toSecond(time0[1]);
                        int diff=toSecond(new int[]{hour,minute,second})-toSecond(time0[0]);
                        time[0]=toHour(base+diff);
                        text1=TimeMasterActivity.toString(time[0]);
                        time[2]=toHour((int)((base+diff)/rate));
                        text3=TimeMasterActivity.toString(time[2]);
                        tv1.setText(text1);
                        tv3.setText(text3);
                    }
                    if(blueOn){
                        Utils.getTime();
                        int hour=Utils.hour;int minute=Utils.minute;int second=Utils.second;
                        int base=toSecond(time0[2]);
                        int diff=toSecond(new int[]{hour,minute,second})-toSecond(time0[0]);
                        time[1]=toHour(base+diff);
                        text2=TimeMasterActivity.toString(time[1]);
                        tv2.setText(text2);
                        lock();
                    }
                    break;
            }
        }
    }

    //代码中动态注册广播
    private void registerReceiver() {
        if (!mReceiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            mReceiverTag = true;    //标识值 赋值为 true 表示广播已被注册
            // 动态注册广播接收器
            myReceiver = new MyReceiver();
            IntentFilter itFilter = new IntentFilter();
            itFilter.addAction("com.JohnZero.lock.service.MyService");
            registerReceiver(myReceiver, itFilter);
        }
    }

    //注销广播
    private void unregisterReceiver() {
        if (mReceiverTag) {   //判断广播是否注册
            mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
            try {
                unregisterReceiver(myReceiver); //注销广播 为什么这里会报错：Receiver not registered
            } catch (IllegalArgumentException e) {
                String error = "Receiver not registered";
                if (e.getMessage().contains(error)) Utils.log(error);
                else throw e;   // unexpected, re-throw
            }
        }
    }

    public static String toString(int[]t){
        return (t[0] > 0 ? t[0] + "时" : "") + (t[1] > 0 ? t[1] + "分" : "") + (t[2] > 0 ? t[2] + "秒" : "");
    }

    public int toSecond(int[]t){
        return 3600*t[0]+60*t[1]+t[2];
    }

    public int[] toHour(int s){
        int[] t=new int[3];
        t[0]=s/3600;
        t[1]=(s%3600)/60;
        t[2]=s%60;
        return t;
    }

    public void clear(){
        isClear=false;
        redOn=false;
        blueOn=false;
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                time0[i][j]=0;
        upload();
        tv1.setText("");
        tv2.setText("");
        tv3.setText("");
    }

    public void update(){
        SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
        isClear=sharedPreferences.getBoolean("isClear",false);
        redOn=sharedPreferences.getBoolean("redOn",false);
        blueOn=sharedPreferences.getBoolean("blueOn",false);
        rate=sharedPreferences.getFloat("rate",10);
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++){
                time0[i][j]=sharedPreferences.getInt("time0["+i+"]["+j+"]", 0);
                if(i>0) time[i-1][j]=time0[i][j];
            }
    }

    public void upload(){
        SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isClear",isClear);
        editor.putBoolean("redOn",redOn);
        editor.putBoolean("blueOn",blueOn);
        editor.putFloat("rate",rate);
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                editor.putInt("time0["+i+"]["+j+"]", time0[i][j]);
        editor.commit();
    }

    public void lock(){
        if(toSecond(time[1])>toSecond(time[2])){
            for(int i=0;i<3;i++) time[1][i]=time[2][i];
            text2=toString(time[1]);
            tv2.setText(text2);
            if(redOn) layout_red.performClick();
            if(blueOn) layout_blue.performClick();
            sendBroadcast(new Intent("com.JohnZero.lock.Lock"));
        }
    }
}