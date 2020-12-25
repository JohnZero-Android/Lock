package com.JohnZero.lock.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.JohnZero.lock.entity.AppInfo;
import com.JohnZero.lock.AppInfoList;
import com.JohnZero.lock.R;
import com.JohnZero.lock.adapter.StringAdapter;
import com.JohnZero.lock.util.Utils;
import java.util.ArrayList;

/**
 * @author: JohnZero
 * @date: 2020-09-07
 **/
public class AppInfoActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener, GestureDetector.OnGestureListener{
    AppInfo appInfo;
    int type;//1:已加密；2：未加密；3：已忽略
    int index;
    Context mContext;
    StringAdapter mAdapter;
    GestureDetector mGestureDetector;
    LinearLayout layout_background;
    ImageView icon;
    TextView appName;
    LinearLayout layout_progressBar;
    ProgressBar progressBar;
    TextView appTime;
    LinearLayout layout_editTime;
    EditText et_editTime;
    Button btn_editTime;
    RelativeLayout layout_lock;
    Switch switch_lock;
    RelativeLayout layout_ignore;
    Switch switch_ignore;
    TextView tv_child;
    ListView listView;
    AlertDialog alertDialog;
    AlertDialog.Builder builder;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    switch_lock.setChecked(false);
                    break;
                case 1:
                    switch_lock.setChecked(true);
                    break;
                case 2:
                    switch_ignore.setChecked(false);
                    break;
                case 3:
                    switch_ignore.setChecked(true);
                    break;
            }
        }
    };

    @Override
    protected int initLayout() {
        return R.layout.activity_appinfo;
    }

    @Override
    protected void initView() {
        layout_background=findViewById(R.id.layout_background);
        icon = findViewById(R.id.icon);
        appName = findViewById(R.id.appName);
        layout_progressBar = findViewById(R.id.layout_progressBar);
        progressBar = findViewById(R.id.progressBar);
        appTime = findViewById(R.id.appTime);
        layout_editTime = findViewById(R.id.layout_editTime);
        et_editTime = findViewById(R.id.et_editTime);
        btn_editTime = findViewById(R.id.btn_editTime);
        layout_lock = findViewById(R.id.layout_lock);
        switch_lock = findViewById(R.id.switch_lock);
        layout_ignore = findViewById(R.id.layout_ignore);
        switch_ignore = findViewById(R.id.switch_ignore);
        tv_child=findViewById(R.id.tv_child);
        listView=findViewById(R.id.listView);

        btn_editTime.setOnClickListener(this);
        switch_lock.setOnClickListener(this);
        switch_ignore.setOnClickListener(this);
        layout_lock.setOnTouchListener(this);
        layout_lock.setLongClickable(true);
        layout_ignore.setOnTouchListener(this);
        layout_ignore.setLongClickable(true);
        tv_child.setOnTouchListener(this);
        tv_child.setLongClickable(true);
        listView.setOnTouchListener(this);
        listView.setLongClickable(true);
        layout_background.setOnTouchListener(this);
        layout_background.setLongClickable(true);
    }

    @Override
    protected void initData() {
        mContext = this;
        alertDialog=null;
        builder=null;
        mGestureDetector=new GestureDetector(this,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        type= AppInfoList.type;
        index=AppInfoList.index;
        if (type == 1) appInfo = AppInfoList.appList1.get(index);
        if (type == 2) appInfo = AppInfoList.appList2.get(index);
        if (type == 3) appInfo = AppInfoList.appList3.get(index);

        icon.setImageDrawable(appInfo.getIcon());
        appName.setText(appInfo.getAppName());
        switch_lock.setChecked(appInfo.isLock());
        switch_ignore.setChecked(appInfo.isIgnore());

        if (!appInfo.isLock()) {
            layout_progressBar.setVisibility(View.GONE);
            layout_editTime.setVisibility(View.GONE);
        }else{
            progressBar.setProgress(appInfo.getCurTime()/60);
            progressBar.setMax(appInfo.getMaxTime()/60);
            appTime.setText(appInfo.getCurTime()/60 + "/" + appInfo.getMaxTime()/60);
        }
//        if (!Utils.compareTime()) layout_editTime.setVisibility(View.GONE);
        if(AppInfoList.childList.isEmpty()||type!=1){   //!Utils.compareTime()||
            tv_child.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
        }else updateListView(AppInfoList.childList);

    }

    //onPause后另一个界面会onResume
    @Override
    protected void onPause() {
        super.onPause();
        switch(type){
            case 1:
                AppInfoList.appList1.remove(index);
                AppInfoList.isChanged1=true;
                break;
            case 2:
                AppInfoList.appList2.remove(index);
                AppInfoList.isChanged2=true;
                break;
            case 3:
                AppInfoList.appList3.remove(index);
                AppInfoList.isChanged3=true;
                break;
        }
        if(appInfo.isLock()){
            appInfo.setIgnore(false);
            AppInfoList.appList1.add(appInfo);
            AppInfoList.isChanged1=true;
        }else{
            appInfo.setCurTime(0);
            appInfo.setMaxTime(1800);
            if(appInfo.isIgnore()) {
                AppInfoList.appList3.add(appInfo);
                AppInfoList.isChanged3=true;
            }
            else {
                AppInfoList.appList2.add(appInfo);
                AppInfoList.isChanged2=true;
            }
        }
        AppInfoList.sortAppList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_editTime:
                if(!Utils.compareTime()){
                    Utils.toast(mContext,Utils.PERIOD);
                    break;
                }
                String str=et_editTime.getText().toString();
                if(str.isEmpty()) break;
                int maxTime = Integer.parseInt(str);
                if(maxTime>120){
                    Utils.toast(mContext,"上限2小时");
                    break;
                }
                appInfo.setMaxTime(maxTime*60);
                if (appInfo.getCurTime() > maxTime*60) appInfo.setCurTime(maxTime*60);
                progressBar.setProgress(appInfo.getCurTime()/60);
                progressBar.setMax(appInfo.getMaxTime()/60);
                appTime.setText(appInfo.getCurTime()/60 + "/" + appInfo.getMaxTime()/60);
                Utils.toast(mContext,"设置成功");
                break;
            case R.id.switch_lock:
                if (switch_lock.isChecked()) {
                    if(!appInfo.getAppName().equals("Lock")) appInfo.setLock(true);
                    else {
                        mHandler.sendEmptyMessage(0);
                        Utils.toast(mContext,"设置无效");
                    }
//                    if (Utils.compareTime()) appInfo.setLock(true);
//                    else {
//                        mHandler.sendEmptyMessage(0);
//                        toast(Utils.PERIOD);
//                    }
                } else {
                    if (Utils.compareTime()) appInfo.setLock(false);
                    else {
                        mHandler.sendEmptyMessage(1);
                        Utils.toast(mContext,Utils.PERIOD);
                    }
                }
                break;
            case R.id.switch_ignore:
                if (switch_ignore.isChecked()) {
                    if (Utils.compareTime()) appInfo.setIgnore(true);
                    else {
                        mHandler.sendEmptyMessage(2);
                        Utils.toast(mContext,Utils.PERIOD);
                    }
                } else {
                    if (Utils.compareTime()) appInfo.setIgnore(false);
                    else {
                        mHandler.sendEmptyMessage(3);
                        Utils.toast(mContext,Utils.PERIOD);
                    }
                }
                break;
        }
    }

    public void updateListView(final ArrayList<String> childList) {
        mAdapter = new StringAdapter(mContext, childList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                builder=new AlertDialog.Builder(mContext);
                alertDialog=builder.setIcon(R.mipmap.settings)
                        .setTitle("提示：")
                        .setMessage("是否删除子应用？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                if(Utils.compareTime()){
                                    AppInfoList.childList.remove(i);
                                    updateListView(AppInfoList.childList);
                                    if(AppInfoList.childList.isEmpty()){
                                        tv_child.setVisibility(View.GONE);
                                        listView.setVisibility(View.GONE);
                                        SharedPreferences sharedPreferences = mContext.getSharedPreferences("Lock", Utils.mode);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("childList",null);
                                        editor.commit();
                                    }
                                    Utils.toast(mContext,"删除成功");
                                    Utils.updateChildList(mContext);
                                }else{
                                    Utils.toast(mContext,Utils.PERIOD);
                                }
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });
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
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) { }

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