package edu.vt.ece.hw4.locks;

import java.util.concurrent.atomic.AtomicReference;

public class MCSLock implements Lock {
    private final AtomicReference<QNode> queue;
    private final ThreadLocal<QNode> myNode;

    public MCSLock() {
        queue = new AtomicReference<>(null);
        myNode = ThreadLocal.withInitial(QNode::new);
    }

    @Override
    public void lock() {
        QNode qnode = myNode.get();
        qnode.locked = true;
        qnode.next = null;

        QNode pred = queue.getAndSet(qnode);
        if (pred != null) {
            pred.next = qnode;
            //System.out.println(Thread.currentThread().getName() + " A");

            while (qnode.locked) {
                Thread.yield();
            }
        }
        //System.out.println(Thread.currentThread().getName() + " B.");
    }

    @Override
    public void unlock() {
        QNode qnode = myNode.get();
        if (qnode.next == null) {
            if (queue.compareAndSet(qnode, null)) {
                //System.out.println(Thread.currentThread().getName() + " C");
                return;
            }

            //System.out.println(Thread.currentThread().getName() + " D");
            while (qnode.next == null) {
                Thread.yield();
            }
        }
        //System.out.println(Thread.currentThread().getName() + " E");
        qnode.next.locked = false;
        qnode.next = null;
    }

    static class QNode {     // Queue node inner class
        volatile boolean locked = false;
        volatile QNode next = null;
    }
}
