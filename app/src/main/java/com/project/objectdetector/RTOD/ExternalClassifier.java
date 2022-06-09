package com.project.objectdetector.RTOD;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;

import androidx.camera.core.ImageProxy;
import androidx.camera.core.internal.utils.ImageUtil;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

import java.nio.ByteBuffer;
import java.util.List;

public class ExternalClassifier {
    CustomObjectDetectorHelper FASHION_GOOD_CLASSIFIER;
    CustomObjectDetectorHelper HOME_GOOD_CLASSIFIER;
    CustomObjectDetectorHelper FOOD_CLASSIFIER;
    CustomObjectDetectorHelper PLACE_CLASSIFIER;
    CustomObjectDetectorHelper PLANT_CLASSIFIER;
    CustomObjectDetectorHelper DEFAULT_CLASSIFIER;

    public ExternalClassifier() {
        FASHION_GOOD_CLASSIFIER = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,.5f,2, "fashion_good");
        HOME_GOOD_CLASSIFIER = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,.5f,2, "home_good");
        FOOD_CLASSIFIER = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,.5f,2, "food");
        PLACE_CLASSIFIER = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,.5f,2, "place");
        PLANT_CLASSIFIER = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,.5f,2, "plant");
        DEFAULT_CLASSIFIER = new CustomObjectDetectorHelper(CustomObjectDetectorOptions.STREAM_MODE
                ,.5f,2, "default_mobilenet");
    }

    public Task<List<DetectedObject>> performClassification(ImageProxy imageProxy, Rect rect, String coarseLabel)
            throws ImageUtil.CodecFailedException {
        switch (coarseLabel){
            case PredefinedCategory.FASHION_GOOD:{
                return FASHION_GOOD_CLASSIFIER.process(cropImageProxy(imageProxy,rect)
                        ,rect.right-rect.left
                        ,rect.bottom-rect.top);
            }
            case PredefinedCategory.FOOD:{
                return FOOD_CLASSIFIER.process(cropImageProxy(imageProxy,rect)
                        ,rect.right-rect.left
                        ,rect.bottom-rect.top);
            }
            case PredefinedCategory.HOME_GOOD:{
                return HOME_GOOD_CLASSIFIER.process(cropImageProxy(imageProxy,rect)
                        ,rect.right-rect.left
                        ,rect.bottom-rect.top);
            }
            case PredefinedCategory.PLANT:{
                return PLANT_CLASSIFIER.process(cropImageProxy(imageProxy,rect)
                        ,rect.right-rect.left
                        ,rect.bottom-rect.top);
            }
            case PredefinedCategory.PLACE:{
                return PLACE_CLASSIFIER.process(cropImageProxy(imageProxy,rect)
                        ,rect.right-rect.left
                        ,rect.bottom-rect.top);
            }
            default:{
                return DEFAULT_CLASSIFIER.process(cropImageProxy(imageProxy,rect)
                        ,rect.right-rect.left
                        ,rect.bottom-rect.top);
            }
        }
    }

//    private void initializeExternalClassifier(ImageProxy imageProxy, Rect rect, String label){
//        CustomObjectDetectorHelper.getInstance(CustomObjectDetectorOptions.STREAM_MODE
//                        ,.5f,1,"fashion_good")
//                .process(cropImageProxy(imageProxy,rect)
//                        , rect.right-rect.left
//                        , rect.bottom-rect.top);
//    }

    @SuppressLint("RestrictedApi")
    private byte[] cropImageProxy(ImageProxy imageProxy, Rect rect) throws ImageUtil.CodecFailedException {
        return ImageUtil.jpegImageToJpegByteArray(imageProxy
//                ,new Rect(rect.left,rect.top,rect.right,rect.bottom)
                ,rect
                ,100);
    }

    private byte[] cropByteBuffer(RectF bb, Image image){
        Image.Plane[] planes    = image.getPlanes();
        ByteBuffer buffer       = planes[0].getBuffer();
        int stride              = planes[0].getRowStride();
        buffer.rewind();
//        byte[] Y = new byte[buffer.capacity()];
//        buffer.get(Y);

        int t= (int) bb.top; int l= (int) bb.left;
        int out_h = (int) bb.bottom; int out_w = (int) bb.right;
        byte[] out = new byte[out_w*out_h];

        int firstRowOffset = stride * t + l;
        for (int row = 0; row < out_h; row++) {
            buffer.position(firstRowOffset + row * stride);
            buffer.get(out, row * out_w, out_w);
        }
        return out;
    }
}
