package com.jallier.rbuzz;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final long TIME_WINDOW = 5000; // ms for how long to check times between btn pushes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get vibration api class
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final ArrayList<Long> pattern = new ArrayList<>();
        pattern.add((long) 0); // First value in array represents delay before first buzz, so set to 0
        final SimpleBool initialBtnPush = new SimpleBool(true);

        final Button btnPlayBuzz = (Button) findViewById(R.id.btnPlayBuzz);
        btnPlayBuzz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Play button clicked. Pattern: " + pattern.toString());
                long[] pt = new long[pattern.size()];
                for (int i = 0; i < pattern.size(); i++) {
                    pt[i] = pattern.get(i);
                }
                vibrator.vibrate(pt, -1);
//                resetPattern(pattern); Add this in once done testing
            }
        });

        final SimpleTime initialDownTime = new SimpleTime(0);
        final SimpleTime initialUpTime = new SimpleTime(0);

        final Button btnRecordBuzz = (Button) findViewById(R.id.btnRecordBuzz);
        btnRecordBuzz.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                long currentTime = System.currentTimeMillis();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialDownTime.updateTime();
                    long elapsed = currentTime - initialUpTime.getTime();
                    if (elapsed > TIME_WINDOW) {
                        elapsed = 0;
                    }
                    if (!initialBtnPush.isTrue()) {
                        pattern.add(elapsed);
                    }
                    initialBtnPush.setBool(false);
                    Log.d(TAG, "record pushed down. Length between pushes: " + elapsed);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    initialUpTime.updateTime();
                    long elapsed = currentTime - initialDownTime.getTime();
                    pattern.add(elapsed);
                    Log.d(TAG, "record released. Length: " + elapsed);
                }
                return true;
            }
        });

        final Button btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPattern(pattern);
                initialBtnPush.setBool(true);
                vibrator.cancel();
                Log.d(TAG, "Pattern cleared");
            }
        });
    }

    private void resetPattern(ArrayList<Long> pattern) {
        pattern.clear();
        pattern.add((long) 0);
    }

    private class SimpleBool {
        boolean bool;

        public SimpleBool(boolean bool) {
            this.bool = bool;
        }

        public boolean isTrue() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }
    }
}


