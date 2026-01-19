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

import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Event;

/**
 * Test fixture: An entity that is NOT marked as @Transformed.
 * Used to verify that in a mixed scenario, untransformed entities
 * are still processed while transformed ones are skipped.
 *
 * @author hal.hildebrand
 */
@Entity
public class PartiallyTransformedEntity {

    @Event
    public void doSomething() {
        System.out.println("Not yet transformed");
    }

    @Event
    public String compute(int value) {
        return "Result: " + value;
    }
}
