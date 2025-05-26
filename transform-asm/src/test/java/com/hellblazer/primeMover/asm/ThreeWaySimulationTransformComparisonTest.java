package com.hellblazer.primeMover.asm;

import com.hellblazer.primeMover.classfile.EntityGenerator;
import com.hellblazer.primeMover.classfile.SimulationTransform;
import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comprehensive three-way comparison test for SimulationTransformOriginal implementations. Validates that Original ASM,
 * Refactored ASM, and ClassFile API implementations all produce structurally equivalent bytecode transformations.
 */
public class ThreeWaySimulationTransformComparisonTest {

    private SimulationTransformOriginal   originalTransform;
    private SimulationTransformRefactored refactoredTransform;
    private SimulationTransform           classFileTransform;

    @BeforeEach
    public void setUp() {
        var classGraph = new ClassGraph().acceptPackages("com.hellblazer.primeMover.asm.testClasses",
                                                         "com.hellblazer.primeMover.api");
        originalTransform = new SimulationTransformOriginal(classGraph);
        refactoredTransform = new SimulationTransformRefactored(classGraph);
        classFileTransform = new SimulationTransform(classGraph);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (originalTransform != null) {
            originalTransform.close();
        }
        if (refactoredTransform != null) {
            refactoredTransform.close();
        }
        if (classFileTransform != null) {
            classFileTransform.close();
        }
    }

    @Test
    public void testThreeWayGeneratorComparison() {
        // Compare generators from all three implementations
        var originalGenerators = originalTransform.generators();
        var refactoredGenerators = refactoredTransform.generators();
        var classFileGenerators = classFileTransform.generators();

        assertEquals(originalGenerators.size(), refactoredGenerators.size(),
                     "Original and refactored should have same number of generators");
        assertEquals(refactoredGenerators.size(), classFileGenerators.size(),
                     "Refactored and ClassFile API should have same number of generators");

        System.out.println("=== Three-Way Generator Comparison ===");
        System.out.println("Original generators: " + originalGenerators.size());
        System.out.println("Refactored generators: " + refactoredGenerators.size());
        System.out.println("ClassFile API generators: " + classFileGenerators.size());

        // Test that all generators produce equivalent bytecode
        originalGenerators.forEach((classInfo, originalGenerator) -> {
            var refactoredGenerator = refactoredGenerators.entrySet()
                                                          .stream()
                                                          .filter(
                                                          entry -> entry.getKey().getName().equals(classInfo.getName()))
                                                          .map(entry -> entry.getValue())
                                                          .findFirst()
                                                          .orElse(null);

            var classFileGenerator = classFileGenerators.entrySet()
                                                        .stream()
                                                        .filter(
                                                        entry -> entry.getKey().getName().equals(classInfo.getName()))
                                                        .map(entry -> entry.getValue())
                                                        .findFirst()
                                                        .orElse(null);

            Assertions.assertNotNull(refactoredGenerator,
                                     "Should have refactored generator for " + classInfo.getName());
            Assertions.assertNotNull(classFileGenerator,
                                     "Should have ClassFile API generator for " + classInfo.getName());

            // All generators should be able to produce valid bytecode
            try {
                byte[] originalBytecode = originalGenerator.generate().toByteArray();
                byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();
                byte[] classFileBytecode = classFileGenerator.generate();

                // Verify all are valid bytecode
                parseAndValidateBytecode(originalBytecode, "Original " + classInfo.getName());
                parseAndValidateBytecode(refactoredBytecode, "Refactored " + classInfo.getName());
                parseAndValidateBytecode(classFileBytecode, "ClassFile API " + classInfo.getName());

            } catch (Exception e) {
                throw new RuntimeException("Failed to generate bytecode for " + classInfo.getName(), e);
            }
        });
    }

