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

package demo;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.Entity;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

@Entity()
public class Threaded {
    /*
     * (non-Javadoc)
     *
     * @see testClasses.Threaded#process(int)
     */
    public void process(int id) {
        for (int i = 1; i <= 5; i++) {
            System.out.println(Kronos.currentTime() + ": thread=" + id + ", i=" + i);
            Kronos.blockingSleep(1);
        }
    }
}
