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
    private Paint paint1 = new Paint(), paint2 = new Paint(), paint3 = new Paint();
    private TextView mTextView, thresh_txt, sens_txt;
    private SeekBar thresh_adj;
    private SeekBar sensitivity_adj;
    private int thresh = 0, sensitivity = 0, rgb_max = 255;

    static long prevtime = 0; // for FPS calculation

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keeps the screen from turning off

        mTextView = (TextView) findViewById(R.id.cameraStatus);
        thresh_txt = (TextView) findViewById(R.id.thresh_val);
        sens_txt = (TextView) findViewById(R.id.sens_val);
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

            paint2.setColor(0xffffffff); // white
            paint2.setTextSize(24);

            mTextView.setText("Started Samera");
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
        int l_edge, r_edge;

        // every time there is a new Camera preview frame
        mTextureView.getBitmap(bmp);
        final Canvas c = mSurfaceHolder.lockCanvas();

        if (c != null) {
            readSlider(); // Read threshold and sensitivity sliders
            int line_height = 400;
            int step = 6;
            int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, line_height, bmp.getWidth(), 1);

            l_edge = get_left_edge(step, pixels);
            r_edge = get_right_edge(step, pixels);

            int middle = (l_edge+r_edge)/2;
            canvas.drawCircle(l_edge, line_height, 4, paint1); // x position, y position, diameter, color
            canvas.drawCircle(r_edge, line_height, 4, paint2);
            canvas.drawCircle(middle, line_height, 6, paint2);

        }

        // Draw canvas
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
                float rgb_val = (progress*rgb_max)/100; // Map value to rgb scale
                thresh = (int) rgb_val;
                thresh_txt.setText("Black Threshold = "+thresh);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sensitivity_adj.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int sens_changed = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sens_changed = progress;
                float sens_rgb = (progress*rgb_max)/100;    // Map value to rgb scale
                sensitivity = (int) sens_rgb;
                sens_txt.setText("Sensitivity = "+sensitivity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int get_left_edge(int step, int[] pixels) {
        int edge = 0;
        for (int i = 0; i < (3*bmp.getWidth()/5)-step; i++) {
            if (green(pixels[i]) < thresh && green(pixels[i+step]) > thresh && green(pixels[i+step]) - green(pixels[i]) > sensitivity) {
                edge = i+(step/2);
                return edge;    // Stop searching when an edge is found
            }
        }
        return edge;
    }

    private int get_right_edge(int step, int[] pixels) {
        int edge = bmp.getWidth();
        for (int i = 2*bmp.getWidth()/5; i < bmp.getWidth()-step; i++) {
            if (blue(pixels[i]) > thresh && blue(pixels[i+step]) < thresh && blue(pixels[i]) - blue(pixels[i+step]) > sensitivity) {
                edge = i+(step/2);
                return edge;    // Stop searching when an edge is found
            }
        }
        return edge;
    }
}
