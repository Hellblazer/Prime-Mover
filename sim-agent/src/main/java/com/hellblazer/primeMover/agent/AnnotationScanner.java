/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.primeMover.agent;

import com.hellblazer.primeMover.annotations.*;

import java.lang.classfile.Annotation;
import java.lang.classfile.ClassModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.util.Set;

/**
 * ClassFile API-based annotation scanner to detect transformation annotations.
 * <p>
 * Scans class, field, and method level annotations to determine if a class
 * requires entity transformation or has already been transformed.
 *
 * @author hal.hildebrand
 */
public class AnnotationScanner {

    // Annotation descriptors in class file format (L<classname>;)
    private static final String ENTITY_DESC = descriptorOf(Entity.class);
    private static final String TRANSFORMED_DESC = descriptorOf(Transformed.class);
    private static final String EVENT_DESC = descriptorOf(Event.class);
    private static final String BLOCKING_DESC = descriptorOf(Blocking.class);
    private static final String ALL_METHODS_DESC = descriptorOf(AllMethodsMarker.class);

    private static final Set<String> TRANSFORM_ANNOTATIONS = Set.of(
            ENTITY_DESC, EVENT_DESC, BLOCKING_DESC, ALL_METHODS_DESC
    );

    private boolean requiresTransformation = false;
    private boolean previouslyTransformed = false;

    /**
     * Convert a class to its descriptor format (L<internal-name>;)
     */
    private static String descriptorOf(Class<?> clazz) {
        return "L" + clazz.getName().replace('.', '/') + ";";
    }

    /**
     * Check if the given descriptor is a transformation annotation
     */
    public static boolean isTransformAnnotation(String desc) {
        return TRANSFORM_ANNOTATIONS.contains(desc);
    }

    /**
     * @return true if the class was previously transformed (has @Transformed annotation)
     */
    public boolean isPreviouslyTransformed() {
        return previouslyTransformed;
    }

    /**
     * @return true if the class requires entity transformation
     */
    public boolean isTransform() {
        return requiresTransformation;
    }

    /**
     * Scan the class model for transformation annotations
     */
    public void scan(ClassModel classModel) {
        // Check class-level annotations
        scanClassAnnotations(classModel);

        // Early exit if already determined
        if (previouslyTransformed || requiresTransformation) {
            return;
        }

        // Check field-level annotations
        scanFieldAnnotations(classModel);

        if (requiresTransformation) {
            return;
        }

        // Check method-level annotations
        scanMethodAnnotations(classModel);
    }

    private void scanClassAnnotations(ClassModel classModel) {
        for (var attr : classModel.attributes()) {
            if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                for (Annotation ann : rva.annotations()) {
                    var desc = ann.className().stringValue();
                    if (TRANSFORMED_DESC.equals(desc)) {
                        previouslyTransformed = true;
                        return; // No need to continue if already transformed
                    } else if (isTransformAnnotation(desc)) {
                        requiresTransformation = true;
                    }
                }
            }
        }
    }

    private void scanFieldAnnotations(ClassModel classModel) {
        for (FieldModel field : classModel.fields()) {
            for (var attr : field.attributes()) {
                if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                    for (Annotation ann : rva.annotations()) {
                        if (isTransformAnnotation(ann.className().stringValue())) {
                            requiresTransformation = true;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void scanMethodAnnotations(ClassModel classModel) {
        for (MethodModel method : classModel.methods()) {
            for (var attr : method.attributes()) {
                if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                    for (Annotation ann : rva.annotations()) {
                        if (isTransformAnnotation(ann.className().stringValue())) {
                            requiresTransformation = true;
                            return;
                        }
                    }
                }
            }
        }
    }
}