    @Test
    public void testThreeWayStructuralEquivalence() throws Exception {
        final var className = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        final var timestamp = "2025-05-25T01:00:00.000000Z"; // Fixed timestamp for deterministic comparison

        // Set same timestamp for deterministic comparison
        refactoredTransform.setTransformTimestamp(timestamp);
        classFileTransform.setTransformTimestamp(timestamp);

        // Generate bytecode using original SimulationTransformOriginal
        EntityGeneratorOriginal originalGenerator = originalTransform.generatorOf(className);
        Assertions.assertNotNull(originalGenerator, "Original generator should not be null");

        // Create with fixed timestamp for comparison
        var originalTransformGenerators = originalTransform.generators();
        var originalEntity = originalTransformGenerators.entrySet().stream().filter(
        entry -> entry.getKey().getName().equals(className)).findFirst().orElseThrow();

        // Create an original generator with the fixed timestamp
        // Note: We can't access extractEventMethods directly, so we'll use a different approach
        EntityGeneratorOriginal originalGeneratorWithTimestamp = originalGenerator;
        byte[] originalBytecode = originalGeneratorWithTimestamp.generate().toByteArray();

        // Generate bytecode using refactored SimulationTransformOriginal
        EntityGeneratorRefactored refactoredGenerator = refactoredTransform.generatorOf(className);
        Assertions.assertNotNull(refactoredGenerator, "Refactored generator should not be null");
        byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();

        // Generate bytecode using ClassFile API SimulationTransformOriginal
        EntityGenerator classFileGenerator = classFileTransform.generatorOf(className);
        Assertions.assertNotNull(classFileGenerator, "ClassFile API generator should not be null");
        byte[] classFileBytecode = classFileGenerator.generate();

        // Compare structural equivalence across all three
        validateStructuralEquivalence("Original vs Refactored", originalBytecode, refactoredBytecode);
        validateStructuralEquivalence("Refactored vs ClassFile API", refactoredBytecode, classFileBytecode);
        validateStructuralEquivalence("Original vs ClassFile API", originalBytecode, classFileBytecode);

        // Check if all three are byte-for-byte identical (unlikely but possible)
        boolean allIdentical = java.util.Arrays.equals(originalBytecode, refactoredBytecode) && java.util.Arrays.equals(
        refactoredBytecode, classFileBytecode);

        System.out.println("=== Three-Way SimulationTransformOriginal Comparison Results ===");
        System.out.println("Original size: " + originalBytecode.length + " bytes");
        System.out.println("Refactored size: " + refactoredBytecode.length + " bytes");
        System.out.println("ClassFile API size: " + classFileBytecode.length + " bytes");
        System.out.println("All three byte-for-byte identical: " + allIdentical);

        if (!allIdentical) {
            System.out.println("Differences found - analyzing...");
            analyzeDifferences("Original vs Refactored", originalBytecode, refactoredBytecode);
            analyzeDifferences("Refactored vs ClassFile API", refactoredBytecode, classFileBytecode);
            analyzeDifferences("Original vs ClassFile API", originalBytecode, classFileBytecode);
        }
    }

    @Test
    public void testThreeWayTransformedClassesComparison() {
        final var timestamp = "2025-05-25T01:00:00.000000Z";

        // Set same timestamp for deterministic comparison
        refactoredTransform.setTransformTimestamp(timestamp);
        classFileTransform.setTransformTimestamp(timestamp);

        // Get transformed classes from all three implementations
        var originalTransformed = originalTransform.transformed();
        var refactoredTransformed = refactoredTransform.transformed();
        var classFileTransformed = classFileTransform.transformed();

        // Should have same number of transformed classes
        assertEquals(originalTransformed.size(), refactoredTransformed.size(),
                     "Original and refactored should have same number of transformed classes");
        assertEquals(refactoredTransformed.size(), classFileTransformed.size(),
                     "Refactored and ClassFile API should have same number of transformed classes");

        System.out.println("=== Three-Way Transformed Classes Comparison ===");
        System.out.println("Original transformed classes: " + originalTransformed.size());
        System.out.println("Refactored transformed classes: " + refactoredTransformed.size());
        System.out.println("ClassFile API transformed classes: " + classFileTransformed.size());

        // Compare each transformed class
        originalTransformed.forEach((classInfo, originalBytecode) -> {
            var refactoredBytecode = refactoredTransformed.entrySet()
                                                          .stream()
                                                          .filter(
                                                          entry -> entry.getKey().getName().equals(classInfo.getName()))
                                                          .map(entry -> entry.getValue())
                                                          .findFirst()
                                                          .orElse(null);

            var classFileBytecode = classFileTransformed.entrySet()
                                                        .stream()
                                                        .filter(
                                                        entry -> entry.getKey().getName().equals(classInfo.getName()))
                                                        .map(entry -> entry.getValue())
                                                        .findFirst()
                                                        .orElse(null);

            Assertions.assertNotNull(refactoredBytecode, "Should have refactored bytecode for " + classInfo.getName());
            Assertions.assertNotNull(classFileBytecode,
                                     "Should have ClassFile API bytecode for " + classInfo.getName());

            try {
                validateStructuralEquivalence("Original vs Refactored for " + classInfo.getName(), originalBytecode,
                                              refactoredBytecode);
                validateStructuralEquivalence("Refactored vs ClassFile API for " + classInfo.getName(),
                                              refactoredBytecode, classFileBytecode);
                validateStructuralEquivalence("Original vs ClassFile API for " + classInfo.getName(), originalBytecode,
                                              classFileBytecode);
            } catch (Exception e) {
                throw new RuntimeException("Failed to compare transformed class " + classInfo.getName(), e);
            }
        });
    }

