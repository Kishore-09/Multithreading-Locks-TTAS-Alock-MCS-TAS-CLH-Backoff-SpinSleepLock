package edu.vt.ece.hw4.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadCluster {

    private static int totalClusters = 1;
    private static AtomicInteger nextID = new AtomicInteger(0);
    private static ThreadLocal<Integer> threadID = ThreadLocal.withInitial(() -> nextID.getAndIncrement());

    /**
     * Set the total number of clusters.
     */
    public static void setTotalClusters(int clusters) {
        totalClusters = clusters;
    }
    /**
     * Get the unique thread ID for the current thread.
     */
    public static int get() {
        return threadID.get();
    }
    /**
     * Map the thread ID to a cluster index within the bounds of totalClusters.
     */
    public static int getCluster() {
        return threadID.get() % totalClusters;
    }

    /**
     * Reset thread ID state (useful for running multiple tests).
     */
    public static void reset() {
        nextID.set(0);
    }
}
