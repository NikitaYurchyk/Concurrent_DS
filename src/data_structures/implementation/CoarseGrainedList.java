package data_structures.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

// Replace the exceptions in the code below with the actual code for the assignment.
// Leave the public API the same so it can be tested with the existing evaluation framework.
// Document your design/implementation choices in comments.


class NodeCLL<T extends Comparable<T>> {
    public volatile T value;
    public volatile NodeCLL<T> next;
    public volatile AtomicInteger counter;

    public NodeCLL(T value) {
        this.value = value;
        this.counter = new AtomicInteger(1);
        this.next = null;
    }
}

public class CoarseGrainedList<T extends Comparable<T>> implements Sorted<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private NodeCLL<T> head;

    public CoarseGrainedList() {
        head = new NodeCLL<>(null);
    }

    public void add(T t) {
        lock.lock();
        try {
            NodeCLL<T> pred = head;
            NodeCLL<T> curr = head.next;

            while (curr != null && curr.value.compareTo(t) < 0) {
                pred = curr;
                curr = curr.next;
            }

            if (curr != null && curr.value.equals(t)) {
                curr.counter.incrementAndGet();
            } else {
                NodeCLL<T> newNode = new NodeCLL<>(t);
                pred.next = newNode;
                newNode.next = curr;
            }
        } finally {
            lock.unlock();
        }
    }

    public void remove(T t) {
        lock.lock();
        try {
            NodeCLL<T> pred = head;
            NodeCLL<T> curr = head.next;

            while (curr != null && !curr.value.equals(t)) {
                pred = curr;
                curr = curr.next;
            }

            if (curr != null) {
                if (curr.counter.get() > 1) {
                    curr.counter.decrementAndGet();
                } else {
                    pred.next = curr.next;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public T removeLowest() {
        lock.lock();
        try {
            if (head.next == null) {
                return null;
            }

            NodeCLL<T> lowestNode = head.next;
            if (lowestNode.counter.get() > 1) {
                lowestNode.counter.decrementAndGet();
            } else {
                head.next = lowestNode.next;
            }

            return lowestNode.value;
        } finally {
            lock.unlock();
        }
    }

    public ArrayList<T> toArrayList() {
        lock.lock();
        try {
            NodeCLL<T> tmpNode = head.next;
            ArrayList<T> result = new ArrayList<>();

            while (tmpNode != null) {
                for (int i = 0; i < tmpNode.counter.get(); i++) {
                    result.add(tmpNode.value);
                }
                tmpNode = tmpNode.next;
            }

            return result;
        } finally {
            lock.unlock();
        }
    }
}