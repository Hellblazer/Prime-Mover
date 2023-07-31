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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.classgraph.ClassGraph;
import testClasses.LocalLoader;

public class DemoTest {

    @Test
    public void runDemo() throws Exception {
        final var transformed = getTransformed();
        assertTrue(transformed.containsKey("testClasses/Demo"));
        var loader = new LocalLoader(transformed);
        Class<?> demoClazz = loader.loadClass("testClasses.Demo");
        Method event = demoClazz.getMethod("runAll");
        event.invoke(null);
    }

    private Map<String, byte[]> getTransformed() throws Exception {
        try (var transform = new SimulationTransform(new ClassGraph().acceptPackages("testClasses",
                                                                                     "com.hellblazer.*"))) {
            return transform.transformed()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getKey().getName().replace('.', '/'), e -> e.getValue()));
        }
    }
}
