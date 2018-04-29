package com.hazy.today;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import static android.content.ContentValues.TAG;

public class RenderScriptBlur {

    public static Bitmap Blur(Context context, Bitmap originBitmap, int radius){
        RenderScript renderScript = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(renderScript,originBitmap);
        Allocation output = Allocation.createTyped(renderScript,input.getType());
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setRadius(radius);
        scriptIntrinsicBlur.setInput(input);
        scriptIntrinsicBlur.forEach(output);
        output.copyTo(originBitmap);
        renderScript.destroy();
        return originBitmap;
    }

    public static void bitmapBlur(Context context, ImageView ivBlurBg, Bitmap bitmap, int scaleRatio){
        int x = (int) ivBlurBg.getX();
        int y = (int) ivBlurBg.getY();
        int bitmapX = bitmap.getWidth();
        int bitmapY = bitmap.getHeight();
        Bitmap bitmapNew = Bitmap.createBitmap(bitmap,x,y,bitmapX-x,bitmapY-y);
        if(bitmap != null){
            Bitmap overlay = Bitmap.createScaledBitmap(bitmapNew, bitmapNew.getWidth() / scaleRatio, bitmapNew.getHeight() / scaleRatio, false);
            overlay = RenderScriptBlur.Blur(context,overlay,5);
            ivBlurBg.setImageBitmap(overlay);
        }
        //手动回收，再次使用会报错
        //bitmap.recycle();
        System.gc();
    }
}


