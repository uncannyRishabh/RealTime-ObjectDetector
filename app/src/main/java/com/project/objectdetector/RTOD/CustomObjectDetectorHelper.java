package com.project.objectdetector.RTOD;

import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.project.objectdetector.UI.Views.BoundingBox;

import java.util.List;

public class CustomObjectDetectorHelper {
    private BoundingBox box;
    private LocalModel localModel;
    private ObjectDetector objectDetector;

    public CustomObjectDetectorHelper(int detectorMode, float confidenceThreshold, int maxLabelPerObject, String modelName){
        loadModel(modelName);
        initializeDetector(detectorMode, confidenceThreshold, maxLabelPerObject);
    }

    public static CustomObjectDetectorHelper getInstance(int detectorMode, float confidenceThreshold, int maxLabelPerObject, String modelName){
        return new CustomObjectDetectorHelper(detectorMode, confidenceThreshold, maxLabelPerObject, modelName);
    }

    private void loadModel(String modelName){
        localModel = new LocalModel.Builder()
                .setAssetFilePath(modelName+".tflite")
                .build();
    }

    private void initializeDetector(int detectorMode, float confidenceThreshold, int maxLabelPerObject) {
        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                        .setDetectorMode(detectorMode) // CustomObjectDetectorOptions.STREAM_MODE
                        .enableClassification()
                        .setClassificationConfidenceThreshold(confidenceThreshold)
                        .setMaxPerObjectLabelCount(maxLabelPerObject)
                        .build();

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
    }

    public Task<List<DetectedObject>> process(byte [] byteArray, int width, int height){
        return objectDetector.process(inputImageFromByteArray(byteArray, width, height));
    }

    private InputImage inputImageFromByteArray(byte [] byteArray, int width, int height){
        return InputImage.fromByteArray(
                byteArray,
                width,
                height,
                0,
                InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        );
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

    public Task<List<DetectedObject>> process(InputImage image) {
        return objectDetector.process(image);
    }
}
