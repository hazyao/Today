package com.hazy.today;

import android.app.PendingIntent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Master on 2018/4/8.
 */

class DataBase {
    //定义一个datebase对象
    private SQLiteDatabase db=null;

    //构造函数
    //需要传入 :MyDataBaseHelper my=new MyDataBaseHelper(...)
    // SQLiteDatabase db=my.getWritableDatabase()
    public void setDb(SQLiteDatabase db){this.db=db;}

    //增
    //参数为表的字段，最后一个参数可为""
    public Boolean insert(Thing thing)
    {
        try {
            Object[] items=new Object[]{thing.getName(),thing.getCreateDate(),thing.getCallDate(),thing.getCallTime(),thing.getFinishDateRecord(),thing.getToday_do()};
            db.execSQL("insert into Thing(Name,CreateDate,CallDate,CallTime,FinishDateRecord,Today_do) values(?,?,?,?,?,?)",items);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //删
    //基于id删除数据
    public Boolean delete(Thing thing)
    {
        try {
            db.execSQL("delete from Thing where Id="+thing.getId());
            //删除对应闹钟
            AlarmManagerUtils.CancelAlarmManagerWorkOnReceiver(thing.getId());
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //查
    //基于id查询一条数据
    public Thing select(int id)
    {
        Thing t=new Thing();
        Cursor c=db.rawQuery("select * from Thing where Id="+id,null);
        if (c.moveToFirst())
        {
            do {
                t.setId(c.getInt(0));
                t.setName(c.getString(1));
                t.setCreateDate(c.getString(2));
                t.setCallDate(c.getString(3));
                t.setCallTime(c.getString(4));
                t.setFinishDateRecord(c.getString(5));
                t.setToday_do(c.getInt(6));
            }while (c.moveToNext());
        }
        c.close();
        return t;
    }

    //查
    //查询所有
    public ArrayList<Thing> selectALL()
    {
        ArrayList<Thing> tALL=new ArrayList<>();
        Cursor c=db.rawQuery("select * from Thing",null);
        if (c.moveToFirst())
        {
            do {
                Thing t=new Thing();
                t.setId(c.getInt(0));
                t.setName(c.getString(1));
                t.setCreateDate(c.getString(2));
                t.setCallDate(c.getString(3));
                t.setCallTime(c.getString(4));
                t.setFinishDateRecord(c.getString(5));
                t.setToday_do(c.getInt(6));
                tALL.add(t);
            }while (c.moveToNext());
        }
        c.close();
        return tALL;
    }

    //改
    //创建时间不可更改
    public Boolean update(Thing thing)
    {
        try {
            Object[] items=new Object[]{thing.getName(),thing.getCallDate(),thing.getCallTime(),thing.getFinishDateRecord(),thing.getToday_do(),thing.getId()};
            db.execSQL("update Thing set Name=?,CallDate=?,CallTime=?,FinishDateRecord=?,Today_do=? where Id=?",items);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //关闭数据库连接
    public boolean close()
    {
        try
        {
            db.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //计算习惯坚持的天数，连续
    public int count(int id) {

        //获得所有完成的日期
        int cou = 0;
        Thing t_count = new Thing();
        t_count = select(id);
        String all=t_count.getFinishDateRecord();

        //没有完成一次
        if (all.length()==0)
        {
            return 0;
        }

        //获得2017-1-1到2027-1-1的所有时间的字符串数组
        ArrayList<String> every=new ArrayList<>();
        try
        {
            Calendar start=Calendar.getInstance();
            Calendar end=Calendar.getInstance();
            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
            Date startDate=df.parse("2016-12-31");
            start.setTime(startDate);
            Date endDate=df.parse("2027-1-1");
            end.setTime(endDate);
            while(true)
            {
                start.add(Calendar.DAY_OF_YEAR,1);
                if (start.getTimeInMillis()<end.getTimeInMillis())
                {
                    every.add(df.format(start.getTime()));
                }
                else
                    break;
            }
        }
        catch (Exception e)
        {
            return -2;
        }

        //计算
        String[] al=all.split("=");
        int value=every.indexOf(al[0]);
        if (value!=-1)
        {
            for (int i = 0; i < al.length; i++)
            {
                if (al[i].equals(every.get(value)))
                {
                    value++;
                    cou++;
                }
                else
                {
                    cou=0;
                    i--;
                    value=every.indexOf(al[i]);
                }
            }
        }
        else
        {
            return -3;
        }

        return cou;//返回最后一次连续时间

        //下面是通过自己设计的算法计算，但是比较臃肿
//        String[] al=all.split("\\|");
//        //设置游标,代表满足连续的下一天
//        int year= Integer.parseInt(al[0].split("-")[0]);
//        int month=Integer.parseInt(al[0].split("-")[1]);
//        int day=Integer.parseInt(al[0].split("-")[2]);
//        for (String e:al)
//        {
//            String[] yMd=e.split("-");
//            //代表要检测的這一天
//            int y= Integer.parseInt(yMd[0]);
//            int M=Integer.parseInt(yMd[1]);
//            int d=Integer.parseInt(yMd[2]);
//            if (y==year&&M==month&&d==day)
//            {
//                cou++;
//                //13578  10 12月为31天
//                if (month==1||month==3||month==5||month==7||month==8||month==10||month==12)
//                {
//                    if (day<31)
//                        day++;
//                    else
//                    {
//                        day=1;
//                        if (month<12)
//                            month++;
//                        else
//                        {
//                            month=1;
//                            year++;
//                        }
//                    }
//                }
//                else//不会跨年
//                {
//                    if ((year%4==0&&year%100!=0)&&month==2)//闰年的二月
//                    {
//                        if (day<29)
//                            day++;
//                        else
//                        {
//                            day=1;
//                            month++;
//                        }
//                    }
//                    else if (month==2)//不是闰年的二月
//                    {
//                        if (day<28)
//                            day++;
//                        else
//                        {
//                            day=1;
//                            month++;
//                        }
//                    }
//                    else//4 6 9 11月为30 天
//                    {
//                        if (day<30)
//                            day++;
//                        else
//                        {
//                            day=1;
//                            month++;
//                        }
//                    }
//                }
//            }
//            else//连续断掉
//            {
//                cou=0;
//                //将断掉這一天，作为下一次连续的开始
//                year=Integer.parseInt(yMd[0]);
//                month=Integer.parseInt(yMd[1]);
//                day=Integer.parseInt(yMd[2]);
//            }
//        }
    }
}

