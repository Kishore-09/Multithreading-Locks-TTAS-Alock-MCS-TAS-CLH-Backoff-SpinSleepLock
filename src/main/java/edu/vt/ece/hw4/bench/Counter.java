package edu.vt.ece.hw4.bench;

public class Counter {
    private int value;

    public Counter(int c) {
        value = c;
    }

    public int getAndIncrement() {
        int temp = value;
        value = temp + 1;
        return temp;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
