package edu.vt.ece.hw4;

import edu.vt.ece.hw4.barriers.Barrier;
import edu.vt.ece.hw4.barriers.FirstBarrier;
import edu.vt.ece.hw4.barriers.SecondBarrier;
import edu.vt.ece.hw4.barriers.TTASBarrier;
import edu.vt.ece.hw4.bench.*;
import edu.vt.ece.hw4.locks.ALock;
import edu.vt.ece.hw4.locks.BackoffLock;
import edu.vt.ece.hw4.locks.Lock;
import edu.vt.ece.hw4.locks.TTASLock;
import edu.vt.ece.hw4.locks.MCSLock;
import edu.vt.ece.hw4.locks.SpinSleepLock;
import edu.vt.ece.hw4.locks.SimpleHLock;
import edu.vt.ece.hw4.locks.PriorityQueueLock;
import edu.vt.ece.hw4.locks.CLHLock;

public class Benchmark {

    private static final String ALOCK = "ALock";
    private static final String BACKOFFLOCK = "BackoffLock";
    private static final String MCSLOCK = "MCSLock";
    private static final String TTASLOCK = "TTASLock";
    private static final String FIRSTBARRIER = "FirstBarrier";
    private static final String SECONDBARRIER = "SecondBarrier";
    private static final String SPINSLEEPLOCK = "SpinSleepLock";
    private static final String SIMPLEHLOCK = "SimpleHLock";
    private static final String PRIORITYQUEUELOCK = "PriorityQueueLock";
    private static final String CLHLOCK = "CLHLock";

    public static void main(String[] args) throws Exception {
        String mode = args.length <= 0 ? "normal" : args[0];
        String lockClass = (args.length <= 1 ? CLHLOCK : args[1]);
        int threadCount = (args.length <= 2 ? 8 : Integer.parseInt(args[2]));
        int totalIters = (args.length <= 3 ? 64000 : Integer.parseInt(args[3]));
        int clusters = (args.length > 4) ? Integer.parseInt(args[4]) : 2;
        int iters = totalIters / threadCount;

        run(args, mode, lockClass, threadCount, iters, clusters);
    }

    private static void run(String[] args, String mode, String lockClass, int threadCount, int iters, int clusters) throws Exception {
        for (int i = 0; i < 4; i++) {
            Lock lock = null;

            if (!"barrier".equalsIgnoreCase(mode)) {
                switch (lockClass.trim()) {
                    case ALOCK:
                        lock = new ALock(threadCount);
                        break;
                    case BACKOFFLOCK:
                        String backoffStrategy = (args.length > 5) ? args[5] : "Linear";
                        lock = new BackoffLock(backoffStrategy);
                        break;
                    case MCSLOCK:
                        lock = new MCSLock();
                        break;
                    case TTASLOCK:
                        lock = new TTASLock();
                        break;
                    case SPINSLEEPLOCK:
                        lock = new SpinSleepLock(8);
                        break;
                    case SIMPLEHLOCK:
                        lock = new SimpleHLock(clusters);
                        break;
                    case PRIORITYQUEUELOCK:
                        lock = new PriorityQueueLock(2);
                        break;
                    case CLHLOCK:
                        lock = new CLHLock();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown lock class: " + lockClass);
                }
            }

            switch (mode.trim().toLowerCase()) {
                case "normal":
                    final Counter counter = new SharedCounter(0, lock);
                    runNormal(counter, threadCount, iters);
                    break;
                case "empty":
                    runEmptyCS(lock, threadCount, iters);
                    break;
                case "long":
                    runLongCS(lock, threadCount, iters);
                    break;
                case "barrier":
                    Barrier barrier;
                    switch (lockClass.trim()) {
                        case "FirstBarrier":
                            barrier = new FirstBarrier(threadCount);
                            break;
                        case "SecondBarrier":
                            barrier = new SecondBarrier(threadCount);
                            break;
                        case "TTASBarrier":
                            barrier = new TTASBarrier(threadCount);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown barrier class: " + lockClass);
                    }
                    runBarrier(barrier, threadCount, lockClass);
                    break;
                case "cluster":
                    if (!(lock instanceof SimpleHLock)) {
                        throw new IllegalArgumentException("For 'cluster' mode, use SimpleHLock.");
                    }
                    runClusterCS((SimpleHLock) lock, threadCount, iters, clusters);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown mode: " + mode);
            }
        }
    }

    private static void runNormal(Counter counter, int threadCount, int iters) throws Exception {
        final TestThread[] threads = new TestThread[threadCount];
        TestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new TestThread(counter, iters);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }

    private static void runEmptyCS(Lock lock, int threadCount, int iters) throws Exception {
        final EmptyCSTestThread[] threads = new EmptyCSTestThread[threadCount];
        EmptyCSTestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new EmptyCSTestThread(lock, iters);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }

    private static void runLongCS(Lock lock, int threadCount, int iters) throws Exception {
        final Counter counter = new Counter(0);
        final LongCSTestThread[] threads = new LongCSTestThread[threadCount];
        LongCSTestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new LongCSTestThread(lock, counter, iters);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }

    private static void runBarrier(Barrier barrier, int threadCount, String barrierName) throws InterruptedException {
        final BarrierTestThread[] threads = new BarrierTestThread[threadCount];

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new BarrierTestThread(barrier);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }
        long startTime = System.nanoTime();

        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
        }
        long endTime = System.nanoTime();
        System.out.println("Total barrier time for " + barrierName + " with " + threadCount + " threads: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    private static void runClusterCS(SimpleHLock lock, int threadCount, int iters, int clusters) throws Exception {
        final LongCSTestThread[] threads = new LongCSTestThread[threadCount];
        LongCSTestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new LongCSTestThread(lock, new Counter(0), iters);
        }
        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }
        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }
        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }

    static class BarrierTestThread extends Thread {
        private final Barrier barrier;

        BarrierTestThread(Barrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " executing foo()");
                barrier.await();

                System.out.println(Thread.currentThread().getName() + " executing bar()");
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                System.out.println("Thread " + Thread.currentThread().getName() + " interrupted.");
            }
        }
    }
}
