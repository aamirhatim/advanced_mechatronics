package com.aamirhatim.linefinder;

// libraries

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.SeekBar;

import java.io.IOException;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static android.graphics.Color.rgb;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView mTextureView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    private Canvas canvas = new Canvas(bmp);
    private Paint paint1 = new Paint();
    private TextView mTextView;
    private SeekBar thresh_adj;
    private SeekBar sensitivity_adj;
    private int thresh = 0, sensitivity = 0, rgb_max = 255;

    static long prevtime = 0; // for FPS calculation

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keeps the screen from turning off

        mTextView = (TextView) findViewById(R.id.cameraStatus);
        thresh_adj = (SeekBar) findViewById(R.id.seek1);
        sensitivity_adj = (SeekBar) findViewById(R.id.seek2);

        // see if the app has permission to use the camera
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
            mSurfaceHolder = mSurfaceView.getHolder();

            mTextureView = (TextureView) findViewById(R.id.textureview);
            mTextureView.setSurfaceTextureListener(this);

            // set the paintbrush for writing text on the image
            paint1.setColor(0xffff0000); // red
            paint1.setTextSize(24);

            mTextView.setText("started camera");
        } else {
            mTextView.setText("no camera permissions");
        }

    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(640, 480);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY); // no autofocusing
        parameters.setAutoExposureLock(true); // keep the white balance constant
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90); // rotate to portrait mode

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    // the important function
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // every time there is a new Camera preview frame
        mTextureView.getBitmap(bmp);

        final Canvas c = mSurfaceHolder.lockCanvas();
        if (c != null) {
            readSlider(); // Read threshold slider
            int line_height = 200;
            int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, line_height, bmp.getWidth(), 1);
//            int[] prev_pix = {0,0,0,0};         // Create averaging array
            for (int i = 4; i < bmp.getWidth()-5; i++) {
//                int pix_avg = get_pixel_average(prev_pix);
//                if ((((red(pixels[i])+blue(pixels[i])+green(pixels[i]))/3) - pix_avg > 20) && pix_avg < thresh) {
//                    pixels[i] = rgb(0, 255, 0); // over write the pixel with pure green
//
//                    // update the row
//                    bmp.setPixels(pixels, 0, bmp.getWidth(), 0, line_height, bmp.getWidth(), 1);
//                }
//                prev_pix = shift_averager(prev_pix, pixels);

//                if (red(pixels[i]) > thresh || green(pixels[i]) > thresh || blue(pixels[i]) > thresh) {
//                    pixels[i] = rgb(0, 255, 0); // over write the pixel with pure green
//                }

                if (green(pixels[i]) > thresh && green(pixels[i+5]) - green(pixels[i]) > thresh) {
                    pixels[i] = rgb(255, 0, 0); // over write the pixel with pure red

                }


            }
            bmp.setPixels(pixels, 0, bmp.getWidth(), 0, line_height, bmp.getWidth(), 1);




//            for (int j = 0; j < bmp.getHeight(); j+=4) {
//                int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
//                bmp.getPixels(pixels, 0, bmp.getWidth(), 0, j, bmp.getWidth(), 1);
//
//                // in the row, see if there is more green than red
//                for (int i = 0; i < bmp.getWidth(); i++) {
//                    if ((green(pixels[i]) - red(pixels[i])) > thresh) {
//                        pixels[i] = rgb(0, 255, 0); // over write the pixel with pure green
//                    }
//                }
//
//                // update the row
//                bmp.setPixels(pixels, 0, bmp.getWidth(), 0, j, bmp.getWidth(), 1);
//            }


        }

        // draw a circle at some position
        int pos = 50;
        canvas.drawCircle(pos, 240, 5, paint1); // x position, y position, diameter, color

        // write the pos as text
        canvas.drawText("thresh = " + thresh, 10, 200, paint1);
        c.drawBitmap(bmp, 0, 0, null);
        mSurfaceHolder.unlockCanvasAndPost(c);

        // calculate the FPS to see how fast the code is running
        long nowtime = System.currentTimeMillis();
        long diff = nowtime - prevtime;
        mTextView.setText("FPS " + 1000 / diff);
        prevtime = nowtime;
    }

    private void readSlider() {
        thresh_adj.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int thresh_changed = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thresh_changed = progress;
                float rgb_val = (progress*rgb_max)/100;
                thresh = (int) rgb_val;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int get_pixel_average(int[] pixels) {
        int avg = 0;
        for (int i = 0; i < pixels.length; i++) {
            avg += pixels[i];
        }
        avg = (int) avg/pixels.length;
        return avg;
    }

    private int[] shift_averager(int[] pixels, int[] new_val) {
        for (int i = pixels.length-1; i > 0; i--) {
            pixels[i] = pixels[i - 1];
        }
        int avg = (new_val[0]+new_val[1]+new_val[2])/3;
        pixels[0] = avg;
        return pixels;
    }
}
