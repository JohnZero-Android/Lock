package com.JohnZero.lock.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author: JohnZero
 * @date: 2020-09-03
 **/
public abstract class BaseActivity extends AppCompatActivity {
    public static Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initLayout());
        mContext = this;
        initData();
        initView();
    }

    protected abstract int initLayout();

    protected abstract void initData();

    protected abstract void initView();

    public void navigate(Class cls) {
        Intent intent=new Intent(mContext,cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

}
