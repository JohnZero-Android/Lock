package com.JohnZero.lock.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.JohnZero.lock.entity.AppInfo;
import com.JohnZero.lock.adapter.AppInfoAdapter;
import com.JohnZero.lock.AppInfoList;
import com.JohnZero.lock.sqlite.MyDBOpenHelper;
import com.JohnZero.lock.service.MyService;
import com.JohnZero.lock.adapter.PasswordAdapter;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;
import java.util.ArrayList;

/**
 * @author: JohnZero
 * @date: 2020-09-03
 **/
public class MainActivity extends BaseActivity implements View.OnClickListener, TextWatcher, View.OnTouchListener, GestureDetector.OnGestureListener {
    Context mContext;
    SQLiteDatabase db;
    MyDBOpenHelper myDBOpenHelper;
    AppInfoAdapter mAdapter;
    MyReceiver myReceiver;
    private boolean mReceiverTag = false;   //广播接受者标识
    GestureDetector mGestureDetector;
    Vibrator vibrator;
    int MINUTE = 0;   //上一次更新数据时间
    boolean onPause = false;
    public static boolean onDestroy = false;
    public static String title = "Lock";
    public static String text = "服务正在运行";
    public static int id = 1;    //2
    public static String channelId = "Channel1"; //Channel2
    public static String channelName = "通知"; //"通知"
    long clickTime = 0;    //设置图标被点击的时间间隔
    int clickNum = 0; //设置图标被连续点击的次数
    public static boolean isAdmin = false;  //是否是管理员
    String serviceName = "com.JohnZero.lock.service.MyService";
    public static float minMove = 250;         //最小滑动距离
    public static float maxMove = 100;           //最大滑动距离
    public static float minVelocity = 2500;    //最小滑动速度

    NotificationManager mNManager;
    Notification notification;
    Notification.Builder builder;

