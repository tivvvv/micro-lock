package com.tiv.lock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 自定义锁
 */
public class MyLock {

    AtomicBoolean flag = new AtomicBoolean(false);

    /**
     * 持有锁的线程
     */
    Thread owner = null;

    AtomicReference<Node> head = new AtomicReference<>(new Node());
    AtomicReference<Node> tail = new AtomicReference<>(head.get());

    void lock() {
        // 尝试获取锁
        if (flag.compareAndSet(false, true)) {
            owner = Thread.currentThread();
            return;
        }
        // 没有获取到锁,则将当前线程加入队列尾部
        Node currentNode = new Node();
        currentNode.thread = Thread.currentThread();
        while (true) {
            Node currentTail = tail.get();
            if (tail.compareAndSet(currentTail, currentNode)) {
                currentNode.pre = currentTail;
                currentTail.next = currentNode;
                break;
            }
        }
        while (true) {
            // 尝试获取锁
            if (currentNode.pre == head.get() && flag.compareAndSet(false, true)) {
                owner = Thread.currentThread();
                head.set(currentNode);
                currentNode.pre.next = null;
                currentNode.pre = null;
                return;
            }
            // 阻塞直到被唤醒
            LockSupport.park();
        }
    }

    void unlock() {
        if (Thread.currentThread() != this.owner) {
            throw new IllegalStateException("当前线程不是持有锁的线程,不能解锁");
        }
        Node headNode = head.get();
        Node nextNode = headNode.next;
        flag.set(false);
        if (nextNode != null) {
            LockSupport.unpark(nextNode.thread);
        }
    }

    static class Node {
        Node pre;
        Node next;
        Thread thread;
    }
}
