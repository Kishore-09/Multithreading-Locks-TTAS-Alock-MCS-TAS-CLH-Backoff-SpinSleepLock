package edu.vt.ece.hw4.locks;

import edu.vt.ece.hw4.backoff.Backoff;
import edu.vt.ece.hw4.backoff.BackoffFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class BackoffLock implements Lock {
    private final AtomicBoolean state = new AtomicBoolean(false);
    private final Backoff backoff;

    public BackoffLock(String backoffType) {
        this.backoff = BackoffFactory.getBackoff(backoffType);
    }

    @Override
    public void lock() {
        while (true) {
            while (state.get()) { }
            if (!state.getAndSet(true)) {
                return;
            } else {
                try {
                    backoff.backoff();
                } catch (InterruptedException ignored) { }
            }
        }
    }

    @Override
    public void unlock() {
        state.set(false);
    }
}
