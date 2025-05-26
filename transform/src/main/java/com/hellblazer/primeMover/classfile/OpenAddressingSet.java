/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.classfile;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public abstract class OpenAddressingSet<T> extends AbstractSet<T> {
    public static class OpenSet<T> extends OpenAddressingSet<T> {

        public OpenSet() {
            super();
        }

        public OpenSet(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        protected boolean equals(Object key, Object ob) {
            return key.equals(ob);
        }

        @Override
        protected int getHash(Object key) {
            return key.hashCode();
        }

    }

    private static final Object DELETED   = new Object();
    private static final int    PRIME     = -1640531527;
    private static final float  THRESHOLD = 0.75f;
    int                         load;
    int                         size      = 0;
    Object                      table[];

    public OpenAddressingSet() {
        this(4);
    }

    public OpenAddressingSet(int initialCapacity) {
        init(initialCapacity);
    }

    @Override
    public final boolean add(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Null key");
        }
        if (table == null) {
            init(1);
        } else if (size >= table.length * THRESHOLD) {
            rehash();
        }
        return insert(key);
    }

    @Override
    public void clear() {
        table = null;
        size = 0;
    }

    @Override
    public Object clone() {
        try {
            IdentitySet<?> t = (IdentitySet<?>) super.clone();
            if (table != null) {
                t.table = new Object[table.length];
                for (int i = table.length; i-- > 0;) {
                    t.table[i] = table[i];
                }
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean contains(Object key) {
        if (key == null || size == 0) {
            return false;
        }
        int hash = PRIME * getHash(key) >>> load;
        int index = hash;
        do {
            Object ob = table[index];
            if (ob == null) {
                return false;
            }
            if (equals(key, ob)) {
                return true;
            }
            index = index + (hash | 1) & table.length - 1;
        } while (index != hash);
        return false;
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int next = 0;

            @Override
            public boolean hasNext() {
                while (next < table.length) {
                    if (table[next] != null && table[next] != DELETED) {
                        return true;
                    }
                    next++;
                }
                return false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                while (next < table.length) {
                    if (table[next] != null && table[next] != DELETED) {
                        return (T) table[next++];
                    }
                    next++;
                }
                throw new NoSuchElementException("Enumerator");
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported");
            }
        };
    }

    @Override
    public final boolean remove(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Null key");
        }
        if (!isEmpty()) {
            int hash = PRIME * getHash(key) >>> load;
            int index = hash;
            do {
                Object ob = table[index];
                if (ob == null) {
                    return false;
                }
                if (equals(key, ob)) {
                    table[index] = DELETED;
                    size -= 1;
                    return true;
                }
                index = index + (hash | 1) & table.length - 1;
            } while (index != hash);
        }
        return false;
    }

    @Override
    public final int size() {
        return size;
    }

    abstract protected boolean equals(Object key, Object ob);

    abstract protected int getHash(Object key);

    protected void init(int initialCapacity) {
        if (initialCapacity < 4) {
            initialCapacity = 4;
        }
        int cap = 4;
        load = 2;
        while (cap < initialCapacity) {
            load += 1;
            cap += cap;
        }
        table = new Object[cap];
        load = 32 - load;
    }

    private boolean insert(Object key) {
        int hash = PRIME * getHash(key) >>> load;
        int index = hash;
        do {
            Object ob = table[index];
            if (ob == null || ob == DELETED) {
                table[index] = key;
                size += 1;
                return true;
            }
            if (equals(key, ob)) {
                table[index] = key;
                return false;
            }
            index = index + (hash | 1) & table.length - 1;
        } while (index != hash);
        rehash();
        return insert(key);
    }

    private void rehash() {
        Object[] oldMap = table;
        int oldCapacity = oldMap.length;
        load -= 1;
        table = new Object[oldCapacity * 2];
        size = 0;
        for (int i = oldCapacity - 1; i >= 0; i -= 1) {
            Object ob = oldMap[i];
            if (ob != null && ob != DELETED) {
                insert(ob);
            }
        }
    }

}
