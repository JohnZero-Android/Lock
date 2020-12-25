package com.JohnZero.lock.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;
import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.patternlockview.utils.ResourceUtils;
import com.andrognito.rxpatternlockview.RxPatternLockView;
import com.andrognito.rxpatternlockview.events.PatternLockCompleteEvent;
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import io.reactivex.functions.Consumer;

/**
 * @author: JohnZero
 * @date: 2020-09-27
 **/
public class PatternLockViewActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener, GestureDetector.OnGestureListener{
    Context mContext;
    GestureDetector mGestureDetector;
    public static String gesturePassword="";
    private boolean isLock=true;
    private PatternLockView mPatternLockView;
    LinearLayout linearLayout;
    ImageView profile_image;
    TextView tv_title;
    TextView tv_forget;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.isAdmin){
            setResult(1);
            finish();
        }

        super.onCreate(savedInstanceState);
        //设置为全屏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_pattern_lock_view);
        mContext=this;
        mGestureDetector=new GestureDetector(this,this);
        linearLayout=findViewById(R.id.linearLayout);
        profile_image=findViewById(R.id.profile_image);
        tv_title=findViewById(R.id.tv_title);
        tv_forget=findViewById(R.id.tv_forget);
        tv_title.setOnClickListener(this);
        tv_forget.setOnClickListener(this);

        linearLayout.setOnTouchListener(this);
        linearLayout.setLongClickable(true);
        profile_image.setOnTouchListener(this);
        profile_image.setLongClickable(true);

        mPatternLockView = (PatternLockView) findViewById(R.id.patter_lock_view); //获取控件对象
        mPatternLockView.setDotCount(3); // n*n大小   3*3
        mPatternLockView.setDotNormalSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_size)); //没有点击时点的大小
        mPatternLockView.setDotSelectedSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_selected_size)); //点击时点的大小
        mPatternLockView.setPathWidth((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_path_width));//更改路径距离
        mPatternLockView.setAspectRatioEnabled(true);
        mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        mPatternLockView.setDotAnimationDuration(150);
        mPatternLockView.setPathEndAnimationDuration(100);
        mPatternLockView.setInStealthMode(false);
        mPatternLockView.setTactileFeedbackEnabled(true);
        mPatternLockView.setInputEnabled(true);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);

        RxPatternLockView.patternComplete(mPatternLockView)
                .subscribe(new Consumer<PatternLockCompleteEvent>() {
                    @Override
                    public void accept(PatternLockCompleteEvent patternLockCompleteEvent) throws Exception {
                        Utils.log( "Complete: " + patternLockCompleteEvent.getPattern().toString());
                    }
                });

        RxPatternLockView.patternChanges(mPatternLockView)
                .subscribe(new Consumer<PatternLockCompoundEvent>() {
                    @Override
                    public void accept(PatternLockCompoundEvent event) throws Exception {
                        if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_STARTED) {
                            Utils.log( "Pattern drawing started");
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_PROGRESS) {
                            Utils.log( "Pattern progress: " + PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_COMPLETE) {
                            Utils.log( "Pattern complete: " + PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_CLEARED) {
                            Utils.log( "Pattern has been cleared");
                        }
                    }
                });

        getPassword();
    }

    //设置监听器
    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Utils.log( "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Utils.log( "Pattern progress: " + PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Utils.log( "Pattern complete: " + PatternLockUtils.patternToString(mPatternLockView, pattern));
            //String paswd = "0364258"; //密码
            String patternToString = PatternLockUtils.patternToString(mPatternLockView, pattern);
            if(!TextUtils.isEmpty(patternToString)){
                if(gesturePassword.isEmpty()){
                    updatePassword(patternToString);
                }else{
                    if(verify(patternToString)){
                        gesturePassword="";
                        tv_title.setText("重设密码");
                    }else if(patternToString.equals(gesturePassword)){
                        //判断为正确
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        //Toast.makeText(PatternLockViewActivity.this,"您绘制的密码是："+patternToString+"\n"+"密码正确，开锁成功",Toast.LENGTH_SHORT).show();
                        Toast.makeText(PatternLockViewActivity.this,"密码正确，开锁成功",Toast.LENGTH_SHORT).show();
                        isLock=false;
                        setResult(1);
                        finish();
                    }else {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        //Toast.makeText(PatternLockViewActivity.this,"您绘制的密码是："+patternToString+"\n"+"密码错误，请重新绘制", Toast.LENGTH_SHORT).show();
                        Toast.makeText(PatternLockViewActivity.this,"密码错误，请重新绘制", Toast.LENGTH_SHORT).show();
                        isLock=true;
                    }
                }
            }
            //3s后清除图案
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPatternLockView.clearPattern();
                }
            },1000);
        }
        @Override
        public void onCleared() {
            Utils.log( "Pattern has been cleared");
        }
    };

    void getPassword(){
        SharedPreferences sharedPreferences=mContext.getSharedPreferences("Lock",Utils.mode);
        gesturePassword=sharedPreferences.getString("gesturePassword","");
        if(gesturePassword.isEmpty()) tv_forget.performClick();
    }

    void updatePassword(String newPassword){
        SharedPreferences sharedPreferences=mContext.getSharedPreferences("Lock",Utils.mode);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("gesturePassword",newPassword);
        editor.commit();
        gesturePassword=newPassword;
        tv_title.setText("密码锁");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_forget:
                Utils.toast(mContext,"请联系开发者获取手势码");
                break;
        }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //修改后退按钮，使后退按钮失效
//        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK&&isLock) {
//            return true;//true和false都会使后退键失效
//        }
        return super.onKeyDown(keyCode, event);
    }

    boolean verify(String password){
        if(password.length()!=4) return false;
        int []code=new int[4];
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(day<10) {
            code[0]=0;
            code[1]=day%9;
        }else{
            code[0]=day/10;
            code[1]=(day%10)%9;
        }
        if(hour<10) {
            code[2]=0;
            code[3]=hour%9;
        }else{
            code[2]=hour/10;
            code[3]=(hour%10)%9;
        }
        for(int i=0;i<4;i++){
            boolean isRepeat=true;
            while(isRepeat){
                isRepeat=false;
                for(int j=0;j<i;j++)
                    if(code[i]==code[j]) {
                        code[i] = (code[i] + 1) % 9;
                        isRepeat = true;
                    }
                if(code[i]!=4&&i>0){
                    if((code[i-1]+code[i])%2==0){
                        code[i] = (code[i] + 1) % 9;
                        isRepeat = true;
                    }
                }
            }
        }
        String str="";
        for(int i=3;i>=0;i--)
            str+=8-code[i];
        Utils.log("手势码："+str);
        if(password.equals(str)) return true;
        return false;
    }
}