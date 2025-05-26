package com.hellblazer.primeMover.asm;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import com.hellblazer.primeMover.classfile.OpenAddressingSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

/**
 * Detailed analysis of bytecode differences between original and refactored EntityGeneratorOriginal.
 * This test examines constant pools, method bytecode, line numbers, and other potential sources of differences.
 */
public class DetailedBytecodeAnalysisTest {

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
    public void analyzeConstantPoolDifferences() throws Exception {
        final var className = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        
        // Generate bytecode using both generators
        var generators = transform.generators();
        var matchingEntry = generators.entrySet().stream()
            .filter(entry -> entry.getKey().getName().equals(className))
            .findFirst()
            .orElse(null);
        
        Assertions.assertNotNull(matchingEntry, "Should find matching entity class");
        
        var entity = matchingEntry.getKey();
        
        // Create refactored generator with same timestamp as original
        var events = createEventSet(entity);
        String commonTimestamp = "2025-05-25T01:00:00.000000Z"; // Fixed timestamp for deterministic comparison
        EntityGeneratorRefactored refactoredGenerator = new EntityGeneratorRefactored(entity, events, commonTimestamp);
        
        // Create new original generator with same timestamp
        EntityGeneratorOriginal originalGeneratorWithTimestamp = new EntityGeneratorOriginal(entity, createEventSetFromMethodInfo(entity), commonTimestamp);
        
        byte[] originalBytecode = originalGeneratorWithTimestamp.generate().toByteArray();
        byte[] refactoredBytecode = refactoredGenerator.generate().toByteArray();

        System.out.println("=== DETAILED BYTECODE DIFFERENCE ANALYSIS ===");
        System.out.println("Class: " + className);
        System.out.println("Original size: " + originalBytecode.length + " bytes");
        System.out.println("Refactored size: " + refactoredBytecode.length + " bytes");
        
        // Parse into ClassNode for detailed analysis
        ClassReader originalReader = new ClassReader(originalBytecode);
        ClassNode originalClass = new ClassNode();
        originalReader.accept(originalClass, ClassReader.EXPAND_FRAMES);

        ClassReader refactoredReader = new ClassReader(refactoredBytecode);
        ClassNode refactoredClass = new ClassNode();
        refactoredReader.accept(refactoredClass, ClassReader.EXPAND_FRAMES);

        // Analyze constant pool differences
        analyzeConstantPool(originalReader, refactoredReader);
        
        // Analyze method-level differences
        analyzeMethodDifferences(originalClass, refactoredClass);
        
        // Analyze instruction-level differences
        analyzeInstructionDifferences(originalClass, refactoredClass);
        
        // Show raw bytecode comparison
        showRawBytecodeComparison(originalBytecode, refactoredBytecode);
        
        // Generate textual disassembly for comparison
        generateDisassemblyComparison(originalBytecode, refactoredBytecode);
    }

