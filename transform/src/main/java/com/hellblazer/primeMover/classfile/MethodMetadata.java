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
package com.hellblazer.primeMover.classfile;

import java.lang.classfile.ClassFile;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.constant.MethodTypeDesc;
import java.util.*;

/**
 * Method metadata extracted from bytecode using the ClassFile API.
 * Replaces io.github.classgraph.MethodInfo.
 *
 * @author hal.hildebrand
 */
public class MethodMetadata {
    private final ClassMetadata declaringClass;
    private final MethodModel methodModel;
    private final String name;
    private final String descriptor;
    private final MethodTypeDesc methodTypeDesc;
    private final int accessFlags;
    private final Map<String, AnnotationMetadata> annotations = new HashMap<>();
    private final List<ParameterMetadata> parameters;
    private final TypeMetadata returnType;

    public MethodMetadata(ClassMetadata declaringClass, MethodModel methodModel) {
        this.declaringClass = declaringClass;
        this.methodModel = methodModel;
        this.name = methodModel.methodName().stringValue();
        this.methodTypeDesc = methodModel.methodTypeSymbol();
        this.descriptor = methodTypeDesc.descriptorString();
        this.accessFlags = methodModel.flags().flagsMask();

        // Parse parameters from descriptor
        this.parameters = parseParameters();
        this.returnType = TypeMetadata.fromDescriptor(methodTypeDesc.returnType().descriptorString());

        // Scan annotations
        scanAnnotations();
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public MethodTypeDesc getMethodTypeDesc() {
        return methodTypeDesc;
    }

    public MethodModel getMethodModel() {
        return methodModel;
    }

    public ClassMetadata getDeclaringClass() {
        return declaringClass;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public boolean isPublic() {
        return (accessFlags & ClassFile.ACC_PUBLIC) != 0;
    }

    public boolean isProtected() {
        return (accessFlags & ClassFile.ACC_PROTECTED) != 0;
    }

    public boolean isPrivate() {
        return (accessFlags & ClassFile.ACC_PRIVATE) != 0;
    }

    public boolean isStatic() {
        return (accessFlags & ClassFile.ACC_STATIC) != 0;
    }

    public boolean isAbstract() {
        return (accessFlags & ClassFile.ACC_ABSTRACT) != 0;
    }

    public boolean isFinal() {
        return (accessFlags & ClassFile.ACC_FINAL) != 0;
    }

    public boolean hasAnnotation(String annotationName) {
        return annotations.containsKey(annotationName);
    }

    public boolean hasAnnotation(Class<?> annotationClass) {
        return annotations.containsKey(annotationClass.getName());
    }

    public AnnotationMetadata getAnnotation(String annotationName) {
        return annotations.get(annotationName);
    }

    public AnnotationMetadata getAnnotation(Class<?> annotationClass) {
        return annotations.get(annotationClass.getName());
    }

    public Collection<AnnotationMetadata> getAnnotations() {
        return annotations.values();
    }

    public List<ParameterMetadata> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public TypeMetadata getReturnType() {
        return returnType;
    }

    public boolean isVoid() {
        return returnType.isVoid();
    }

    private void scanAnnotations() {
        for (var attr : methodModel.attributes()) {
            if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
                for (var ann : rva.annotations()) {
                    var annName = ann.className().stringValue();
                    // Convert Lcom/example/Ann; to com.example.Ann
                    var className = annName.substring(1, annName.length() - 1).replace('/', '.');
                    annotations.put(className, new AnnotationMetadata(className, ann));
                }
            }
        }
    }

    private List<ParameterMetadata> parseParameters() {
        var result = new ArrayList<ParameterMetadata>();
        var paramTypes = methodTypeDesc.parameterList();
        for (int i = 0; i < paramTypes.size(); i++) {
            var paramDesc = paramTypes.get(i).descriptorString();
            result.add(new ParameterMetadata(i, TypeMetadata.fromDescriptor(paramDesc)));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodMetadata that)) return false;
        return Objects.equals(declaringClass.getName(), that.declaringClass.getName())
               && Objects.equals(name, that.name)
               && Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringClass.getName(), name, descriptor);
    }

    @Override
    public String toString() {
        return declaringClass.getName() + "." + name + descriptor;
    }
}
