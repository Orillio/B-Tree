package com.company;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args)
    {

    }
}
class BTree<K extends Comparable<K>, V> implements RangeMap<K, V>{
    public Node<K, V> rootNode;
    int degree;
    public BTree(int deg){
        degree = deg;
        rootNode = new Node<K, V>(deg, null);
    }
    @Override
    public int size() {
        var tempNode = rootNode;
        var size = 0;
        for (int i = 0; i < tempNode.children.size(); i++) {
            size += tempNode.size();
        }
        return size;
    }
    @Override
    public boolean isEmpty() {
        return rootNode.size() == 0;
    }
    @Override
    public boolean contains(K key) {
        var tempNode = rootNode;
        var index = tempNode.getIndexOfKeys(key);
        if(index != tempNode.entries.size() && tempNode.entries.get(index).key == key) return true;

        while(tempNode.children.size() != 0){
            tempNode = tempNode.children.get(index);
            index = tempNode.getIndexOfKeys(key);

            if(index == tempNode.entries.size()){
                if(tempNode.entries.get(index - 1).key == key) return true;
            }
            else{
                if(tempNode.entries.get(index).key == key) return true;
            }
        }
        return false;
    }
    @Override
    public V lookup(K key) {
        var tempNode = rootNode;
        var index = tempNode.getIndexOfKeys(key);
        if(index != tempNode.entries.size() && tempNode.entries.get(index).key == key) return tempNode.entries.get(index).value;

        while(tempNode.children.size() != 0){
            tempNode = tempNode.children.get(index);
            index = tempNode.getIndexOfKeys(key);

            if(index == tempNode.entries.size()){
                if(tempNode.entries.get(index - 1).key == key) return tempNode.entries.get(index - 1).value;
            }
            else{
                if(tempNode.entries.get(index).key == key) return tempNode.entries.get(index).value;
            }
        }
        return null;
    }
    @Override
    public List<V> lookupRange(K from, K to) {
        Node<K, V> upperNode = new Node<>(degree, null);
        upperNode.children.add(rootNode);
        List<V> result = new ArrayList<>();
        rootNode.preOrder(upperNode, result, from, to);
        return result;
    }

    @Override
    public void add(K key, V value) {
        var newEntry = new Record<K, V>(key, value);
        var tempNode = rootNode;
        var indexToTraverse = rootNode.getIndexOfKeys(newEntry.key);

        while(tempNode.children.size() != 0){
            tempNode = tempNode.children.get(indexToTraverse);
            indexToTraverse = tempNode.getIndexOfKeys(newEntry.key);
        }
        tempNode.entries.add(indexToTraverse, newEntry);
        if(tempNode.isReadyToSplit()){
            var result = tempNode.split((tempNode.entries.size() / 2) - 1);
            if(result != null){
                rootNode = result;
            }
        }

    }
}

class Node<K extends Comparable<K>, V> {
    int degree;
    Node<K, V> parent;
    ArrayList<Record<K, V>> entries;
    ArrayList<Node<K, V>> children;

    public Node(int deg, Node p) {
        parent = p;
        degree = deg;
        entries = new ArrayList<>();
        children = new ArrayList<>();
    }

    public boolean isFull() {
        return entries.size() == degree * 2 - 1;
    }

