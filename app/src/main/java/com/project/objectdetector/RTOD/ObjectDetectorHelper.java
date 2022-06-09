package com.project.objectdetector.RTOD;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.annotation.IntRange;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.project.objectdetector.UI.Views.BoundingBox;

import java.util.List;

public class ObjectDetectorHelper {
    private BoundingBox box;
    private ObjectDetector objectDetector;
    public static final int CLASSIFY_SINGLE_OBJECT = 0;
    public static final int CLASSIFY_MULTIPLE_OBJECTS = 1;

    public ObjectDetectorHelper(){
        initializeObjectDetector(CLASSIFY_SINGLE_OBJECT);
    }

    public ObjectDetectorHelper(int detectionMode,int detectorMode){
        initializeObjectDetector(detectionMode, detectorMode);
    }

    public void initializeObjectDetector(int detectionMode){
        //TODO: HANDLE CLOSE
        ObjectDetectorOptions options;
        if(detectionMode == CLASSIFY_SINGLE_OBJECT) {
            options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()
                    .build();
        }
        else {
            options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()
                    .enableMultipleObjects()
                    .build();
        }

        objectDetector = ObjectDetection.getClient(options);
    }

    public void initializeObjectDetector(@IntRange(from = 0, to = 1) int detectionMode,int detectorMode){
        //TODO: HANDLE CLOSE
        ObjectDetectorOptions options;

        if(detectionMode == CLASSIFY_SINGLE_OBJECT) {
            options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(detectorMode)
                    .enableClassification()
                    .build();
        }
        else {
            options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(detectorMode)
                    .enableClassification()
                    .enableMultipleObjects()
                    .build();
        }

        objectDetector = ObjectDetection.getClient(options);
    }

    public void createDetectorFromBitmap(Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap,0);

        processImage(image).addOnSuccessListener(detectedObjects -> {
                    getView().setDetectedObjects(detectedObjects);
                    for(DetectedObject detectedObject : detectedObjects){
                        Log.e("TAG", "createDetectorFromBitmap: " + detectedObject.getBoundingBox());
                    }
                })
                .addOnFailureListener(e -> {
                    getView().postInvalidate();
                    Log.e("TAG", "analyze: unable to detect");
                });
    }

    public Task<List<DetectedObject>> processImage(InputImage image){
        return objectDetector.process(image);
    }

    public void closeDetector(){
        box.setVisibility(View.GONE);
         objectDetector.close();
    }

    public void setView(BoundingBox box){
        this.box = box;
        box.setVisibility(View.VISIBLE);
    }

    public BoundingBox getView(){
        return box;
    }
}
