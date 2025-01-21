package edu.vt.ece.hw4.locks;

import java.util.concurrent.atomic.AtomicInteger;

public class ALock implements Lock {
    private ThreadLocal<Integer> mySlotIndex = ThreadLocal.withInitial(() -> 0);

    private AtomicInteger tail;
    private boolean[] flag;
    private int size;

    /**
     * Constructor
     * @param capacity max number of array slots
     */
    public ALock(int capacity) {
        size = capacity;
        tail = new AtomicInteger(0);
        flag = new boolean[capacity];
        flag[0] = true;
    }

    @Override
    public void lock() {
        int slot = tail.getAndIncrement() % size;
        mySlotIndex.set(slot);
        while (! flag[mySlotIndex.get()]) {};
    }

    @Override
    public void unlock() {
        flag[mySlotIndex.get()] = false;
        flag[(mySlotIndex.get() + 1) % size] = true;
    }
}