package com.project.objectdetector;

import static androidx.camera.core.CameraSelector.LENS_FACING_BACK;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.project.objectdetector.RTOD.FrameAnalyzer;
import com.project.objectdetector.RTOD.ObjectDetectorHelper;
import com.project.objectdetector.UI.Edge2EdgeLayout;
import com.project.objectdetector.UI.Views.BoundingBox;
import com.project.objectdetector.UI.Views.HorizontalPicker;
import com.project.objectdetector.Utils.BitmapUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings({"FieldCanBeLocal"
        , "FieldMayBeLocal"})
public class MainActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private ConstraintLayout parent;

    private PreviewView previewView;
    private FrameAnalyzer analyzer;
    private HorizontalPicker picker;
    private BoundingBox boundingBox;
    private ShapeableImageView fps, resolution, flash, capture;
    private LinearLayout btnHolder;
    private TextView toolTip;
    private ImageView previewImage;

    private float pointerX, pointerY;
    private boolean flashState = false;
    private boolean gestureDetected = false;

    private BitmapUtils bmpUtil;
    private Bitmap bitmap,compressedBmp;

    private Camera camera;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private final Runnable hideToolTip = new Runnable() {
        @Override
        public void run() {
            toolTip.setVisibility(View.GONE);
        }
    };

    private final Runnable showToolTip = new Runnable() {
        @Override
        public void run() {
            toolTip.setVisibility(View.VISIBLE);
        }
    };

    private final Runnable hideImageView = new Runnable() {
        @Override
        public void run() {
            previewImage.setVisibility(View.GONE);
            previewImage.setImageDrawable(null);
        }
    };

    private final Runnable showImageView = new Runnable() {
        @Override
        public void run() {
            previewImage.setVisibility(View.VISIBLE);
        }
    };

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
        parent = findViewById(R.id.parent);
        parent.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        previewView = findViewById(R.id.previewView);
        picker = findViewById(R.id.horizontal_picker);
        picker.setValues(new String[]{"Still Image", "Tap To Detect", "Realtime"});
        picker.setSelectedItem(1);
        picker.setOverScrollMode(View.OVER_SCROLL_NEVER);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                buildCameraPreview();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        if (!hasCameraPermission()) requestPermission();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toolTip = findViewById(R.id.tooltip);
        setTooltipText("Tap the capture button to start detection");
        toolTip.postDelayed(hideToolTip, 3500);
        previewImage=findViewById(R.id.preview_image);

        capture = findViewById(R.id.capture_btn);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            @ExperimentalGetImage
            public void onClick(View v) {
                if (getState() == State.REALTIME_DETECTION) {
                    analyzer = new FrameAnalyzer(ObjectDetectorHelper.CLASSIFY_MULTIPLE_OBJECTS, ObjectDetectorOptions.STREAM_MODE);
                    analyzer.setView(boundingBox);
                    analyzer.setInputResolution(new Size(360, 640));
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
            if (camera.getCameraInfo().hasFlashUnit()) {
                if (camera != null) {
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
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    pointerX = event.getX();
                    pointerY = event.getY();
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    /*
                     * SWIPE GESTURES
                     */
                    if (event.getPointerCount() == 1) {
                        if (pointerX - event.getX() > getScreenWidth() / 4f) {
                            Log.e("TAG", "onTouch: FLING RIGHT");
                            gestureDetected = true;
                            pointerX = event.getX();
                            if (picker.getSelectedItem() >= 0 && picker.getSelectedItem() < picker.getItems() - 1) {
                                picker.setSelectedItem(picker.getSelectedItem() + 1);
                                modeSwitch(picker.getSelectedItem());
                                return true;
                            }
                        } else if (pointerX - event.getX() < -getScreenWidth() / 4f) {
                            Log.e("TAG", "onTouch: FLING LEFT");
                            gestureDetected = true;
                            pointerX = event.getX();
                            if (picker.getSelectedItem() > 0 && picker.getSelectedItem() < picker.getItems()) {
                                picker.setSelectedItem(picker.getSelectedItem() - 1);
                                modeSwitch(picker.getSelectedItem());
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    v.performClick();
                    /*
                     * FOCUS
                     */
                    if (!gestureDetected) {
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
        flash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_flash_on));
    }

    private void disableTorch() {
        flashState = false;
        camera.getCameraControl().enableTorch(false);
        flash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_flash_off));
    }

    private void buildCameraPreview() {
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(360, 640))
//                .setTargetResolution(new Size(720,1280))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(LENS_FACING_BACK)
                .build();

        bindCamera();
    }

    private void bindCamera() {
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    private void unbindCamera() {
        cameraProvider.unbindAll();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void modeSwitch(int index) {
        picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        toolTip.removeCallbacks(hideToolTip);

        switch (index) {
            case 0: {
                //pass image through classifier
                //load results into bottom modal

                setState(State.STILL_IMAGE);
                if(analyzer!=null) analyzer.closeDetector();
                disableTorch();
                imageAnalysis.clearAnalyzer();
                unbindCamera();
                openImagePicker();

                Log.e("TAG", "modeSwitch: FROM STILL IMAGE");
                break;
            }
            case 1: {
                setState(State.TAP_TO_DETECT);
                bindCamera();
                if(analyzer!=null) analyzer.closeDetector();
                imageAnalysis.clearAnalyzer();
                capture.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_round_capture_ttd));
                setTooltipText("Tap the capture button to start detection");
                toolTip.postDelayed(hideToolTip, 2500);

                Log.e("TAG", "modeSwitch: TAP TO DETECT");
                break;
            }
            case 2: {
                setState(State.REALTIME_DETECTION);
                bindCamera();
                capture.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_capture_realtime));
                setTooltipText("Tap the dot to inspect");
                toolTip.postDelayed(hideToolTip, 2500);

                Log.e("TAG", "modeSwitch: REALTIME DETECTION");
                break;
            }
        }

        previewImage.post(getState() != State.STILL_IMAGE ? hideImageView : showImageView);
        toolTip.post(getState() != State.STILL_IMAGE ? showToolTip : hideToolTip);
        btnHolder.setVisibility(getState() == State.STILL_IMAGE ? View.INVISIBLE : View.VISIBLE);
        capture.setVisibility(getState() == State.STILL_IMAGE ? View.GONE : View.VISIBLE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePicker.launch(intent);

    }

    ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if(data != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , Uri.parse(data.getDataString()));
                            Log.e("TAG", "ActivityResultLauncher: data: "+data.getDataString()+"\n parsedData : "+Uri.parse(data.getDataString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(bitmap.getWidth() > 1080){       //condition for applying compression
                            bmpUtil = new BitmapUtils();
                            Completable.fromRunnable(() -> compressedBmp = bmpUtil.compressBitmap(bitmap))
                                .subscribeOn(Schedulers.computation())
                                .andThen(Completable.fromRunnable(() -> {
                                            previewImage.setImageBitmap(compressedBmp);
                                            bitmap.recycle();
                                        })
                                        .subscribeOn(AndroidSchedulers.mainThread()))
                                .doOnError((e) -> Log.e("onError", "ActivityResultLauncher: "+e))
                                .subscribe();
                        }
                        else
                            previewImage.setImageBitmap(bitmap);
                    }

                }
            });

    private void setTooltipText(String s) {
        toolTip.setText(s);
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(getState() == State.REALTIME_DETECTION) analyzer.closeDetector();
        toolTip.post(hideToolTip);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flashState) {
            flashState = false;
            flash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_flash_off));
        }
    }
}