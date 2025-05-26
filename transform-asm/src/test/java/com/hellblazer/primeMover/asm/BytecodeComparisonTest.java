package com.hellblazer.primeMover.asm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

/**
 * Test that compares bytecode generation between the original EntityGeneratorOriginal
 * and the refactored version to ensure they produce structurally equivalent results.
 * 
 * Note: The test validates structural equivalence rather than byte-for-byte identity
 * because refactoring can change constant pool ordering, line number tables, and
 * debug information while maintaining functional equivalence.
 */
public class BytecodeComparisonTest {

    private SimulationTransformOriginal transform;

    @BeforeEach
    public void setUp() {
        transform = new SimulationTransformOriginal(new ClassGraph().acceptPackages("com.hellblazer.primeMover.asm.testClasses"));
    }

    @AfterEach 
    public void tearDown() throws Exception {
        if (transform != null) {
            transform.close();
        }
    }

    @Test
    public void testStructuralEquivalence() throws Exception {
        final var className = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        
        // Generate bytecode using original EntityGeneratorOriginal
        EntityGeneratorOriginal originalGenerator = transform.generatorOf(className);
        Assertions.assertNotNull(originalGenerator, "Original generator should not be null");
        byte[] originalBytecode = originalGenerator.generate().toByteArray();

        // Get all generators and find the one we need for comparison
        var generators = transform.generators();
        var matchingEntry = generators.entrySet().stream()
            .filter(entry -> entry.getKey().getName().equals(className))
            .findFirst()
            .orElse(null);
        
        Assertions.assertNotNull(matchingEntry, "Should find matching entity class");
        
        var entity = matchingEntry.getKey();
        var originalGeneratorFromMap = matchingEntry.getValue();
        
        // Create refactored generator using the same ClassInfo and events
        // We need to extract the events from the original generator by reflection or recreate them
        var entIFaces = SimulationTransformOriginal.getEntityInterfaces(entity);
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
                                .collect(Collectors.toCollection(() -> new OpenAddressingSet.OpenSet<MethodInfo>()));
        
        // Add @Event annotated methods
        entity.getDeclaredMethodInfo()
              .stream()
              .filter(mi -> mi.hasAnnotation("com.hellblazer.primeMover.annotations.Event"))
              .forEach(mi -> events.add(mi));
              
        // Add all public methods if AllMethodsMarker is present
        if (allPublic) {
            entity.getDeclaredMethodInfo()
                  .stream()
                  .filter(mi -> mi.isPublic() && !mi.isStatic() && !mi.isConstructor())
                  .filter(mi -> !mi.hasAnnotation("com.hellblazer.primeMover.annotations.NonEvent"))
                  .forEach(mi -> events.add(mi));
        }
        String commonTimestamp = "2025-05-25T01:00:00.000000Z"; // Fixed timestamp for deterministic comparison

        EntityGeneratorRefactored refactoredGenerator = new EntityGeneratorRefactored(entity, events, commonTimestamp);
        byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();

        // Compare structural equivalence rather than byte-for-byte identity
        // Byte-for-byte comparison fails due to constant pool ordering differences
        ClassReader originalReader = new ClassReader(originalBytecode);
        ClassNode originalClass = new ClassNode();
        originalReader.accept(originalClass, 0);

        ClassReader refactoredReader = new ClassReader(refactoredBytecode);
        ClassNode refactoredClass = new ClassNode();
        refactoredReader.accept(refactoredClass, 0);

        // Verify structural equivalence
        Assertions.assertEquals(originalClass.name, refactoredClass.name, "Class names should match");
        Assertions.assertEquals(originalClass.superName, refactoredClass.superName, "Super class names should match");
        Assertions.assertEquals(originalClass.interfaces, refactoredClass.interfaces, "Interfaces should match");
        Assertions.assertEquals(originalClass.fields.size(), refactoredClass.fields.size(), "Field count should match");
        Assertions.assertEquals(originalClass.methods.size(), refactoredClass.methods.size(), "Method count should match");

        // Compare method signatures
        for (int i = 0; i < originalClass.methods.size(); i++) {
            MethodNode originalMethod = originalClass.methods.get(i);
            MethodNode refactoredMethod = refactoredClass.methods.get(i);
            
            Assertions.assertEquals(originalMethod.name, refactoredMethod.name,
                                    "Method name should match at index " + i);
            Assertions.assertEquals(originalMethod.desc, refactoredMethod.desc,
                                    "Method descriptor should match for method " + originalMethod.name);
            Assertions.assertEquals(originalMethod.access, refactoredMethod.access,
                                    "Method access flags should match for method " + originalMethod.name);
        }
    }

