package edu.vt.ece.hw4.bench;

import edu.vt.ece.hw4.locks.Lock;

public class LongCSTestThread extends Thread implements ThreadId {
    private static int ID_GEN = 0;

    private final Counter counter;
    private final Lock lock;

    private final int id;
    private long elapsed;
    private final int iter;

    public LongCSTestThread(Lock lock, Counter counter, int iter) {
        id = ID_GEN++;
        this.lock = lock;
        this.counter = counter;
        this.iter = iter;
    }

    public static void reset() {
        ID_GEN = 0;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        lock.lock();
        for(int i=0; i<iter; i++)
            counter.getAndIncrement();
        lock.unlock();

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