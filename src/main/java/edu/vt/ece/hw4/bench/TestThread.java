package edu.vt.ece.hw4.bench;


public class TestThread extends Thread implements ThreadId {
    private static int ID_GEN = 0;

    private Counter counter;
    private int id;
    private long elapsed;
    private int iter;

    public TestThread(Counter counter, int iter) {
        id = ID_GEN++;
        this.counter = counter;
        this.iter = iter;
    }

    public static void reset() {
        ID_GEN = 0;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        for(int i=0; i<iter; i++)
            counter.getAndIncrement();

        long end = System.currentTimeMillis();
        elapsed = end - start;
    }

    public int getThreadId(){
        return id;
    }

    public long getElapsedTime() {
        return elapsed;
    }
}