/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.runtime;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.hellblazer.primeMover.runtime.SplayQueue.BinaryNode.duplicate;

/**
 * Implements a priority queue using a splay tree. This queue implementation
 * allows duplicates.
 * 
 * Original splay tree implementation by Danny Sleator <sleator@cs.cmu.edu>
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */

public class SplayQueue<K extends Comparable<K>> implements Queue<K> {
    static class BinaryNode<K extends Comparable<K>> {
        static class duplicate<K extends Comparable<K>> {
            final duplicate<K> next;
            final K            value;

            duplicate(K value, duplicate<K> next) {
                this.value = value;
                this.next = next;
            }
        }

        duplicate<K>  duplicates;
        BinaryNode<K> left;
        BinaryNode<K> right;
        K             value;

        BinaryNode(K theKey) {
            value = theKey;
            left = right = null;
        }

        public int fill(int i, Object[] result) {
            result[i++] = value;
            duplicate<K> current = duplicates;
            while (current != null) {
                result[i++] = current.value;
                current = current.next;
            }
            return i;
        }

        K getValue() {
            return value;
        }

        void insert(K newValue) {
            if (value == null) {
                value = newValue;
            } else {
                duplicates = new duplicate<K>(newValue, duplicates);
            }
        }

        boolean removeValue() {
            if (duplicates == null) {
                return true;
            }
            value = duplicates.value;
            duplicates = duplicates.next;
            return false;
        }
    }

    private BinaryNode<K> root;

    private int size = 0;

    public SplayQueue() {
        root = null;
    }

