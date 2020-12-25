package com.JohnZero.lock.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.JohnZero.lock.adapter.PasswordAdapter;
import com.JohnZero.lock.entity.PasswordInfo;
import com.JohnZero.lock.R;
import com.JohnZero.lock.view.TimeDialog;
import com.JohnZero.lock.util.Utils;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: JohnZero
 * @date: 2020-09-23
 **/
public class PasswordListActivity extends BaseActivity implements View.OnClickListener, TextWatcher, View.OnTouchListener, GestureDetector.OnGestureListener {
    EditText et_search;
    Button btn_new;
    RecyclerView recyclerView;
    LinearLayout layout_operation;
    Button btn_selectAll;
    Button btn_selectPart;
    Button btn_delete;
    Button btn_cancel;

    List<PasswordInfo> passwordList = new ArrayList<>();
    List<PasswordInfo> searchResult = new ArrayList<>();
    GestureDetector mGestureDetector;
    PasswordAdapter passwordAdapter;
    public static String searchText = "";
    public static int itemPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log("PasswordListActivity:onCreate");
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_password_list;
    }

    @Override
    protected void initData() {
        mGestureDetector = new GestureDetector(this, this);
    }

    @Override
    protected void initView() {
        et_search = findViewById(R.id.et_search);
        btn_new = findViewById(R.id.btn_new);
        recyclerView = findViewById(R.id.recyclerView);
        layout_operation = findViewById(R.id.layout_operation);
        btn_selectAll = findViewById(R.id.btn_selectAll);
        btn_selectPart = findViewById(R.id.btn_selectPart);
        btn_delete = findViewById(R.id.btn_delete);
        btn_cancel = findViewById(R.id.btn_cancel);

        btn_new.setOnClickListener(this);
        btn_selectAll.setOnClickListener(this);
        btn_selectPart.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        et_search.addTextChangedListener(this);

        recyclerView.setOnTouchListener(this);
        recyclerView.setLongClickable(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.log("PasswordListActivity:onResume");
        updateRecyclerView();
        if (PasswordAdapter.isShow) layout_operation.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.log("PasswordListActivity:onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.log("PasswordListActivity:onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_new:
                PasswordInfoActivity.index = -1;
                navigate(PasswordInfoActivity.class);
                break;
            case R.id.btn_selectAll:
                PasswordInfo passwordInfo = new PasswordInfo();
                passwordInfo.setChecked(true);
                passwordInfo.updateAll("isChecked=0");
                updateRecyclerView();
                break;
            case R.id.btn_selectPart:
                for (int i = 0; i < 2; i++) { //不知为何必须重复点击两次才有效
                    int count = 0;
                    for (PasswordInfo info : searchResult)
                        if (!info.isChecked()) {
                            if (count == 1) {
                                info.setChecked(true);
                                info.save();
                            }
                        } else {
                            count++;
                            if (count == 2) break;
                        }
                    updateRecyclerView();
                }
                break;
            case R.id.btn_delete:
                LitePal.deleteAll(PasswordInfo.class, "isChecked=1");
                updateRecyclerView();
                try {
                    Utils.passwordOut(mContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_cancel:
                PasswordAdapter.isShow = false;
                for (PasswordInfo info : searchResult) {
                    info.setChecked(false);
                    info.save();
                }
                updateRecyclerView();
                layout_operation.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        updateRecyclerView();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // 用户按下屏幕就会触发
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // 如果是按下的时间超过瞬间，而且在按下的时候没有松开或者是拖动的，那么onShowPress就会执行
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // 一次单独的轻击抬起操作,也就是轻击一下屏幕，就是普通点击事件
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null && !PasswordAdapter.isShow) {
            itemPosition = recyclerView.getChildPosition(view);
            TextView tv_modifyTime = (TextView) view.findViewById(R.id.tv_modifyTime);
            String modifyTime = tv_modifyTime.getText().toString();
            PasswordInfo passwordInfo = LitePal.where("modifyTime = ?", modifyTime).find(PasswordInfo.class).get(0);
            boolean isNavigate = true;
            try {
                Calendar calendar1 = Calendar.getInstance();
                Calendar calendar2 = Calendar.getInstance();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = passwordInfo.getAccessTime();
                if (!time.isEmpty()) {
                    Date date = dateFormat.parse(time);
                    calendar2.setTime(date);
                    TimeDialog.year = calendar2.get(Calendar.YEAR);
                    TimeDialog.month = calendar2.get(Calendar.MONTH) + 1;
                    TimeDialog.day = calendar2.get(Calendar.DAY_OF_MONTH);
                    TimeDialog.hour = calendar2.get(Calendar.HOUR_OF_DAY);
                    TimeDialog.minute = calendar2.get(Calendar.MINUTE);
                    if (calendar1.before(calendar2)) isNavigate = false;
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            if (isNavigate || MainActivity.isAdmin) {
                PasswordInfoActivity.index = passwordInfo.getId();
                navigate(PasswordInfoActivity.class);
            } else Utils.toast(mContext, "未到访问时间" + passwordInfo.getAccessTime());
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // 在屏幕上拖动事件
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // 长按触摸屏，超过一定时长，就会触发这个事件
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            itemPosition = recyclerView.getChildPosition(view);
            PasswordAdapter.isShow = true;
            updateRecyclerView();
            layout_operation.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // 滑屏，用户按下触摸屏、快速移动后松开
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if (endX - beginX > MainActivity.minMove && Math.abs(endY - beginY) < MainActivity.maxMove && Math.abs(velocityX) > MainActivity.minVelocity) {   //右滑
            finish();
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void updateRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(false); //定位效果好一点点
        recyclerView.setLayoutManager(linearLayoutManager);
        passwordList = LitePal.order("modifyTime desc").find(PasswordInfo.class);
        search();
        passwordAdapter = new PasswordAdapter(searchResult);
        recyclerView.setAdapter(passwordAdapter);
        recyclerView.scrollToPosition(itemPosition);
    }

    public void search() {
        searchText = et_search.getText().toString().toLowerCase();
        searchResult.clear();
        for (PasswordInfo info : passwordList) {
            String title = info.getTitle().toLowerCase();
            if (title.contains(searchText))
                searchResult.add(info);
        }
    }

    public void checkBoxClick(View view) {
        CheckBox checkBox = (CheckBox) view;
        View viewParent = (View) view.getParent();
        TextView tv_modifyTime = (TextView) viewParent.findViewById(R.id.tv_modifyTime);
        String modifyTime = tv_modifyTime.getText().toString();
        PasswordInfo passwordInfo = LitePal.where("modifyTime = ?", modifyTime).find(PasswordInfo.class).get(0);
        if (checkBox.isChecked()) passwordInfo.setChecked(true);
        else passwordInfo.setChecked(false);
        passwordInfo.save();
    }

}