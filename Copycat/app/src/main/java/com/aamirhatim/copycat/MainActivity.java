package com.aamirhatim.copycat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    SeekBar sendNum;
    TextView sliderVal;
    TextView buttonVal;
    TextView usbVal;
    Button button;
    ScrollView scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendNum = (SeekBar) findViewById(R.id.seek1);
        sliderVal = (TextView) findViewById(R.id.textView01);
        buttonVal = (TextView) findViewById(R.id.textView02);
        usbVal = (TextView) findViewById(R.id.textView03);
        button = (Button) findViewById(R.id.button1);
        scrollview = (ScrollView) findViewById(R.id.ScrollView01);

        // Initialize button functionality
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonVal.setText("Value sent: " + sendNum.getProgress());
            }
        });

        setVal();   // Read slider bar value and print it

    }

    private void setVal() {
        sendNum.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int valChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valChanged = progress;
                sliderVal.setText("Slider set to: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
