package edu.vt.ece.hw4.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SpinSleepLock implements Lock {

    private final AtomicBoolean state = new AtomicBoolean(false);
    private final AtomicInteger spinningCount = new AtomicInteger(0);
    private final int maxSpin;

    public SpinSleepLock(int maxSpin) {
        this.maxSpin = maxSpin;
    }

    @Override
    public void lock() {
        boolean isSpinning = false;

        synchronized (this) {
            while (spinningCount.get() >= maxSpin) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            spinningCount.incrementAndGet();
            isSpinning = true; // This thread will spin
        }
        while (isSpinning && !state.compareAndSet(false, true)) {
        }
    }

    @Override
    public void unlock() {
        state.set(false);

        synchronized (this) {
            spinningCount.decrementAndGet();
            this.notify();
        }
    }
}
