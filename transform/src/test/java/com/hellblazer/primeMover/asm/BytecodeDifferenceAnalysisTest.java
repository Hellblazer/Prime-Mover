package com.hellblazer.primeMover.asm;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

/**
 * Test to analyze differences between original and refactored EntityGenerator bytecode.
 */
public class BytecodeDifferenceAnalysisTest {

    private SimulationTransform transform;

    @BeforeEach
    public void setUp() {
        transform = new SimulationTransform(new ClassGraph().acceptPackages("com.hellblazer.primeMover.asm.testClasses"));
    }

    @AfterEach 
    public void tearDown() throws Exception {
        if (transform != null) {
            transform.close();
        }
    }

    @Test
    public void analyzeBytecodeStructuralDifferences() throws Exception {
        final var className = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        
        // Generate bytecode using original EntityGenerator
        EntityGenerator originalGenerator = transform.generatorOf(className);
        assertNotNull(originalGenerator, "Original generator should not be null");
        byte[] originalBytecode = originalGenerator.generate().toByteArray();

        // Get the entity and events for refactored generator
        var generators = transform.generators();
        var matchingEntry = generators.entrySet().stream()
            .filter(entry -> entry.getKey().getName().equals(className))
            .findFirst()
            .orElse(null);
        
        assertNotNull(matchingEntry, "Should find matching entity class");
        
        var entity = matchingEntry.getKey();
        
        // Create refactored generator using the same ClassInfo and events
        var entIFaces = SimulationTransform.getEntityInterfaces(entity);
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

        EntityGeneratorRefactored refactoredGenerator = new EntityGeneratorRefactored(entity, events, transform.getTransformTimestamp());
        byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();

        // Parse both into ClassNode for detailed comparison
        ClassReader originalReader = new ClassReader(originalBytecode);
        ClassNode originalClass = new ClassNode();
        originalReader.accept(originalClass, 0);

        ClassReader refactoredReader = new ClassReader(refactoredBytecode);
        ClassNode refactoredClass = new ClassNode();
        refactoredReader.accept(refactoredClass, 0);

        System.out.println("=== BYTECODE DIFFERENCE ANALYSIS ===");
        System.out.println("Original bytecode size: " + originalBytecode.length);
        System.out.println("Refactored bytecode size: " + refactoredBytecode.length);
        
        // Print basic class structure comparison
        System.out.println("\n=== CLASS STRUCTURE COMPARISON ===");
        System.out.println("Class name: " + originalClass.name + " vs " + refactoredClass.name);
        System.out.println("Super class: " + originalClass.superName + " vs " + refactoredClass.superName);
        System.out.println("Interfaces: " + originalClass.interfaces + " vs " + refactoredClass.interfaces);
        System.out.println("Fields count: " + originalClass.fields.size() + " vs " + refactoredClass.fields.size());
        System.out.println("Methods count: " + originalClass.methods.size() + " vs " + refactoredClass.methods.size());

        // Print method comparison
        System.out.println("\n=== METHOD COMPARISON ===");
        for (int i = 0; i < Math.min(originalClass.methods.size(), refactoredClass.methods.size()); i++) {
            var originalMethod = originalClass.methods.get(i);
            var refactoredMethod = refactoredClass.methods.get(i);
            
            boolean matches = originalMethod.name.equals(refactoredMethod.name) &&
                             originalMethod.desc.equals(refactoredMethod.desc) &&
                             originalMethod.access == refactoredMethod.access;
            
            System.out.println("Method " + i + ": " + originalMethod.name + originalMethod.desc + 
                             " - Match: " + matches);
                             
            if (!matches) {
                System.out.println("  Original:  " + originalMethod.name + originalMethod.desc + " access=" + originalMethod.access);
                System.out.println("  Refactored:" + refactoredMethod.name + refactoredMethod.desc + " access=" + refactoredMethod.access);
            }
        }

        // Generate textual representation of bytecode for comparison
        System.out.println("\n=== ORIGINAL BYTECODE ===");
        StringWriter originalStringWriter = new StringWriter();
        PrintWriter originalPrintWriter = new PrintWriter(originalStringWriter);
        TraceClassVisitor originalTraceVisitor = new TraceClassVisitor(originalPrintWriter);
        originalReader.accept(originalTraceVisitor, ClassReader.EXPAND_FRAMES);
        String originalText = originalStringWriter.toString();
        
        System.out.println("\n=== REFACTORED BYTECODE ===");
        StringWriter refactoredStringWriter = new StringWriter();
        PrintWriter refactoredPrintWriter = new PrintWriter(refactoredStringWriter);
        TraceClassVisitor refactoredTraceVisitor = new TraceClassVisitor(refactoredPrintWriter);
        refactoredReader.accept(refactoredTraceVisitor, ClassReader.EXPAND_FRAMES);
        String refactoredText = refactoredStringWriter.toString();

        // Find first difference
        int firstDiff = -1;
        for (int i = 0; i < Math.min(originalBytecode.length, refactoredBytecode.length); i++) {
            if (originalBytecode[i] != refactoredBytecode[i]) {
                firstDiff = i;
                break;
            }
        }
        
        if (firstDiff >= 0) {
            System.out.println("\n=== FIRST BYTE DIFFERENCE ===");
            System.out.println("At index " + firstDiff + ": original=" + (originalBytecode[firstDiff] & 0xFF) + 
                             ", refactored=" + (refactoredBytecode[firstDiff] & 0xFF));
                             
            // Show surrounding bytes
            int start = Math.max(0, firstDiff - 10);
            int end = Math.min(Math.min(originalBytecode.length, refactoredBytecode.length), firstDiff + 10);
            
            System.out.print("Original bytes around diff:   ");
            for (int i = start; i < end; i++) {
                if (i == firstDiff) System.out.print("[" + (originalBytecode[i] & 0xFF) + "] ");
                else System.out.print((originalBytecode[i] & 0xFF) + " ");
            }
            System.out.println();
            
            System.out.print("Refactored bytes around diff: ");
            for (int i = start; i < end; i++) {
                if (i == firstDiff) System.out.print("[" + (refactoredBytecode[i] & 0xFF) + "] ");
                else System.out.print((refactoredBytecode[i] & 0xFF) + " ");
            }
            System.out.println();
        }
        
        // Check if the differences are just in line numbers or debug info
        boolean structurallyEquivalent = originalClass.name.equals(refactoredClass.name) &&
                                       originalClass.superName.equals(refactoredClass.superName) &&
                                       originalClass.interfaces.equals(refactoredClass.interfaces) &&
                                       originalClass.fields.size() == refactoredClass.fields.size() &&
                                       originalClass.methods.size() == refactoredClass.methods.size();
                                       
        System.out.println("\n=== ANALYSIS RESULT ===");
        System.out.println("Structurally equivalent: " + structurallyEquivalent);
        System.out.println("Byte-for-byte identical: " + (firstDiff == -1));
        
        if (structurallyEquivalent && firstDiff >= 0) {
            System.out.println("CONCLUSION: The generators produce structurally equivalent but not byte-identical bytecode.");
            System.out.println("This is likely due to differences in:");
            System.out.println("- Constant pool ordering");
            System.out.println("- Line number tables");
            System.out.println("- Debug information");
            System.out.println("- Stack map frames");
        }
    }
}
