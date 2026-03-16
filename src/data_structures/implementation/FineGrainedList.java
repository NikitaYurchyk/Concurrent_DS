package data_structures.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
//import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

// Replace the exceptions in the code below with the actual code for the assignment.
// Leave the public API the same so it can be tested with the existing evaluation framework.
// Document your design/implementation choices in comments.




class NodeFLL<T extends Comparable<T>>{
    public  T value;
    public volatile AtomicInteger counter;
    public NodeFLL<T> next;
    public ReentrantLock lock;
    public NodeFLL(T value) {
        this.value = value;
        this.counter = new AtomicInteger(1);
        this.next = null;
        this.lock = new ReentrantLock();
    }
}

public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {
    private final NodeFLL<T> head;
    public FineGrainedList() {
        head = new NodeFLL<>(null);
    }


    public void add(T t) {
        head.lock.lock();
        NodeFLL<T> pred = head;
        NodeFLL<T> curr = head.next;

        if(curr == null){
            pred.next = new NodeFLL<>(t);
            head.lock.unlock();
            return;
        }

        if(curr != null && curr.value.compareTo(t) == 0){
            curr.counter.incrementAndGet();
            head.lock.unlock();
            return;
        }

        curr.lock.lock();

        while(curr != null && curr.value.compareTo(t) < 0) {
            NodeFLL<T> tmp = pred;
            pred = curr;
            curr = curr.next;
            if(curr != null)
                curr.lock.lock();
            tmp.lock.unlock();
        }
        if(curr != null && curr.value.compareTo(t) == 0) {
            curr.counter.incrementAndGet();
            curr.lock.unlock();
            pred.lock.unlock();
            return;
        }
        if(curr != null && curr.value.compareTo(t) > 0){
            var newNode = new NodeFLL<>(t);
            newNode.next = curr;
            pred.next = newNode;
            curr.lock.unlock();
            pred.lock.unlock();
            return;
        }

        var newNode = new NodeFLL<>(t);
        pred.next = newNode;
        pred.lock.unlock();

    }



    public void remove(T t) {
        head.lock.lock();
        NodeFLL<T> prev = head;
        NodeFLL<T> curr = prev.next;

        if(curr == null){
            head.lock.unlock();
            return;
        }
        curr.lock.lock();
        if(curr.value.compareTo(t) == 0){
            if(curr.counter.get() == 1) {
                prev.next = curr.next;
            }else {
                curr.counter.decrementAndGet();
            }
            curr.lock.unlock();
            prev.lock.unlock();
            return;
        }

        while(curr != null && !curr.value.equals(t)) {
            NodeFLL<T> tmpprev = prev;
            prev = curr;
            curr = curr.next;
            tmpprev.lock.unlock();
            if(curr != null) {
                curr.lock.lock();
            }
        }

        if(curr != null && curr.counter.get() > 1) {
            curr.counter.decrementAndGet();
            curr.lock.unlock();
            prev.lock.unlock();
            return;
        }

        if(curr != null) {
            prev.next = curr.next;
            curr.next = null;
        }

        prev.lock.unlock();

        if(curr != null) {
            curr.lock.unlock();
        }
    }


    public T removeLowest() {
        head.lock.lock();
        var prev = head;
        var curr = head.next;
        if(curr == null) {
            head.lock.unlock();
            return null;
        }

        curr.lock.lock();

        if(curr.counter.get() == 1) {
            prev.next = curr.next;
        }

        if(curr.counter.get() > 1) {
            curr.counter.decrementAndGet();
        }
        prev.lock.unlock();
        curr.lock.unlock();
        return curr.value;
    }

    public ArrayList<T> toArrayList() {
        head.lock.lock();
        NodeFLL<T> prev = head;
        NodeFLL<T> tmpNode = head.next;
        ArrayList<T> result = new ArrayList<>();
        while(tmpNode != null) {
            tmpNode.lock.lock();
            prev.lock.unlock();
            for(int i = 0; i < tmpNode.counter.get(); i++) {
                result.add(tmpNode.value);
            }
            prev = tmpNode;
            tmpNode = tmpNode.next;
        }
        prev.lock.unlock();
        return result;
    }
}