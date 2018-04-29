package com.hazy.today;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Master on 2018/4/8.
 */

public class MyDataBaseHelper extends SQLiteOpenHelper {
    private final String createdb="create table Thing(" +
            //primary key 将id列设为主键    autoincrement表示id列是自增长的
            "Id integer primary key autoincrement," +
            "Name text," +
            "CreateDate text,"+
            "CallDate text,"+
            "CallTime text,"+
            "FinishDateRecord text,"+
            "Today_do int)";
    private Context Mycontext;
    //构造方法：第一个参数Context
    // 第二个参数数据库名
    // 第三个参数cursor允许我们在查询数据的时候返回一个自定义的光标位置
    // 一般传入的都是null，第四个参数表示目前库的版本号（用于对库进行升级）
    public  MyDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory , int version){
        super(context,name ,factory,version);
        Mycontext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //调用SQLiteDatabase中的execSQL（）执行建表语句。
        db.execSQL(createdb);
        //db.execSQL("insert into Book(id,shu,name) values(?,?,?)",new String[]{"1","22","安卓"});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
