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
import com.hellblazer.primeMover.annotations.Transformed;

/**
 * Test fixture: A class that's already been transformed and should be skipped.
 * This simulates a class that was previously transformed and is now being
 * processed again (e.g., in a subsequent build or transformation pass).
 *
 * @author hal.hildebrand
 */
@Entity(Foo.class)
@Transformed(
    value = "com.hellblazer.primeMover.classfile.SimulationTransform",
    date = "2026-01-19T10:00:00.000Z",
    comment = "Pre-transformed test fixture"
)
public class AlreadyTransformed implements Foo {

    @Override
    public void bar() {
        System.out.println("Already transformed bar");
    }

    @Override
    @Blocking
    public String myMy() {
        return "already transformed";
    }

    @Override
    public String[] someArgs(String arg1, Object arg2) throws RuntimeException {
        return new String[] { "pre", "transformed" };
    }

    /**
     * Additional method to verify class is usable
     */
    public String getStatus() {
        return "Pre-transformed";
    }
}
