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

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public interface EventThroughput {

    /**
     * Event with primitive double parameter.
     * 
     * @param d
     *            dummy double
     */
    void doubleOperation(double d);

    /**
     * End benchmark.
     */
    void finish();

    /**
     * Event with primitive integer parameter.
     * 
     * @param i
     *            dummy int
     */
    void intOperation(int i);

    /**
     * Event with no parameters.
     */
    void nullOperation();

    /**
     * Begin bechmark.
     */
    void start();

    /**
     * Event with String parameter.
     * 
     * @param s
     *            dummy string
     */
    void stringOperation(String s);

}
