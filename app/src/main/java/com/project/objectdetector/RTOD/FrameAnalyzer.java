package com.project.objectdetector.RTOD;


import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;
import com.project.objectdetector.Utils.SerialExecutor;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FrameAnalyzer implements ImageAnalysis.Analyzer {
//    private Image image;
    private ObjectDetector objectDetector;
    private SerialExecutor serialExecutor;
    public FrameAnalyzer(){
        initializeObjectDetector();
    }

    public void initializeObjectDetector(){
        //TODO: HANDLE CLOSE
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()  // Optional
//                        .enableMultipleObjects()
                        .build();

        objectDetector = ObjectDetection.getClient(options);
        serialExecutor = new SerialExecutor(Executors.newFixedThreadPool(2));
    }

    @Override
    @ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        Log.e("TAG", "analyze: FRAMES "+mediaImage.getHeight()+"x"+mediaImage.getWidth());
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        Task<List<DetectedObject>> task = objectDetector.process(image);

        task.addOnSuccessListener(detectedObjects -> {
            if (detectedObjects != null) {
                for (DetectedObject detectedObject : detectedObjects) {
                    Rect boundingBox = detectedObject.getBoundingBox();
                    Integer trackingId = detectedObject.getTrackingId();
                    for (DetectedObject.Label label : detectedObject.getLabels()) {
                        String text = label.getText();
                        int index = label.getIndex();
                        float confidence = label.getConfidence();
                        Log.e("TAG", "onSuccess: TEXT : "+text+" index : "+index+" confidence + "+confidence);
                    }
                }
            }
        })
        .addOnFailureListener(e -> Log.e("TAG", "analyze: onFailure"))
        .addOnCompleteListener((result)-> imageProxy.close());

    }

}
