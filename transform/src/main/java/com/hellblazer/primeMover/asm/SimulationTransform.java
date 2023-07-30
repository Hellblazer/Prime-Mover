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
package com.hellblazer.primeMover.asm;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.AllMethodsMarker;
import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Event;
import com.hellblazer.primeMover.annotations.NonEvent;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.asm.OpenAddressingSet.OpenSet;
import com.hellblazer.primeMover.runtime.Kairos;

import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ClassInfoList.ClassInfoFilter;
import io.github.classgraph.ScanResult;

/**
 * @author hal.hildebrand
 */
public class SimulationTransform implements Closeable {

    public static Set<ClassInfo> getEntityInterfaces(ClassInfo ci) {
        Set<ClassInfo> returned = Arrays.stream((Object[]) ci.getAnnotationInfo(Entity.class)
                                                             .getParameterValues(true)
                                                             .getValue("value"))
                                        .map(o -> (AnnotationClassRef) o)
                                        .map(acr -> acr.getClassInfo())
                                        .collect(Collectors.toCollection(() -> new OpenSet<ClassInfo>()));
        ci.getSuperclasses()
          .stream()
          .flatMap(s -> Arrays.stream((Object[]) s.getAnnotationInfo(Entity.class)
                                                  .getParameterValues(true)
                                                  .getValue("value")))
          .map(o -> (AnnotationClassRef) o)
          .map(acr -> acr.getClassInfo())
          .forEach(c -> returned.add(c));
        return returned;
    }

    private final ClassInfo     allMethodsMarker;
    private final ClassInfoList entities;
    private final ScanResult    scan;

    public SimulationTransform(ClassGraph graph) {
        graph.enableAllInfo().enableInterClassDependencies().enableExternalClasses().ignoreMethodVisibility();
        scan = graph.scan();
        entities = scan.getClassesWithAnnotation(Entity.class.getCanonicalName());
        allMethodsMarker = scan.getClassInfo(AllMethodsMarker.class.getCanonicalName());
    }

    @Override
    public void close() throws IOException {
        if (scan != null) {
            scan.close();
        }
    }

    public EntityGenerator generatorOf(String classname) {
        var entity = entities.get(classname);
        if (entity == null) {
            return null;
        }
        return generateEntity(entity);
    }

    public Map<ClassInfo, EntityGenerator> generators() {
        return entities.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(ClassInfo classInfo) {
                return !classInfo.hasAnnotation(Transformed.class);
            }
        }).stream().collect(Collectors.toMap(ci -> ci, ci -> generateEntity(ci)));
    }

    @SuppressWarnings("deprecation")
    public Map<ClassInfo, byte[]> transformed() {
        var transformed = new HashMap<ClassInfo, byte[]>();
        var map = new HashMap<String, String>();
        map.put(Kronos.class.getCanonicalName().replace('.', '/'), Kairos.class.getCanonicalName().replace('.', '/'));
        var apiRemapper = new SimpleRemapper(map);
        scan.getAllClasses()
            .filter(ci -> !entities.contains(ci))
            .filter(ci -> !ci.getPackageName().equals(Kairos.class.getPackageName()))
            .filter(ci -> !ci.getPackageName().equals(Kronos.class.getPackageName()))
            .filter(ci -> !ci.getPackageName().equals(Entity.class.getPackageName()))
            .filter(ci -> !ci.getPackageName().equals(EntityGenerator.class.getPackageName()))
            .filter(ci -> !ci.getPackageName().startsWith(ClassVisitor.class.getPackageName()))
            .filter(ci -> !ci.getPackageName().startsWith("org.junit"))
            .filter(ci -> !ci.getPackageName().startsWith(ClassInfo.class.getPackageName()))
            .forEach(ci -> {
                if (ci.getResource() != null) {
                    try (var is = ci.getResource().open()) {
                        final var classReader = new ClassReader(is);
                        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        classReader.accept(new ClassRemapper(cw, apiRemapper), ClassReader.EXPAND_FRAMES);
                        final var written = cw.toByteArray();
                        if (!written.equals(classReader.b))
                            transformed.put(ci, written);
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable to read class bytes: " + ci, e);
                    }
                }
            });
        generators().forEach((ci, generator) -> {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassReader classReader;
            try {
                classReader = new ClassReader(generator.generate().toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException("unable to transform: " + ci, e);
            }
            classReader.accept(new ClassRemapper(cw, apiRemapper), ClassReader.EXPAND_FRAMES);
            final var written = cw.toByteArray();
            transformed.put(ci, written);
        });

        return transformed;
    }

    private EntityGenerator generateEntity(ClassInfo ci) {
        var entIFaces = getEntityInterfaces(ci);
        var allPublic = entIFaces.contains(allMethodsMarker);
        var interfaces = ci.getInterfaces();
        var implemented = new OpenSet<ClassInfo>();
        entIFaces.forEach(c -> {
            if (interfaces.contains(c)) {
                implemented.add(c);
            }
        });
        var events = implemented.stream()
                                .flatMap(intf -> intf.getMethodInfo().stream())
                                .map(mi -> ci.getMethodInfo(mi.getName())
                                             .stream()
                                             .filter(m -> m.getTypeDescriptorStr().equals(mi.getTypeDescriptorStr()))
                                             .findFirst()
                                             .orElseGet(() -> ci.getSuperclasses()
                                                                .stream()
                                                                .flatMap(c -> c.getMethodInfo(mi.getName()).stream())
                                                                .filter(m -> m.getTypeDescriptorStr()
                                                                              .equals(mi.getTypeDescriptorStr()))
                                                                .findFirst()
                                                                .orElse(null)))
                                .filter(mi -> mi != null)
                                .filter(mi -> !mi.hasAnnotation(NonEvent.class))
                                .collect(Collectors.toCollection(() -> new OpenSet<>()));
        ci.getDeclaredMethodInfo()
          .stream()
          .filter(m -> !m.hasAnnotation(NonEvent.class))
          .filter(m -> allPublic ? m.isPublic() : m.hasAnnotation(Blocking.class) || m.hasAnnotation(Event.class))
          .forEach(mi -> events.add(mi));
        return new EntityGenerator(ci, events);
    }
}
