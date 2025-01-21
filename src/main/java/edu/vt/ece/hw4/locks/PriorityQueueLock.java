package edu.vt.ece.hw4.locks;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.TimeUnit;

public class PriorityQueueLock implements Lock {
    private static final int DEFAULT_PRIORITY = 1 ;
    private final PriorityBlockingQueue<Node> queue = new PriorityBlockingQueue<>();
    private volatile Node currentHolder = null;

    private final long timeout;

    public PriorityQueueLock(long timeout) {
        this.timeout = timeout;
    }
    private static class Node implements Comparable<Node> {
        final Thread thread;
        final int priority;

        Node(Thread thread, int priority) {
            this.thread = thread;
            this.priority = priority;
        }
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.priority, other.priority);
        }
    }
    @Override
    public void lock() {
        int priority = Thread.currentThread().getPriority();
        Node node = new Node(Thread.currentThread(), priority);
        queue.add(node);

        while (true) {
            Node topNode = queue.peek();
            if (topNode == node && (currentHolder == null || currentHolder == node)) {
                currentHolder = node;
                queue.poll();
                return;
            }
            LockSupport.parkNanos(100);
        }
    }
    public boolean tryLock() {
        int priority = Thread.currentThread().getPriority();
        Node node = new Node(Thread.currentThread(), priority);
        queue.add(node);

        long endTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        while (System.nanoTime() < endTime) {
            Node topNode = queue.peek();
            if (topNode == node && (currentHolder == null || currentHolder == node)) {
                currentHolder = node;
                queue.poll();
                return true;
            }
            LockSupport.parkNanos(100);
        }
        queue.remove(node);
        return false;
    }
    @Override
    public void unlock() {
        if (currentHolder != null && currentHolder.thread == Thread.currentThread()) {
            currentHolder = null;
            Node nextNode = queue.peek();
            if (nextNode != null) {
                LockSupport.unpark(nextNode.thread);
            }
        } else {
        }
    }
}