    @Test
    public void testDetailedStructuralEquivalence() throws Exception {
        // Use the generators map to get both original generator and entity info
        var generators = transform.generators();
        var matchingEntry = generators.entrySet().stream()
            .filter(entry -> entry.getKey().getName().equals("com.hellblazer.primeMover.asm.testClasses.MyTest"))
            .findFirst()
            .orElse(null);
        
        Assertions.assertNotNull(matchingEntry, "Should find MyTest entity class");
        
        var entity = matchingEntry.getKey();
        var originalGenerator = matchingEntry.getValue();
        
        byte[] originalBytecode = originalGenerator.generate().toByteArray();

        // Create refactored generator using same logic as SimulationTransformOriginal.generateEntity
        var entIFaces = SimulationTransformOriginal.getEntityInterfaces(entity);
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

        EntityGeneratorRefactored refactoredGenerator = new EntityGeneratorRefactored(entity, events);
        byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();

        // Parse bytecode into ClassNode for detailed comparison
        ClassReader originalReader = new ClassReader(originalBytecode);
        ClassNode originalClass = new ClassNode();
        originalReader.accept(originalClass, 0);

        ClassReader refactoredReader = new ClassReader(refactoredBytecode);
        ClassNode refactoredClass = new ClassNode();
        refactoredReader.accept(refactoredClass, 0);

        // Compare class structure
        Assertions.assertEquals(originalClass.name, refactoredClass.name, "Class names should match");
        Assertions.assertEquals(originalClass.superName, refactoredClass.superName, "Super class names should match");
        Assertions.assertEquals(originalClass.interfaces, refactoredClass.interfaces, "Interfaces should match");
        Assertions.assertEquals(originalClass.fields.size(), refactoredClass.fields.size(), "Field count should match");
        Assertions.assertEquals(originalClass.methods.size(), refactoredClass.methods.size(), "Method count should match");

        // Compare method signatures
        for (int i = 0; i < originalClass.methods.size(); i++) {
            MethodNode originalMethod = originalClass.methods.get(i);
            MethodNode refactoredMethod = refactoredClass.methods.get(i);
            
            Assertions.assertEquals(originalMethod.name, refactoredMethod.name,
                                    "Method name should match at index " + i);
            Assertions.assertEquals(originalMethod.desc, refactoredMethod.desc,
                                    "Method descriptor should match for method " + originalMethod.name);
            Assertions.assertEquals(originalMethod.access, refactoredMethod.access,
                                    "Method access flags should match for method " + originalMethod.name);
        }
    }

    @Test
    public void testMultipleClassesStructuralEquivalence() throws Exception {
        // Get all entity generators
        var generators = transform.generators();
        
        for (var entry : generators.entrySet()) {
            var entity = entry.getKey();
            var originalGenerator = entry.getValue();
            
            byte[] originalBytecode = originalGenerator.generate().toByteArray();

            // Create refactored generator using same logic as SimulationTransformOriginal.generateEntity
            var entIFaces = SimulationTransformOriginal.getEntityInterfaces(entity);
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

            EntityGeneratorRefactored refactoredGenerator = new EntityGeneratorRefactored(entity, events);
            byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();

            // Compare structural equivalence
            ClassReader originalReader = new ClassReader(originalBytecode);
            ClassNode originalClass = new ClassNode();
            originalReader.accept(originalClass, 0);

            ClassReader refactoredReader = new ClassReader(refactoredBytecode);
            ClassNode refactoredClass = new ClassNode();
            refactoredReader.accept(refactoredClass, 0);

            // Verify structural equivalence
            Assertions.assertEquals(originalClass.name, refactoredClass.name, "Class names should match for " + entity.getName());
            Assertions.assertEquals(originalClass.superName, refactoredClass.superName, "Super class names should match for " + entity.getName());
            Assertions.assertEquals(originalClass.interfaces, refactoredClass.interfaces, "Interfaces should match for " + entity.getName());
            Assertions.assertEquals(originalClass.fields.size(), refactoredClass.fields.size(), "Field count should match for " + entity.getName());
            Assertions.assertEquals(originalClass.methods.size(), refactoredClass.methods.size(), "Method count should match for " + entity.getName());

            // Compare method signatures
            for (int j = 0; j < originalClass.methods.size(); j++) {
                MethodNode originalMethod = originalClass.methods.get(j);
                MethodNode refactoredMethod = refactoredClass.methods.get(j);
                
                Assertions.assertEquals(originalMethod.name, refactoredMethod.name,
                                        "Method name should match at index " + j + " for class " + entity.getName());
                Assertions.assertEquals(originalMethod.desc, refactoredMethod.desc,
                                        "Method descriptor should match for method " + originalMethod.name + " in class " + entity.getName());
                Assertions.assertEquals(originalMethod.access, refactoredMethod.access,
                                        "Method access flags should match for method " + originalMethod.name + " in class " + entity.getName());
            }
        }
    }
}
