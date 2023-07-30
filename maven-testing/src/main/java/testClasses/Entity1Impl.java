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

package testClasses;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

@Entity(Entity1.class)
public class Entity1Impl implements Entity1 {
    protected Object field1;
    protected Entity2 field2;

    /* (non-Javadoc)
     * @see testClasses.Entity1#event1()
     */
    @Override
    public void event1() {
        System.out.println("Entity1.event 1");
        event2(new Entity2Impl());
        // note that if event2() is not blocking, field 2 would be null
        field2.myEvent();
        Kronos.blockingSleep(1000);
    }

    /* (non-Javadoc)
     * @see testClasses.Entity1#event2(testClasses.Entity2)
     */
    @Override
    @Blocking
    public void event2(Entity2 e2) {
        System.out.println("Entity1.event 2");
        field2 = e2;
    }
}
