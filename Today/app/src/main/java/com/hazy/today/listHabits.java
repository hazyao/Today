package com.hazy.today;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

    /*实现组合控件textView+button*/
public class listHabits extends LinearLayout {
    private TextView habitTitile;
    private Button durationDays;

    public listHabits(Context context) {
        this(context,null);
    }

    /*这里thing的FinishDateRecord属性在传入之前需要设置为连续次数*/
    public listHabits(Context context, AttributeSet attr) {
        super(context, attr);
        //获得手机宽
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = manager.getDefaultDisplay().getWidth();
        //解析xml中的布局
        LayoutInflater.from(context).inflate(R.layout.habit_contents, this, true);
        habitTitile = findViewById(R.id.showTitle);
        durationDays = findViewById(R.id.showDay);
        habitTitile.setWidth((int) (width * 0.75));
        habitTitile.setTextSize(17f);
        habitTitile.setElevation(3f);
        habitTitile.setGravity(Gravity.CENTER);
    }
    public TextView getText(){ return habitTitile; }
    public Button getBut(){return durationDays;}
}
