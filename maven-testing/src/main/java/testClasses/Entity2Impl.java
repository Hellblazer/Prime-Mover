/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
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

package testClasses;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

@Entity(Entity2.class)
public class Entity2Impl implements Entity2 {
    protected Object field1;
    protected Entity1 field2;

    /* (non-Javadoc)
     * @see testClasses.Entity2#myEvent()
     */
    @Override
    public void myEvent() {
        System.out.println("Entity2.myEvent");
        field1 = field2;
    }

    /* (non-Javadoc)
     * @see testClasses.Entity2#myEvent2(testClasses.Entity1)
     */
    @Override
    public void myEvent2(Entity1 e1) {
        e1.event2(test(getField2(e1)));
    }

    protected Entity2 foo() {
        return this;
    }

    protected Entity1 getField2(Object arg) {
        return field2;
    }

    protected Entity2 test(Entity1 test) {
        return this;
    }
}
