package com.hazy.today;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telecom.Call;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        String values=intent.getStringExtra("msg");

        int id = Integer.parseInt(values.split(":")[0]);
        String Calldate=values.split(":")[1];
        String name=values.split(":")[2];

        //判断是否完成，完成不提醒
        MyDataBaseHelper my=new MyDataBaseHelper(context,"Thing",null,1);
        SQLiteDatabase db=(SQLiteDatabase) my.getWritableDatabase();
        DataBase database=new DataBase();
        database.setDb(db);
        if (database.select(id).getToday_do()==1)
        {
            return;
        }

        //高版本重复设置闹钟达到低版本中setRepeating相同效果
        PendingIntent pending = PendingIntent.getBroadcast(context, id, intent, 0);
        AlarmManagerUtils.getInstance(context).getUpAlarmManagerWorkOnReceiver(id,pending);

        Calendar c=Calendar.getInstance();
        int week=c.get(Calendar.DAY_OF_WEEK);
        String today="";
        switch (week)
        {
            case 1:today="周日";break;
            case 2:today="周一";break;
            case 3:today="周二";break;
            case 4:today="周三";break;
            case 5:today="周四";break;
            case 6:today="周五";break;
            case 7:today="周六";break;
        }

        if (Calldate!="") {
            for (String call : Calldate.split("=")) {
                if (call.equals(today)) {
                    //Toast.makeText(context,id+"...."+name,Toast.LENGTH_SHORT).show();
                    //满足条件，发送状态栏通知
                    NotificationManager nm;
                    //获得系统服务
                    nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                    //为通知附加一个action
                    Intent mainintent=new Intent(context,MainActivity.class);
                    PendingIntent mainpending=PendingIntent.getActivity(context,0,mainintent,PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mNotification;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = nm.getNotificationChannel("chat");

                        //判断通知渠道的权限是否打开
                        if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                            Intent set = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                            set.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                            set.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
                            context.startActivity(set);
                            Toast.makeText(context, "请手动将通知打开", Toast.LENGTH_SHORT).show();
                        }
                        mNotification= new NotificationCompat.Builder(context,"chat");
                    }
                    else {
                        mNotification = new NotificationCompat.Builder(context);
                    }
                    mNotification.setSmallIcon(R.mipmap.activity_icon);
                    mNotification.setContentTitle("Today");
                    mNotification.setContentText(name);
                    mNotification.setContentIntent(mainpending);

                    Notification notification=mNotification.build();
                    notification.flags|=Notification.FLAG_AUTO_CANCEL;

                    //判断是否添加震动
                    if (MainActivity.setup_shock)
                    {
                        notification.defaults|=Notification.DEFAULT_VIBRATE;
                    }
                    nm.notify(1,notification);
                }
            }
        }

        //问题总结
//        1：进程被杀死，AlarmManager停止工作
//        在Demo运行的过程中发现我们主动杀死进程AlarmManager也就停止运行了。
//        在应用打开或者应用中存在服务在服务重启的时候重新注册一下AlarmManager
//        2：手机重启，AlarmManager停止工作
//        这种情况我们可以注册一个监听手机重启的广播
//        在收到广播的时候重新注册一下AlarmManager就可以了。
//        3：各厂商的“心跳对齐”
//        系统会将你应用强制"心跳对齐"，使你的APP不那么频繁唤醒CPU。比如你APP设定
//        的是2s执行一次，但是实际在各大厂商运行起来是10s一次。
    }
}
