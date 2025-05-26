package com.hellblazer.primeMover.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import io.github.classgraph.ClassGraph;

/**
 * Test suite for SimulationTransformRefactored to ensure it produces
 * structurally equivalent results to the original SimulationTransformOriginal.
 */
public class SimulationTransformRefactoredTest {

    private SimulationTransformOriginal   originalTransform;
    private SimulationTransformRefactored refactoredTransform;

    @BeforeEach
    public void setUp() {
        var classGraph = new ClassGraph().acceptPackages("com.hellblazer.primeMover.asm.testClasses", 
                                                         "com.hellblazer.primeMover");
        originalTransform = new SimulationTransformOriginal(classGraph);
        refactoredTransform = new SimulationTransformRefactored(classGraph);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (originalTransform != null) {
            originalTransform.close();
        }
        if (refactoredTransform != null) {
            refactoredTransform.close();
        }
    }

    @Test
    public void testGeneratorOfStructuralEquivalence() throws Exception {
        final var className = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        
        // Generate using original transform
        EntityGeneratorOriginal originalGenerator = originalTransform.generatorOf(className);
        Assertions.assertNotNull(originalGenerator, "Original generator should not be null");
        byte[] originalBytecode = originalGenerator.generate().toByteArray();

        // Generate using refactored transform
        EntityGeneratorRefactored refactoredGenerator = refactoredTransform.generatorOf(className);
        Assertions.assertNotNull(refactoredGenerator, "Refactored generator should not be null");
        byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();

        // Compare structural equivalence
        assertStructuralEquivalence(originalBytecode, refactoredBytecode, className);
    }

    @Test
    public void testGeneratorsStructuralEquivalence() throws Exception {
        // Get generators from both transforms
        var originalGenerators = originalTransform.generators();
        var refactoredGenerators = refactoredTransform.generators();

        // Should have same number of generators
        assertEquals(originalGenerators.size(), refactoredGenerators.size(),
            "Should have same number of entity generators");

        // Compare each entity
        originalGenerators.forEach((classInfo, originalGenerator) -> {
            var refactoredGenerator = refactoredGenerators.entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(classInfo.getName()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElse(null);

            Assertions.assertNotNull(refactoredGenerator,
                                     "Should have refactored generator for " + classInfo.getName());

            try {
                byte[] originalBytecode = originalGenerator.generate().toByteArray();
                byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();
                
                assertStructuralEquivalence(originalBytecode, refactoredBytecode, classInfo.getName());
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate bytecode for " + classInfo.getName(), e);
            }
        });
    }

    @Test
    public void testTransformedClassesEquivalence() {
        // Get transformed classes from both transforms
        var originalTransformed = originalTransform.transformed();
        var refactoredTransformed = refactoredTransform.transformed();

        // Should have same number of transformed classes
        assertEquals(originalTransformed.size(), refactoredTransformed.size(),
            "Should have same number of transformed classes");

        // Compare each transformed class
        originalTransformed.forEach((classInfo, originalBytecode) -> {
            var refactoredBytecode = refactoredTransformed.entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(classInfo.getName()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElse(null);

            Assertions.assertNotNull(refactoredBytecode,
                                     "Should have refactored bytecode for " + classInfo.getName());

            try {
                assertStructuralEquivalence(originalBytecode, refactoredBytecode, classInfo.getName());
            } catch (Exception e) {
                throw new RuntimeException("Failed to compare transformed class " + classInfo.getName(), e);
            }
        });
    }

    @Test
    public void testEntityInterfacesExtraction() {
        // Test the static utility method
        var generators = originalTransform.generators();
        
        generators.keySet().forEach(classInfo -> {
            var originalInterfaces = SimulationTransformOriginal.getEntityInterfaces(classInfo);
            var refactoredInterfaces = SimulationTransformRefactored.getEntityInterfaces(classInfo);
            
            assertEquals(originalInterfaces.size(), refactoredInterfaces.size(),
                "Should extract same number of entity interfaces for " + classInfo.getName());
            
            var originalNames = originalInterfaces.stream()
                .map(ci -> ci.getName())
                .collect(Collectors.toSet());
            var refactoredNames = refactoredInterfaces.stream()
                .map(ci -> ci.getName())
                .collect(Collectors.toSet());
            
            assertEquals(originalNames, refactoredNames,
                "Should extract same entity interfaces for " + classInfo.getName());
        });
    }

    @Test
    public void testRefactoredTransformFunctionality() throws Exception {
        // Test that refactored transform provides all expected functionality
        final var className = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        
        // Test generatorOf
        var generator = refactoredTransform.generatorOf(className);
        Assertions.assertNotNull(generator, "Should create generator for known entity class");
        
        // Test generators
        var generators = refactoredTransform.generators();
        Assertions.assertTrue(generators.size() > 0, "Should find entity classes");
        
        // Test transformed
        var transformed = refactoredTransform.transformed();
        Assertions.assertTrue(transformed.size() > 0, "Should transform classes");
        
        // Test that generated bytecode is valid
        byte[] bytecode = generator.generate().toByteArray();
        Assertions.assertTrue(bytecode.length > 0, "Should generate non-empty bytecode");
        
        // Test that bytecode is valid by parsing it
        ClassReader reader = new ClassReader(bytecode);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);
        
        Assertions.assertEquals(className.replace('.', '/'), classNode.name,
                                "Generated class should have correct name");
        Assertions.assertTrue(classNode.methods.size() > 0, "Generated class should have methods");
    }

    /**
     * Asserts that two bytecode arrays are structurally equivalent
     * (same class structure, methods, interfaces) even if not byte-identical.
     */
    private void assertStructuralEquivalence(byte[] originalBytecode, byte[] refactoredBytecode, String className) {
        // Parse both bytecode arrays
        ClassReader originalReader = new ClassReader(originalBytecode);
        ClassNode originalClass = new ClassNode();
        originalReader.accept(originalClass, 0);

        ClassReader refactoredReader = new ClassReader(refactoredBytecode);
        ClassNode refactoredClass = new ClassNode();
        refactoredReader.accept(refactoredClass, 0);

        // Compare structural elements
        Assertions.assertEquals(originalClass.name, refactoredClass.name,
                                "Class names should match for " + className);
        Assertions.assertEquals(originalClass.superName, refactoredClass.superName,
                                "Super class names should match for " + className);
        Assertions.assertEquals(originalClass.interfaces, refactoredClass.interfaces,
                                "Interfaces should match for " + className);
        Assertions.assertEquals(originalClass.fields.size(), refactoredClass.fields.size(),
                                "Field count should match for " + className);
        Assertions.assertEquals(originalClass.methods.size(), refactoredClass.methods.size(),
                                "Method count should match for " + className);

        // Compare method signatures
        for (int i = 0; i < originalClass.methods.size(); i++) {
            MethodNode originalMethod = originalClass.methods.get(i);
            MethodNode refactoredMethod = refactoredClass.methods.get(i);
            
            Assertions.assertEquals(originalMethod.name, refactoredMethod.name,
                                    "Method name should match at index " + i + " for " + className);
            Assertions.assertEquals(originalMethod.desc, refactoredMethod.desc,
                                    "Method descriptor should match for method " + originalMethod.name + " in " + className);
            Assertions.assertEquals(originalMethod.access, refactoredMethod.access,
                                    "Method access flags should match for method " + originalMethod.name + " in " + className);
        }
    }
}
