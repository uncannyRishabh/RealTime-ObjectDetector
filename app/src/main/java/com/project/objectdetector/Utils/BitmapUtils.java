package com.project.objectdetector.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class BitmapUtils {

    public BitmapUtils(){}

    public Bitmap compressBitmap(String data){
        BitmapFactory.Options Options = new BitmapFactory.Options();
        Options.inSampleSize = 4;
        Options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(Uri.parse(data).getPath().startsWith("/raw") ?
                Uri.parse(data).getPath().split("raw")[1]
                : Uri.parse(data).getPath() , Options);
    }
}
