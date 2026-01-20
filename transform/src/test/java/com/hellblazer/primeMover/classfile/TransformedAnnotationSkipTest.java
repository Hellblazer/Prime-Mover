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
package com.hellblazer.primeMover.classfile;

import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.classfile.testClasses.AlreadyTransformed;
import com.hellblazer.primeMover.classfile.testClasses.MyTest;
import com.hellblazer.primeMover.classfile.testClasses.PartiallyTransformedEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.classfile.ClassFile;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for @Transformed annotation skip logic.
 * Tests that already-transformed classes are correctly skipped during
 * transformation passes, preventing duplicate transformations.
 * <p>
 * Phase 5.3: Testing & Verification
 *
 * @author hal.hildebrand
 */
@DisplayName("@Transformed Annotation Skip Logic Tests")
public class TransformedAnnotationSkipTest {

    private SimulationTransform transform;
    private ClassScanner scanner;
    private TestLogHandler logHandler;

    @BeforeEach
    public void setUp() throws Exception {
        scanner = new ClassScanner()
            .addClasspathEntry(Path.of("target/test-classes"))
            .addClasspathEntry(Path.of("target/classes"))
            .scan();
        transform = new SimulationTransform(scanner);

        // Set up log handler to capture transformation events
        logHandler = new TestLogHandler();
        Logger logger = Logger.getLogger(SimulationTransform.class.getName());
        logger.addHandler(logHandler);
        logger.setLevel(Level.ALL);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (transform != null) {
            transform.close();
        }
        if (logHandler != null) {
            Logger logger = Logger.getLogger(SimulationTransform.class.getName());
            logger.removeHandler(logHandler);
        }
    }

    @Test
    @DisplayName("1. Already Transformed - Class with @Transformed should not be re-transformed")
    public void testAlreadyTransformedClassIsSkipped() {
        // Given: A class that's already marked with @Transformed
        var alreadyTransformedClass = scanner.getClass(AlreadyTransformed.class.getCanonicalName());
        assertNotNull(alreadyTransformedClass, "AlreadyTransformed test fixture should be found");
        assertTrue(alreadyTransformedClass.hasAnnotation(Transformed.class),
                   "AlreadyTransformed fixture should have @Transformed annotation");

        // When: Requesting all generators (which internally uses EXCLUDE_TRANSFORMED_FILTER)
        var generators = transform.generators();

        // Then: AlreadyTransformed should NOT have a generator created
        var transformedEntityNames = generators.keySet().stream()
                                               .map(ClassMetadata::getName)
                                               .collect(Collectors.toSet());

        assertFalse(transformedEntityNames.contains(AlreadyTransformed.class.getCanonicalName()),
                    "AlreadyTransformed should be excluded from generator creation");
    }

    @Test
    @DisplayName("2. Skip Verification - EXCLUDE_TRANSFORMED_FILTER correctly identifies transformed classes")
    public void testExcludeTransformedFilterLogic() {
        // Given: Scanner with both transformed and untransformed entities
        var alreadyTransformedClass = scanner.getClass(AlreadyTransformed.class.getCanonicalName());
        var untransformedClass = scanner.getClass(MyTest.class.getCanonicalName());

        // When: Applying EXCLUDE_TRANSFORMED_FILTER
        boolean alreadyTransformedFiltered = SimulationTransform.EXCLUDE_TRANSFORMED_FILTER.test(alreadyTransformedClass);
        boolean untransformedFiltered = SimulationTransform.EXCLUDE_TRANSFORMED_FILTER.test(untransformedClass);

        // Then: Filter should exclude transformed, accept untransformed
        assertFalse(alreadyTransformedFiltered,
                    "EXCLUDE_TRANSFORMED_FILTER should reject class with @Transformed");
        assertTrue(untransformedFiltered,
                   "EXCLUDE_TRANSFORMED_FILTER should accept class without @Transformed");
    }

    @Test
    @DisplayName("3. Normal Usage - @Transformed classes can be scanned and metadata read")
    public void testTransformedClassMetadataAccess() {
        // Given: A class marked as @Transformed
        var alreadyTransformedClass = scanner.getClass(AlreadyTransformed.class.getCanonicalName());
        assertNotNull(alreadyTransformedClass, "Should be able to get metadata for @Transformed class");

        // When: Accessing class metadata
        var transformedAnnotation = alreadyTransformedClass.getAnnotation(Transformed.class);

        // Then: All metadata should be accessible
        assertNotNull(transformedAnnotation, "@Transformed annotation should be readable");
        assertEquals("com.hellblazer.primeMover.classfile.SimulationTransform",
                     transformedAnnotation.getValue("value"),
                     "Transformer value should match fixture");
        assertEquals("2026-01-19T10:00:00.000Z",
                     transformedAnnotation.getValue("date"),
                     "Transform date should match fixture");
        assertEquals("Pre-transformed test fixture",
                     transformedAnnotation.getValue("comment"),
                     "Transform comment should match fixture");

        // Verify class is still a valid entity
        assertTrue(alreadyTransformedClass.hasAnnotation(com.hellblazer.primeMover.annotations.Entity.class),
                   "Class should still be marked as @Entity");

        // Verify methods are accessible
        var methods = alreadyTransformedClass.getDeclaredMethods();
        assertFalse(methods.isEmpty(), "Should be able to read methods from @Transformed class");
    }

