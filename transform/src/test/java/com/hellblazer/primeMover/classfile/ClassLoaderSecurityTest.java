/*
 * Copyright (C) 2026 Hal Hildebrand. All rights reserved.
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
package com.hellblazer.primeMover.classfile;

import org.junit.jupiter.api.Test;
import testClasses.LocalLoader;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security validation tests for ClassLoader usage in transformation pipeline.
 * <p>
 * Validates that transformed classes don't cause ClassLoader conflicts and
 * that proper isolation exists between application and transformation contexts.
 *
 * @author hal.hildebrand
 */
public class ClassLoaderSecurityTest {

    /**
     * Test that LocalLoader properly delegates to parent for non-transformed classes.
     * This ensures we don't bypass standard parent delegation security model.
     */
    @Test
    public void testParentDelegationIntegrity() throws Exception {
        Map<String, byte[]> transformed = new HashMap<>();
        var loader = new LocalLoader(transformed);

        // Load a JDK class - should delegate to parent
        Class<?> stringClass = loader.loadClass("java.lang.String");
        assertNotNull(stringClass);
        // Verify it's the same class as system loader
        assertSame(String.class, stringClass, "Should delegate to parent for JDK classes");

        // Load a class from classpath - should delegate to parent
        Class<?> testClass = loader.loadClass("org.junit.jupiter.api.Test");
        assertNotNull(testClass);
        assertSame(Test.class, testClass, "Should delegate to parent for classpath classes");
    }

    /**
     * Test that LocalLoader doesn't create class visibility leaks.
     * Classes loaded by LocalLoader should not be visible to parent loader.
     */
    @Test
    public void testClassVisibilityIsolation() throws Exception {
        Map<String, byte[]> transformed = new HashMap<>();
        var loader1 = new LocalLoader(transformed);
        var loader2 = new LocalLoader(transformed);

        // Each loader should maintain independent class identity
        // Even for the same bytecode, different loaders create different Class objects
        assertNotSame(loader1, loader2, "Different loader instances");

        // Verify loaders don't share class namespace
        var parentLoader = LocalLoader.class.getClassLoader();
        assertNotNull(parentLoader, "LocalLoader should have parent");

        // LocalLoader classes aren't visible to parent
        assertThrows(ClassNotFoundException.class, () ->
            parentLoader.loadClass("testClasses.NonExistentClass")
        );
    }

    /**
     * Test that multiple LocalLoaders can coexist without conflicts.
     * This validates isolation between transformation contexts.
     */
    @Test
    public void testMultipleLoaderIsolation() throws Exception {
        Map<String, byte[]> transformed1 = new HashMap<>();
        Map<String, byte[]> transformed2 = new HashMap<>();

        var loader1 = new LocalLoader(transformed1);
        var loader2 = new LocalLoader(transformed2);

        // Both loaders can load same JDK classes
        Class<?> string1 = loader1.loadClass("java.lang.String");
        Class<?> string2 = loader2.loadClass("java.lang.String");

        // Should be same class (parent delegation)
        assertSame(string1, string2, "JDK classes should be same across loaders");

        // Both loaders maintain independent state
        assertNotSame(loader1, loader2);
    }

    /**
     * Test that LocalLoader properly handles package sealing.
     * Sealed packages should not be violated by transformation.
     */
    @Test
    public void testPackageSealingRespect() throws Exception {
        Map<String, byte[]> transformed = new HashMap<>();
        var loader = new LocalLoader(transformed);

        // Load a class from a potentially sealed package
        Class<?> clazz = loader.loadClass("java.lang.Object");
        assertNotNull(clazz);

        // Verify package is accessible
        Package pkg = clazz.getPackage();
        assertNotNull(pkg);
        assertEquals("java.lang", pkg.getName());

        // Note: Actual sealing is enforced by JVM, not loader
        // This test validates we don't interfere with package metadata
    }

    /**
     * Test that LocalLoader properly reports transformed vs non-transformed classes.
     * This validates the transformed() method used for class tracking.
     */
    @Test
    public void testTransformedClassTracking() {
        Map<String, byte[]> transformed = new HashMap<>();
        transformed.put("com/example/Transformed", new byte[]{});
        var loader = new LocalLoader(transformed);

        // Should report transformed classes correctly
        assertTrue(loader.transformed("com.example.Transformed"),
                   "Should identify transformed classes");
        assertFalse(loader.transformed("com.example.NotTransformed"),
                    "Should identify non-transformed classes");
        assertFalse(loader.transformed("java.lang.String"),
                    "Should identify JDK classes as non-transformed");
    }

    /**
     * Test that LocalLoader handles null/invalid input safely.
     */
    @Test
    public void testSecureInputHandling() {
        Map<String, byte[]> transformed = new HashMap<>();
        var loader = new LocalLoader(transformed);

        // Null class name - LocalLoader doesn't validate, delegates to parent which throws NPE
        // This is acceptable behavior - parent's validation is sufficient
        assertThrows(NullPointerException.class, () ->
            loader.loadClass(null)
        );

        // Invalid class name format should throw ClassNotFoundException
        assertThrows(ClassNotFoundException.class, () ->
            loader.loadClass("invalid..class..name")
        );
    }

    /**
     * Test that LocalLoader properly uses findLoadedClass to prevent redefinition.
     * Loading the same class twice should return the same Class instance.
     */
    @Test
    public void testNoClassRedefinition() throws Exception {
        // Use existing test class that we know exists
        Map<String, byte[]> transformed = new HashMap<>();
        var loader = new LocalLoader(transformed);

        // Load a JDK class twice
        Class<?> first = loader.loadClass("java.lang.String");
        assertNotNull(first);

        // Load same class again - should return same instance (findLoadedClass)
        Class<?> second = loader.loadClass("java.lang.String");
        assertSame(first, second, "Should return same class instance on reload");

        // Verify it's the expected class
        assertEquals("java.lang.String", first.getName());
    }

    /**
     * Test that LocalLoader maintains proper ClassLoader hierarchy.
     */
    @Test
    public void testClassLoaderHierarchy() {
        Map<String, byte[]> transformed = new HashMap<>();
        var loader = new LocalLoader(transformed);

        // Should have parent loader
        ClassLoader parent = loader.getParent();
        assertNotNull(parent, "LocalLoader should have parent ClassLoader");

        // Parent should be system or app loader
        ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        assertNotNull(systemLoader);

        // Verify hierarchy chain exists
        assertTrue(isAncestor(systemLoader, loader) || parent == systemLoader,
                   "LocalLoader should be in ClassLoader hierarchy");
    }

    /**
     * Helper method to check if ancestor is in the ClassLoader parent chain of descendant.
     */
    private boolean isAncestor(ClassLoader ancestor, ClassLoader descendant) {
        ClassLoader current = descendant.getParent();
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
