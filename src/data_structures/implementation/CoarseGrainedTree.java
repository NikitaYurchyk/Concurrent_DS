package data_structures.implementation;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

// Replace the exceptions in the code below with the actual code for the assignment.
// Leave the public API the same so it can be tested with the existing evaluation framework.
// Document your design/implementation choices in comments.

class NodeCGT<T extends Comparable<T>>{
    volatile T value;
    volatile AtomicInteger counter;
    NodeCGT<T> left;
    NodeCGT<T> right;
    NodeCGT(T value){
        this.value = value;
        this.counter = new AtomicInteger(1);
        left = null;
        right = null;
    }
}


public class CoarseGrainedTree<T extends Comparable<T>> implements Sorted<T> {
    ReentrantLock lock = new ReentrantLock();
    NodeCGT<T> root;
    public void add(T t) {
        lock.lock();
        try {
            root = insert(root, t);
        } finally {
            lock.unlock();
        }
    }

    public NodeCGT<T> insert(NodeCGT node, T val) {
        if (node == null) {
            return new NodeCGT<>(val);
        }
        if(node.value.compareTo(val) > 0){
            node.left = insert(node.left, val);
        }
        else if(node.value.compareTo(val) < 0){
            node.right = insert(node.right, val);
        }
        else if(node.value.compareTo(val) == 0) {
            node.counter.incrementAndGet();
        }

        return node;
    }

    public void remove(T t) {
        lock.lock();
        try {
            if (root == null) {
                return;
            }
            root = delete(root, t);
        } finally {
            lock.unlock();
        }
    }

    public NodeCGT<T> delete(NodeCGT node, T val) {
        if (node == null) {
            return null;
        }
        if (node.value.compareTo(val) > 0) {
            node.left = delete(node.left, val);
        }
        else if (node.value.compareTo(val) < 0) {
            node.right = delete(node.right, val);
        }
        else if (node.value.compareTo(val) == 0) {
            if(node.counter.get() > 1){
                node.counter.decrementAndGet();
                return node;
            }
            if (node.left == null) {
                return node.right;
            }

            if (node.right == null) {
                return node.left;
            }

            NodeCGT<T> successor = findMin(node.right);
            node.value = successor.value;
            node.counter = new AtomicInteger(successor.counter.get());
            node.right = deleteMin(node.right);
        }
        return node;
    }

    private NodeCGT<T> findMin(NodeCGT<T> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private NodeCGT<T> deleteMin(NodeCGT<T> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = deleteMin(node.left);
        return node;
    }

    public T removeLowest() {
        lock.lock();
        try {
            if (root != null) {
                var lowestMin = findMin(root);
                remove(lowestMin.value);
                return lowestMin.value;
            }
        }
        finally{
            lock.unlock();
        }
        return null;
    }

    public ArrayList<T> toArrayList() {
        lock.lock();
        try {
            NodeCGT<T> current = root;
            ArrayList<T> result = new ArrayList<>();
            inOrder(result, current);
            return result;
        } finally {
            lock.unlock();
        }
    }

    private void inOrder(ArrayList<T> arr, NodeCGT<T> node){
        if(node == null){
            return;
        }
        inOrder(arr, node.left);
        if(node != null) {
            for(int i = 0; i < node.counter.get(); i++) {
                arr.add(node.value);
            }
        }
        inOrder(arr, node.right);
    }
}

