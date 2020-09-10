package com.kds.tone.model;

public class RequestCounter {

    private int counter;
    private long timestamp;

    public RequestCounter(int counter, long timestamp) {
        this.counter = counter;
        this.timestamp = timestamp;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
