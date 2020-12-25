package com.JohnZero.lock.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import com.JohnZero.lock.R;
import com.JohnZero.lock.util.Utils;

/**
 * @author: JohnZero
 * @date: 2020-09-23
 **/
public class PasswordGeneratorActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,SeekBar.OnSeekBarChangeListener{
    private CheckBox cb_number;
    private CheckBox cb_symbol;
    private CheckBox cb_lowerLetter;
    private CheckBox cb_upperLetter;
    private SeekBar sb_length;
    private Button btn_confirm;

    private Context mContext;
    public static boolean[] isChecked=new boolean[]{true,true,true,true};
    public static String[] checkItems=new String[]{"0123456789","`~!@#$￥%^&*()-_=+[]{}\\|;:'\",<.>/?*","abcdefghijklmnopqrstuvwxyz","ABCDEFGHIJKLMNOPQRSTUVWXYZ"};
    public static int length=8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_password_generator;
    }

    @Override
    protected void initView() {
        cb_number=findViewById(R.id.cb_number);
        cb_symbol=findViewById(R.id.cb_symbol);
        cb_lowerLetter=findViewById(R.id.cb_lowerLetter);
        cb_upperLetter=findViewById(R.id.cb_upperLetter);
        sb_length=findViewById(R.id.sb_length);
        btn_confirm=findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(this);
        cb_number.setOnClickListener(this);
        cb_symbol.setOnClickListener(this);
        cb_lowerLetter.setOnClickListener(this);
        cb_upperLetter.setOnClickListener(this);

        cb_number.setOnCheckedChangeListener(this);
        cb_symbol.setOnCheckedChangeListener(this);
        cb_lowerLetter.setOnCheckedChangeListener(this);
        cb_upperLetter.setOnCheckedChangeListener(this);

        cb_number.setChecked(isChecked[0]);
        cb_symbol.setChecked(isChecked[1]);
        cb_lowerLetter.setChecked(isChecked[2]);
        cb_upperLetter.setChecked(isChecked[3]);

        sb_length.setOnSeekBarChangeListener(this);
        sb_length.setEnabled(true);
        sb_length.setProgress(length-4);
        sb_length.setSecondaryProgress(0);
    }

    @Override
    protected void initData() {
        mContext=this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_confirm:
                finish();
                break;
            case R.id.cb_number:
                if(cb_number.isChecked()) isChecked[0]=true;
                else isChecked[0]=false;
                //Utils.log("cb_number:"+isChecked[0]);
                break;
            case R.id.cb_symbol:
                if(cb_symbol.isChecked()) isChecked[1]=true;
                else isChecked[1]=false;
                break;
            case R.id.cb_lowerLetter:
                if(cb_lowerLetter.isChecked()) isChecked[2]=true;
                else isChecked[2]=false;
                break;
            case R.id.cb_upperLetter:
                if(cb_upperLetter.isChecked()) isChecked[3]=true;
                else isChecked[3]=false;
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String str = buttonView.getText().toString();
        if (buttonView.isChecked()) {
//            Utils.toast(mContext,str);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        length=progress+4;
        Utils.toast(mContext,"密码长度："+length);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //触碰SeekBar
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //放开SeekBar
    }

    public static String passwordGenerator(){
        String password="";
        for(int i=0;i<length;i++){
            int type=(int)(Math.random()*4);
            if(!isChecked[type]){
                i--;
                continue;
            }
            int index=(int)(Math.random()*(checkItems[type].length()));
            password+=checkItems[type].charAt(index);
        }
        return password;
    }
}