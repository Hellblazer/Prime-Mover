package com.hellblazer.primeMover.classfile;

import java.lang.classfile.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;

/**
 * Explore ClassFile API patterns to understand correct usage
 */
public class ClassFileAPIPatterns {
    
    public static void main(String[] args) {
        testBasicTransformation();
        testMethodGeneration();
        testTableSwitch();
    }
    
    public static void testBasicTransformation() {
        System.out.println("=== Testing Basic Transformation ===");
        
        ClassFile cf = ClassFile.of();
        
        try {
            // Get bytecode of this class
            String thisClassName = ClassFileAPIPatterns.class.getName().replace('.', '/') + ".class";
            byte[] originalBytes;
            try (var is = ClassFileAPIPatterns.class.getClassLoader().getResourceAsStream(thisClassName)) {
                originalBytes = is.readAllBytes();
            }
            
            // Parse and examine the structure
            ClassModel classModel = cf.parse(originalBytes);
            System.out.println("Class: " + classModel.thisClass().asInternalName());
            System.out.println("Methods: " + classModel.methods().size());
            
            // Simple transformation - add a field
            byte[] transformed = cf.transformClass(classModel, 
                ClassTransform.endHandler(classBuilder -> {
                    classBuilder.withField("testField", ConstantDescs.CD_String, ClassFile.ACC_PRIVATE);
                })
            );
            
            System.out.println("Original size: " + originalBytes.length);
            System.out.println("Transformed size: " + transformed.length);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void testMethodGeneration() {
        System.out.println("\n=== Testing Method Generation ===");
        
        ClassFile cf = ClassFile.of();
        
        try {
            byte[] classBytes = cf.build(ClassDesc.of("TestMethodGen"), classBuilder -> {
                classBuilder
                    .withFlags(ClassFile.ACC_PUBLIC)
                    .withMethodBody("testMethod", 
                        MethodTypeDesc.of(ConstantDescs.CD_String, ConstantDescs.CD_int),
                        ClassFile.ACC_PUBLIC,
                        codeBuilder -> {
                            codeBuilder
                                .ldc("Method called with: ")
                                .areturn();
                        });
            });
            
            System.out.println("Generated class with method: " + classBytes.length + " bytes");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void testTableSwitch() {
        System.out.println("\n=== Testing TableSwitch ===");
        
        ClassFile cf = ClassFile.of();
        
        try {
            byte[] classBytes = cf.build(ClassDesc.of("TestSwitch"), classBuilder -> {
                classBuilder
                    .withFlags(ClassFile.ACC_PUBLIC)
                    .withMethodBody("switchMethod", 
                        MethodTypeDesc.of(ConstantDescs.CD_String, ConstantDescs.CD_int),
                        ClassFile.ACC_PUBLIC,
                        codeBuilder -> {
                            // Let's look at what tableswitch expects
                            codeBuilder.iload(1);
                            
                            // Try a simple switch
                            Label defaultLabel = codeBuilder.newLabel();
                            Label case0 = codeBuilder.newLabel();
                            Label case1 = codeBuilder.newLabel();
                            
                            codeBuilder.tableswitch(0, 1, defaultLabel, 
                                java.util.List.of(
                                    SwitchCase.of(0, case0),
                                    SwitchCase.of(1, case1)
                                ));
                            
                            codeBuilder.labelBinding(case0);
                            codeBuilder.ldc("Case 0");
                            codeBuilder.areturn();
                            
                            codeBuilder.labelBinding(case1);
                            codeBuilder.ldc("Case 1");
                            codeBuilder.areturn();
                            
                            codeBuilder.labelBinding(defaultLabel);
                            codeBuilder.ldc("Default");
                            codeBuilder.areturn();
                        });
            });
            
            System.out.println("Generated class with switch: " + classBytes.length + " bytes");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