    @Override
    public boolean add(K e) {
        if (e == null) {
            throw new NullPointerException("Cannot add null elements");
        }
        BinaryNode<K> n;
        int c;
        if (root == null) {
            root = new BinaryNode<K>(e);
        } else {
            splay(e);
            if ((c = e.compareTo(root.value)) == 0) {
                root.insert(e);
            } else {
                n = new BinaryNode<K>(e);
                if (c < 0) {
                    n.left = root.left;
                    n.right = root;
                    root.left = null;
                } else {
                    n.right = root.right;
                    n.left = root;
                    root.right = null;
                }
                root = n;
            }
        }
        size++;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        boolean changed = false;
        for (K e : c) {
            changed |= add(e);
        }
        return changed;
    }

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            throw new NullPointerException("Does not allow null elements");
        }
        if (!(o instanceof Comparable<?>)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        K k = (K) o;
        if (root == null) {
            return false;
        }
        splay(k);
        if (root.value.compareTo(k) != 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public K element() {
        BinaryNode<K> x = root;
        if (root == null) {
            throw new NoSuchElementException();
        }
        while (x.left != null) {
            x = x.left;
        }
        splay(x.value);
        return x.value;
    }

    /**
     * Test if the tree is logically empty.
     * 
     * @return true if empty, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            BinaryNode<K>        currentRoot = root;
            duplicate<K>         duplicates;
            Deque<BinaryNode<K>> visiting    = new ArrayDeque<BinaryNode<K>>();

            @Override
            public boolean hasNext() {
                return duplicates == null && currentRoot != null;
            }

            @Override
            public K next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                K result;
                if (duplicates != null) {
                    result = duplicates.value;
                    duplicates = duplicates.next;
                    return result;
                }
                if (visiting.isEmpty()) {
                    pushLeft(currentRoot);
                }
                BinaryNode<K> node = visiting.pop();
                result = node.value;
                duplicates = node.duplicates;
                if (node.right != null) {
                    BinaryNode<K> right = node.right;
                    pushLeft(right);
                }
                if (visiting.isEmpty()) {
                    currentRoot = null;
                }
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void pushLeft(BinaryNode<K> node) {
                if (node != null) {
                    visiting.push(node);
                    pushLeft(node.left);
                }
            }
        };
    }

    @Override
    public boolean offer(K e) {
        return add(e);
    }

    @Override
    public K peek() {
        BinaryNode<K> x = root;
        if (root == null) {
            return null;
        }
        while (x.left != null) {
            x = x.left;
        }
        splay(x.value);
        return x.value;
    }

    @Override
    public K poll() {
        BinaryNode<K> x = root;
        if (root == null) {
            return null;
        }
        while (x.left != null) {
            x = x.left;
        }
        splay(x.value);
        K result = x.value;
        deleteRoot(result);
        return result;
    }

    @Override
    public K remove() {
        BinaryNode<K> x = root;
        if (root == null) {
            throw new NoSuchElementException();
        }
        while (x.left != null) {
            x = x.left;
        }
        splay(x.value);
        K result = x.value;
        if (x.removeValue()) {
            deleteRoot(result);
        }
        size--;
        return result;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            throw new NullPointerException("null elements are not allowed");
        }
        @SuppressWarnings("unchecked")
        K key = (K) o;
        splay(key);
        if (key.compareTo(root.value) != 0) {
            return false;
        }
        if (root.removeValue()) {
            deleteRoot(key);
        }
        size--;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        fill(result);
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            @SuppressWarnings("unchecked")
            T[] newInstance = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            a = newInstance;
        }
        fill(a);
        return a;
    }

    /**
     * Returns a string representation of this collection. The string representation
     * consists of a list of the collection's elements in the order they are
     * returned by its iterator, enclosed in square brackets ( <tt>"[]"</tt>).
     * Adjacent elements are separated by the characters <tt>", "</tt> (comma and
     * space). Elements are converted to strings as by
     * {@link String#valueOf(Object)}.
     * 
     * @return a string representation of this collection
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        BinaryNode<K> node = root;
        int i = 0;
        while (null != node) {
            Deque<BinaryNode<K>> nodes = new ArrayDeque<BinaryNode<K>>();
            while (!nodes.isEmpty() || null != node) {
                if (null != node) {
                    nodes.push(node);
                    node = node.left;
                } else {
                    node = nodes.pop();
                    K e = node.value;
                    sb.append(e == this ? "(this Collection)" : e);
                    i++;
                    duplicate<K> current = node.duplicates;
                    while (current != null) {
                        e = current.value;
                        sb.append(", ");
                        sb.append(e == this ? "(this Collection)" : e);
                        i++;
                        current = current.next;
                    }
                    node = node.right;
                    if (i != size) {
                        sb.append(", ");
                    }
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private void deleteRoot(K result) {
        BinaryNode<K> x;
        // Now delete the root
        if (root.left == null) {
            root = root.right;
        } else {
            x = root.right;
            root = root.left;
            splay(result);
            root.right = x;
        }
    }

    private void fill(Object[] result) {
        BinaryNode<K> node = root;
        int i = 0;
        while (null != node) {
            Deque<BinaryNode<K>> nodes = new ArrayDeque<BinaryNode<K>>();
            while (!nodes.isEmpty() || null != node) {
                if (null != node) {
                    nodes.push(node);
                    node = node.left;
                } else {
                    node = nodes.pop();
                    i = node.fill(i, result);
                    node = node.right;
                }
            }
        }
    }

    /**
     * splay(key) does the splay operation on the given key. If key is in the tree,
     * then the BinaryNode containing that key becomes the root. If key is not in
     * the tree, then after the splay, key.root is either the greatest key < key in
     * the tree, or the lest key > key in the tree.
     * 
     * This means, among other things, that if you splay with a key that's larger
     * than any in the tree, the rightmost node of the tree becomes the root. This
     * property is used in the delete() method.
     */

    private void splay(K key) {
        BinaryNode<K> l, r, t, y, header;
        header = new BinaryNode<K>(null);
        l = r = header;
        t = root;
        header.left = header.right = null;
        for (;;) {
            if (key.compareTo(t.value) < 0) {
                if (t.left == null) {
                    break;
                }
                if (key.compareTo(t.left.value) < 0) {
                    y = t.left; /* rotate right */
                    t.left = y.right;
                    y.right = t;
                    t = y;
                    if (t.left == null) {
                        break;
                    }
                }
                r.left = t; /* link right */
                r = t;
                t = t.left;
            } else if (key.compareTo(t.value) > 0) {
                if (t.right == null) {
                    break;
                }
                if (key.compareTo(t.right.value) > 0) {
                    y = t.right; /* rotate left */
                    t.right = y.left;
                    y.left = t;
                    t = y;
                    if (t.right == null) {
                        break;
                    }
                }
                l.right = t; /* link left */
                l = t;
                t = t.right;
            } else {
                break;
            }
        }
        l.right = t.left; /* assemble */
        r.left = t.right;
        t.left = header.right;
        t.right = header.left;
        root = t;
    }
}
