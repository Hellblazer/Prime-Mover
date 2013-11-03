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

package com.hellblazer.primeMover.runtime;

import java.io.Serializable;

import com.hellblazer.primeMover.Event;

/**
 * A simulated stack frame representing the return location for a blocking event
 * continuation
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
abstract public class ContinuationFrame implements Serializable {
    public static final ContinuationFrame BASE             = new ContinuationFrame() {
                                                               private static final long serialVersionUID = 1L;
                                                           };
    private static final long             serialVersionUID = 1L;
    public Event                          caller;
    public int                            location;

    public ContinuationFrame              next;

    public ContinuationFrame() {

    }
}
