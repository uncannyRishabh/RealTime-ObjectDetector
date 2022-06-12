package com.project.objectdetector.UI.Views;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.project.objectdetector.R;

public class CaptureButton extends View {
    private Paint bgPaint;
    private Paint fgPaint;
    private Bitmap bmp;
    private Matrix matrix;
    private float innerRadius,irc;
    private float iconMultiplier = 4f;
    private final float imc = 4f;
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
        matrix = new Matrix();

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(ContextCompat.getColor(getContext(), R.color.theme_primary_light));

        fgPaint.setStyle(Paint.Style.FILL);
        fgPaint.setColor(ContextCompat.getColor(getContext(), R.color.theme_primary_dark));

        Drawable d = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_round_capture_ttd, null);
        bmp = getBitmapFromVectorDrawable(d);

        innerRadius = getWidthFromResources()/12f;
        irc = innerRadius;

        valueAnimator = new ValueAnimator();
    }

    public void setBitmapDrawable(Drawable drawable){
        matrix.postTranslate(0,0);
        bmp = getBitmapFromVectorDrawable(drawable);
        invalidate();
    }

    public void setBitmapWithTransition(Drawable drawable, boolean left){
        if(left){
            valueAnimator.setValues(PropertyValuesHolder.ofFloat("translation",imc,imc/2));
            valueAnimator.setDuration(70);
            valueAnimator.setInterpolator(new AccelerateInterpolator());
            valueAnimator.addUpdateListener(animation -> {
                iconMultiplier = (float) animation.getAnimatedValue("radius");
                invalidate();
            });
            valueAnimator.start();
        }
        else {
            valueAnimator.setValues(PropertyValuesHolder.ofFloat("radius",imc,imc));
            valueAnimator.setDuration(70);
            valueAnimator.setInterpolator(new AccelerateInterpolator());
            valueAnimator.addUpdateListener(animation -> {
                iconMultiplier = (float) animation.getAnimatedValue("radius");
                invalidate();
            });
        }
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
        valueAnimator.setValues(PropertyValuesHolder.ofFloat("radius",irc,irc*.8f));
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

        matrix.setScale(1,1);
        matrix.postTranslate(getWidth()/iconMultiplier,getWidth()/4f);
        canvas.drawBitmap(bmp,matrix,null);
//                .drawBitmap(bmp,getWidth()/4f,getWidth()/4f,null);
    }
}
