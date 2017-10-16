package com.jallier.rbuzz;

/**
 * Created by Justin on 16/10/2017.
 */

public class SimpleTime {
    private long time;

    public SimpleTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }

    public void updateTime(){
        this.time = System.currentTimeMillis();
    }
}
