package com.hazy.today;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmManagerUtils {
    private static final long TIME_INTERVAL = 5 * 1000;//闹钟间隔24小时
    private long TIME_START;//开始时间
    private Context context;
    private static AlarmManager am;
    private static PendingIntent pendingIntent;
    private static Map<Integer,PendingIntent> all_pending=new HashMap<>();//使用静态变量存储对应id的intent，以便删除

    //
    private AlarmManagerUtils(Context aContext) {
        this.context = aContext;
    }

    //返回一个对象,且整个程序只有這一个对象
    private static AlarmManagerUtils instance = null;

    public static AlarmManagerUtils getInstance(Context aContext) {
        if (instance == null) {
            synchronized (AlarmManagerUtils.class) {
                if (instance == null) {
                    instance = new AlarmManagerUtils(aContext);
                }
            }
        }
        return instance;
    }

    //完成对Intent的封装
    public void createGetUpAlarmManager(int id,String name,String Calldate) {
        Log.i("创建pending","pending");
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("ELITOR_CLOCK");
        //用intent发送Thing对象
        //Serializable方式.....序列化方式，简单，但是开销十分大，因为需要将整个对象序列化
        //Parcelable方式.....较高效，因为可以自定义选择需要传送的数据，但是实现代码较多可用插件实现
        //JSON方式.....需要导入GOSN包
        intent.putExtra("msg",id+":"+Calldate+":"+name);
        pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        //创建一个闹钟，就记录一个闹钟
        all_pending.put(id,pendingIntent);
    }

    @SuppressLint("NewApi")
    //第一次执行闹钟
    public void getUpAlarmManagerStartWork(Calendar c) {
        TIME_START=c.getTimeInMillis();
        //版本适配
        // 6.0及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("第一次注册闹钟6.0以上","first,6.0");
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    TIME_START, pendingIntent);
        }
        // 4.4及以上
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.i("第一次注册闹钟4.4-6.0","first,4.4-6.0");
            am.setExact(AlarmManager.RTC_WAKEUP, TIME_START,
                    pendingIntent);
        }
        else {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    TIME_START, TIME_INTERVAL, pendingIntent);
        }
    }

    @SuppressLint("NewApi")
    //高版本重复注册实现repeat效果
    //传入有id的pendingintent，更新对应的闹钟
    public void getUpAlarmManagerWorkOnReceiver(int id,PendingIntent pending) {
        //高版本重复设置闹钟达到低版本中setRepeating相同效果
        // 6.0及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("重复闹钟6.0","repeat6.0");
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + TIME_INTERVAL, pending);
        }
        // 4.4及以上
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.i("重复闹钟4.4","repeat4.4-6.0");
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + TIME_INTERVAL, pending);
        }
        //因为重复set,所以更新all_pending对应的pendingintent
        //因为实现重复闹钟，在onreceiver中的参数content与第一次执行闹钟的content不一致
        // 不更新会导致cancel无效
        //因为cancel判定pendingintent和id一致才取消闹钟
        all_pending.put(id,pending);
    }

    @SuppressLint("NewApi")
    //高版本重复注册实现repeat效果
    //传入有id的pendingintent，更新对应的闹钟
    public static void CancelAlarmManagerWorkOnReceiver(int id) {
        Log.i("cancel","cancel");
        am.cancel(all_pending.get(id));
    }
}
