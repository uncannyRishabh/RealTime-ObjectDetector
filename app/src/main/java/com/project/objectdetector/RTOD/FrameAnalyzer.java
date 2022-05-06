package com.project.objectdetector.RTOD;


import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;

public class FrameAnalyzer implements ImageAnalysis.Analyzer {
    private ObjectDetector objectDetector;
    public FrameAnalyzer(){
        initializeObjectDetector();
    }

    public void initializeObjectDetector(){
        //TODO: HANDLE CLOSE
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
//                        .enableClassification()  // Optional
//                        .enableMultipleObjects()
                        .build();

        objectDetector = ObjectDetection.getClient(options);
    }

    @Override
    @ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
//        Log.e("TAG", "analyze: FRAMES "+mediaImage.getHeight()+"x"+mediaImage.getWidth());
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
                        Log.e("TAG", "onSuccess: TEXT : "+text
                                +" tracking ID "+trackingId+
                                " index : "+index+
                                " confidence + "+confidence);
                    }
                }
            }
        })
        .addOnFailureListener(e -> Log.e("TAG", "analyze: onFailure"))
        .addOnCompleteListener((result)-> imageProxy.close());

    }

}
