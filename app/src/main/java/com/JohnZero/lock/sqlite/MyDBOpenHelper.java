package com.JohnZero.lock.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author: JohnZero
 * @date: 2020-09-07
 **/
public class MyDBOpenHelper extends SQLiteOpenHelper {
    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    @Override
    //数据库第一次创建时被调用 INTEGER VARCHAR(20)
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    //软件版本号发生改变时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("ALTER TABLE appList1 ADD packageName VARCHAR(12) NULL");
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE appList1(appName VARCHAR(20),curTime INTEGER,maxTime INTEGER,isLock INTEGER,isIgnore INTEGER)");
        db.execSQL("CREATE TABLE appList2(appName VARCHAR(20),curTime INTEGER,maxTime INTEGER,isLock INTEGER,isIgnore INTEGER)");
        db.execSQL("CREATE TABLE appList3(appName VARCHAR(20),curTime INTEGER,maxTime INTEGER,isLock INTEGER,isIgnore INTEGER)");
    }
}