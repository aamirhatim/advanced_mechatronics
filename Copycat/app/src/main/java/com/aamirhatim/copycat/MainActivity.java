package com.aamirhatim.copycat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    SeekBar sendNum;
    TextView numVal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendNum = (SeekBar) findViewById(R.id.seek1);
        numVal = (TextView) findViewById(R.id.textView01);

        setVal();   // Read slider bar value and print it

    }

    private void setVal() {
        sendNum.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int valChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valChanged = progress;
                numVal.setText("Value set to: " + progress);
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
