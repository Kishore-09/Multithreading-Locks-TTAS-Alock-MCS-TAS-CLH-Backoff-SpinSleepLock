package edu.vt.ece.hw4.locks;

import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements Lock {
    private final AtomicReference<QNode> tail;
    private final ThreadLocal<QNode> myNode;
    private final ThreadLocal<QNode> myPred;

    public CLHLock() {
        tail = new AtomicReference<>(new QNode());

        myNode = ThreadLocal.withInitial(QNode::new);
        myPred = ThreadLocal.withInitial(() -> null);
    }

    @Override
    public void lock() {
        QNode node = myNode.get();
        node.locked = true;
        QNode pred = tail.getAndSet(node);
        myPred.set(pred);

        while (pred.locked) {

        }
    }

    @Override
    public void unlock() {
        QNode node = myNode.get();
        node.locked = false;
        myNode.set(myPred.get());
    }

    private static class QNode {
        private volatile boolean locked = false;
    }
}
