package com.jallier.rbuzz;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get vibration api class
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final Button btnPlayBuzz = (Button) findViewById(R.id.btnPlayBuzz);
        btnPlayBuzz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Play button clicked");
                vibrator.vibrate(250);
            }
        });

        final SimpleTime currentTime = new SimpleTime(0);
        final SimpleTime compareTime = new SimpleTime(0);

        final Button btnRecordBuzz = (Button) findViewById(R.id.btnRecordBuzz);
        btnRecordBuzz.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "record pushed down");
                    currentTime.updateTime();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    compareTime.updateTime();
                    Log.d(TAG, "record released");
                    Log.d(TAG, "" + (compareTime.getTime() - currentTime.getTime()));
                }
                return true;
            }
        });
    }
}


