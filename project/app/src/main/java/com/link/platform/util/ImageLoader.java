package com.link.platform.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public class ImageLoader {

    private static ImageLoader Instance = null;

    public static ImageLoader getInstance() {
        if( Instance == null ) {
            synchronized (ImageLoader.class) {
                if( Instance == null ) {
                    Instance = new ImageLoader();
                }
            }
        }
        return Instance;
    }

    private String path;

    private ImageLoader() {
        path = Environment.getExternalStorageDirectory().getPath() + Utils.STORAGE_PATH + Utils.IMG_CACHE;
    }

    public void loadImage(ImageView view, String url, int default_id) {
        if( StringUtil.isBlank(url) ) {
            view.setImageResource(default_id);
            return;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;// 设置成了true,不占用内存，只获取bitmap宽高
        BitmapFactory.decodeFile(url, opts);
        opts.inSampleSize = computeSampleSize(opts, -1, 1024 * 800);

        opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        opts.inDither = false;
        opts.inPurgeable = true;
        opts.inTempStorage = new byte[16 * 1024];
        FileInputStream is = null;
        Bitmap bmp = null;
        InputStream ins = null;
        try {
            is = new FileInputStream(path);
            bmp = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
            double scale = getScaling(opts.outWidth * opts.outHeight, 1024 * 600);
            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp,
                    (int) (opts.outWidth * scale),
                    (int) (opts.outHeight * scale), true);
            bmp.recycle();
            view.setImageBitmap(bmp2);
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                ins.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.gc();
        }
        view.setImageResource(default_id);
    }

    private double getScaling(int src, int des) {
        /**
         * 目标尺寸÷原尺寸 sqrt开方，得出宽高百分比
         */
        double scale = Math.sqrt((double) des / (double) src);
        return scale;
    }


    public int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
