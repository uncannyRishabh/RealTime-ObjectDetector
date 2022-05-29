package com.project.objectdetector.UI.Views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.project.objectdetector.R;

@SuppressWarnings({"FieldCanBeLocal","FieldMayBeFinal"})
public class TemperatureMonitor extends View {
    private Paint tPaint;
    private Rect tRect;
    private Typeface poppins;
    private int tColor = 0xFF000000;
    private float tSize = 20f;
    private float temperature = 0f;
    private String tString = "";
    private IntentFilter intentfilter;

    public TemperatureMonitor(Context context) {
        this(context, null);
    }

    public TemperatureMonitor(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.temperatureMonitorStyle);
    }

    public TemperatureMonitor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TemperatureMonitor,
                defStyleAttr, 0
        );

        try{
            tColor = a.getColor(R.styleable.TemperatureMonitor_android_textColor, tColor);
            tSize = a.getDimension(R.styleable.TemperatureMonitor_android_textSize, tSize);
        }
        finally {
            a.recycle();
            init();
        }

        intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                float temp = (float) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
                if(temp != temperature) {
                    temperature = temp;
                    setText(temperature + "" + (char) 0x00B0 + "C");
                }
            }
        };

        getContext().registerReceiver(broadcastreceiver,intentfilter);
    }

    private void init() {
        tPaint = new Paint();
        tRect = new Rect();

        poppins = Typeface.create("Poppins", Typeface.NORMAL);
        tPaint.setColor(tColor);
        tPaint.setTypeface(poppins);
        tPaint.setTextSize(tSize);

        tPaint.getTextBounds("00.0* F", 0, 7, tRect);
    }

    private void setText(String tString){
        this.tString = tString;
        postInvalidate();
        Log.e("TEMPERATURE MONITOR", "setText: "+tString+"  "+tRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0x80000000);
        canvas.drawText(tString,
                (getWidth()-tRect.right)/2f,
                (getHeight()-tRect.top)/2f,
                tPaint);
    }
}
