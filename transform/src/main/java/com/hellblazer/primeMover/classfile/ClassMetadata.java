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

import com.hellblazer.primeMover.classfile.OpenAddressingSet.OpenSet;

import java.lang.classfile.*;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.constantpool.*;
import java.util.*;

/**
 * Class metadata extracted from bytecode using the ClassFile API.
 * Replaces io.github.classgraph.ClassInfo.
 *
 * @author hal.hildebrand
 */
public class ClassMetadata {
    private final String name;
    private final String internalName;
    private final ClassModel classModel;
    private final byte[] originalBytes;

    // Class hierarchy
    private final String superclassName;
    private final List<String> interfaceNames;
    private ClassMetadata superclass;
    private final List<ClassMetadata> resolvedInterfaces = new ArrayList<>();

    // Annotations
    private final Map<String, AnnotationMetadata> annotations = new HashMap<>();

    // Methods
    private final List<MethodMetadata> methods = new ArrayList<>();
    private final List<MethodMetadata> declaredMethods = new ArrayList<>();

    // Dependencies (classes referenced in constant pool)
    private final Set<String> dependencies = new OpenSet<>();

    // Access flags
    private final int accessFlags;

    public ClassMetadata(String name, ClassModel classModel, byte[] originalBytes) {
        this.name = name;
        this.internalName = name.replace('.', '/');
        this.classModel = classModel;
        this.originalBytes = originalBytes;
        this.accessFlags = classModel.flags().flagsMask();

        // Extract superclass
        this.superclassName = classModel.superclass()
                                        .map(ce -> ce.asInternalName().replace('/', '.'))
                                        .orElse(null);

        // Extract interfaces
        this.interfaceNames = classModel.interfaces().stream()
                                        .map(ce -> ce.asInternalName().replace('/', '.'))
                                        .toList();

        // Scan annotations
        scanAnnotations();

        // Scan methods
        scanMethods();

        // Scan dependencies from constant pool
        scanDependencies();
    }

    public String getName() {
        return name;
    }

    public String getInternalName() {
        return internalName;
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public byte[] getOriginalBytes() {
        return originalBytes;
    }

    public String getSuperclassName() {
        return superclassName;
    }

    public ClassMetadata getSuperclass() {
        return superclass;
    }

    void setSuperclass(ClassMetadata superclass) {
        this.superclass = superclass;
    }

    public List<String> getInterfaceNames() {
        return interfaceNames;
    }

    public List<ClassMetadata> getInterfaces() {
        return Collections.unmodifiableList(resolvedInterfaces);
    }

    void addResolvedInterface(ClassMetadata iface) {
        resolvedInterfaces.add(iface);
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

    public List<MethodMetadata> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public List<MethodMetadata> getDeclaredMethods() {
        return Collections.unmodifiableList(declaredMethods);
    }

    public List<MethodMetadata> getMethods(String methodName) {
        return methods.stream()
                      .filter(m -> m.getName().equals(methodName))
                      .toList();
    }

    public List<MethodMetadata> getDeclaredMethods(String methodName) {
        return declaredMethods.stream()
                              .filter(m -> m.getName().equals(methodName))
                              .toList();
    }

    public Set<String> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public boolean isInterface() {
        return (accessFlags & ClassFile.ACC_INTERFACE) != 0;
    }

    public boolean isAbstract() {
        return (accessFlags & ClassFile.ACC_ABSTRACT) != 0;
    }

    public boolean isPublic() {
        return (accessFlags & ClassFile.ACC_PUBLIC) != 0;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public String getPackageName() {
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : "";
    }

    /**
     * Get all superclasses (excluding java.lang.Object)
     */
    public List<ClassMetadata> getSuperclasses() {
        var result = new ArrayList<ClassMetadata>();
        var current = superclass;
        while (current != null && !current.getName().equals("java.lang.Object")) {
            result.add(current);
            current = current.superclass;
        }
        return result;
    }

    private void scanAnnotations() {
        for (var attr : classModel.attributes()) {
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

    private void scanMethods() {
        for (MethodModel methodModel : classModel.methods()) {
            var methodMeta = new MethodMetadata(this, methodModel);
            methods.add(methodMeta);
            if (!methodMeta.getName().equals("<init>") && !methodMeta.getName().equals("<clinit>")) {
                declaredMethods.add(methodMeta);
            }
        }
    }

    private void scanDependencies() {
        for (PoolEntry entry : classModel.constantPool()) {
            if (entry instanceof ClassEntry ce) {
                var depName = ce.asInternalName().replace('/', '.');
                if (!depName.equals(name)) {
                    dependencies.add(depName);
                }
            } else if (entry instanceof Utf8Entry ue) {
                // Also check for class references in descriptors
                var value = ue.stringValue();
                if (value.startsWith("L") && value.endsWith(";")) {
                    var depName = value.substring(1, value.length() - 1).replace('/', '.');
                    if (!depName.equals(name)) {
                        dependencies.add(depName);
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMetadata that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "ClassMetadata{" + name + '}';
    }
}
