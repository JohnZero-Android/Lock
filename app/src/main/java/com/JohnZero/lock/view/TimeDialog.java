package com.JohnZero.lock.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;
import com.JohnZero.lock.activity.MainActivity;
import com.JohnZero.lock.activity.PasswordInfoActivity;
import java.util.Calendar;

/**
 * @author: JohnZero
 * @date: 2020-09-23
 **/
public class TimeDialog extends Dialog {
    public static boolean tag = true; //ture则显示日期选择器，false显示时间选择器
    public static int year=0;
    public static int month;
    public static int day;
    public static int hour;
    public static int minute;

    public TimeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder implements View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener {
        private View mLayout;
        private LinearLayout linearLayout;
        private ImageView tv_left;
        private ImageView tv_right;
        private DatePicker datePicker;
        private TimePicker timePicker;
        private Button btn_confirm;
        private GestureDetector mGestureDetector;
        private TimeDialog timeDialog;

        @RequiresApi(api = Build.VERSION_CODES.O)
        public Builder(Context context) {
            mGestureDetector = new GestureDetector(context, this);
            timeDialog = new TimeDialog(context, R.style.MyDialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //加载布局文件
            mLayout = inflater.inflate(R.layout.layout_timedialog, null, false);
            //添加布局文件到 Dialog
            timeDialog.addContentView(mLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout = mLayout.findViewById(R.id.linearLayout);
            tv_left = mLayout.findViewById(R.id.iv_left);
            tv_right = mLayout.findViewById(R.id.iv_right);
            datePicker = mLayout.findViewById(R.id.datePicker);
            timePicker = mLayout.findViewById(R.id.timePicker);
            btn_confirm = mLayout.findViewById(R.id.btn_confirm);
            tv_left.setOnClickListener(this);
            tv_right.setOnClickListener(this);
            btn_confirm.setOnClickListener(this);
            linearLayout.setOnTouchListener(this);
            linearLayout.setLongClickable(true);
            datePicker.setOnTouchListener(this);
            datePicker.setLongClickable(true);
            timePicker.setOnTouchListener(this);
            timePicker.setLongClickable(true);

            if (year == 0) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH)+1;
                day = calendar.get(Calendar.DAY_OF_MONTH);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            }
            datePicker.updateDate(year,month-1,day);
            timePicker.setHour(hour);
            timePicker.setMinute(minute);

            datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year1, int monthOfYear, int dayOfMonth) {
//                    Utils.log(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);
                    year = year1;
                    month = monthOfYear + 1;
                    day = dayOfMonth;
                }
            });
            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute1) {
//                    Utils.log(hourOfDay + ":" + minute);
                    hour = hourOfDay;
                    minute = minute1;
                }
            });

        }

        public TimeDialog create() {
            timeDialog.setContentView(mLayout);
            timeDialog.setCancelable(true);                //用户可以点击后退键关闭 Dialog
            timeDialog.setCanceledOnTouchOutside(true);   //用户可以点击外部来关闭 Dialog
            return timeDialog;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_left:
                    toggle();
                    break;
                case R.id.iv_right:
                    toggle();
                    break;
                case R.id.btn_confirm:
                    PasswordInfoActivity.newAccessTime = year + "-" + Utils.prefix(month) + "-" + Utils.prefix(day) + " " + Utils.prefix(hour) + ":" + Utils.prefix(minute) + ":00";
                    timeDialog.dismiss();
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

            if (endX - beginX > MainActivity.minMove && Math.abs(endY - beginY) < MainActivity.maxMove && Math.abs(velocityX) > MainActivity.minVelocity) {   //右滑
                toggle();
            }
            return false;
        }

        //不能省略
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return mGestureDetector.onTouchEvent(motionEvent);
        }

        public void toggle() {
            if (tag) {
                tag = false;
                datePicker.setVisibility(View.GONE);
                timePicker.setVisibility(View.VISIBLE);
            } else {
                tag = true;
                datePicker.setVisibility(View.VISIBLE);
                timePicker.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        year = 0;
    }
}
