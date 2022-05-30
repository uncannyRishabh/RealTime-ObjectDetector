package com.project.objectdetector.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;

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

    public Bitmap compressBitmap(Bitmap bitmap){
        BitmapFactory.Options Options = new BitmapFactory.Options();
        Options.inSampleSize = 4;
        Options.inJustDecodeBounds = false;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,70,baos);

        byte []bytes = baos.toByteArray();
        Bitmap r = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, Options);
        Log.e("TAG", "compressBitmap: before : "+bitmap.getByteCount()+" after : "+ r.getByteCount());
        return r;
    }
}
