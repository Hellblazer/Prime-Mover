/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.hellblazer.primeMover.classfile;

import com.hellblazer.primeMover.runtime.SimulationEnd;
import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.Test;
import testClasses.LocalLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DemoTest {

    @Test
    public void runDemo() throws Throwable {
        final var transformed = getTransformed();
        assertTrue(transformed.containsKey("testClasses/Demo"));
        var loader = new LocalLoader(transformed);
        Class<?> demoClazz = loader.loadClass("testClasses.Demo");
        Method event = demoClazz.getMethod("runAll");
        try {
            event.invoke(null);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof SimulationEnd se) {
                // expected
            } else {
                throw e.getCause();
            }
        }
    }

    private Map<String, byte[]> getTransformed() throws Exception {
        try (var transform = new SimulationTransform(
        new ClassGraph().acceptPackages("testClasses", "com.hellblazer.*"))) {
            return transform.transformed().entrySet().stream().collect(
            Collectors.toMap(e -> e.getKey().getName().replace('.', '/'), e -> e.getValue()));
        }
    }
}
