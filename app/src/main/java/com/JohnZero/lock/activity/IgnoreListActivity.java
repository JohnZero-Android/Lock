package com.JohnZero.lock.activity;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.JohnZero.lock.entity.AppInfo;
import com.JohnZero.lock.adapter.AppInfoAdapter;
import com.JohnZero.lock.AppInfoList;
import com.JohnZero.lock.R;
import java.util.ArrayList;

/**
 * @author: JohnZero
 * @date: 2020-09-11
 **/
public class IgnoreListActivity extends BaseActivity implements View.OnClickListener, TextWatcher,View.OnTouchListener, GestureDetector.OnGestureListener{
    String searchText="";
    int itemPosition=0;
    Context mContext;
    AppInfoAdapter mAdapter;
    GestureDetector mGestureDetector;

    EditText et_search;
    Button btn_search;
    ListView listView;

    @Override
    protected int initLayout() {
        return R.layout.activity_ignorelist;
    }

    @Override
    protected void initView() {
        et_search = findViewById(R.id.et_search);
        btn_search = findViewById(R.id.btn_search);
        listView = findViewById(R.id.listView);
        btn_search.setOnClickListener(this);
        et_search.addTextChangedListener(this);
        listView.setOnTouchListener(this);
        listView.setLongClickable(true);
    }

    @Override
    protected void initData() {
        mContext = this;
        mGestureDetector=new GestureDetector(this,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView(AppInfoList.search(searchText,3));
        listView.setSelection(itemPosition);
    }

    public void updateListView(ArrayList<AppInfo> appList) {
        mAdapter = new AppInfoAdapter(mContext, appList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                itemPosition=i;
                TextView appName=(TextView)view.findViewById(R.id.appName);
                AppInfoList.findItem(appName.getText().toString());
                startActivity(new Intent(mContext, AppInfoActivity.class));
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_search:
                searchText=et_search.getText().toString();
                updateListView(AppInfoList.search(searchText,3));
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        searchText=et_search.getText().toString();
        updateListView(AppInfoList.search(searchText,3));
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