package com.project.objectdetector.RTOD;

import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.project.objectdetector.UI.Views.BoundingBox;

import java.util.List;

public class FrameAnalyzer implements ImageAnalysis.Analyzer {
    private BoundingBox box;
    private Size previewRes,inputRes;
    private ObjectDetector objectDetector;

    public static int CLASSIFY_SINGLE_OBJECT = 0;
    public static int CLASSIFY_MULTIPLE_OBJECTS = 1;

    public FrameAnalyzer(){
        initializeObjectDetector(CLASSIFY_SINGLE_OBJECT);
    }

    public FrameAnalyzer(@IntRange(from = 0, to = 1) int mode){
        initializeObjectDetector(mode);
    }

    public void initializeObjectDetector(int mode){
        //TODO: HANDLE CLOSE
        ObjectDetectorOptions options;
        if(mode == CLASSIFY_SINGLE_OBJECT) {
            options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()
                    .enableMultipleObjects()
                    .build();
        }
        else {
            options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()
                    .build();
        }

        objectDetector = ObjectDetection.getClient(options);
    }

    public void closeDetector(){
        box.setVisibility(View.GONE);
        objectDetector.close();
    }

    public void setView(BoundingBox box){
        this.box = box;
        box.setVisibility(View.VISIBLE);
    }

    public void setPreviewResolution(Size res){
        this.previewRes = res;
    }

    public void setInputResolution(Size res){
        this.inputRes = res;
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
                    box.setBoxRect(mapBoxRect(boundingBox));

                    for (DetectedObject.Label label : detectedObject.getLabels()) {
                        String text = label.getText();
                        int index = label.getIndex();
                        float confidence = label.getConfidence();
                        box.setLabelText(text);
                        Log.e("TAG", "onSuccess: TEXT : "+text
                                +" tracking ID "+trackingId+
                                " index : "+index+
                                " confidence + "+confidence);
                    }
                }
            }
        })
        .addOnFailureListener(e -> Log.e("TAG", "analyze: unable to detect"))
        .addOnCompleteListener((result)-> imageProxy.close());

    }

    private RectF mapBoxRect(Rect boundingBox){
        if(inputRes !=null){
//            Log.e("TAG", "mapBoxRect: inputres : "+inputRes);
//            Log.e("TAG", "mapBoxRect: previewres : "+previewRes);

            int w = previewRes.getWidth()/inputRes.getWidth();
            int h = previewRes.getHeight()/inputRes.getHeight();

            boundingBox.set(boundingBox.left * w,
                    boundingBox.top * h,
                    boundingBox.right * w,
                    boundingBox.bottom * h);

        }
        return new RectF(boundingBox);
    }

}
