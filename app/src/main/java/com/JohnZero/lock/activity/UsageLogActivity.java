package com.JohnZero.lock.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import com.JohnZero.lock.AppInfoList;
import com.JohnZero.lock.R;
import com.JohnZero.lock.adapter.StringAdapter;
import com.JohnZero.lock.util.Utils;
import java.util.ArrayList;

/**
 * @author: JohnZero
 * @date: 2020-09-11
 **/
public class UsageLogActivity extends BaseActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {
    Context mContext;
    StringAdapter mAdapter;
    ListView listView;
    AlertDialog alertDialog;
    AlertDialog.Builder builder;
    GestureDetector mGestureDetector;

    @Override
    protected int initLayout() {
        return R.layout.activity_usagelog;
    }

    @Override
    protected void initView() {
        listView = findViewById(R.id.listView);
        listView.setOnTouchListener(this);
        listView.setLongClickable(true);
    }

    @Override
    protected void initData() {
        mContext = this;
        alertDialog = null;
        builder = null;
        mGestureDetector = new GestureDetector(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String> usageList=new ArrayList<>();
        for(int i = 0; i< AppInfoList.usageList.size(); i++){
            usageList.add(AppInfoList.usageList.get(i));
        }
        updateListView(usageList);
    }

    public void updateListView(final ArrayList<String> usageList) {
        mAdapter = new StringAdapter(mContext, usageList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                builder = new AlertDialog.Builder(mContext);
                alertDialog = builder.setIcon(R.mipmap.settings)
                        .setTitle("提示：")
                        .setMessage("是否添加为子应用？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String appName = usageList.get(i);
                                for (int j = 0; j < AppInfoList.childList.size(); j++) {
                                    if (AppInfoList.childList.get(j).equals(appName)) {
                                        AppInfoList.childList.remove(j);
                                        break;
                                    }
                                }
                                AppInfoList.childList.add(0, appName);
                                Utils.toast(mContext,"添加成功");
                                Utils.updateChildList(mContext);
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
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if (endX - beginX > MainActivity.minMove && Math.abs(endY - beginY) < MainActivity.maxMove && Math.abs(velocityX) > MainActivity.minVelocity) {   //右滑
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