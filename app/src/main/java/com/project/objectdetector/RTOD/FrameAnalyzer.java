package com.project.objectdetector.RTOD;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.internal.utils.ImageUtil;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.internal.ImageUtils;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.project.objectdetector.UI.Views.BoundingBox;

import java.nio.ByteBuffer;
import java.util.List;

public class FrameAnalyzer implements ImageAnalysis.Analyzer {
    private Image mediaImage;
//    private final ObjectDetectorHelper detectorHelper;
    private final CustomObjectDetectorHelper customdetectorHelper;
//    private final ExternalClassifier externalClassifier;

    public FrameAnalyzer(){
//        detectorHelper = new ObjectDetectorHelper();
//        externalClassifier = new ExternalClassifier();
        customdetectorHelper = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,0.5f,2,"default_mobilenet");
    }

    public FrameAnalyzer(int detectionMode,int detectorMode){
//        detectorHelper = new ObjectDetectorHelper(detectionMode,detectorMode);
//        externalClassifier = new ExternalClassifier();
        customdetectorHelper = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,0.5f,2,"default_mobilenet");
    }

    public void initializeObjectDetector(int detectionMode){
//        detectorHelper.initializeObjectDetector(detectionMode);
    }

    public void closeDetector(){
        customdetectorHelper.closeDetector();
    }

    public void setView(BoundingBox box){
        customdetectorHelper.setView(box);
    }

    /**
     * Format: w * h
     */
    public void setPreviewResolution(Size res){
        customdetectorHelper.getView().setPreviewResolution(res);
    }

    /**
     * Format: h * w
     */
    public void setInputResolution(Size res){
        customdetectorHelper.getView().setInputResolution(res);
    }

    @Override
    @ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        mediaImage = imageProxy.getImage();
//        Log.e("TAG", "analyze: FRAMES "+mediaImage.getHeight()+"x"+mediaImage.getWidth());
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        Task<List<DetectedObject>> task = customdetectorHelper.process(image);

        task.addOnSuccessListener(detectedObjects -> customdetectorHelper.getView().setDetectedObjects(detectedObjects))
        .addOnFailureListener(e -> {
            customdetectorHelper.getView().postInvalidate();
            Log.e("TAG", "analyze: unable to detect");
        })
        .addOnCompleteListener((result)-> imageProxy.close());
    }

}