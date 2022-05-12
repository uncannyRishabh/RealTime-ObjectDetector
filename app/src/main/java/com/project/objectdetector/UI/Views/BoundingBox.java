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
    private RectF rectRect;
    private Typeface poppins;

    private boolean objectDetected = false;
    private Size previewRes,inputRes;

    private String labelText = "Label";
    private float labelSize = 20f;
    private int labelColor = ContextCompat.getColor(getContext(), R.color.theme_primary_dark);

    private List<DetectedObject> detectedObjects;

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
        rectRect.set(rect.left+10f, rect.top+14f
                        , rect.right-10f, rect.top+60f);
        invalidate();
    }

    public void setLabelText(String text){
        labelText = text;
    }

    public void setPreviewResolution(Size res){
        this.previewRes = res;
    }

    public void setInputResolution(Size res){
        this.inputRes = res;
    }

    public void setDetectedObjects(List<DetectedObject> detectedObjects) {
        this.detectedObjects = detectedObjects;
        for (DetectedObject detectedObject : detectedObjects) {
            Rect boundingBox = detectedObject.getBoundingBox();
            Integer trackingId = detectedObject.getTrackingId();
            setBoxRect(mapBoxRect(boundingBox));
            for (DetectedObject.Label label : detectedObject.getLabels()) {
                String text = label.getText();
                int index = label.getIndex();
                float confidence = label.getConfidence();
                setLabelText(text);
                Log.e("TAG", "onSuccess: TEXT : "+text
                        +" tracking ID "+trackingId+
                        " index : "+index+
                        " confidence + "+confidence);
            }
        }

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

    private void drawOverlay(Canvas canvas){
        canvas.drawRoundRect(boxRect, 24f, 24f, boxPaint);     //bounding box
//            canvas.drawRoundRect(rectRect,30f,30f,rectPaint);   //label box
        canvas.drawText(labelText, boxRect.left, boxRect.top - 10f, textPaint);  //label text
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        if (detectedObjects == null) {
            Log.e("TAG", "onDraw: detectedObjects == null");
            boxRect.set(0, 0, 0, 0);
        }
        drawOverlay(canvas);

    }

    public RectF mapBoxRect(Rect boundingBox){
        if(inputRes !=null){
//            Log.e("TAG", "mapBoxRect: inputres : "+inputRes);
//            Log.e("TAG", "mapBoxRect: previewres : "+previewRes);

            int w = previewRes.getWidth()/inputRes.getWidth();
            int h = previewRes.getHeight()/inputRes.getHeight();

            boundingBox.set(boundingBox.left * w,
                    boundingBox.top * h,
                    boundingBox.right * w,
                    boundingBox.bottom * h);

        }
        return new RectF(boundingBox);
    }

}