    public int size() {
        return entries.size();
    }
    public static int count = 0;
    public boolean isReadyToSplit() {
        return entries.size() == (degree * 2 - 1) + 1;
    }
    public int getIndexOfKeys(K key) {
        if (entries.size() == 0) return 0;

        if (entries.get(0).key.compareTo(key) >= 0) {
            return 0;
        }
        for (int i = 0; i < entries.size() - 1; i++) {
            if (entries.get(i).key.compareTo(key) == -1 && entries.get(i + 1).key.compareTo(key) == 1) {
                return i + 1;
            } else if ((entries.get(i).key.compareTo(key) == 0)) {
                return i;
            } else if ((entries.get(i + 1).key.compareTo(key) == 0)) {
                return i + 1;
            }
        }
        return entries.size();
    }
    public Node<K, V> split(int index) {

        ArrayList<Record<K, V>> left = new ArrayList<>();
        ArrayList<Record<K, V>> right = new ArrayList<>();
        Node<K, V> leftNode;
        Node<K, V> rightNode;
        Record<K, V> midEntry = entries.get(index);
        entries.remove(index);
        for (int i = 0; i < index; i++) {
            left.add(entries.get(i));
        }
        for (int i = index; i < entries.size(); i++) {
            right.add(entries.get(i));
        }

        leftNode = new Node<>(degree, parent);
        rightNode = new Node<>(degree, parent);


        if (parent == null) {
            Node<K, V> newParent = new Node<>(degree, null);
            leftNode = new Node<>(degree, newParent);
            rightNode = new Node<>(degree, newParent);
            if (children.size() != 0) {
                var childrenInd = 0;
                for (int i = 0; i <= left.size(); i++) {
                    children.get(childrenInd).parent = leftNode;
                    leftNode.children.add(children.get(childrenInd));
                    childrenInd++;
                }
                for (int i = 0; i <= right.size(); i++) {
                    children.get(childrenInd).parent = rightNode;
                    rightNode.children.add(children.get(childrenInd));
                    childrenInd++;
                }
            }
            newParent.entries.add(midEntry);
            leftNode.entries = left;
            rightNode.entries = right;
            newParent.children.add(leftNode);
            newParent.children.add(rightNode);
            return newParent;
        }

        if (children.size() != 0) {
            var childrenInd = 0;
            for (int i = 0; i <= left.size(); i++) {
                children.get(childrenInd).parent = leftNode;
                leftNode.children.add(children.get(childrenInd));
                childrenInd++;
            }
            for (int i = 0; i <= right.size(); i++) {
                children.get(childrenInd).parent = rightNode;
                rightNode.children.add(children.get(childrenInd));
                childrenInd++;
            }
        }

        var indexUp = parent.getIndexOfKeys(midEntry.key);
        var indexOfThisNode = parent.children.indexOf(this);
        parent.entries.add(indexUp, midEntry);
        leftNode.entries = left;
        rightNode.entries = right;
        parent.children.remove(indexOfThisNode);
        parent.children.add(indexOfThisNode, rightNode);
        parent.children.add(indexOfThisNode, leftNode);

        if (parent.isReadyToSplit()) {
            return parent.split((parent.entries.size() / 2) - 1);
        }
        return null;
    }
    public void preOrder(Node<K,V> node, List<V> result,  K from, K to){
        for (int j = 0; j < node.entries.size(); j++) {
            if(node.entries.get(j).key.compareTo(from) >= 0 && node.entries.get(j).key.compareTo(to) <= 0){
                result.add(node.entries.get(j).value);
            }
        }
        for (int i = 0; i < node.children.size(); i++) {
            if(node.entries.size() == 0) {
                preOrder(node.children.get(i), result, from, to);
                break;
            }
            if(i == 0){
                if(node.entries.get(i).key.compareTo(from) >= 0){
                    preOrder(node.children.get(i), result, from, to);
                }
            }
            else if(i == node.children.size() - 1){
                if(node.entries.get(i - 1).key.compareTo(to) <= 0){
                    preOrder(node.children.get(i), result, from, to);
                }
            }
            else{
                if((node.entries.get(i - 1).key.compareTo(to) <= 0 || node.entries.get(i).key.compareTo(to) >= 0)
                    || (node.entries.get(i - 1).key.compareTo(from) <= 0 && node.entries.get(i).key.compareTo(from) >= 0 )){
                    preOrder(node.children.get(i), result, from, to);
                }
            }
        }
    }
}

class Record<K, V>{
    K key;
    V value;
    public Record(K k, V v){
        key = k;
        value = v;
    }
}
interface RangeMap<K,V> {
    int size();
    boolean isEmpty();
    void add(K key, V value);
    boolean contains(K key);
    V lookup(K key);
    List<V> lookupRange(K from, K to);
}
