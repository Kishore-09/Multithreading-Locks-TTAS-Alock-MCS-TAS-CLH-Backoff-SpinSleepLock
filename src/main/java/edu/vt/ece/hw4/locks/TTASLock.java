package edu.vt.ece.hw4.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock implements Lock {

    private AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (true) {
            while (state.get()) {};  // ece.vt.edu.spin
            if (!state.getAndSet(true))
                return;
        }
    }

    public void unlock() {
        state.set(false);
    }

}