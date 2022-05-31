package com.project.objectdetector.UI.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.objects.DetectedObject;
import com.project.objectdetector.R;

import java.util.List;

@SuppressWarnings({"FieldCanBeLocal"
        , "FieldMayBeLocal"})
public class BoundingBox extends View {
    private Paint boxPaint;
    private Paint rectPaint;
    private Paint textPaint;
    private RectF boxRect;
    private Typeface poppins;
    private final Canvas canvas = new Canvas();

    private Size previewRes,inputRes;

    private float labelSize = 20f;
    private int labelColor = ContextCompat.getColor(getContext(), R.color.theme_primary_dark);

    private List<DetectedObject> detectedObjects;

    public BoundingBox(Context context) {
        this(context, null);
        init();
    }

    public BoundingBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.boundingBoxStyle);
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

    public void setPreviewResolution(Size res){
        this.previewRes = res;
    }

    public void setInputResolution(Size res){
        this.inputRes = res;
    }

    public void setDetectedObjects(List<DetectedObject> detectedObjects) {
        this.detectedObjects = detectedObjects;
        invalidate();
    }

    private void init(){
        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxRect = new RectF();

//        screenHeight = getResources().getDisplayMetrics().heightPixels;

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

    private void spawnBoxes(Canvas canvas,RectF rect){
        canvas.drawRoundRect(rect, 24f, 24f, boxPaint);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        if(visibility == View.GONE){
            Log.e("TAG", "onVisibilityChanged: ");
            this.draw(canvas);
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(detectedObjects != null) {
            for (DetectedObject object : detectedObjects) {
                Log.e("TAG", "onSuccess: tracking ID "+object.getTrackingId());
                spawnBoxes(canvas, mapBoxRect(object.getBoundingBox()));
            }
        }

        if (detectedObjects == null) {
            Log.e("TAG", "onDraw: detectedObjects == null");
            boxRect.set(0, 0, 0, 0);
        }

    }

    public RectF mapBoxRect(Rect boundingBox){
        float w;
        float h;
        if(inputRes !=null){
            h = previewRes.getHeight()/(float) inputRes.getWidth();
            w = previewRes.getWidth()/(float) inputRes.getHeight();

            boxRect.set(boundingBox.left * w,
                    boundingBox.top * h,
                    boundingBox.right * w,
                    boundingBox.bottom * h);
//            Log.e("TAG", "mapBoxRect: inputres : "+inputRes);
//            Log.e("TAG", "mapBoxRect: previewres : "+previewRes);
//            Log.e("TAG", "mapBoxRect: boundingBox :"+boundingBox);
//            Log.e("TAG", "mapBoxRect: w :"+w+" h : "+h);

//            Log.e("TAG", "mapBoxRect: boundingBox :"+new RectF(boundingBox.left * w,
//                    boundingBox.top * h,
//                    boundingBox.right * w,
//                    boundingBox.bottom * h));
        }
        return boxRect;
    }

}
