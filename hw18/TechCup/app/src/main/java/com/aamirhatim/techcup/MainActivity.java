package com.aamirhatim.techcup;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorEventListener;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.SeekBar;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private TextView mTextView, thresh_txt, sens_txt, pic_data;
    private SeekBar thresh_adj;
    private SeekBar sensitivity_adj;
    private int thresh = 0, sensitivity = 0, rgb_max = 255;

    private UsbManager manager;
    private UsbSerialPort sPort;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;
    private ScrollView data_stream;


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
//        pic_data = (TextView) findViewById(R.id.usbComm);
//        data_stream = (ScrollView) findViewById(R.id.dataStream);

        // see if the app has permission to use the camera
//        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
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

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

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
            int line_height = 250;
            int step = 6;
            int[] pixels = new int[bmp.getWidth()]; // pixels[] is the RGBA data
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, line_height, bmp.getWidth(), 1);

            l_edge = get_left_edge(step, pixels);
            r_edge = get_right_edge(step, pixels);

            int middle = (l_edge+r_edge)/2;

            String sendString = String.valueOf(middle) + '\n';
            try {
                sPort.write(sendString.getBytes(), 10); // 10 is the timeout
            } catch (IOException e) { }

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
//        for (int i = 0; i < (4*bmp.getWidth()/5)-step; i++) {
//            if (green(pixels[i]) < thresh && green(pixels[i+step]) > thresh && green(pixels[i+step]) - green(pixels[i]) > sensitivity) {
//                edge = i+(step/2);
//                return edge;    // Stop searching when an edge is found
//            }
//        }

        int avg, avg_step;
        for (int i = 0; i < (4*bmp.getWidth()/5)-step; i++) {
            avg = (red(pixels[i])+green(pixels[i])+blue(pixels[i]))/3;
            avg_step = (red(pixels[i+step])+green(pixels[i+step])+blue(pixels[i+step]))/3;
            if (avg > thresh && avg_step < thresh) {
                edge = i+(step/2);
                return edge;    // Stop searching when an edge is found
            }
        }
        return edge;
    }

    private int get_right_edge(int step, int[] pixels) {
        int edge = bmp.getWidth();
//        for (int i = bmp.getWidth()/5; i < bmp.getWidth()-step; i++) {
//            if (blue(pixels[i]) > thresh && blue(pixels[i+step]) < thresh && blue(pixels[i]) - blue(pixels[i+step]) > sensitivity) {
//                edge = i+(step/2);
//                return edge;    // Stop searching when an edge is found
//            }
//        }

        int avg, avg_step;
        for (int i = bmp.getWidth()/5; i < bmp.getWidth()-step; i++) {
            avg = (red(pixels[i])+green(pixels[i])+blue(pixels[i]))/3;
            avg_step = (red(pixels[i+step])+green(pixels[i+step])+blue(pixels[i+step]))/3;
            if (avg < thresh && avg_step > thresh) {
                edge = i+(step/2);
                return edge;    // Stop searching when an edge is found
            }
        }
        return edge;
    }

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onRunError(Exception e) {

                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    @Override
    protected void onPause(){
        super.onPause();
        stopIoManager();
        if(sPort != null){
            try{
                sPort.close();
            } catch (IOException e){ }
            sPort = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x04D8,0x000A, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);

        final List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);

        if(availableDrivers.isEmpty()) {
            //check
            return;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        sPort = driver.getPorts().get(0);

        if (sPort == null){
            //check
        }else{
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
            if (connection == null){
                //check
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
                usbManager.requestPermission(driver.getDevice(), pi);
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            }catch (IOException e) {
                //check
                try{
                    sPort.close();
                } catch (IOException e1) { }
                sPort = null;
                return;
            }
        }
        onDeviceStateChange();
    }

    private void stopIoManager(){
        if(mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if(sPort != null){
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange(){
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        //do something with received data
        return;

        //for displaying:
//        String rxString = null;
//        try {
//            rxString = new String(data, "UTF-8"); // put the data you got into a string
//            pic_data.append(rxString);
//            data_stream.fullScroll(View.FOCUS_DOWN);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }
}