    TextView tv_title;
    Button btn_lock;
    Button btn_unlock;
    Button btn_search;
    ImageView iv_password;
    ImageView iv_settings;
    ListView listView;
    EditText et_search;
    int type = 1;//1：已加密；2：未加密
    public static String searchText = "";
    public static int itemPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log("MainActivity:onCreate");
        Utils.requestDrawOverLays(mContext);
        Utils.requestNotificationListener(mContext);
        Utils.requestNotification(mContext);
        Utils.requestBackgroundStart(mContext);
//        Utils.getAllOpsField(mContext);
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        tv_title = findViewById(R.id.tv_title);
        btn_lock = findViewById(R.id.btn_lock);
        btn_unlock = findViewById(R.id.btn_unlock);
        btn_search = findViewById(R.id.btn_search);
        iv_password=findViewById(R.id.iv_password);
        iv_settings = findViewById(R.id.iv_settings);
        listView = findViewById(R.id.listView);
        et_search = findViewById(R.id.et_search);
        tv_title.setOnClickListener(this);
        btn_lock.setOnClickListener(this);
        btn_unlock.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        iv_password.setOnClickListener(this);
        iv_settings.setOnClickListener(this);
        et_search.addTextChangedListener(this);
        listView.setOnTouchListener(this);
        listView.setLongClickable(true);
    }

    @Override
    protected void initData() {
        mContext = this;
        myDBOpenHelper = new MyDBOpenHelper(mContext, "appList.db", null, 1);
        db = myDBOpenHelper.getWritableDatabase();
        mGestureDetector = new GestureDetector(this, this);
        // 震动效果的系统服务
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.log("MainActivity:onResume");
        onPause = false;
        onDestroy = false;
        updateListView(AppInfoList.search(searchText, type));
        listView.setSelection(itemPosition);
        PasswordAdapter.isShow=false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.log("MainActivity:onPause");
        onPause = true;
    }

    @Override
    protected void onDestroy() {
        onDestroy = true;
        unregisterReceiver();
        Utils.log("MainActivity:onDestroy");
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_title:
                if (Utils.isServiceRunning(mContext, serviceName)) {
                    if (tv_title.getText().toString().equals("Lock")) {
                        Intent intent = new Intent(mContext, MyService.class);
                        stopService(intent);
                        startService(intent);
                        Utils.toast(mContext, "正在启动");
                    } else Utils.toast(mContext, "服务正在运行");
                } else {
                    Utils.toast(mContext, "服务已停止，正重新启动");
                    Utils.initData(mContext, db);
                    btn_lock.performClick();
                }
                break;
            case R.id.btn_lock:
                type = 1;
                searchText = "";
                btn_lock.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape3));
                btn_unlock.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape4));
                updateListView(AppInfoList.appList1);
                break;
            case R.id.btn_unlock:
                type = 2;
                searchText = "";
                btn_lock.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape4));
                btn_unlock.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape3));
                updateListView(AppInfoList.appList2);
                break;
            case R.id.btn_search:
                searchText = et_search.getText().toString();
                updateListView(AppInfoList.search(searchText, type));
                break;
            case R.id.iv_password:
                Intent intent=new Intent(MainActivity.this,PatternLockViewActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.iv_settings:
                if (System.currentTimeMillis() - clickTime < 500) {
                    clickNum++;
                    if (clickNum > 7) {
                        isAdmin = isAdmin ? false : true;
                        String toastText = isAdmin ? "管理员模式已开启" : "管理员模式已关闭";
                        Utils.toast(mContext, toastText);
                        clickNum = 0;
                    }
                } else clickNum = 0;
                clickTime = System.currentTimeMillis();
                if (clickNum == 0) initPopWindow(view);
                break;
        }
    }

    public void updateListView(ArrayList<AppInfo> appList) {
        mAdapter = new AppInfoAdapter(mContext, appList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                itemPosition = i;
                TextView appName = (TextView) view.findViewById(R.id.appName);
                AppInfoList.findItem(appName.getText().toString());
                navigate(AppInfoActivity.class);
            }
        });
    }

    //编辑框的内容发生改变之前的回调方法
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    //编辑框的内容正在发生改变时的回调方法
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    //编辑框的内容改变以后,用户没有继续输入时的回调方法
    @Override
    public void afterTextChanged(Editable editable) {
        searchText = et_search.getText().toString();
        updateListView(AppInfoList.search(searchText, type));
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

        if (Math.abs(endX - beginX) > minMove && Math.abs(endY - beginY) < maxMove && Math.abs(velocityX) > minVelocity) {
            if (type == 1) btn_unlock.performClick();
            else btn_lock.performClick();
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
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;//true和false都会使后退键失效
        }
        return super.onKeyDown(keyCode, event);
    }

    //显示悬浮框
    private void initPopWindow(View v) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_settings, null, false);
        Button btn_timeMaster = (Button) view.findViewById(R.id.btn_timeMaster);
        Button btn_permission = (Button) view.findViewById(R.id.btn_permission);
        Button btn_ignore = (Button) view.findViewById(R.id.btn_ignore);
        Button btn_usageLog = (Button) view.findViewById(R.id.btn_usageLog);
        Button btn_passwordIn=(Button) view.findViewById(R.id.btn_passwordIn);
        Button btn_passwordOut=(Button) view.findViewById(R.id.btn_passwordOut);
        Button btn_get = (Button) view.findViewById(R.id.btn_get);
        Button btn_save = (Button) view.findViewById(R.id.btn_save);
        Button btn_clear = (Button) view.findViewById(R.id.btn_clear);
        Button btn_parameter = (Button) view.findViewById(R.id.btn_parameter);
        Button btn_quit = (Button) view.findViewById(R.id.btn_quit);
        Button btn_help = (Button) view.findViewById(R.id.btn_help);
        Button btn_about = (Button) view.findViewById(R.id.btn_about);
        LinearLayout layout_admin = (LinearLayout) view.findViewById(R.id.layout_admin);
        if (isAdmin) layout_admin.setVisibility(View.VISIBLE);
        else layout_admin.setVisibility(View.GONE);

        final PopupWindow popWindow = new PopupWindow(view, 400, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popWindow.setAnimationStyle(R.anim.anim_pop);  //设置加载动画
        popWindow.setTouchable(true);
        popWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popWindow.showAsDropDown(v, -100, 10);

        btn_timeMaster.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                navigate(TimeMasterActivity.class);
            }
        });
        btn_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(PermissionActivity.class);
            }
        });
        btn_ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(IgnoreListActivity.class);
            }
        });
        btn_usageLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(UsageLogActivity.class);
            }
        });
        btn_passwordIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.passwordIn(mContext);
                Utils.toast(mContext, "导入成功");
            }
        });
        btn_passwordOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,PatternLockViewActivity.class);
                startActivityForResult(intent,2);
            }
        });
        btn_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfoList.getAppList(db);
                AppInfoList.updateList(mContext.getPackageManager());
                Utils.toast(mContext, "获取成功");
                if(type==1) btn_lock.performClick();
                else btn_unlock.performClick();
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfoList.uploadAppList(db);
                Utils.toast(mContext, "保存成功");
            }
        });
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clearData(mContext, db);
                btn_lock.performClick();
                Utils.toast(mContext, "清除成功");
            }
        });
        btn_parameter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ParameterActivity.class);
            }
        });
        btn_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
                onDestroy = true;
                unregisterReceiver(myReceiver);
                finish();
            }
        });
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(HelpActivity.class);
            }
        });
        btn_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(AboutActivity.class);
            }
        });
    }

    //Android8/9/10可以显示,若未显示则是因为未开启通知权限
    void showNotification() {
        mNManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent nIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, nIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, channelId);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.mipmap.ic_launcher);//只能用小图标
        builder.setAutoCancel(false);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(Notification.PRIORITY_HIGH);

        //设置下拉之后显示的图片
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);//是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN);//小红点颜色
            channel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
            mNManager.createNotificationChannel(channel);
        }

        notification = builder.build();
        mNManager.notify(id, notification);
    }

    //MyReceiver接受广播
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "android.intent.action.SCREEN_ON":
                    MyService.isScreenOn = true;
                    tv_title.performClick();
                    break;
                case "android.intent.action.SCREEN_OFF":
                    MyService.isScreenOn = false;
                    break;
                case "com.JohnZero.lock.service.MyService":
                    Utils.getTime();
                    int day = Utils.day;
                    int hour = Utils.hour;
                    int minute = Utils.minute;
                    int second = Utils.second;
                    tv_title.setText(hour + ":" + minute + ":" + second);
                    //清空当日使用时长
                    if (day != Utils.DAY) {
                        SharedPreferences sharedPreferences = getSharedPreferences("TimeMasterActivity", Utils.mode);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isClear",true);
                        editor.commit();
                        for (int i = 0; i < AppInfoList.appList1.size(); i++) {
                            AppInfoList.appList1.get(i).setCurTime(0);
                        }
                        btn_lock.performClick();
                        Utils.updateDay(mContext, day);
                    }
                    //卡屏8s
                    if (minute % 2 == 1 && minute != MINUTE) {
                        MINUTE = minute;
                        if (onPause) {
                            AppInfoList.uploadAppList(db);
                        }
                    }
                    //判断静态数据是否被清空
                    if (AppInfoList.appList1.isEmpty() && AppInfoList.appList2.isEmpty() && AppInfoList.appList3.isEmpty()) {
                        AppInfoList.getAppList(db);
                        AppInfoList.updateList(mContext.getPackageManager());
                        btn_lock.performClick();
                    }
                    break;
                case "com.JohnZero.lock.MyServiceDestroy":
                    startService(new Intent(mContext, MyService.class));
                    break;
                case "com.JohnZero.lock.Lock":
                    if (LockActivity.screenTag||isAdmin) break;
                    LockActivity.screenTag = true;
                    vibrator.vibrate(2000);//振动两秒
                    Utils.toTaskTop(mContext);
                    //调用程序锁界面
                    navigate(LockActivity.class);
                    break;
                case "com.JohnZero.lock.Notification":
                    showNotification();
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
            itFilter.addAction(Intent.ACTION_SCREEN_ON);//亮屏
            itFilter.addAction(Intent.ACTION_SCREEN_OFF);//息屏
            itFilter.addAction("com.JohnZero.lock.service.MyService");
            itFilter.addAction("com.JohnZero.lock.MyServiceDestroy");
            itFilter.addAction("com.JohnZero.lock.Lock");
            itFilter.addAction("com.JohnZero.lock.Notification");   //由Utils.sendNotification()发出该广播
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1:
                if(resultCode==1) navigate(PasswordListActivity.class);
                break;
            case 2:
                if(resultCode==1){
                    try {
                        Utils.passwordOut(mContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Utils.toast(mContext, "导出成功");
                }
                break;
        }
    }
}