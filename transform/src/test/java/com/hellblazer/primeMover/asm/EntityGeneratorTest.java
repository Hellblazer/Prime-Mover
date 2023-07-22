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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import io.github.classgraph.ClassGraph;

/**
 * @author hal.hildebrand
 */
public class EntityGeneratorTest {

    @Test
    public void smokin() throws Exception {
        var transform = new SimulationTransform(new ClassGraph().verbose()
                                                                .acceptPackages("com.hellblazer.primeMover.asm.testClasses"));

        final var name = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        EntityGenerator generator = transform.generatorOf(name);
        assertNotNull(generator);
        var cw = generator.generate();
        assertNotNull(cw);
        final var bytes = cw.toByteArray();
        assertNotNull(bytes);
        TraceClassVisitor visitor = new TraceClassVisitor(null, new PrintWriter(System.out, true));
        CheckClassAdapter checkAdapter = new CheckClassAdapter(null);
        ClassReader reader = new ClassReader(bytes);
        reader.accept(visitor, 0);
        reader.accept(checkAdapter, 0);
    }
}
