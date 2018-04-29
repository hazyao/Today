package com.hazy.today;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public  class MyPagerAdapter extends PagerAdapter {

    private List<View> viewList;

    public MyPagerAdapter(List<View> viewList){
        this.viewList=viewList;
    }
    @Override
    public int getCount() {
        return viewList.size();
    }

    /*view是否来自对象*/
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    /*实例化*/
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewList.get(position));
        return viewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }
}
