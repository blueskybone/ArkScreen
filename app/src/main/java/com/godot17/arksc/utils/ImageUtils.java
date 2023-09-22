package com.godot17.arksc.utils;

import android.graphics.Bitmap;
import android.media.Image;

import java.nio.ByteBuffer;

public class ImageUtils {

    public static Bitmap image_2_bitmap(Image image, Bitmap.Config config) {

        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap bitmap;

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride/*equals: rowStride/pixelStride */
                , height, config);
        bitmap.copyPixelsFromBuffer(buffer);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    /*
     * Cut the screenshot to middle rect only contain 5 tags.
     * These magic numbers come from experimental results of arknights UI layout
     *
     * */
    public static Bitmap bitmap_2_target_bitmap(Bitmap srcBitmap, int width, int height) {
        int x, y, tag_width, tag_height;
        if (width > 2 * height) {
            y = (int) (height / 2.06);
            tag_height = (int) (height / 5.143);
            x = (int) (width / 2 - height / 2.572);
            tag_width = (int) (tag_height * 3.7);
        } else {
            x = (int) (width / 3.636);
            tag_width = (int) (width / 2.5);
            y = (int) (height / 2 - width / 111.111);
            tag_height = (int) (width * 0.281);
        }
        return Bitmap.createBitmap(srcBitmap, x, y, tag_width, tag_height);
    }

    public static int getScale(int width){
        return width/640;
    }
}
