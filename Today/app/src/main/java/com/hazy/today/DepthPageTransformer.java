package com.hazy.today;

import android.support.v4.view.ViewPager;
import android.view.View;
/*ViewPager翻页动画*/
public class DepthPageTransformer implements ViewPager.PageTransformer {
    /*层叠效果时定义
    private static final float MIN_SCALE = 0.67f;
    */
    @Override
    public void transformPage(View page, float position) {
        /*//层叠缩放效果
        int pageWidth = view.getWidth();
        if (position < -1) {
            view.setAlpha(0);
        } else if (position <= 0) {
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);
        } else if (position <= 1) {
            view.setAlpha(1 - position);
            view.setTranslationX(pageWidth * -position);
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {
            view.setAlpha(0);
        }*/
        /*翻页效果
        if(position>=-1&&position<=1){
            page.setPivotX(0);
            if(position<0){
                page.setTranslationX(-position*page.getWidth());
                page.setRotationY(90*position);
                page.setScaleX(1-Math.abs(position));
            }
            else{
                page.setTranslationX(-position*page.getWidth());
            }
        }*/

        //Y轴翻转效果
        if (position <= -1) {
            //页面已经在屏幕左侧且不可视
            //设置离开的page不可点击,不可见
            page.setClickable(false);
            page.setAlpha(0);
        } else if (position <= 0) {
            page.setClickable(false);
            //页面从左侧进入或者向左侧滑出的状态
            //把旋转中心改为中间
            page.setAlpha(1);
            if (position <= -0.5)
                //旋转到中间时该页page隐藏掉
                page.setAlpha(0);
            page.setPivotX(page.getWidth() / 2);
            page.setPivotY(page.getHeight() / 2);
            page.setTranslationX(position * -page.getWidth());
            page.setCameraDistance(10000);
            page.setRotationY(position * 180);
        } else if (position <= 1) {
            //页面从右侧进入或者向右侧滑出的状态
            //初始状态要是隐藏状态
            page.setAlpha(0);
            if (position <= 0.5)
                //旋转到中间时该页page显示出来
                page.setAlpha(1);
            page.setPivotX(page.getWidth() / 2);
            page.setPivotY(page.getHeight() / 2);
            page.setTranslationX(position * -page.getWidth());
            page.setCameraDistance(10000);
            page.setRotationY(-180 - (1 - position) * 180);
        } else if (position >= 1) {
        }
    }
}