    private OpenAddressingSet.OpenSet<MethodInfo> createEventSet(ClassInfo entity) {
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
                                .collect(java.util.stream.Collectors.toCollection(() -> new OpenAddressingSet.OpenSet<MethodInfo>()));
        
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
    
    private java.util.Set<MethodInfo> createEventSetFromMethodInfo(ClassInfo entity) {
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
                                .collect(java.util.stream.Collectors.toCollection(() -> new java.util.HashSet<>()));
        
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

    private void analyzeConstantPool(ClassReader original, ClassReader refactored) {
        System.out.println("\n=== CONSTANT POOL ANALYSIS ===");
        
        try {
            // Use reflection to access the constant pool
            Field originalCpInfo = ClassReader.class.getDeclaredField("cpInfoOffsets");
            originalCpInfo.setAccessible(true);
            int[] originalOffsets = (int[]) originalCpInfo.get(original);
            
            int[] refactoredOffsets = (int[]) originalCpInfo.get(refactored);
            
            System.out.println("Original constant pool entries: " + (originalOffsets != null ? originalOffsets.length : 0));
            System.out.println("Refactored constant pool entries: " + (refactoredOffsets != null ? refactoredOffsets.length : 0));
            
            if (originalOffsets != null && refactoredOffsets != null) {
                System.out.println("Constant pool size difference: " + (refactoredOffsets.length - originalOffsets.length));
                
                // Compare first few entries to see ordering differences
                int maxCompare = Math.min(10, Math.min(originalOffsets.length, refactoredOffsets.length));
                System.out.println("First " + maxCompare + " constant pool entry offsets:");
                for (int i = 0; i < maxCompare; i++) {
                    if (originalOffsets[i] != refactoredOffsets[i]) {
                        System.out.println("  Entry " + i + ": original=" + originalOffsets[i] + ", refactored=" + refactoredOffsets[i] + " (DIFF)");
                    } else {
                        System.out.println("  Entry " + i + ": " + originalOffsets[i] + " (SAME)");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not analyze constant pool details: " + e.getMessage());
        }
    }

    private void analyzeMethodDifferences(ClassNode original, ClassNode refactored) {
        System.out.println("\n=== METHOD-LEVEL ANALYSIS ===");
        
        for (int i = 0; i < Math.min(original.methods.size(), refactored.methods.size()); i++) {
            MethodNode origMethod = original.methods.get(i);
            MethodNode refMethod = refactored.methods.get(i);
            
            if (!origMethod.name.equals(refMethod.name) || 
                !origMethod.desc.equals(refMethod.desc) ||
                origMethod.access != refMethod.access) {
                System.out.println("Method " + i + " signature differs:");
                System.out.println("  Original:  " + origMethod.access + " " + origMethod.name + origMethod.desc);
                System.out.println("  Refactored:" + refMethod.access + " " + refMethod.name + refMethod.desc);
            }
            
            // Compare instruction counts
            int origInsnCount = origMethod.instructions != null ? origMethod.instructions.size() : 0;
            int refInsnCount = refMethod.instructions != null ? refMethod.instructions.size() : 0;
            
            if (origInsnCount != refInsnCount) {
                System.out.println("Method " + origMethod.name + " instruction count differs:");
                System.out.println("  Original: " + origInsnCount + " instructions");
                System.out.println("  Refactored: " + refInsnCount + " instructions");
            }
            
            // Compare exception table
            int origExceptionCount = origMethod.tryCatchBlocks != null ? origMethod.tryCatchBlocks.size() : 0;
            int refExceptionCount = refMethod.tryCatchBlocks != null ? refMethod.tryCatchBlocks.size() : 0;
            
            if (origExceptionCount != refExceptionCount) {
                System.out.println("Method " + origMethod.name + " exception table differs:");
                System.out.println("  Original: " + origExceptionCount + " entries");
                System.out.println("  Refactored: " + refExceptionCount + " entries");
            }
            
            // Compare local variable table
            int origLocalVarCount = origMethod.localVariables != null ? origMethod.localVariables.size() : 0;
            int refLocalVarCount = refMethod.localVariables != null ? refMethod.localVariables.size() : 0;
            
            if (origLocalVarCount != refLocalVarCount) {
                System.out.println("Method " + origMethod.name + " local variable table differs:");
                System.out.println("  Original: " + origLocalVarCount + " entries");
                System.out.println("  Refactored: " + refLocalVarCount + " entries");
            }
            
            // Compare attributes and other metadata
            int origAttrCount = 0;
            int refAttrCount = 0;
            
            // Count various method attributes
            if (origMethod.annotationDefault != null) origAttrCount++;
            if (origMethod.visibleAnnotations != null) origAttrCount += origMethod.visibleAnnotations.size();
            if (origMethod.invisibleAnnotations != null) origAttrCount += origMethod.invisibleAnnotations.size();
            
            if (refMethod.annotationDefault != null) refAttrCount++;
            if (refMethod.visibleAnnotations != null) refAttrCount += refMethod.visibleAnnotations.size();
            if (refMethod.invisibleAnnotations != null) refAttrCount += refMethod.invisibleAnnotations.size();
            
            if (origAttrCount != refAttrCount) {
                System.out.println("Method " + origMethod.name + " attribute count differs:");
                System.out.println("  Original: " + origAttrCount + " attributes");
                System.out.println("  Refactored: " + refAttrCount + " attributes");
            }
        }
    }

    private void analyzeInstructionDifferences(ClassNode original, ClassNode refactored) {
        System.out.println("\n=== INSTRUCTION-LEVEL ANALYSIS ===");
        
        for (int i = 0; i < Math.min(original.methods.size(), refactored.methods.size()); i++) {
            MethodNode origMethod = original.methods.get(i);
            MethodNode refMethod = refactored.methods.get(i);
            
            if (origMethod.instructions == null || refMethod.instructions == null) {
                continue;
            }
            
            boolean foundDifference = false;
            
            // Compare each instruction
            for (int j = 0; j < Math.min(origMethod.instructions.size(), refMethod.instructions.size()); j++) {
                var origInsn = origMethod.instructions.get(j);
                var refInsn = refMethod.instructions.get(j);
                
                if (origInsn.getOpcode() != refInsn.getOpcode() || 
                    origInsn.getType() != refInsn.getType()) {
                    if (!foundDifference) {
                        System.out.println("Method " + origMethod.name + " instruction differences:");
                        foundDifference = true;
                    }
                    System.out.println("  Instruction " + j + ": original=" + origInsn.getOpcode() + 
                                     " (type=" + origInsn.getType() + "), refactored=" + refInsn.getOpcode() + 
                                     " (type=" + refInsn.getType() + ")");
                    
                    if (j < 5) { // Only show first few differences
                        break;
                    }
                }
            }
        }
    }

    private void showRawBytecodeComparison(byte[] original, byte[] refactored) {
        System.out.println("\n=== RAW BYTECODE COMPARISON ===");
        
        int firstDiff = -1;
        for (int i = 0; i < Math.min(original.length, refactored.length); i++) {
            if (original[i] != refactored[i]) {
                firstDiff = i;
                break;
            }
        }
        
        if (firstDiff >= 0) {
            System.out.println("First difference at byte " + firstDiff);
            System.out.println("Original: 0x" + String.format("%02X", original[firstDiff] & 0xFF) + 
                             " (" + (original[firstDiff] & 0xFF) + ")");
            System.out.println("Refactored: 0x" + String.format("%02X", refactored[firstDiff] & 0xFF) + 
                             " (" + (refactored[firstDiff] & 0xFF) + ")");
            
            // Show context around the difference
            int start = Math.max(0, firstDiff - 10);
            int end = Math.min(Math.min(original.length, refactored.length), firstDiff + 10);
            
            System.out.println("\nContext (bytes " + start + " to " + (end-1) + "):");
            System.out.print("Original:  ");
            for (int i = start; i < end; i++) {
                if (i == firstDiff) {
                    System.out.print("[" + String.format("%02X", original[i] & 0xFF) + "] ");
                } else {
                    System.out.print(String.format("%02X", original[i] & 0xFF) + " ");
                }
            }
            System.out.println();
            
            System.out.print("Refactored:");
            for (int i = start; i < end; i++) {
                if (i == firstDiff) {
                    System.out.print("[" + String.format("%02X", refactored[i] & 0xFF) + "] ");
                } else {
                    System.out.print(String.format("%02X", refactored[i] & 0xFF) + " ");
                }
            }
            System.out.println();
            
            // Try to determine what section this is in
            if (firstDiff < 10) {
                System.out.println("Location: Class file header/magic number area");
            } else if (firstDiff < 50) {
                System.out.println("Location: Constant pool area");
            } else {
                System.out.println("Location: Class metadata or method bytecode area");
            }
        } else {
            System.out.println("No byte differences found!");
        }
    }

    private void generateDisassemblyComparison(byte[] original, byte[] refactored) {
        System.out.println("\n=== DISASSEMBLY COMPARISON (first differences) ===");
        
        try {
            // Generate textual representation using Textifier
            StringWriter originalStr = new StringWriter();
            StringWriter refactoredStr = new StringWriter();
            
            Printer originalPrinter = new Textifier();
            Printer refactoredPrinter = new Textifier();
            
            TraceClassVisitor originalTracer = new TraceClassVisitor(null, originalPrinter, new PrintWriter(originalStr));
            TraceClassVisitor refactoredTracer = new TraceClassVisitor(null, refactoredPrinter, new PrintWriter(refactoredStr));
            
            new ClassReader(original).accept(originalTracer, ClassReader.EXPAND_FRAMES);
            new ClassReader(refactored).accept(refactoredTracer, ClassReader.EXPAND_FRAMES);
            
            String[] originalLines = originalStr.toString().split("\n");
            String[] refactoredLines = refactoredStr.toString().split("\n");
            
            // Find first difference in textual representation
            int maxLines = Math.min(originalLines.length, refactoredLines.length);
            int firstLineDiff = -1;
            
            for (int i = 0; i < maxLines; i++) {
                if (!originalLines[i].equals(refactoredLines[i])) {
                    firstLineDiff = i;
                    break;
                }
            }
            
            if (firstLineDiff >= 0) {
                System.out.println("First textual difference at line " + (firstLineDiff + 1));
                
                // Show context around the difference
                int start = Math.max(0, firstLineDiff - 3);
                int end = Math.min(maxLines, firstLineDiff + 4);
                
                for (int i = start; i < end; i++) {
                    String marker = (i == firstLineDiff) ? " >>> " : "     ";
                    System.out.println("Original" + marker + (i+1) + ": " + originalLines[i]);
                    if (i == firstLineDiff) {
                        System.out.println("Refactored" + marker + (i+1) + ": " + refactoredLines[i]);
                        System.out.println();
                    }
                }
            } else {
                System.out.println("No textual differences found in disassembly!");
            }
            
        } catch (Exception e) {
            System.out.println("Could not generate disassembly comparison: " + e.getMessage());
        }
    }
}
