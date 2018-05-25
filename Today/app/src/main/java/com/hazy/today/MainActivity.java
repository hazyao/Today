package com.hazy.today;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout layout,menu,mainLayout;
    private Button user;
    private Vibrator vibrator;
    private ImageView container;
    private Dialog dialog;
    private MySpinner repeat,remind;//重复和提醒Spinner
    private EditText habitName;//添加对话框输入习惯标题
    private TextView repeatText;//重复选项的提示
    private DataBase database;
    private ArrayList<Thing> list=new ArrayList<>();//接收数据库数据的动态数组
    public static Thing onTouchHabit;
    public static boolean setup_shock;
    private Thing t_eit;//编辑习惯时用在addHabit
    //获得手机宽
    private boolean shock;//逻辑控制，只震动一次
    double del_x_min;
    double del_x_max;
    double tod_x_min;
    double tod_x_max;
    double eit_x_min;
    double eit_x_max;
    double y_min;
    double y_max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*状态栏透明*/
        Window statusBarBefore = getWindow();
        statusBarBefore.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        statusBarBefore.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        statusBarBefore.setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);
    initView();
        load();
    }

    private void initView() {
        //启动一像素activity开关屏监听服务
        //LiveService.toLiveService(MainActivity.this);
        //android8.0以上通知栏适配,创建通知渠道
        //创建之后不会重复创建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "提醒";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        /*获取手机震动服务  */
        vibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        //vibrator.vibrate(new long[]{0,100},-1);
        MyDataBaseHelper my=new MyDataBaseHelper(this,"Thing",null,1);
        SQLiteDatabase db=(SQLiteDatabase) my.getWritableDatabase();
        database=new DataBase();
        database.setDb(db);
        menu = findViewById(R.id.menu);
        layout = findViewById(R.id.layout);
        layout.setVisibility(View.VISIBLE);
        layout.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            boolean menuVisibility = false;
            @Override
            public void onDoubleClick() {
                if (menuVisibility == false){
                    Animation alphaAnimation = new AlphaAnimation(0f,1f);
                    alphaAnimation.setDuration(500);
                    menu.startAnimation(alphaAnimation);
                    menu.setVisibility(View.VISIBLE);
                    menuVisibility = true;
                    }else {
                    menu.setVisibility(View.GONE);
                    menuVisibility = false;
                }
            }
        }));
        user  = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });
        container = findViewById(R.id.container);
        container.setVisibility(View.GONE);
        mainLayout = findViewById(R.id.mainLayout);

        //获得手机宽
        WindowManager manager = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
        int width = manager.getDefaultDisplay().getWidth();
        int height = manager.getDefaultDisplay().getHeight();
        del_x_min=width*0.176;
        del_x_max=width*0.287;
        tod_x_min=width*0.444;
        tod_x_max=width*0.555;
        eit_x_min=width*0.722;
        eit_x_max=width*0.833;
        y_min=height*0.766;
        y_max=height*0.828;

        //判断是否是第一次打开APP，是则打开引导页
        //使用shared存放用户设置的数据
        SharedPreferences sp=getSharedPreferences("remindshock",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp.edit();
        if (sp.getBoolean("first",true))
        {
            Intent intent=new Intent(this,GuideActivity.class);
            startActivity(intent);
            editor.putBoolean("first",false);
            editor.commit();
        }
    }

    /*加载习惯*/
    public void load() {
        list=database.selectALL();
        //获得今天日期
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String date=dateFormat.format(new Date());
        for (Thing t:list)
        {
            listHabits all=new listHabits(this,null);
            if (t.getToday_do()==1)
            {
                String[] t_today=t.getFinishDateRecord().split("=");
                all.getText().setTextColor(Color.parseColor("#A9A9A9"));
                if (!t_today[t_today.length-1].equals(date))
                {
                    //新的一天状态置为未完成
                    t.setToday_do(0);
                    database.update(t);
                }
            }else
            {
                all.getText().setTextColor(Color.parseColor("#000000"));
            }
            all.setId(t.getId());
            all.getText().setText(t.getName());
            all.getBut().setText(database.count(t.getId())+" 天");
            all.setBackgroundResource(R.drawable.text_frame);
            all.setAlpha(0.8f);
            all.setTranslationZ(10f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(65,25,65,25);
            all.setLayoutParams(layoutParams);
            /*习惯加载动画*/
            Animation alphaAnimation = new AlphaAnimation(0f,0.8f);
            alphaAnimation.setDuration(500);
            all.startAnimation(alphaAnimation);
            layout.addView(all);
            all.setOnTouchListener(new viewOnTouch());

            //重新加载闹钟
            if (t.getCallTime().equals(""))
            {
                continue;
            }
            Calendar initA = Calendar.getInstance();
            Calendar c_cur=Calendar.getInstance();
            initA.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t.getCallTime().split(":")[0]));
            initA.set(Calendar.MINUTE, Integer.parseInt(t.getCallTime().split(":")[1]));
            while (initA.getTimeInMillis()<c_cur.getTimeInMillis())
            {
                initA.set(Calendar.DAY_OF_YEAR,initA.get(Calendar.DAY_OF_YEAR)+1);
            }
            initAlarm(MainActivity.this,t.getId(),t.getName(),t.getCallDate(),initA);
        }
    }

    /*注册闹钟*/
    public static void initAlarm(Context context,int id,String name,String Calldate,Calendar c) {
        Log.i("init","init");
        AlarmManagerUtils alarmManagerUtils = AlarmManagerUtils.getInstance(context);
        alarmManagerUtils.createGetUpAlarmManager(id,name,Calldate);
        alarmManagerUtils.getUpAlarmManagerStartWork(c);
    }

    /*获得最大的ID，解决添加了不能马上在onTouch看见标题，及其他的一些问题*/
    public int maxId() {
        int i=0;
        ArrayList<Thing> all=new ArrayList<>();
        all=database.selectALL();
        for (Thing t:all)
            i=t.getId();
        return i;
    }

    /*添加习惯*/
    public void addHabit(final View view){
        Log.i("add","add");
        //判断习惯数量是否达到上限
        list=database.selectALL();
        if (list.size()>=6) {
            if(!(view instanceof listHabits)) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setMessage("最多只能添加6个习惯哦").setTitle(" (>_<)");
                AlertDialog alertDialog = builder.create();
                Window window = alertDialog.getWindow();
                window.setWindowAnimations(R.style.DialogAnim);
                alertDialog.show();
                return;
            }
        }
        final Thing t = new Thing();
        //定义对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加习惯");
        final View add = getLayoutInflater().inflate(R.layout.dialog_add,null);
        remind = add.findViewById(R.id.remind);
        habitName = add.findViewById(R.id.habitName);
        repeat = add.findViewById(R.id.repeat);
        repeatText = add.findViewById(R.id.repeatText);
        if (view instanceof listHabits)
        {
            builder.setTitle("修改习惯");
            t_eit=database.select(view.getId());
            habitName.setText(t_eit.getName());
        }
        builder.setView(add);//设置对话提示框
        builder.setCancelable(true);
        //设置确定按钮，并做事件处理
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //获得习惯名称,并且判断是否为空
                if (habitName.getText().toString().trim().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("习惯名不能为空").setTitle("添加失败");
                    AlertDialog alertDialog = builder.create();
                    Window window = alertDialog.getWindow();
                    window.setWindowAnimations(R.style.DialogAnim);
                    alertDialog.show();
                    return;
                }

                t.setName(habitName.getText().toString());
                //获得习惯创建时间：YYYY-MM-dd
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateFormat.format(new Date());
                if (view instanceof listHabits) {
                    t.setToday_do(t_eit.getToday_do());
                    t.setCreateDate(t_eit.getCreateDate());
                    t.setFinishDateRecord(t_eit.getFinishDateRecord());
                    layout.removeView(view);
                    database.delete(t_eit);
                } else {
                    t.setCreateDate(date);
                }
                //提交数据存入数据库,注册闹钟
                database.insert(t);
                //如果不提醒，则不注册闹钟
                if (t.getCallTime() != "") {
                    Calendar initA = Calendar.getInstance();
                    initA.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t.getCallTime().split(":")[0]));
                    initA.set(Calendar.MINUTE, Integer.parseInt(t.getCallTime().split(":")[1]));
                    //如果设置时间小于当前时间，不修改会立即出发广播
                    Calendar c_cur=Calendar.getInstance();
                    if (initA.getTimeInMillis()<c_cur.getTimeInMillis())
                    {
                        initA.set(Calendar.DAY_OF_YEAR,initA.get(Calendar.DAY_OF_YEAR)+1);
                    }
                    initAlarm(MainActivity.this, maxId(), t.getName(), t.getCallDate(), initA);
                }
                //添加一个显示控件
                listHabits all = new listHabits(MainActivity.this, null);
                all.setId(maxId());
                if (view instanceof listHabits) {
                    all.getBut().setText(database.count(maxId()) + " 天");
                    if (t.getToday_do() == 1) {
                        all.getText().setTextColor(Color.parseColor("#000000"));
                    }
                } else {
                    all.getBut().setText("0 天");
                }
                all.getText().setText(t.getName());
                all.setBackgroundResource(R.drawable.text_frame);
                all.setTranslationZ(10f);
                all.setAlpha(0.8f);
                //设置testView大小和Margin
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(65, 25, 65, 25);
                all.setLayoutParams(layoutParams);
                Animation alphaAnimation = new AlphaAnimation(0f,0.8f);
                alphaAnimation.setDuration(500);
                all.startAnimation(alphaAnimation);
                layout.addView(all);
                all.setOnTouchListener(new viewOnTouch());
            }
        });
        //设置取消按钮，并做事件处理
        builder.setNegativeButton("放弃",null);

        //重复选项绑定数据源
        List<String> data_list=new ArrayList<>();
        data_list.add("每天");
        data_list.add("自定义");
        ArrayAdapter<String> arr_adapter=new ArrayAdapter<String>(this,R.layout.simple_spinner_item,data_list);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeat.setAdapter(arr_adapter);
        //提醒选项绑定数据源
        List<String> data_list1=new ArrayList<>();
        data_list1.add("打开");
        data_list1.add("关闭");
        ArrayAdapter<String> arr_adapter1=new ArrayAdapter<String>(this,R.layout.simple_spinner_item,data_list1);
        arr_adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        remind.setAdapter(arr_adapter1);

        //提醒选项spinner的监听，默认不提醒,打开提醒默认每天提醒
        remind.setSelection(1);
        remind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i==1)
                {
                    //不提醒
                    t.setCallDate("");
                    t.setCallTime("");
                }
                else if (i==0)
                {
                    //默认每天提醒
                    t.setCallDate("周一=周二=周三=周四=周五=周六=周日=");
                    //显示提醒时间选择,24小时制
                    Calendar c=Calendar.getInstance();
                    int hour=c.get(Calendar.HOUR_OF_DAY);
                    int minute=c.get(Calendar.MINUTE);
                    TimePickerDialog tpd=new TimePickerDialog(MainActivity.this,R.style.timePicker,new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                            t.setCallTime(i+":"+i1);
                            //Toast.makeText(MainActivity.this,i+":"+i1,Toast.LENGTH_SHORT).show();
                        }
                    }, hour, minute, true);
                    //timepicker取消按钮onclick事件
                    tpd.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            remind.setSelection(1);
                        }
                    });
                    tpd.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //选择提醒的方式，重复是每天，或者自定义.监听spinner
        repeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //先判断是否提醒
                if (remind.getSelectedItem().toString().equals("关闭"))
                {
                    //可能会导致提醒功能关闭，但CallTime为每天，不过注册闹钟有判断
                    repeat.setSelection(0);
                    return;
                }
                //判断为打开提醒
                if (i==0)//每天提醒
                {
                    t.setCallDate("周一=周二=周三=周四=周五=周六=周日=");
                }
                else if (i==1)//自定义提醒，显示提醒日期选择
                {
                    //在這个对话框中，riqiselected必须是boolean[]，不能是Boolean[]
                    final String[] riqi=new String[]{"周一","周二","周三","周四","周五","周六","周日"};
                    final boolean[] riqiselected=new boolean[]{false,false,false,false,false,false,false};
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("选择日期")
                            .setMultiChoiceItems(riqi, riqiselected, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                    riqiselected[i] = b;
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                //自定义的提醒时间
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    int j=0;
                                    String r = "";
                                    for (boolean a:riqiselected)
                                    {
                                        if (a)
                                        {
                                            r=r+riqi[j]+"=";
                                        }
                                        j++;
                                    }
                                    //如果选择了全部
                                    if (r.equals("周一=周二=周三=周四=周五=周六=周日="))
                                    {
                                        repeat.setSelection(0);
                                    }
                                    t.setCallDate(r);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    repeat.setSelection(0);
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Edittext提示文字消失
        habitName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                habitName.setHint(null);
            }
        });

        builder.show();//显示Dialog对话框
    }

    /*用户登录*/
    private void userLogin(){
        Dialog builder = new Dialog(this,R.style.HazyDialog);
        final View set = getLayoutInflater().inflate(R.layout.dialog_userlogin,null);
        builder.setContentView(set);//设置对话提示框
        WindowManager.LayoutParams params = builder.getWindow().getAttributes();
        params.dimAmount = 0.5f;  //设置activity的亮度的dimAmount在0.0f和1.0f之间
        builder.getWindow().setWindowAnimations(R.style.DetailAnim);   //设置dialog的显示动画
        builder.setCanceledOnTouchOutside(true);
        builder.show();
    }

    /*打开设置窗口*/
    public void Settings(View view) {
        //定义对话框
        Dialog builder = new Dialog(this,R.style.HazyDialog);
        final View set = getLayoutInflater().inflate(R.layout.activity_setup,null);
        Switch sw=set.findViewById(R.id.remindshock);
        builder.setContentView(set);//设置对话提示框
        WindowManager.LayoutParams params = builder.getWindow().getAttributes();
        params.dimAmount = 0.5f;  //设置activity的亮度的dimAmount在0.0f和1.0f之间
        builder.getWindow().setWindowAnimations(R.style.DetailAnim);   //设置dialog的显示动画
        builder.setCanceledOnTouchOutside(true);
        //使用shared存放用户设置的数据
        SharedPreferences sp=getSharedPreferences("remindshock",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp.edit();
        if (sp.getBoolean("shock",false))
        {
            sw.setChecked(true);
        }
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)//打开震动
                {
                    editor.putBoolean("shock",true);
                    editor.commit();
                    setup_shock=true;
                }
                else
                {
                    editor.putBoolean("shock",false);
                    editor.commit();
                    setup_shock=false;
                }
            }
        });
        builder.show();
    }

    /*打开关于窗口*/
    public void About(View view){
        final View about = getLayoutInflater().inflate(R.layout.about,null);
        dialog = new Dialog(this, R.style.HazyDialog);
        dialog.setContentView(about);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.dimAmount = 0.5f;  //设置activity的亮度的dimAmount在0.0f和1.0f之间
        dialog.getWindow().setWindowAnimations(R.style.DetailAnim);   //设置dialog的显示动画
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /*长按习惯监听事件*/
    private class viewOnTouch implements View.OnTouchListener {
        @Override
        public boolean onTouch(final View v, MotionEvent event){
            //获得触发onTouch的习惯,YH
            listHabits x=(listHabits)v;
            int id_habit=x.getId();
            onTouchHabit =database.select(id_habit);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    shock=true;
                    //hazy
                    screenShot();
                    showDetail(onTouchHabit.getName());
                    break;
                case MotionEvent.ACTION_MOVE:
                    double x_move=event.getRawX();
                    double y_move=event.getRawY();
                    if ((x_move>del_x_min&&x_move<del_x_max&&y_move>y_min&&y_move<y_max)
                            ||(x_move>tod_x_min&&x_move<tod_x_max&&y_move<y_max&&y_move>y_min)
                            ||(x_move>eit_x_min&&x_move<eit_x_max&&y_move<y_max&&y_move>y_min))
                    {
                        if (shock)
                        {
                            shock=false;
                            vibrator.vibrate(new long[]{0,10},-1);
                        }
                    }
                    else
                    {
                        shock=true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    double x_up=event.getRawX();
                    double y_up=event.getRawY();
                    dialogCancel();
                    if (x_up>del_x_min&&x_up<del_x_max&&y_up>y_min&&y_up<y_max)
                    {
                        //删除
                        dialogCancel();
                        AlertDialog.Builder del=new AlertDialog.Builder(MainActivity.this);
                        del.setTitle("删除");
                        del.setMessage("确定删除这个习惯吗?");
                        del.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                layout.removeView(v);
                                database.delete(onTouchHabit);
                            }
                        });
                        del.setNegativeButton("取消",null);
                        AlertDialog alertDialog = del.create();
                        Window window = alertDialog.getWindow();
                        window.setWindowAnimations(R.style.DialogAnim);
                        alertDialog.show();
                        return false;
                    }
                    else if(x_up>tod_x_min&&x_up<tod_x_max&&y_up<y_max&&y_up>y_min)
                    {
                        //完成与取消
                        Thing update_do = database.select(v.getId());
                        if (onTouchHabit.getToday_do() == 0) {//完成
                            //加入一条完成日期
                            SimpleDateFormat do_dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String date = do_dateFormat.format(new Date());
                            x.getText().setTextColor(Color.parseColor("#A9A9A9"));
                            x.getBut().setText(Integer.parseInt(x.getBut().getText().toString().split(" 天")[0]) + 1 + " 天");
                            update_do.setFinishDateRecord(update_do.getFinishDateRecord() + date + "=");
                            update_do.setToday_do(1);
                        }
                        else//取消
                        {
                            x.getText().setTextColor(Color.parseColor("#000000"));
                            x.getBut().setText(Integer.parseInt(x.getBut().getText().toString().split(" 天")[0]) - 1 + " 天");
                            String[] ddd=update_do.getFinishDateRecord().split("=");
                            String dd = "";
                            for (int i=0;i<ddd.length;i++)
                            {
                                if (i==ddd.length-1)
                                    continue;
                                dd+=ddd[i]+"=";
                            }
                            update_do.setFinishDateRecord(dd);
                            update_do.setToday_do(0);
                        }
                        database.update(update_do);
                    }
                    else if (x_up>eit_x_min&&x_up<eit_x_max&&y_up<y_max&&y_up>y_min)
                    {
                        //编辑
                        addHabit(x);
                    }
                    break;
            }
            return true;
        }
    }

    /*关闭对话框*/
    public void dialogCancel() {
        //消失对话框，背景模糊取消
        container.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        dialog.dismiss();
        /*//状态栏半透明
        Window statusBarAfter = getWindow();
        statusBarAfter.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);*/
    }

    /*截屏并传给RenderScript*/
    public void screenShot(){
        container.setVisibility(View.VISIBLE);
        mainLayout.setDrawingCacheEnabled(true);
        mainLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        Bitmap bitmap = mainLayout.getDrawingCache();
        RenderScriptBlur.bitmapBlur(this,container,bitmap,8);
        mainLayout.setVisibility(View.INVISIBLE);
        /*这一步很重要！！！花了我一个通宵去找bug QAQ*/
        mainLayout.setDrawingCacheEnabled(false);
    }

    /*显示习惯详情*/
    @SuppressLint("ClickableViewAccessibility")
    private void showDetail(String name){
        final View habit = getLayoutInflater().inflate(R.layout.dialog_habit,null);
        dialog = new Dialog(this, R.style.HazyDialog);
        dialog.setContentView(habit);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.dimAmount = 0f;  //设置activity的亮度的dimAmount在0.0f和1.0f之间
        dialog.getWindow().setWindowAnimations(R.style.DetailAnim);   //设置dialog的显示动画
        TextView text=habit.findViewById(R.id.titleDetail);
        text.setText(name);
        dialog.show();
    }
}
