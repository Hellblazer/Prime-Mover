package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.annotations.*;

import java.lang.classfile.Annotation;
import java.lang.classfile.ClassModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.util.Set;

/**
 * ClassFile API-based annotation scanner to detect transformation annotations
 *
 * @author hal.hildebrand
 **/
public class AnnotationScanner {

    // Annotation descriptors in class file format (L<classname>;)
    private static final String ENTITY_DESC      = "L%s;".formatted(
    Entity.class.getCanonicalName().replace('.', '/'));
    private static final String TRANSFORMED_DESC = "L%s;".formatted(
    Transformed.class.getCanonicalName().replace('.', '/'));
    private static final String EVENT_DESC       = "L%s;".formatted(
    Event.class.getCanonicalName().replace('.', '/'));
    private static final String BLOCKING_DESC    = "L%s;".formatted(
    Blocking.class.getCanonicalName().replace('.', '/'));
    private static final String ALL_METHODS_DESC = "L%s;".formatted(
    AllMethodsMarker.class.getCanonicalName().replace('.', '/'));

    private static final Set<String> TXFM_ANNOTATIONS = Set.of(ENTITY_DESC, EVENT_DESC, BLOCKING_DESC,
                                                                ALL_METHODS_DESC);

    private boolean transform             = false;
    private boolean previouslyTransformed = false;

    public AnnotationScanner() {
    }

    public static boolean isTransformAnnotation(String desc) {
        return TXFM_ANNOTATIONS.contains(desc);
    }

    public boolean isPreviouslyTransformed() {
        return previouslyTransformed;
    }

    public boolean isTransform() {
        return transform;
    }

    public void scan(ClassModel classModel) {
        // Check class-level annotations
        for (var attr : classModel.attributes()) {
            if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                for (Annotation ann : rva.annotations()) {
                    var annClassName = ann.className().stringValue();
                    if (TRANSFORMED_DESC.equals(annClassName)) {
                        previouslyTransformed = true;
                    } else if (isTransformAnnotation(annClassName)) {
                        transform = true;
                    }
                }
            }
        }

        // Check field-level annotations
        for (FieldModel field : classModel.fields()) {
            for (var attr : field.attributes()) {
                if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                    for (Annotation ann : rva.annotations()) {
                        if (isTransformAnnotation(ann.className().stringValue())) {
                            transform = true;
                            return;
                        }
                    }
                }
            }
        }

        // Check method-level annotations
        for (MethodModel method : classModel.methods()) {
            for (var attr : method.attributes()) {
                if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                    for (Annotation ann : rva.annotations()) {
                        if (isTransformAnnotation(ann.className().stringValue())) {
                            transform = true;
                            return;
                        }
                    }
                }
            }
        }
    }
}