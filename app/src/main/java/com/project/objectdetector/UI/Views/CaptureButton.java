package com.project.objectdetector.UI.Views;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.project.objectdetector.R;

public class CaptureButton extends View {
    private Paint bgPaint;
    private Paint fgPaint;
    private Bitmap bmp;
    private float innerRadius,irc;
    private ValueAnimator valueAnimator;

    public CaptureButton(Context context) {
        super(context);
        init();
    }

    public CaptureButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CaptureButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(ContextCompat.getColor(getContext(), R.color.theme_primary_light));

        fgPaint.setStyle(Paint.Style.FILL);
        fgPaint.setColor(ContextCompat.getColor(getContext(), R.color.theme_primary_dark));

        Drawable d = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_round_capture_ttd, null);
        bmp = getBitmapFromVectorDrawable(d);

        innerRadius = getWidthFromResources()/13f;
        irc = innerRadius;

        valueAnimator = new ValueAnimator();
        Log.e("TAG", "init: width : "+getWidthFromResources());
    }

    public void setBitmapDrawable(Drawable drawable){
        bmp = getBitmapFromVectorDrawable(drawable);
        invalidate();
    }

    private Bitmap getBitmapFromVectorDrawable(Drawable drawable){
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:{
                expandBtn();
                break;
            }
            case MotionEvent.ACTION_UP:{
                performClick();
                shrinkBtn();
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void expandBtn() {
        valueAnimator.setValues(PropertyValuesHolder.ofFloat("radius",irc,irc*1.2f));
        valueAnimator.setDuration(70);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            innerRadius = (float) animation.getAnimatedValue("radius");
            invalidate();
        });
        valueAnimator.start();
    }

    private void shrinkBtn() {
        valueAnimator.setValues(PropertyValuesHolder.ofFloat("radius",innerRadius,irc));
        valueAnimator.setDuration(160);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            innerRadius = (float) animation.getAnimatedValue("radius");
            invalidate();
        });
        valueAnimator.start();
    }

    private int getWidthFromResources(){
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //outer circle
        canvas.drawCircle(getWidth()/2f
                ,getHeight()/2f
                ,getWidth()/2f
                ,bgPaint);

        //inner circle
        canvas.drawCircle(getWidth()/2f
                ,getHeight()/2f
                ,innerRadius
                ,fgPaint);

        canvas.drawBitmap(bmp,getWidth()/4f,getWidth()/4f,null);
    }
}
