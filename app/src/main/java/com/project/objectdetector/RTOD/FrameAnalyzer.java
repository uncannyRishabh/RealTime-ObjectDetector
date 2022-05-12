package com.project.objectdetector.RTOD;

import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.project.objectdetector.MainActivity;
import com.project.objectdetector.UI.Views.BoundingBox;

import java.util.List;

public class FrameAnalyzer implements ImageAnalysis.Analyzer {
    private ObjectDetectorHelper detectorHelper;

    public FrameAnalyzer(){
        detectorHelper = new ObjectDetectorHelper();
    }

    public FrameAnalyzer(int detectionMode,int detectorMode){
        detectorHelper = new ObjectDetectorHelper(detectionMode,detectorMode);
    }

    public void initializeObjectDetector(int detectionMode){
        detectorHelper.initializeObjectDetector(detectionMode);
    }

    public void closeDetector(){
        detectorHelper.closeDetector();
    }

    public void setView(BoundingBox box){
        detectorHelper.setView(box);
    }

    public void setPreviewResolution(Size res){
        detectorHelper.getView().setPreviewResolution(res);
    }

    public void setInputResolution(Size res){
        detectorHelper.getView().setInputResolution(res);
    }

    @Override
    @ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
//        Log.e("TAG", "analyze: FRAMES "+mediaImage.getHeight()+"x"+mediaImage.getWidth());
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        Task<List<DetectedObject>> task = detectorHelper.processImage(image);
        task.addOnSuccessListener(detectedObjects -> detectorHelper.getView().setDetectedObjects(detectedObjects))
        .addOnFailureListener(e -> {
            detectorHelper.getView().postInvalidate();
            Log.e("TAG", "analyze: unable to detect");
        })
        .addOnCompleteListener((result)-> imageProxy.close());
    }


}