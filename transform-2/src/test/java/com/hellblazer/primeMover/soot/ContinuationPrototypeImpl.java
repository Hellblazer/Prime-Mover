/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
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

package com.hellblazer.primeMover.soot;

import com.hellblazer.primeMover.Continuable;

/**
 * A class to test the continuation transforms.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class ContinuationPrototypeImpl implements ContinuationPrototype {

    @Override
    @Continuable
    public String arguments(String a, String b, String c) {
        String first = "first";
        String second = second();
        second.length();
        return first + a + b + c;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.soot.ContinuationPrototypeInterface#first()
     */
    @Override
    @Continuable
    public String first() {
        return "A" + second();
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.soot.ContinuationPrototypeInterface#nested()
     */
    @Override
    @Continuable
    public String nested() {
        return third(third(first(), second()), second());
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.soot.ContinuationPrototypeInterface#second()
     */
    @Override
    @Continuable
    public String second() {
        return "B";
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.soot.ContinuationPrototypeInterface#third(java.lang.String, java.lang.String)
     */
    @Override
    @Continuable
    public String third(String a, String b) {
        return a + b;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.soot.ContinuationPrototypeInterface#zeroth()
     */
    @Override
    @Continuable
    public int zeroth() {
        return first().length();
    }
}
