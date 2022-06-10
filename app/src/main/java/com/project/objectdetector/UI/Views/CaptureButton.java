package com.project.objectdetector.UI.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.project.objectdetector.R;

public class CaptureButton extends View {
    private Paint bgPaint;
    private Paint fgPaint;
    private Bitmap bmp;
//    private

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
//        bmp =((BitmapDrawable)d).getBitmap();
        bmp = getBitmapFromVectorDrawable(d);
//        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_capture_realtime);
//        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),"");
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(getWidth()/2f
                ,getHeight()/2f
                ,getWidth()/2f
                ,bgPaint); //outer circle

        canvas.drawCircle(getWidth()/2f
                ,getHeight()/2f
                ,2*getWidth()/5f
                ,fgPaint); //inner circle


        canvas.drawBitmap(bmp,getWidth()/4f,getWidth()/4f,null);
    }
}
