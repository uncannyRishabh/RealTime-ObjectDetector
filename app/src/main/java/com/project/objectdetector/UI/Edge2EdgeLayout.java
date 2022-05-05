package com.project.objectdetector.UI;

import static androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class Edge2EdgeLayout {
    private final Activity activity;

    public Edge2EdgeLayout(Activity activity){
        this.activity = activity;
        setLayout();
    }

    private boolean is720p(){
        return Resources.getSystem().getDisplayMetrics().widthPixels < 1080;
    }

    private boolean is169(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display manager = windowManager.getDefaultDisplay();
        manager.getRealMetrics(displayMetrics);

        float r = ((float) Resources.getSystem().getDisplayMetrics().heightPixels
                / (float) Resources.getSystem().getDisplayMetrics().widthPixels);
//        Log.e("TAG", "is169: "+r);
        return r < 1.78f;
    }

    private void setLayout(){
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);

        if(is169()){
            Log.e("Edge2EdgeLayout", "setLayout: Resolution "+Resources.getSystem().getDisplayMetrics().heightPixels
                    +" x "+Resources.getSystem().getDisplayMetrics().widthPixels);
            activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }

}