    @Test
    @DisplayName("4. Mixed Scenario - Project with both transformed and untransformed classes")
    public void testMixedTransformedAndUntransformedEntities() {
        // Given: A project with both @Transformed and non-@Transformed entities
        var alreadyTransformedClass = scanner.getClass(AlreadyTransformed.class.getCanonicalName());
        var untransformedClass = scanner.getClass(MyTest.class.getCanonicalName());
        var partiallyTransformedClass = scanner.getClass(PartiallyTransformedEntity.class.getCanonicalName());

        // When: Generating transformations for all entities
        var generators = transform.generators();
        var transformedClasses = transform.transformed();

        // Then: Only untransformed entities should be processed
        var generatorNames = generators.keySet().stream()
                                      .map(ClassMetadata::getName)
                                      .collect(Collectors.toSet());

        assertFalse(generatorNames.contains(AlreadyTransformed.class.getCanonicalName()),
                    "AlreadyTransformed should be skipped");
        assertTrue(generatorNames.contains(MyTest.class.getCanonicalName()),
                   "MyTest (untransformed) should be processed");
        assertTrue(generatorNames.contains(PartiallyTransformedEntity.class.getCanonicalName()),
                   "PartiallyTransformedEntity (untransformed) should be processed");

        // Verify transformed bytecode only includes untransformed entities
        var transformedNames = transformedClasses.keySet().stream()
                                                 .map(ClassMetadata::getName)
                                                 .collect(Collectors.toSet());

        assertFalse(transformedNames.contains(AlreadyTransformed.class.getCanonicalName()),
                    "AlreadyTransformed should not have transformed bytecode");
    }

    @Test
    @DisplayName("5. Explicit Filter - ACCEPT_ALL_FILTER includes transformed classes")
    public void testAcceptAllFilterIncludesTransformed() {
        // Given: Using ACCEPT_ALL_FILTER instead of EXCLUDE_TRANSFORMED_FILTER
        var alreadyTransformedClass = scanner.getClass(AlreadyTransformed.class.getCanonicalName());

        // When: Applying ACCEPT_ALL_FILTER
        boolean result = SimulationTransform.ACCEPT_ALL_FILTER.test(alreadyTransformedClass);

        // Then: Filter should accept the class
        assertTrue(result, "ACCEPT_ALL_FILTER should accept @Transformed classes");
    }

