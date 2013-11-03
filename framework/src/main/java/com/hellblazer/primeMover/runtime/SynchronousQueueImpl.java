/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hellblazer.primeMover.Blocking;
import com.hellblazer.primeMover.Entity;
import com.hellblazer.primeMover.SynchronousQueue;

/**
 * The implementation of the CSP channel.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 * @param <E>
 *            - the type of the contents
 */
@Entity
public class SynchronousQueueImpl<E> implements SynchronousQueue<E> {

    static class EmptyIterator<E> implements Iterator<E> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new IllegalStateException();
        }
    }

    /**
     * The simulated entity.
     * 
     * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
     * 
     */
    static class entity<E> extends SynchronousQueueImpl<E> implements
            EntityReference {
        private final static int OFFER_TIMEOUT = 0;
        private final static int POLL_TIMEOUT  = 1;
        private final static int PUT           = 2;
        private final static int TAKE          = 3;

        entity(Devi controller) {
            this.controller = controller;
        }

        @Override
        public void __bindTo(Devi controller) {
            this.controller = controller;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object __invoke(int event, Object[] arguments) throws Throwable {
            switch (event) {
                case OFFER_TIMEOUT: {
                    return super.offer((E) arguments[0], (Integer) arguments[1]);
                }
                case POLL_TIMEOUT: {
                    return super.poll((Integer) arguments[1]);
                }
                case PUT: {
                    super.put((E) arguments[0]);
                    return null;
                }
                case TAKE: {
                    return super.take();
                }
                default:
                    throw new IllegalArgumentException(
                                                       "Unknown event ordinal: "
                                                               + event);
            }
        }

        @Override
        public String __signatureFor(int event) {
            switch (event) {
                case OFFER_TIMEOUT:
                    return "<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: boolean offer(java.lang.Object, int, java.util.concurrent.TimeUnit";
                case POLL_TIMEOUT:
                    return "<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object poll(int, java.util.concurrent.TimeUnit)";
                case PUT:
                    return "<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>";
                case TAKE:
                    return "<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>";
                default:
                    throw new IllegalArgumentException(
                                                       "Unknown event ordinal: "
                                                               + event);
            }
        }

        @Override
        public boolean offer(E e, long timeout) {
            try {
                return super.offer(e, timeout);
            } catch (Throwable ex) {
                throw new IllegalStateException("Exception invoking event", ex);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public E poll(long timeout) {
            try {
                return (E) controller.postContinuingEvent(this, POLL_TIMEOUT,
                                                          timeout);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception invoking event", e);
            }
        }

        @Override
        public void put(Object data) {
            try {
                controller.postContinuingEvent(this, PUT, data);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception invoking event", e);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public E take() {
            try {
                return (E) controller.postContinuingEvent(this, TAKE);
            } catch (Throwable e) {
                throw new IllegalStateException("Exception invoking event", e);
            }
        }

    }

    private class Node {
        EventImpl consumer;
        EventImpl producer;
        E         data;

        boolean hasData() {
            return data != null;
        }
    }

    protected Devi            controller;

    private final Deque<Node> waitList = new ArrayDeque<SynchronousQueueImpl<E>.Node>();

    /**
     * Inserts the specified element into this queue if it is possible to do so
     * immediately without violating capacity restrictions, returning
     * <tt>true</tt> upon success and throwing an <tt>IllegalStateException</tt>
     * if no space is currently available.
     * 
     * <p>
     * This implementation returns <tt>true</tt> if <tt>offer</tt> succeeds,
     * else throws an <tt>IllegalStateException</tt>.
     * 
     * @param e
     *            the element to add
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws IllegalStateException
     *             if the element cannot be added at this time due to capacity
     *             restrictions
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being
     *             added to this queue
     * @throws NullPointerException
     *             if the specified element is null and this queue does not
     *             permit null elements
     * @throws IllegalArgumentException
     *             if some property of this element prevents it from being added
     *             to this queue
     */
    @Override
    public boolean add(E e) {
        if (offer(e)) {
            return true;
        } else {
            throw new IllegalStateException("Queue full");
        }
    }

    /**
     * Adds all of the elements in the specified collection to this queue.
     * Attempts to addAll of a queue to itself result in
     * <tt>IllegalArgumentException</tt>. Further, the behavior of this
     * operation is undefined if the specified collection is modified while the
     * operation is in progress.
     * 
     * <p>
     * This implementation iterates over the specified collection, and adds each
     * element returned by the iterator to this queue, in turn. A runtime
     * exception encountered while trying to add an element (including, in
     * particular, a <tt>null</tt> element) may result in only some of the
     * elements having been successfully added when the associated exception is
     * thrown.
     * 
     * @param c
     *            collection containing elements to be added to this queue
     * @return <tt>true</tt> if this queue changed as a result of the call
     * @throws ClassCastException
     *             if the class of an element of the specified collection
     *             prevents it from being added to this queue
     * @throws NullPointerException
     *             if the specified collection contains a null element and this
     *             queue does not permit null elements, or if the specified
     *             collection is null
     * @throws IllegalArgumentException
     *             if some property of an element of the specified collection
     *             prevents it from being added to this queue, or if the
     *             specified collection is this queue
     * @throws IllegalStateException
     *             if not all the elements can be added at this time due to
     *             insertion restrictions
     * @see #add(Object)
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        boolean modified = false;
        Iterator<? extends E> e = c.iterator();
        while (e.hasNext()) {
            if (add(e.next())) {
                modified = true;
            }
        }
        return modified;
    }

    private void addConsumer() {
        Node node;
        node = new Node();
        node.consumer = controller.swapCaller(null);
        waitList.add(node);
    }

    private void addProducer(E data) {
        Node node;
        node = new Node();
        node.data = data;
        node.producer = controller.swapCaller(null);
        waitList.add(node);
    }

    /**
     * Does nothing. A <tt>SynchronousQueue</tt> has no internal capacity.
     */
    @Override
    public void clear() {
    }

    /**
     * Always returns <tt>false</tt>. A <tt>SynchronousQueue</tt> has no
     * internal capacity.
     * 
     * @param o
     *            the element
     * @return <tt>false</tt>
     */
    @Override
    public boolean contains(Object o) {
        return false;
    }

    /**
     * Returns <tt>false</tt> unless the given collection is empty. A
     * <tt>SynchronousQueue</tt> has no internal capacity.
     * 
     * @param c
     *            the collection
     * @return <tt>false</tt> unless given collection is empty
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    /**
     * @throws UnsupportedOperationException
     *             {@inheritDoc}
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             {@inheritDoc}
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     */
    @Override
    @Blocking
    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        int n = 0;
        E e;
        while ((e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * @throws UnsupportedOperationException
     *             {@inheritDoc}
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             {@inheritDoc}
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     */
    @Override
    @Blocking
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        int n = 0;
        E e;
        while (n < maxElements && (e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * Always returns <tt>null</tt>. A <tt>SynchronousQueue</tt> does not return
     * elements unless actively waited on.
     * 
     * @return <tt>null</tt>
     */
    @Override
    public E element() {
        return null;
    }

    private void enqueue(E data) {
        Node node;
        if (waitList.isEmpty()) {
            addProducer(data);
            return;
        } else {
            node = waitList.getFirst();
            if (node.hasData()) {
                // iterate and find a slot for this producer
                for (Node waiter : waitList) {
                    if (!node.hasData()) {
                        waiter.data = data;
                        waiter.producer = controller.swapCaller(null);
                    }
                    return;
                }
                Node node1;
                node1 = new Node();
                node1.data = data;
                node1.producer = controller.swapCaller(null);
                waitList.add(node1);
                return;
            }
        }
        node.data = data;
        node.producer = controller.swapCaller(null);
        if (node.consumer != null) {
            // schedule receive callback with result
            node.consumer.setTime(controller.getCurrentTime());
            node.consumer.getContinuation().setReturnValue(node.data);
            controller.post(node.consumer);
            // return to sender
            controller.swapCaller(node.producer);
            waitList.removeFirst();
        }
    }

    /**
     * Always returns <tt>true</tt>. A <tt>SynchronousQueue</tt> has no internal
     * capacity.
     * 
     * @return <tt>true</tt>
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * Returns an empty iterator in which <tt>hasNext</tt> always returns
     * <tt>false</tt>.
     * 
     * @return an empty iterator
     */
    @Override
    public Iterator<E> iterator() {
        return new EmptyIterator<E>();
    }

    /**
     * Inserts the specified element into this queue, if another thread is
     * waiting to receive it.
     * 
     * @param e
     *            the element to add
     * @return <tt>true</tt> if the element was added to this queue, else
     *         <tt>false</tt>
     * @throws NullPointerException
     *             if the specified element is null
     */
    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (waitList.isEmpty()) {
            return false;
        }
        Node node = waitList.getFirst();
        if (node.hasData()) {
            return false;
        }
        if (node.consumer == null) {
            return false; // is this even possible if data is null?
        }
        // schedule receive callback with result
        node.consumer.setTime(controller.getCurrentTime());
        node.consumer.getContinuation().setReturnValue(node.data);
        controller.post(node.consumer);
        waitList.removeFirst();
        return true;
    }

    @Override
    @Blocking
    public boolean offer(E e, long timeout) {
        throw new UnsupportedOperationException("Currently unsupported");
    }

    /**
     * Always returns <tt>null</tt>. A <tt>SynchronousQueue</tt> does not return
     * elements unless actively waited on.
     * 
     * @return <tt>null</tt>
     */
    @Override
    public E peek() {
        return null;
    }

    /**
     * Retrieves and removes the head of this queue, if another thread is
     * currently making an element available.
     * 
     * @return the head of this queue, or <tt>null</tt> if no element is
     *         available.
     */
    @Override
    public E poll() {
        if (waitList.isEmpty()) {
            return null;
        }
        Node node = waitList.getFirst();
        if (node.consumer == null && node.hasData()) {
            // schedule send callback
            if (node.producer != null) {
                node.producer.setTime(controller.getCurrentTime());
                controller.post(node.producer);
            }
            waitList.removeFirst();
            return node.data;
        }
        return null;
    }

    @Override
    @Blocking
    public E poll(long timeout) {
        throw new UnsupportedOperationException("Currently unsupported");
    }

    /**
     * Adds the specified element to this queue, waiting if necessary for
     * another thread to receive it.
     * 
     * @throws InterruptedException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             {@inheritDoc}
     */
    @Override
    @Blocking
    public void put(E data) {
        enqueue(data);
    }

    /**
     * Always returns zero. A <tt>SynchronousQueue</tt> has no internal
     * capacity.
     * 
     * @return zero.
     */
    @Override
    public int remainingCapacity() {
        return 0;
    }

    /**
     * Retrieves and removes the head of this queue. This method differs from
     * {@link #poll poll} only in that it throws an exception if this queue is
     * empty.
     * 
     * <p>
     * This implementation returns the result of <tt>poll</tt> unless the queue
     * is empty.
     * 
     * @return the head of this queue
     * @throws NoSuchElementException
     *             if this queue is empty
     */
    @Override
    @Blocking
    public E remove() {
        E x = poll();
        if (x != null) {
            return x;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Always returns <tt>false</tt>. A <tt>SynchronousQueue</tt> has no
     * internal capacity.
     * 
     * @param o
     *            the element to remove
     * @return <tt>false</tt>
     */
    @Override
    public boolean remove(Object o) {
        return false;
    }

    /**
     * Always returns <tt>false</tt>. A <tt>SynchronousQueue</tt> has no
     * internal capacity.
     * 
     * @param c
     *            the collection
     * @return <tt>false</tt>
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns <tt>false</tt>. A <tt>SynchronousQueue</tt> has no
     * internal capacity.
     * 
     * @param c
     *            the collection
     * @return <tt>false</tt>
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns zero. A <tt>SynchronousQueue</tt> has no internal
     * capacity.
     * 
     * @return zero.
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary for
     * another thread to insert it.
     * 
     * @return the head of this queue
     */
    @Override
    @Blocking
    public E take() {
        Node node;
        if (waitList.isEmpty()) {
            addConsumer();
            return null;
        } else {
            node = waitList.getFirst();
            if (node.consumer != null) {
                // Iterate and find a slot for this consumer
                for (Node waiter : waitList) {
                    if (waiter.consumer == null) {
                        waiter.consumer = controller.swapCaller(null);
                        return null;
                    }
                }
                addConsumer();
                return null;
            }
        }
        node.consumer = controller.swapCaller(null);
        if (node.hasData()) {
            // schedule send callback 
            node.producer.setTime(controller.getCurrentTime());
            controller.post(node.producer);
            // return to receiver
            controller.swapCaller(node.consumer);
            waitList.removeFirst();
            return node.data;
        }
        return null; // won't return anywhere
    }

    /**
     * Returns a zero-length array.
     * 
     * @return a zero-length array
     */
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * Sets the zeroeth element of the specified array to <tt>null</tt> (if the
     * array has non-zero length) and returns it.
     * 
     * @param a
     *            the array
     * @return the specified array
     * @throws NullPointerException
     *             if the specified array is null
     */
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length > 0) {
            a[0] = null;
        }
        return a;
    }
}