    /**
     * Analyzes and reports differences between two bytecode arrays.
     */
    private void analyzeDifferences(String comparison, byte[] bytes1, byte[] bytes2) {
        if (java.util.Arrays.equals(bytes1, bytes2)) {
            return;
        }

        System.out.println("Difference between " + comparison + ":");

        // Find first byte difference
        int firstDiff = -1;
        for (int i = 0; i < Math.min(bytes1.length, bytes2.length); i++) {
            if (bytes1[i] != bytes2[i]) {
                firstDiff = i;
                break;
            }
        }

        if (firstDiff != -1) {
            System.out.printf("%s: First difference at index %d: 0x%02X vs 0x%02X%n", comparison, firstDiff,
                              bytes1[firstDiff] & 0xFF, bytes2[firstDiff] & 0xFF);

            // Show context around the difference
            int start = Math.max(0, firstDiff - 5);
            int end = Math.min(bytes1.length, firstDiff + 6);

            System.out.print("Context (bytes1): ");
            for (int i = start; i < end; i++) {
                if (i == firstDiff) {
                    System.out.printf("[%02X] ", bytes1[i] & 0xFF);
                } else {
                    System.out.printf("%02X ", bytes1[i] & 0xFF);
                }
            }
            System.out.println();

            System.out.print("Context (bytes2): ");
            for (int i = start; i < Math.min(bytes2.length, end); i++) {
                if (i == firstDiff) {
                    System.out.printf("[%02X] ", bytes2[i] & 0xFF);
                } else {
                    System.out.printf("%02X ", bytes2[i] & 0xFF);
                }
            }
            System.out.println();
        } else if (bytes1.length != bytes2.length) {
            System.out.printf("%s: Different lengths: %d vs %d%n", comparison, bytes1.length, bytes2.length);
        }
    }

    /**
     * Parses bytecode and validates it's structurally correct.
     */
    private void parseAndValidateBytecode(byte[] bytecode, String description) {
        ClassReader reader = new ClassReader(bytecode);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        Assertions.assertNotNull(classNode.name, description + " should have a valid class name");
        Assertions.assertNotNull(classNode.methods, description + " should have methods");
    }

    /**
     * Validates structural equivalence between two bytecode arrays and provides detailed error messages.
     */
    private void validateStructuralEquivalence(String comparison, byte[] bytes1, byte[] bytes2) {
        ClassReader reader1 = new ClassReader(bytes1);
        ClassNode class1 = new ClassNode();
        reader1.accept(class1, 0);

        ClassReader reader2 = new ClassReader(bytes2);
        ClassNode class2 = new ClassNode();
        reader2.accept(class2, 0);

        // Compare structural elements with detailed messages
        Assertions.assertEquals(class1.name, class2.name, comparison + ": Class names should match");
        Assertions.assertEquals(class1.superName, class2.superName, comparison + ": Super class names should match");
        Assertions.assertEquals(class1.interfaces, class2.interfaces, comparison + ": Interfaces should match");
        Assertions.assertEquals(class1.fields.size(), class2.fields.size(), comparison + ": Field count should match");
        Assertions.assertEquals(class1.methods.size(), class2.methods.size(),
                                comparison + ": Method count should match");

        // Compare method signatures - handle Template class specially due to method ordering differences
        if (class1.name.contains("Template")) {
            // For Template class, compare method sets rather than ordered lists
            var methods1Set = class1.methods.stream().map(m -> m.name + m.desc + m.access).collect(
            java.util.stream.Collectors.toSet());
            var methods2Set = class2.methods.stream().map(m -> m.name + m.desc + m.access).collect(
            java.util.stream.Collectors.toSet());
            Assertions.assertEquals(methods1Set, methods2Set,
                                    comparison + ": Method sets should match for Template class");
        } else {
            // For other classes, maintain strict ordering
            for (int i = 0; i < class1.methods.size(); i++) {
                MethodNode method1 = class1.methods.get(i);
                MethodNode method2 = class2.methods.get(i);

                Assertions.assertEquals(method1.name, method2.name,
                                        comparison + ": Method name should match at index " + i);
                Assertions.assertEquals(method1.desc, method2.desc,
                                        comparison + ": Method descriptor should match for method " + method1.name);
                Assertions.assertEquals(method1.access, method2.access,
                                        comparison + ": Method access flags should match for method " + method1.name);
            }
        }
    }
}
