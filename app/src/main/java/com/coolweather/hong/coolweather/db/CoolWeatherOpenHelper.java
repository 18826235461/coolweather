package com.coolweather.hong.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 11603 on 2016/11/5.
 */

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {

    //Province 表建表语句
    private static final String CREATE_PROVONCE = "create table Province{" +
            "id integer primary key autoincrement, " +
            "province_name text," +
            "province_code text} ";

    //City 表建表语句
    private static final String CREATE_CITY = "create table City{" +
            "id integer primary key autoincrement, " +
            "city_name text, " +
            "city_code text, " +
            "province_id integer}";

    //County 表建表语句
    private static final String CREATE_COUNTY = "create table County{" +
            "id integer primary key autoincrement. " +
            "count_name text, " +
            "count_code text, " +
            "city_id integer}";

     public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表
        db.execSQL(CREATE_PROVONCE);//创建province表
        db.execSQL(CREATE_CITY);//创建city表
        db.execSQL(CREATE_COUNTY);//创建county表

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
