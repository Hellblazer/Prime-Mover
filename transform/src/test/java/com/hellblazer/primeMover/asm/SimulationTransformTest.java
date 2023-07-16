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
package com.hellblazer.primeMover.asm;

import org.junit.jupiter.api.Test;

import io.github.classgraph.ClassGraph;

/**
 * @author hal.hildebrand
 */
public class SimulationTransformTest {

    @Test
    public void smokin() throws Exception {
        var transform = new SimulationTransform(new ClassGraph().verbose()
                                                                .acceptPackages("testClasses", "com.hellblazer"));
        transform.findAllEntities().forEach(ci -> System.out.println(ci.getSimpleName()));
    }
}
