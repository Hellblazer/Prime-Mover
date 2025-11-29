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
package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Event;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.classfile.ClassFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AnnotationScanner
 */
class AnnotationScannerTest {

    @Test
    void testDetectsEntityAnnotation() throws IOException {
        var bytes = loadClassBytes(EntityClass.class);
        var classModel = ClassFile.of().parse(bytes);

        var scanner = new AnnotationScanner();
        scanner.scan(classModel);

        assertTrue(scanner.isTransform(), "Should detect @Entity annotation");
        assertFalse(scanner.isPreviouslyTransformed());
    }

    @Test
    void testDetectsMethodAnnotation() throws IOException {
        var bytes = loadClassBytes(MethodAnnotatedClass.class);
        var classModel = ClassFile.of().parse(bytes);

        var scanner = new AnnotationScanner();
        scanner.scan(classModel);

        assertTrue(scanner.isTransform(), "Should detect @Blocking/@Event method annotation");
        assertFalse(scanner.isPreviouslyTransformed());
    }

    @Test
    void testPlainClassNotTransformed() throws IOException {
        var bytes = loadClassBytes(PlainClass.class);
        var classModel = ClassFile.of().parse(bytes);

        var scanner = new AnnotationScanner();
        scanner.scan(classModel);

        assertFalse(scanner.isTransform(), "Plain class should not require transformation");
        assertFalse(scanner.isPreviouslyTransformed());
    }

    @Test
    void testIsTransformAnnotation() {
        assertTrue(AnnotationScanner.isTransformAnnotation("Lcom/hellblazer/primeMover/annotations/Entity;"));
        assertTrue(AnnotationScanner.isTransformAnnotation("Lcom/hellblazer/primeMover/annotations/Event;"));
        assertTrue(AnnotationScanner.isTransformAnnotation("Lcom/hellblazer/primeMover/annotations/Blocking;"));
        assertFalse(AnnotationScanner.isTransformAnnotation("Ljava/lang/Override;"));
        assertFalse(AnnotationScanner.isTransformAnnotation("Lcom/hellblazer/primeMover/annotations/Transformed;"));
    }

    private byte[] loadClassBytes(Class<?> clazz) throws IOException {
        var resourceName = "/" + clazz.getName().replace('.', '/') + ".class";
        try (var is = clazz.getResourceAsStream(resourceName)) {
            assertNotNull(is, "Could not load class bytes for: " + clazz.getName());
            return is.readAllBytes();
        }
    }

    // Test fixtures
    @Entity(PlainInterface.class)
    static class EntityClass implements PlainInterface {
        @Override
        public void doSomething() {
        }
    }

    interface PlainInterface {
        void doSomething();
    }

    static class MethodAnnotatedClass {
        @Blocking
        public void blockingMethod() {
        }

        @Event
        public void eventMethod() {
        }
    }

    static class PlainClass {
        public void regularMethod() {
        }
    }
}
