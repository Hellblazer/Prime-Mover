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

package com.hellblazer.primeMover.soot;

import java.util.UUID;

import testClasses.Entity2;
import testClasses.Entity2Impl;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Transformed;

/**
 * A prototypical simulation Entity to exercise the transformation framework.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

@Transformed(value = "foo", comment = "bar", date = "today")
@Entity(CompositeInterface.class)
public class PrototypicalEntityImpl implements CompositeInterface {
    protected UUID entityId = UUID.randomUUID();
    protected Object field1;
    protected Entity2 field2;

    public boolean invoke1 = false;
    public boolean invoke2 = false;
    public boolean invoke3 = false;

    public PrototypicalEntityImpl() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PrototypicalEntityImpl other = (PrototypicalEntityImpl) obj;
        if (entityId == null) {
            if (other.entityId != null) {
                return false;
            }
        } else if (!entityId.equals(other.entityId)) {
            return false;
        }
        return true;
    }

    @Override
    public void event1() {
        invoke1 = true;
        event2(new Entity2Impl());
    }

    @Override
    @Blocking
    public void event2(Entity2 e2) {
        invoke2 = true;
        field2 = e2;
    }

    public boolean event3() {
        invoke3 = true;
        return true;
    }

    @Override
    public int hashCode() {
        return entityId.hashCode();
    }

    @Override
    public String toString() {
        return "PrototypicalEntityImpl [" + entityId + "]";
    }
}
