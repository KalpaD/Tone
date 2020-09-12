package com.kds.tone.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestCounter that = (RequestCounter) o;

        if (counter != that.counter) return false;
        return timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(counter, timestamp);
    }

    @Override
    public String toString() {
        return "RequestCounter{" + "counter=" + counter +
                ", timestamp=" + timestamp +
                '}';
    }
}
