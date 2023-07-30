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

package demo;

import com.hellblazer.primeMover.annotations.Blocking;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public interface ContinuationThroughput {

    /**
     * Perform benchmark.
     */
    void go();

    /**
     * Blocking operation with array parameter.
     * 
     * @param b dummy array parameter
     * @return dummy return
     */
    @Blocking
    byte[] operation_array(byte[] b);

    /**
     * Blocking operation with primitive double parameter.
     * 
     * @param d dummy double parameter
     */
    @Blocking
    void operation_double(double d);

    /**
     * Blocking operation with primitive integer parameter.
     * 
     * @param i dummy int parameter
     */
    @Blocking
    void operation_int(int i);

    /**
     * Blocking operation with no parameters.
     */
    @Blocking
    void operation_null();

    /**
     * Blocking operation that displays time.
     */
    @Blocking
    void operation_show();

    /**
     * Blocking operation with String parameter.
     * 
     * @param s dummy string parameter
     */
    @Blocking
    void operation_string(String s);

}
