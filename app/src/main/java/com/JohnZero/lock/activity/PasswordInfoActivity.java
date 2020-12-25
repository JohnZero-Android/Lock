package com.JohnZero.lock.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.JohnZero.lock.entity.PasswordInfo;
import com.JohnZero.lock.R;
import com.JohnZero.lock.view.TimeDialog;
import com.JohnZero.lock.util.Utils;
import org.litepal.LitePal;
import java.util.Calendar;

/**
 * @author: JohnZero
 * @date: 2020-09-23
 **/
public class PasswordInfoActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener, GestureDetector.OnGestureListener{
    LinearLayout linearLayout;
    TextView tv_title;
    AnalogClock clock;
    ImageView dustbin;
    EditText et_title;
    EditText et_password;
    EditText et_note;
    Button btn_get;
    Button btn_change;

    private Context mContext;
    GestureDetector mGestureDetector;
    PasswordInfo passwordInfo;
    public static int index=-1; //-1表示新建
    boolean isChanged=false;
    String oldTitle="";
    String oldPassword="";
    String oldNote="";
    String oldAccessTime="";
    String oldModifyTime="";
    String newTitle="";
    String newPassword="";
    String newNote="";
    public static String newAccessTime="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int initLayout() { return R.layout.activity_password_info; }

    @Override
    protected void initData() {
        mContext=this;
        mGestureDetector=new GestureDetector(this,this);
        newAccessTime="";
        if(index>=0){
            passwordInfo= LitePal.find(PasswordInfo.class,index);
            oldTitle=passwordInfo.getTitle();
            oldPassword=passwordInfo.getPassword();
            oldNote=passwordInfo.getNote();
            oldAccessTime=passwordInfo.getAccessTime();
            oldModifyTime=passwordInfo.getModifyTime();
        }
    }

    @Override
    protected void initView() {
        linearLayout=findViewById(R.id.linearLayout);
        tv_title=findViewById(R.id.tv_title);
        clock=findViewById(R.id.clock);
        dustbin=findViewById(R.id.dustbin);
        et_title=findViewById(R.id.et_title);
        et_password=findViewById(R.id.et_password);
        et_note=findViewById(R.id.et_note);
        btn_get=findViewById(R.id.btn_get);
        btn_change=findViewById(R.id.btn_change);

        tv_title.setOnClickListener(this);
        clock.setOnClickListener(this);
        dustbin.setOnClickListener(this);
        btn_get.setOnClickListener(this);
        btn_change.setOnClickListener(this);

        linearLayout.setOnTouchListener(this);
        linearLayout.setLongClickable(true);
        et_note.setOnTouchListener(this);
        et_note.setLongClickable(true);

        if(index>=0){
            et_title.setText(oldTitle);
            et_password.setText(oldPassword);
            et_note.setText(oldNote);
            if(!oldModifyTime.isEmpty()) tv_title.setText(oldModifyTime);
        }
    }

    //onPause后另一个界面会onResume
    @Override
    protected void onPause() {
        super.onPause();
        newTitle=et_title.getText().toString();
        newPassword=et_password.getText().toString();
        newNote=et_note.getText().toString();

        if(index>=0){
            if(newTitle.isEmpty()) {
                dustbin.performClick();
                try {
                    Utils.passwordOut(mContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else{
                //未被删除且标题不为空，之后判读是否被修改
                if (!newTitle.equals(oldTitle)) {
                    isChanged = true;
                    passwordInfo.setTitle(newTitle);
                }
                if (!newPassword.equals(oldPassword)) {
                    isChanged = true;
                    passwordInfo.setPassword(newPassword);
                }
                if (!newNote.equals(oldNote)) {
                    isChanged = true;
                    passwordInfo.setNote(newNote);
                }
                if (!newAccessTime.equals(oldAccessTime)&&!newAccessTime.isEmpty()) {
                    isChanged = true;
                    passwordInfo.setAccessTime(newAccessTime);
                }
                if (isChanged) {
                    passwordInfo.setModifyTime(Utils.parseString(Calendar.getInstance()));
                    passwordInfo.save();
                    try {
                        Utils.passwordOut(mContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{ //新建
            if(!newTitle.isEmpty()){
                passwordInfo=new PasswordInfo(newTitle,newPassword,newNote,newAccessTime,Utils.parseString(Calendar.getInstance()));
                passwordInfo.save();
                try {
                    Utils.passwordOut(mContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        newAccessTime="";
        TimeDialog.year=0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_title:
                navigate(PasswordGeneratorActivity.class);
                break;
            case R.id.clock:
                TimeDialog timeDialog=new TimeDialog.Builder(this).create();
                timeDialog.show();
                break;
            case R.id.dustbin:
                if(index>=0) LitePal.delete(PasswordInfo.class,index);
                finish();
                break;
            case R.id.btn_get:
                et_password.setText(oldPassword);
                break;
            case R.id.btn_change:
                et_password.setText(PasswordGeneratorActivity.passwordGenerator());
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
}