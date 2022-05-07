package com.project.objectdetector.UI.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.project.objectdetector.R;

public class BoundingBox extends View {
    private Paint boxPaint;
    private Paint rectPaint;
    private Paint textPaint;
    private RectF boxRect;
    private RectF rectRect;
    private Typeface poppins;

    private int labelColor = ContextCompat.getColor(getContext(), R.color.theme_primary_dark);
    private float labelSize = 20f;

    public BoundingBox(Context context) {
        this(context, null);
        init();
    }

    public BoundingBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.horizontalPickerStyle);
    }

    public BoundingBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BoundingBox,
                defStyleAttr, 0
        );

        try{
            labelColor = a.getColor(R.styleable.BoundingBox_android_textColor, labelColor);
            labelSize = a.getDimension(R.styleable.BoundingBox_android_textSize, labelSize);
        }
        finally {
            a.recycle();
            init();
        }

    }

    public void setBoxRect(RectF rect){
        boxRect = rect;
        invalidate();
    }

    private void init(){
        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxRect = new RectF();
        rectRect = new RectF();

        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.theme_primary_dark));
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);

        rectPaint.setColor(ContextCompat.getColor(getContext(), R.color.theme_primary_light));
        rectPaint.setStyle(Paint.Style.FILL);

        poppins = Typeface.create("Poppins", Typeface.NORMAL);

        textPaint.setColor(labelColor);
        textPaint.setTextSize(labelSize);
        textPaint.setTypeface(poppins);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        boxRect.set(0,0,getWidth()/2f,getHeight()/2f);
//        rectRect.set(20,20,getWidth()/4f,getHeight()/6f);

        canvas.drawRoundRect(boxRect,50f,50f,boxPaint);
        canvas.drawRoundRect(rectRect,50f,50f,rectPaint);
        canvas.drawText("LABEL",115,55,textPaint);
    }
}
