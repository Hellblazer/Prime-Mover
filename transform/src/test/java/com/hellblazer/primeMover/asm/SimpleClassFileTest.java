package com.hellblazer.primeMover.asm;

import java.lang.classfile.*;
import java.lang.constant.*;

/**
 * Simple test to explore what's available in the ClassFile API
 */
public class SimpleClassFileTest {
    
    public static void main(String[] args) {
        try {
            exploreClassFileAPI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void exploreClassFileAPI() {
        System.out.println("=== Exploring ClassFile API ===");
        
        // Create a ClassFile instance
        ClassFile cf = ClassFile.of();
        System.out.println("ClassFile created: " + cf.getClass().getName());
        
        // Try to see what methods are available
        Class<?> classFileClass = cf.getClass();
        System.out.println("Available methods:");
        for (var method : classFileClass.getMethods()) {
            if (method.getDeclaringClass() == classFileClass || 
                method.getDeclaringClass().getName().startsWith("java.lang.classfile")) {
                System.out.println("  " + method.getName() + "(" + 
                    java.util.Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(java.util.stream.Collectors.joining(", ")) + ")");
            }
        }
        
        // Try basic class generation
        ClassDesc objectDesc = ClassDesc.of("java.lang.Object");
        ClassDesc testClassDesc = ClassDesc.of("TestClass");
        
        try {
            byte[] classBytes = cf.build(testClassDesc, classBuilder -> {
                classBuilder.withFlags(ClassFile.ACC_PUBLIC);
            });
            System.out.println("Generated basic class: " + classBytes.length + " bytes");
        } catch (Exception e) {
            System.out.println("Error generating class: " + e.getMessage());
        }
    }
}