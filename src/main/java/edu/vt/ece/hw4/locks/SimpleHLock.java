package edu.vt.ece.hw4.locks;

import edu.vt.ece.hw4.utils.ThreadCluster;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleHLock implements Lock {

    private final int numClusters;
    private final ReentrantLock globalLock = new ReentrantLock(true);
    private final ReentrantLock[] localLocks;
    private final AtomicInteger[] localCounters;
    private final int BATCH_COUNT = 5;

    public SimpleHLock(int numClusters) {
        this.numClusters = numClusters;
        this.localLocks = new ReentrantLock[numClusters];
        this.localCounters = new AtomicInteger[numClusters];

        for (int i = 0; i < numClusters; i++) {
            this.localLocks[i] = new ReentrantLock(true);
            this.localCounters[i] = new AtomicInteger(0);
        }
    }

    @Override
    public void lock() {
        int clusterId = ThreadCluster.getCluster();
        ReentrantLock localLock = localLocks[clusterId];

        localLock.lock();  // Lock the local cluster lock

        if (localCounters[clusterId].getAndIncrement() == 0) {
            globalLock.lock();
        }
    }

    @Override
    public void unlock() {
        int clusterId = ThreadCluster.getCluster();
        ReentrantLock localLock = localLocks[clusterId];

        if (localCounters[clusterId].decrementAndGet() == 0 ||
                localCounters[clusterId].get() % BATCH_COUNT == 0) {
            globalLock.unlock();
        }
        localLock.unlock();
    }
}
