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
import android.widget.LinearLayout;

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

@SuppressWarnings({"FieldCanBeLocal"
        , "FieldMayBeLocal"})
public class MainActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private PreviewView previewView;
    private FrameAnalyzer analyzer;
    private HorizontalPicker picker;
    private BoundingBox boundingBox;
    private ShapeableImageView fps, resolution, flash, capture;
    private LinearLayout btnHolder;

    private float pointerX, pointerY;
    private boolean flashState = false;
    private boolean gestureDetected = false;

    private Camera camera;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

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
    protected void onStart() {
        super.onStart();
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
                cameraProvider = cameraProviderFuture.get();
                analyzer = new FrameAnalyzer();
                buildCameraPreview();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        if(!hasCameraPermission()) requestPermission();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        capture =  findViewById(R.id.capture_btn);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            @ExperimentalGetImage
            public void onClick(View v) {
                if(getState() == State.REALTIME_DETECTION){
                    analyzer.initializeObjectDetector();
                    analyzer.setView(boundingBox);
                    analyzer.setInputResolution(new Size(360,640));
                    analyzer.setPreviewResolution(new Size(previewView.getWidth(), previewView.getHeight()));
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MainActivity.this)
                        , image -> {
                        if (getState() == State.REALTIME_DETECTION)
                            analyzer.analyze(image);
                    });
                }
            }
        });

        btnHolder = findViewById(R.id.btn_holder);
        boundingBox = findViewById(R.id.boundingBox);

        flash = findViewById(R.id.flash_toggle);
        flash.setOnClickListener(v -> {
            if(camera.getCameraInfo().hasFlashUnit()) {
                if(camera!=null) {
                    if (flashState) {
                        disableTorch();
                    } else {
                        enableTorch();

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

    private void enableTorch() {
        flashState = true;
        camera.getCameraControl().enableTorch(true);
        flash.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_round_flash_on));
    }

    private void disableTorch() {
        flashState = false;
        camera.getCameraControl().enableTorch(false);
        flash.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_round_flash_off));
    }

    private void buildCameraPreview() {
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(360,640))
//                .setTargetResolution(new Size(720,1280))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(LENS_FACING_BACK)
                .build();

        bindCamera();
    }

    private void bindCamera(){
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    private void unbindCamera(){
        cameraProvider.unbindAll();
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
                setState(State.STILL_IMAGE);
                analyzer.closeDetector();
                disableTorch();
                imageAnalysis.clearAnalyzer();
                unbindCamera();
                Log.e("TAG", "modeSwitch: FROM STILL IMAGE");
                break;
            }
            case 1:{
                setState(State.TAP_TO_DETECT);
                bindCamera();
                analyzer.closeDetector();
                imageAnalysis.clearAnalyzer();
                capture.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_round_capture_ttd));

                Log.e("TAG", "modeSwitch: TAP TO DETECT");
                break;
            }
            case 2:{
                setState(State.REALTIME_DETECTION);
                bindCamera();
                capture.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_baseline_capture_realtime));

                Log.e("TAG", "modeSwitch: REALTIME DETECTION");
                break;
            }
        }

        btnHolder.setVisibility(getState() == State.STILL_IMAGE ? View.INVISIBLE : View.VISIBLE);
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

}