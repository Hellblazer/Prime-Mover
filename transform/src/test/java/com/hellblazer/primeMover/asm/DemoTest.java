/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.hellblazer.primeMover.asm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;

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
            return transform.transformed();
        }
    }
}
