package com.hazy.today;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DetailCalendar extends View {
    private static final int TOTAL_COL = 7;// 7列
    private static final int TOTAL_ROW = 5;// 5行
    private Paint mCirclePaint; // 绘制圆形的画笔
    private Paint mTextPaint; // 绘制文本的画笔
    private int mViewWidth; // 视图的宽度
    private int mViewHeight; // 视图的高度
    private int mCellSpace; // 单元格间距
    private ArrayList<Integer> Finddate;//存放完成的日期

    public DetailCalendar(Context context) {
        super(context);
    }

    public DetailCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //返回当月完成的日期
    public ArrayList FindDate(String Find) {
        ArrayList<Integer> FindDate = new ArrayList<>();
        //如果为空
        if (Find.length() <= 1) {
            return FindDate;
        }
        String[] find = Find.split("=");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        String yearmonth = date.split("-")[0] + "-"
                + date.split("-")[1];
        for (String x : find) {
            if ((x.split("-")[0] + "-" + x.split("-")[1]).equals(yearmonth)) {
                FindDate.add(Integer.parseInt(x.split("-")[2]));
            }
        }
        return FindDate;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Finddate = FindDate(MainActivity.onTouchHabit.getFinishDateRecord());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        String yearmonth = date.split("-")[0] + " 年 "
                + date.split("-")[1] + " 月 ";

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.parseColor("#333333"));
        mTextPaint.setTextSize(45);
        mTextPaint.setAntiAlias(true);
        //绘制年月
        canvas.drawText(yearmonth, dp2px(getContext(), 130), dp2px(getContext(), 25), mTextPaint);

        //绘制一周的星期几
        int yy = dp2px(getContext(), 70);
        canvas.drawPosText("日一二三四五六", new float[]{
                dp2px(getContext(), 37), yy,
                dp2px(getContext(), 82), yy,
                dp2px(getContext(), 128), yy,
                dp2px(getContext(), 172), yy,
                dp2px(getContext(), 217), yy,
                dp2px(getContext(), 262), yy,
                dp2px(getContext(), 307), yy
        }, mTextPaint);

        //绘制日历
        mCirclePaint = new Paint();
        int[] week_dayCount = today_month();
        mCirclePaint.setAntiAlias(true);//抗锯齿
        mCirclePaint.setStrokeWidth(3);//设置边框宽度

        int[] point;
        mTextPaint.setTextSize(30.5f);//设置日期的大小
        mTextPaint.setColor(Color.parseColor("#333333"));
        mTextPaint.setAntiAlias(true);
        boolean ifDone = false;//判断是否重复画
        int Count = 1;//判断画完没有
        for (int i = 1; i <= TOTAL_ROW; i++)//5行
        {
            for (int j = 1; j <= TOTAL_COL; j++)//7列
            {
                if (i == 1 && j < week_dayCount[0])//没有到第一天
                {
                    continue;
                }
                point = getPoint(i, j);//获得這个点的坐标
                for (int k : Finddate)//是否为完成了的日期
                {
                    if (Count == k) {
                        mCirclePaint.setStyle(Paint.Style.FILL);//实心效果
                        mCirclePaint.setColor(Color.parseColor("#6495ED"));//实心圆
                        mTextPaint.setColor(Color.WHITE);
                        ifDone = true;
                        break;
                    }
                }
                if (!ifDone)//不是完成的日期
                {
                    mCirclePaint.setStyle(Paint.Style.STROKE);//描边效果
                    mCirclePaint.setColor(Color.TRANSPARENT);
                    mTextPaint.setColor(Color.parseColor("#333333"));
                }
                canvas.drawCircle(point[0], point[1], dp2px(getContext(), 15), mCirclePaint);
                if (Count < 10) {
                    canvas.drawText(Count + "", point[0] - 10, point[1] + 10, mTextPaint);
                } else {
                    canvas.drawText(Count + "", point[0] - 16, point[1] + 10, mTextPaint);
                }
                if (Count == week_dayCount[1]) {
                    break;
                }
                Count++;
                ifDone = false;
            }
        }
        //绘制详细信息
        mTextPaint.setTextSize(30);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.parseColor("#A9A9A9"));
        String calltime=MainActivity.onTouchHabit.getCallTime();
        String text;
        if (calltime.equals(""))
            text="创建时间："+MainActivity.onTouchHabit.getCreateDate()
                    +"\t\t提醒时间：关闭";
        else
            text="创建时间："+MainActivity.onTouchHabit.getCreateDate()
                    +"\t\t提醒时间："+MainActivity.onTouchHabit.getCallTime();
        canvas.drawText(text, dp2px(getContext(),85),dp2px(getContext(),340),mTextPaint);
    }

    //用来返回日历所有可绘制日期的点
    //i,j代表点的坐标，例如1，1
    public int[] getPoint(int i, int j) {
        int x = dp2px(getContext(), 45);
        int y = dp2px(getContext(), 65);
        int[] r = new int[]{j * x, i * x + y};
        return r;
    }

    //将dp转换为与之相等的px
    private int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    //返回当月的日期数据
    public int[] today_month() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        int week = c.get(Calendar.DAY_OF_WEEK);
        int dayCount = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        int[] i = new int[]{week, dayCount};
        return i;
    }
}
