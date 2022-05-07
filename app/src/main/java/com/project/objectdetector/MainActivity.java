package com.project.objectdetector;

import static androidx.camera.core.CameraSelector.LENS_FACING_BACK;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.util.concurrent.ListenableFuture;
import com.project.objectdetector.RTOD.FrameAnalyzer;
import com.project.objectdetector.UI.Edge2EdgeLayout;
import com.project.objectdetector.UI.Views.BoundingBox;
import com.project.objectdetector.UI.Views.HorizontalPicker;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private PreviewView previewView;
    private FrameAnalyzer analyzer;
    private HorizontalPicker picker;
    private BoundingBox boundingBox;
    private ShapeableImageView fps, resolution, flash;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private float pointerX, pointerY;
    private boolean flashState = false;
    private boolean gestureDetected = false;

    private Camera camera;

    private enum State {
        STILL_IMAGE,
        TAP_TO_DETECT,
        REALTIME_DETECTION
    }

    private State state = State.TAP_TO_DETECT;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    @ExperimentalGetImage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        new Edge2EdgeLayout(this);

        previewView = findViewById(R.id.previewView);
        picker = findViewById(R.id.horizontal_picker);
        picker.setValues(new String[] {"Still Image","Tap To Detect", "Realtime"});
        picker.setSelectedItem(1);
        picker.setOverScrollMode(View.OVER_SCROLL_NEVER);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                analyzer = new FrameAnalyzer();
                bindImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        if(!hasCameraPermission()) requestPermission();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        boundingBox = findViewById(R.id.boundingBox);
        flash = findViewById(R.id.flash_toggle);
        flash.setOnClickListener(v -> {
            if(camera.getCameraInfo().hasFlashUnit()) {
                if(camera!=null) {
                    if (flashState) {
                        flashState = false;
                        camera.getCameraControl().enableTorch(false);
                        flash.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_round_flash_off));
                    } else {
                        flashState = true;
                        camera.getCameraControl().enableTorch(true);
                        flash.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_round_flash_on));
                    }
                }
            }

        });

        previewView.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            switch (action){
                case MotionEvent.ACTION_DOWN:{
                    pointerX = event.getX();
                    pointerY = event.getY();
                    return true;
                }
                case MotionEvent.ACTION_MOVE:{
                    /*
                     * SWIPE GESTURES
                     */
                    if(event.getPointerCount() == 1) {
                        if (pointerX - event.getX() > getScreenWidth() / 4f) {
                            Log.e("TAG", "onTouch: FLING RIGHT");
                            gestureDetected = true;
                            pointerX = event.getX();
                            if(picker.getSelectedItem() >= 0 && picker.getSelectedItem()<picker.getItems()-1) {
                                picker.setSelectedItem(picker.getSelectedItem() + 1);
                                modeSwitch(picker.getSelectedItem());
                                return true;
                            }
                        } else if (pointerX - event.getX() < -getScreenWidth() / 4f) {
                            Log.e("TAG", "onTouch: FLING LEFT");
                            gestureDetected = true;
                            pointerX = event.getX();
                            if(picker.getSelectedItem() > 0 && picker.getSelectedItem()<picker.getItems()) {
                                picker.setSelectedItem(picker.getSelectedItem() - 1);
                                modeSwitch(picker.getSelectedItem());
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    v.performClick();
                    /*
                     * FOCUS
                     */
                    if(!gestureDetected) {
                        MeteringPointFactory meteringPointFactory = previewView.getMeteringPointFactory();
                        MeteringPoint meteringPoint = meteringPointFactory.createPoint(pointerX, pointerY);
                        FocusMeteringAction focusMeteringAction = new FocusMeteringAction.Builder(meteringPoint).build();
                        camera.getCameraControl().startFocusAndMetering(focusMeteringAction);
                    }
                    gestureDetected = false;
                    break;
                }
            }
            return false;
        });

        picker.setOnItemClickedListener(this::modeSwitch);
        picker.setOnItemSelectedListener(this::modeSwitch);

    }

    @ExperimentalGetImage
    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(360,640))
//                .setTargetResolution(new Size(720,1280))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        analyzer.setView(boundingBox);
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> analyzer.analyze(image));

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(LENS_FACING_BACK)
                .build();

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void modeSwitch(int index){
        picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP
                ,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        switch (index){
            case 0:{
//                openImagePicker();
                //load image into fragment
                //pass image through classifier
                //load results into bottom modal
                Log.e("TAG", "modeSwitch: FROM STILL IMAGE");
                break;
            }
            case 1:{
                Log.e("TAG", "modeSwitch: TAP TO DETECT");
                break;
            }
            case 2:{
                Log.e("TAG", "modeSwitch: REALTIME DETECTION");
                break;
            }
        }
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

}