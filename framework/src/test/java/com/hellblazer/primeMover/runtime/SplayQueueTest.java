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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.Random;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class SplayQueueTest extends TestCase {
    SplayQueue<Integer> pqueue;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pqueue = new SplayQueue<Integer>();
        pqueue.add(3);
        pqueue.add(1);
        pqueue.add(2);
    }

    public void testAdd() {
        pqueue.add(5);
        assertTrue(pqueue.size() == 4);
    }

    public void testDuplicates() {
        Random random = new Random(666);
        Queue<Integer> t = new SplayQueue<Integer>();
        final int targetSize = 40000;
        final int gap = 307;
        ArrayList<Integer> inserted = new ArrayList<Integer>();

        for (int i = gap; i != 0; i = (i + gap) % targetSize) {
            int dupes = random.nextInt(6) + 1;
            while (dupes != 0) {
                dupes--;
                t.add(i);
                inserted.add(i);
            }
        }

        Collections.sort(inserted);
        for (int i : inserted) {
            assertEquals(i, t.remove().intValue());
        }
        assertEquals(0, t.size());
    }

    public void testIsEmpty() {
        int n = pqueue.size();
        for (int i = 0; i < n; i++) {
            pqueue.remove();
        }
        pqueue.size();
        assertTrue(pqueue.isEmpty());
    }

    public void testIterator() {
        Queue<Integer> t = new SplayQueue<Integer>();
        final int targetSize = 40000;
        final int gap = 307;
        ArrayList<Integer> inserted = new ArrayList<Integer>();

        for (int i = gap; i != 0; i = (i + gap) % targetSize) {
            t.add(i);
            inserted.add(i);
        }
        Collections.sort(inserted);
        int i = 0;
        for (int e : t) {
            assertEquals(inserted.get(i++).intValue(), e);
        }
    }

    public void testLargeInsert() {

        Queue<Integer> t = new SplayQueue<Integer>();
        final int nums = 40000;
        final int gap = 307;
        ArrayList<Integer> inserted = new ArrayList<Integer>();

        for (int i = gap; i != 0; i = (i + gap) % nums) {
            assertTrue(t.add(i));
            inserted.add(i);
        }

        for (int i : inserted) {
            assertTrue(t.contains(i));
        }

        assertEquals(nums - 1, t.size());
    }

    public void testLargeRemove() {
        Queue<Integer> t = new SplayQueue<Integer>();
        final int targetSize = 40000;
        final int gap = 307;
        ArrayList<Integer> inserted = new ArrayList<Integer>();

        for (int i = gap; i != 0; i = (i + gap) % targetSize) {
            t.add(i);
            inserted.add(i);
        }

        Collections.sort(inserted);
        for (int i : inserted) {
            assertEquals(i, t.remove().intValue());
        }
        assertEquals(0, t.size());
    }

    public void testLargeRemoveObject() {
        Queue<Integer> t = new SplayQueue<Integer>();
        final int targetSize = 40000;
        final int gap = 307;
        ArrayList<Integer> inserted = new ArrayList<Integer>();

        for (int i = gap; i != 0; i = (i + gap) % targetSize) {
            t.add(i);
            inserted.add(i);
        }

        for (int i : inserted) {
            assertTrue(t.remove(i));
        }
        assertEquals(0, t.size());
    }

    public void testRemove() {
        assertTrue(pqueue.remove() == 1);
        assertTrue(pqueue.remove() == 2);
        assertTrue(pqueue.remove() == 3);
    }
}
