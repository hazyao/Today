package com.hazy.today;

import java.io.Serializable;

/**
 * Created by Master on 2018/4/8.
 */

public class Thing implements Serializable{
    private int Id;
    private String Name="";
    private String CreateDate="";//创建日期，格式：2018-4-3
    private String CallDate="";//提醒日期，格式：一=二=三=四=五=六=七
    private String CallTime="";//提醒时间，格式：12:00
    private String FinishDateRecord="";//完成日期汇总，格式：2018-4-3=2018-4-4|,注意结尾是=
    private int Today_do=0;

    //构造器
    public void setId(int id)
    {
        Id=id;
    }
    public int getId()
    {
        return Id;
    }

    public void setName(String name)
    {
        this.Name=name;
    }
    public String getName()
    {
        return Name;
    }

    public void setCreateDate(String CreateDate)
    {
        this.CreateDate=CreateDate;
    }
    public String getCreateDate()
    {
        return CreateDate;
    }

    public void setCallTime(String CallTime)
    {
        this.CallTime=CallTime;
    }
    public String getCallTime()
    {
        return CallTime;
    }

    public void setCallDate(String CallDate)
    {
        this.CallDate=CallDate;
    }
    public String getCallDate()
    {
        return CallDate;
    }

    public void setFinishDateRecord(String FinishDateRecord)
    {
        this.FinishDateRecord=FinishDateRecord;
    }
    public String getFinishDateRecord()
    {
        return FinishDateRecord;
    }

    public void setToday_do(int today_do){this.Today_do=today_do;}
    public int getToday_do(){return Today_do;}
}
