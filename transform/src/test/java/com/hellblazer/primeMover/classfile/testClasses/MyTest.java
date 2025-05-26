/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
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
package com.hellblazer.primeMover.classfile.testClasses;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;

/**
 * @author hal.hildebrand
 */
@Entity(Foo.class)
public class MyTest implements Foo {

    @Override
    public void bar() {
        System.out.println("Hello");
    }

    @Override
    @Blocking
    public String myMy() {
        return "bar";
    }

    @Override
    public String[] someArgs(String arg1, Object arg2) throws RuntimeException {
        return new String[] { "hello", "world" };
    }
}
