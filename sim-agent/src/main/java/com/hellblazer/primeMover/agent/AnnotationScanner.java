package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.annotations.*;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Set;

public class AnnotationScanner extends ClassVisitor {

    public static final String ENTITY_TYPE_SIGNATURE = "L%s;".formatted(
    Entity.class.getCanonicalName().replace('.', '/'));

    public static final String TXFMD_TYPE_SIGNATURE = "L%s;".formatted(
    Transformed.class.getCanonicalName().replace('.', '/'));

    public static final String EVENT_TYPE_SIGNATURE = "L%s;".formatted(
    Event.class.getCanonicalName().replace('.', '/'));

    public static final String BLOCKING_TYPE_SIGNATURE = "L%s;".formatted(
    Blocking.class.getCanonicalName().replace('.', '/'));

    public static final String ALL_METHODS_TYPE_SIGNATURE = "L%s;".formatted(
    AllMethodsMarker.class.getCanonicalName().replace('.', '/'));

    private static final Set<String> TXFM_ANNOTATIONS;

    static {
        TXFM_ANNOTATIONS = Set.of(ENTITY_TYPE_SIGNATURE, EVENT_TYPE_SIGNATURE, BLOCKING_TYPE_SIGNATURE,
                                  ALL_METHODS_TYPE_SIGNATURE);
    }

    private boolean transform             = false;
    private boolean previouslyTransformed = false;

    public AnnotationScanner(int api) {
        super(api);
    }

    public static boolean isTransformAnnotation(String desc) {
        return TXFM_ANNOTATIONS.contains(desc);
    }

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            FileInputStream in = new FileInputStream(new File(arg));
            ClassReader cr = new ClassReader(in);
            cr.accept(new AnnotationScanner(Opcodes.ASM9), 0);
        }
    }

    public boolean isPreviouslyTransformed() {
        return previouslyTransformed;
    }

    public boolean isTransform() {
        return transform;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (TXFMD_TYPE_SIGNATURE.equals(desc)) {
            previouslyTransformed = true;
            return null;
        }
        if (isTransformAnnotation(desc)) {
            transform = true;
            return null;
        }
        return new AnnotationMethodsScanner();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return new FieldAnnotationScanner();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodAnnotationScanner();
    }

    class AnnotationMethodsScanner extends AnnotationVisitor {

        AnnotationMethodsScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            if (isTransformAnnotation(desc)) {
                transform = true;
                return null;
            }
            return super.visitAnnotation(name, desc);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new AnnotationMethodsArrayValueScanner();
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            super.visitEnum(name, desc, value);
        }

        static class AnnotationMethodsArrayValueScanner extends AnnotationVisitor {
            AnnotationMethodsArrayValueScanner() {
                super(Opcodes.ASM9);
            }

            @Override
            public void visit(String name, Object value) {
                super.visit(name, value);
            }
        }
    }

    class FieldAnnotationScanner extends FieldVisitor {
        FieldAnnotationScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (isTransformAnnotation(desc)) {
                transform = true;
                return null;
            }
            return new AnnotationMethodsScanner();
        }
    }

    class MethodAnnotationScanner extends MethodVisitor {
        MethodAnnotationScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (isTransformAnnotation(desc)) {
                transform = true;
                return null;
            }
            return super.visitAnnotation(desc, visible);
        }
    }
}
