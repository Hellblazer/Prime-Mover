package com.hellblazer.primeMover.asm;

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.nio.file.*;

/**
 * Exploration of the Java 24 ClassFile API (JEP 484)
 * 
 * This class demonstrates the key features and capabilities of the new
 * ClassFile API introduced in Java 24.
 */
public class ClassFileAPIExploration {
    
    public static void main(String[] args) throws Exception {
        exploreBasicAPIConcepts();
        generateSimpleClass();
        transformExistingClass();
        parseClassFile();
    }
    
    /**
     * Demonstrates the basic concepts and APIs available in the ClassFile API
     */
    public static void exploreBasicAPIConcepts() {
        System.out.println("=== ClassFile API Basic Concepts ===");
        
        // Create a ClassFile instance - the main entry point
        ClassFile cf = ClassFile.of();
        System.out.println("ClassFile instance created: " + cf.getClass().getName());
        
        // ClassFile API uses three main abstractions:
        System.out.println("Main abstractions:");
        System.out.println("1. Elements - Immutable descriptions of class file parts");
        System.out.println("2. Builders - For constructing/transforming class file elements");
        System.out.println("3. Transforms - Functions for element transformation");
    }
    
    /**
     * Demonstrates generating a simple class using the ClassFile API
     */
    public static void generateSimpleClass() throws Exception {
        System.out.println("\n=== Generating Simple Class ===");
        
        ClassFile cf = ClassFile.of();
        
        // Class descriptor for our generated class
        ClassDesc generatedClassDesc = ClassDesc.of("com.example.GeneratedClass");
        ClassDesc objectDesc = ClassDesc.of("java.lang.Object");
        ClassDesc stringDesc = ClassDesc.of("java.lang.String");
        
        // Generate class bytecode
        byte[] classBytes = cf.build(generatedClassDesc, classBuilder -> {
            classBuilder
                // Set class access flags
                .withFlags(ClassFile.ACC_PUBLIC)
                
                // Add a private String field
                .withField("name", stringDesc, ClassFile.ACC_PRIVATE)
                
                // Add constructor
                .withMethodBody("<init>", 
                    MethodTypeDesc.of(ConstantDescs.CD_void, stringDesc),
                    ClassFile.ACC_PUBLIC,
                    codeBuilder -> {
                        codeBuilder
                            .aload(0) // load 'this'
                            .invokespecial(objectDesc, "<init>", 
                                MethodTypeDesc.of(ConstantDescs.CD_void))
                            .aload(0) // load 'this'
                            .aload(1) // load parameter 'name'
                            .putfield(generatedClassDesc, "name", stringDesc)
                            .return_();
                    })
                
                // Add getName() method
                .withMethodBody("getName", 
                    MethodTypeDesc.of(stringDesc),
                    ClassFile.ACC_PUBLIC,
                    codeBuilder -> {
                        codeBuilder
                            .aload(0) // load 'this'
                            .getfield(generatedClassDesc, "name", stringDesc)
                            .areturn();
                    });
        });
        
        // Save to file (optional)
        // Files.write(Paths.get("GeneratedClass.class"), classBytes);
        
        System.out.println("Generated class bytecode: " + classBytes.length + " bytes");
        
        // Load and test the generated class
        ClassLoader loader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name.equals("com.example.GeneratedClass")) {
                    return defineClass(name, classBytes, 0, classBytes.length);
                }
                return super.findClass(name);
            }
        };
        
        try {
            Class<?> generatedClass = loader.loadClass("com.example.GeneratedClass");
            Object instance = generatedClass.getConstructor(String.class).newInstance("Hello ClassFile API!");
            Object result = generatedClass.getMethod("getName").invoke(instance);
            System.out.println("Generated class method result: " + result);
        } catch (Exception e) {
            System.out.println("Error testing generated class: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrates transforming an existing class
     */
    public static void transformExistingClass() throws Exception {
        System.out.println("\n=== Transforming Existing Class ===");
        
        ClassFile cf = ClassFile.of();
        
        // Get bytecode of this class
        String thisClassName = ClassFileAPIExploration.class.getName().replace('.', '/') + ".class";
        byte[] originalBytes;
        try (var is = ClassFileAPIExploration.class.getClassLoader().getResourceAsStream(thisClassName)) {
            originalBytes = is.readAllBytes();
        }
        
        // Transform the class - example: add a field
        byte[] transformedBytes = cf.transformClass(cf.parse(originalBytes), 
            ClassTransform.endHandler(classBuilder -> {
                classBuilder.withField("addedField", ClassDesc.of("java.lang.String"), 
                    ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC);
            })
        );
        
        System.out.println("Original class size: " + originalBytes.length + " bytes");
        System.out.println("Transformed class size: " + transformedBytes.length + " bytes");
    }
    
    /**
     * Demonstrates parsing and inspecting an existing class file
     */
    public static void parseClassFile() throws Exception {
        System.out.println("\n=== Parsing Class File ===");
        
        ClassFile cf = ClassFile.of();
        
        // Get bytecode of this class
        String thisClassName = ClassFileAPIExploration.class.getName().replace('.', '/') + ".class";
        byte[] classBytes;
        try (var is = ClassFileAPIExploration.class.getClassLoader().getResourceAsStream(thisClassName)) {
            classBytes = is.readAllBytes();
        }
        
        // Parse the class
        ClassModel classModel = cf.parse(classBytes);
        
        System.out.println("Class name: " + classModel.thisClass().asInternalName());
        System.out.println("Super class: " + classModel.superclass().map(c -> c.asInternalName()).orElse("none"));
        System.out.println("Access flags: " + Integer.toHexString(classModel.flags().flagsMask()));
        
        // List fields
        System.out.println("Fields:");
        for (FieldModel field : classModel.fields()) {
            System.out.println("  " + field.fieldName().stringValue() + 
                " : " + field.fieldType().stringValue());
        }
        
        // List methods
        System.out.println("Methods:");
        for (MethodModel method : classModel.methods()) {
            System.out.println("  " + method.methodName().stringValue() + 
                " : " + method.methodType().stringValue());
            
            // If method has code, analyze instructions
            method.code().ifPresent(codeModel -> {
                System.out.println("    Instructions: " + codeModel.elementList().size());
                // Could iterate through instructions here for detailed analysis
            });
        }
    }
}