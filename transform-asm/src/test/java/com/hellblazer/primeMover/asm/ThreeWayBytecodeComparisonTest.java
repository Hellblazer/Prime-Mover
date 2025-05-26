package com.hellblazer.primeMover.asm;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

/**
 * Test that compares bytecode generation between the original EntityGeneratorOriginal,
 * EntityGeneratorRefactored, and EntityGenerator to ensure they
 * produce structurally equivalent results.
 */
public class ThreeWayBytecodeComparisonTest {

    @Test
    public void testStructuralEquivalence() throws Exception {
        final var className = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        final var timestamp = "2025-05-25T01:00:00.000000Z"; // Fixed timestamp for deterministic comparison
        
        // Set up common data
        var transform = new SimulationTransformRefactored(new ClassGraph().acceptPackages("com.hellblazer.primeMover.asm.testClasses"));
        var generators = transform.generators();
        var matchingEntry = generators.entrySet().stream()
            .filter(entry -> entry.getKey().getName().equals(className))
            .findFirst()
            .orElse(null);
        
        Assertions.assertNotNull(matchingEntry, "Should find MyTest entity class");
        
        var entity = matchingEntry.getKey();
        var refactoredGenerator = matchingEntry.getValue();
        
        // Get event methods using same logic as SimulationTransformOriginal.generateEntity
        var events = extractEventMethods(entity);
        
        // Generate bytecode using original EntityGeneratorOriginal
        EntityGeneratorOriginal originalGenerator = new EntityGeneratorOriginal(entity, events, timestamp);
        byte[] originalBytecode = originalGenerator.generate().toByteArray();

        // Generate bytecode using refactored EntityGeneratorOriginal
        EntityGeneratorRefactored refactoredGeneratorWithTimestamp = new EntityGeneratorRefactored(entity, events, timestamp);
        byte[] refactoredBytecode = refactoredGeneratorWithTimestamp.generate().toByteArray();

        // Generate bytecode using ClassFile API EntityGeneratorOriginal
        EntityGenerator classFileGenerator = new EntityGenerator(entity, events, timestamp);
        byte[] classFileBytecode = classFileGenerator.generate();

        // Compare structural equivalence
        validateStructuralEquivalence("Original vs Refactored", originalBytecode, refactoredBytecode);
        validateStructuralEquivalence("Refactored vs ClassFile API", refactoredBytecode, classFileBytecode);
        validateStructuralEquivalence("Original vs ClassFile API", originalBytecode, classFileBytecode);
        
        // Check if all three are byte-for-byte identical
        boolean allIdentical = java.util.Arrays.equals(originalBytecode, refactoredBytecode) &&
                              java.util.Arrays.equals(refactoredBytecode, classFileBytecode);
        
        System.out.println("=== Three-Way Bytecode Comparison Results ===");
        System.out.println("Original size: " + originalBytecode.length + " bytes");
        System.out.println("Refactored size: " + refactoredBytecode.length + " bytes");
        System.out.println("ClassFile API size: " + classFileBytecode.length + " bytes");
        System.out.println("All three byte-for-byte identical: " + allIdentical);
        
        if (!allIdentical) {
            System.out.println("Differences found - analyzing...");
            analyzeBytecodeDifferences(originalBytecode, refactoredBytecode, classFileBytecode);
        }
        
        transform.close();
    }
    
    /**
     * Extract event methods using the same logic as SimulationTransformOriginal
     */
    private java.util.Set<MethodInfo> extractEventMethods(ClassInfo entity) {
        var entIFaces = SimulationTransformRefactored.getEntityInterfaces(entity);
        var allPublic = entIFaces.stream().anyMatch(c -> c.getName().equals("com.hellblazer.primeMover.annotations.AllMethodsMarker"));
        var interfaces = entity.getInterfaces();
        var implemented = new OpenAddressingSet.OpenSet<ClassInfo>();
        entIFaces.forEach(c -> {
            if (interfaces.contains(c)) {
                implemented.add(c);
            }
        });
        
        var events = implemented.stream()
                                .flatMap(intf -> intf.getMethodInfo().stream())
                                .map(mi -> entity.getMethodInfo(mi.getName())
                                             .stream()
                                             .filter(m -> m.getTypeDescriptorStr().equals(mi.getTypeDescriptorStr()))
                                             .findFirst()
                                             .orElse(null))
                                .filter(mi -> mi != null)
                                .filter(mi -> !mi.hasAnnotation("com.hellblazer.primeMover.annotations.NonEvent"))
                                .collect(Collectors.toCollection(() -> new OpenAddressingSet.OpenSet<MethodInfo>()));
        
        entity.getDeclaredMethodInfo()
              .stream()
              .filter(mi -> mi.hasAnnotation("com.hellblazer.primeMover.annotations.Event"))
              .forEach(mi -> events.add(mi));
              
        if (allPublic) {
            entity.getDeclaredMethodInfo()
                  .stream()
                  .filter(mi -> mi.isPublic() && !mi.isStatic() && !mi.isConstructor())
                  .filter(mi -> !mi.hasAnnotation("com.hellblazer.primeMover.annotations.NonEvent"))
                  .forEach(mi -> events.add(mi));
        }
        
        return events;
    }
    