    @Test
    @DisplayName("6. Generator Creation - generatorOf with EXCLUDE_TRANSFORMED_FILTER skips transformed")
    public void testGeneratorOfWithExcludeFilter() {
        // Given: Attempting to create generator for already transformed class with filter
        var className = AlreadyTransformed.class.getCanonicalName();

        // When/Then: Should throw exception because class is filtered out
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            transform.generatorOf(className, SimulationTransform.EXCLUDE_TRANSFORMED_FILTER);
        });

        assertTrue(exception.getMessage().contains("Entity class not found") ||
                   exception.getMessage().contains("Available entities"),
                   "Exception should indicate entity not found due to filtering");
    }

    @Test
    @DisplayName("7. Generator Creation - generatorOf with ACCEPT_ALL_FILTER processes transformed")
    public void testGeneratorOfWithAcceptAllFilter() {
        // Given: Attempting to create generator for already transformed class with ACCEPT_ALL
        var className = AlreadyTransformed.class.getCanonicalName();

        // When: Creating generator with ACCEPT_ALL_FILTER
        // Then: Should succeed and create generator (even though it would be wasteful)
        assertDoesNotThrow(() -> {
            var generator = transform.generatorOf(className, SimulationTransform.ACCEPT_ALL_FILTER);
            assertNotNull(generator, "Should be able to create generator with ACCEPT_ALL_FILTER");
        });
    }

    @Test
    @DisplayName("8. Bytecode Validity - Transformed classes have valid bytecode structure")
    public void testTransformedClassBytecodeValidity() {
        // Given: A class marked as @Transformed
        var alreadyTransformedClass = scanner.getClass(AlreadyTransformed.class.getCanonicalName());
        var originalBytes = alreadyTransformedClass.getOriginalBytes();

        assertNotNull(originalBytes, "Should have bytecode for @Transformed class");

        // When: Parsing bytecode using ClassFile API
        var classModel = ClassFile.of().parse(originalBytes);

        // Then: Bytecode should be structurally valid
        assertNotNull(classModel, "Should parse bytecode successfully");
        assertEquals(AlreadyTransformed.class.getCanonicalName().replace('.', '/'),
                     classModel.thisClass().asInternalName(),
                     "Class name should match");

        // Verify @Transformed annotation is present in bytecode
        boolean hasTransformedAnnotation = classModel.attributes().stream()
            .anyMatch(attr -> attr.toString().contains("Transformed"));

        assertTrue(hasTransformedAnnotation || alreadyTransformedClass.hasAnnotation(Transformed.class),
                   "@Transformed annotation should be present");
    }

    @Test
    @DisplayName("9. Default Filter Behavior - generators() uses EXCLUDE_TRANSFORMED_FILTER by default")
    public void testDefaultGeneratorFilterExcludesTransformed() {
        // When: Using generators() without explicit filter
        var generators = transform.generators();

        // Then: Should automatically exclude @Transformed classes
        var generatorNames = generators.keySet().stream()
                                      .map(ClassMetadata::getName)
                                      .collect(Collectors.toSet());

        assertFalse(generatorNames.contains(AlreadyTransformed.class.getCanonicalName()),
                    "Default generators() should exclude @Transformed classes");

        // Verify at least some untransformed entities were found
        assertFalse(generators.isEmpty(), "Should find at least some untransformed entities");
    }

    @Test
    @DisplayName("10. Transform Count - Verify correct number of transformations with mixed entities")
    public void testTransformCountWithMixedEntities() {
        // Given: Known entity classes in test fixtures
        var allEntityClasses = scanner.stream()
            .filter(cm -> cm.hasAnnotation(com.hellblazer.primeMover.annotations.Entity.class))
            .collect(Collectors.toList());

        var transformedEntityClasses = allEntityClasses.stream()
            .filter(cm -> cm.hasAnnotation(Transformed.class))
            .count();

        var untransformedEntityClasses = allEntityClasses.stream()
            .filter(cm -> !cm.hasAnnotation(Transformed.class))
            .count();

        // When: Generating transformations
        var generators = transform.generators();

        // Then: Generator count should match untransformed count
        assertEquals(untransformedEntityClasses, generators.size(),
                     "Should create generators only for untransformed entities");

        assertTrue(transformedEntityClasses > 0,
                   "Test should have at least one @Transformed entity fixture");
        assertTrue(untransformedEntityClasses > 0,
                   "Test should have at least one untransformed entity fixture");
    }

    @Test
    @DisplayName("11. Coverage - All skip paths are exercised")
    public void testSkipPathCoverage() {
        // This test verifies that the EXCLUDE_TRANSFORMED_FILTER is used in all key paths

        // Path 1: generators()
        var generators1 = transform.generators();
        assertNotNull(generators1, "generators() should work");

        // Path 2: generators(Predicate)
        var generators2 = transform.generators(SimulationTransform.EXCLUDE_TRANSFORMED_FILTER);
        assertNotNull(generators2, "generators(filter) should work");

        // Path 3: transformed()
        var transformed1 = transform.transformed();
        assertNotNull(transformed1, "transformed() should work");

        // Path 4: transformed(Predicate)
        var transformed2 = transform.transformed(SimulationTransform.EXCLUDE_TRANSFORMED_FILTER);
        assertNotNull(transformed2, "transformed(filter) should work");

        // Verify all paths exclude AlreadyTransformed
        var className = AlreadyTransformed.class.getCanonicalName();

        assertFalse(generators1.keySet().stream().anyMatch(cm -> cm.getName().equals(className)),
                    "generators() should exclude AlreadyTransformed");
        assertFalse(generators2.keySet().stream().anyMatch(cm -> cm.getName().equals(className)),
                    "generators(filter) should exclude AlreadyTransformed");
        assertFalse(transformed1.keySet().stream().anyMatch(cm -> cm.getName().equals(className)),
                    "transformed() should exclude AlreadyTransformed");
        assertFalse(transformed2.keySet().stream().anyMatch(cm -> cm.getName().equals(className)),
                    "transformed(filter) should exclude AlreadyTransformed");
    }

    /**
     * Custom log handler to capture transformation events for verification.
     */
    private static class TestLogHandler extends Handler {
        private final java.util.List<LogRecord> records = new java.util.ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public java.util.List<LogRecord> getRecords() {
            return records;
        }

        public boolean containsMessage(String substring) {
            return records.stream()
                         .anyMatch(r -> r.getMessage() != null && r.getMessage().contains(substring));
        }
    }
}
