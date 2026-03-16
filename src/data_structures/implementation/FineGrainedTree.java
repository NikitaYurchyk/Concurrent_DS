package data_structures.implementation;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;


class NodeFGT<T extends Comparable<T>>{
    T value;
    int counter;
    NodeFGT<T> left;
    NodeFGT<T> right;
    ReentrantLock lock;
    NodeFGT(T value){
        this.counter = 1;
        this.value = value;
        lock = new ReentrantLock();
        left = null;
        right = null;
    }
}

public class FineGrainedTree<T extends Comparable<T>> implements Sorted<T> {
    NodeFGT<T> head;
    public FineGrainedTree(){
        head = new NodeFGT<>(null);
    }

    public void add(T t)
    {

        head.lock.lock();
        if (head.right == null) {
            head.right = new NodeFGT<>(t);
            head.lock.unlock();
            return;
        }
        var curr = head.right;
        curr.lock.lock();
        head.lock.unlock();
        while (true) {
            if (curr.value.compareTo(t) == 0) {
                curr.counter++;
                curr.lock.unlock();
                return;
            } else if (curr.value.compareTo(t) > 0) {
                if (curr.left == null) {
                    curr.left = new NodeFGT<>(t);
                    curr.lock.unlock();
                    return;
                }else {
                    NodeFGT<T> next = curr.left;
                    next.lock.lock();
                    curr.lock.unlock();
                    curr = next;
                }
            } else if (curr.value.compareTo(t) < 0) {
                if (curr.right == null) {
                    curr.right = new NodeFGT<>(t);
                    curr.lock.unlock();
                    return;
                } else {
                    NodeFGT<T> next = curr.right;
                    next.lock.lock();
                    curr.lock.unlock();
                    curr = next;
                }
            }
        }
    }

    public void remove(T t) {


        head.lock.lock();
        if(head.right == null){
            head.lock.unlock();
            return;
        }
        NodeFGT<T> prev = head;
        NodeFGT<T> curr = head.right;
        curr.lock.lock();
        while(curr != null && curr.value.compareTo(t) != 0){
            var tmp = prev;

            prev = curr;

            if(curr.value.compareTo(t) < 0) {
                curr = curr.right;
            }else{
                curr = curr.left;
            }

            if(curr != null) {
                curr.lock.lock();
            }
            tmp.lock.unlock();
        }

        if(curr == null){
            prev.lock.unlock();
            return;
        }
        if(curr.counter > 1){
            curr.counter--;
            curr.lock.unlock();
            prev.lock.unlock();
            return;
        }
        if(curr.left == null && curr.right != null){
            if(prev.left == curr){
                prev.left = curr.right;
            }else if(prev.right == curr){
                prev.right = curr.right;
            }
            curr.lock.unlock();
            prev.lock.unlock();
            return;
        }

        else if(curr.left != null && curr.right == null){
            if(prev.left == curr){
                prev.left = curr.left;

            }else if(prev.right == curr){
                prev.right = curr.left;
            }
            curr.lock.unlock();
            prev.lock.unlock();
            return;
        }

        else if(curr.right == null && curr.left == null) {
            if(prev.left == curr){
                prev.left = null;
            }else if(prev.right == curr){
                prev.right = null;
            }
            curr.lock.unlock();
            prev.lock.unlock();
            return;
        }


        else if(curr.left != null && curr.right != null){
            var parSucc = curr;
            var succ = curr.right;
            succ.lock.lock();

            while (succ.left != null) {
                parSucc = succ;
                succ = succ.left;
                if(succ != null) {
                    succ.lock.lock();
                }
                parSucc.lock.unlock();
            }

            if(parSucc == curr){
                curr.right = succ.right;
            }
            else{
                parSucc.left = succ.right;
            }
            curr.value = succ.value;
            curr.counter = succ.counter;


            if(succ.lock.isLocked())
                succ.lock.unlock();
            if(curr.lock.isLocked())
                curr.lock.unlock();
            if(prev.lock.isLocked())
                prev.lock.unlock();
        }
    }


    public T removeLowest() {
        head.lock.lock();
        if(head.right == null){
            head.lock.unlock();
            return null;
        }
        var prev = head;
        var curr = head.right;

        curr.lock.lock();

        while(curr.left != null){
            var tmp = prev;
            prev = curr;
            curr = curr.left;
            curr.lock.lock();
            tmp.lock.unlock();
        }

        if(curr.counter > 1){
            curr.counter--;
            curr.lock.unlock();
            prev.lock.unlock();
            return curr.value;
        }

        if(prev == head){
            head.right = curr.right;
        }else{
            prev.left = curr.right;
        }

            curr.lock.unlock();
            prev.lock.unlock();

        return curr.value;
    }


    public ArrayList<T> toArrayList() {
        head.lock.lock();
        try {
            NodeFGT<T> current = head.right;
            ArrayList<T> result = new ArrayList<>();
            inOrder(result, current);
            return result;
        } finally {
            head.lock.unlock();
        }
    }



    private void inOrder(ArrayList<T> arr, NodeFGT<T> root) {
        if (root == null) {
            return;
        }

        Stack<NodeFGT<T>> stack = new Stack<>();
        NodeFGT<T> current = root;

        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }

            current = stack.pop();
            for (int i = 0; i < current.counter; i++) {
                arr.add(current.value);
            }

            current = current.right;
        }
    }

}