    /**
     * Validate structural equivalence between two bytecode arrays
     */
    private void validateStructuralEquivalence(String comparison, byte[] bytecode1, byte[] bytecode2) {
        ClassReader reader1 = new ClassReader(bytecode1);
        ClassNode class1 = new ClassNode();
        reader1.accept(class1, 0);

        ClassReader reader2 = new ClassReader(bytecode2);
        ClassNode class2 = new ClassNode();
        reader2.accept(class2, 0);

        // Verify structural equivalence
        Assertions.assertEquals(class1.name, class2.name, comparison + ": Class names should match");
        Assertions.assertEquals(class1.superName, class2.superName, comparison + ": Super class names should match");
        Assertions.assertEquals(class1.interfaces, class2.interfaces, comparison + ": Interfaces should match");
        Assertions.assertEquals(class1.fields.size(), class2.fields.size(), comparison + ": Field count should match");
        Assertions.assertEquals(class1.methods.size(), class2.methods.size(), comparison + ": Method count should match");

        // Compare method signatures
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
    
    /**
     * Analyze bytecode differences to understand what's different
     */
    private void analyzeBytecodeDifferences(byte[] original, byte[] refactored, byte[] classFileAPI) {
        // Compare original vs refactored
        if (!java.util.Arrays.equals(original, refactored)) {
            System.out.println("Difference between Original and Refactored:");
            findFirstDifference("Original vs Refactored", original, refactored);
        }
        
        // Compare refactored vs classfile API
        if (!java.util.Arrays.equals(refactored, classFileAPI)) {
            System.out.println("Difference between Refactored and ClassFile API:");
            findFirstDifference("Refactored vs ClassFile API", refactored, classFileAPI);
        }
        
        // Compare original vs classfile API
        if (!java.util.Arrays.equals(original, classFileAPI)) {
            System.out.println("Difference between Original and ClassFile API:");
            findFirstDifference("Original vs ClassFile API", original, classFileAPI);
        }
    }
    
    /**
     * Find the first byte difference between two bytecode arrays
     */
    private void findFirstDifference(String comparison, byte[] bytes1, byte[] bytes2) {
        int minLength = Math.min(bytes1.length, bytes2.length);
        
        for (int i = 0; i < minLength; i++) {
            if (bytes1[i] != bytes2[i]) {
                System.out.printf("%s: First difference at index %d: 0x%02X vs 0x%02X%n", 
                    comparison, i, bytes1[i] & 0xFF, bytes2[i] & 0xFF);
                
                // Show context around the difference
                int start = Math.max(0, i - 5);
                int end = Math.min(minLength, i + 6);
                
                System.out.print("Context (bytes1): ");
                for (int j = start; j < end; j++) {
                    if (j == i) System.out.print("[");
                    System.out.printf("%02X", bytes1[j] & 0xFF);
                    if (j == i) System.out.print("]");
                    System.out.print(" ");
                }
                System.out.println();
                
                System.out.print("Context (bytes2): ");
                for (int j = start; j < end; j++) {
                    if (j == i) System.out.print("[");
                    System.out.printf("%02X", bytes2[j] & 0xFF);
                    if (j == i) System.out.print("]");
                    System.out.print(" ");
                }
                System.out.println();
                return;
            }
        }
        
        if (bytes1.length != bytes2.length) {
            System.out.printf("%s: Different lengths: %d vs %d%n", 
                comparison, bytes1.length, bytes2.length);
        }
    }
}
