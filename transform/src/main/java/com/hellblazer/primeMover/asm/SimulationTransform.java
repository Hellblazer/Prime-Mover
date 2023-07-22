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
import java.util.Set;
import java.util.stream.Collectors;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.AllMethodsMarker;
import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.annotations.Event;
import com.hellblazer.primeMover.annotations.NonEvent;
import com.hellblazer.primeMover.annotations.Transformed;
import com.hellblazer.primeMover.runtime.Kairos;
import com.hellblazer.primeMover.soot.util.OpenAddressingSet.OpenSet;

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
    private final ClassInfo     blockingAnnotation;
    private final ClassInfoList entities;
    private final ClassInfo     entityAnnotation;
    private final ClassInfo     eventAnnotation;
    private final ClassInfo     kairos;
    private final ClassInfo     kronos;
    private final ClassInfo     nonEventAnnotation;
    private final ScanResult    scan;
    private final ClassInfo     transformedAnnotation;

    public SimulationTransform(ClassGraph graph) {
        graph.enableAllInfo().enableInterClassDependencies().enableExternalClasses().ignoreMethodVisibility();
        scan = graph.scan();
        entityAnnotation = scan.getClassInfo(Entity.class.getCanonicalName());
        assert entityAnnotation != null : "cannot find " + Entity.class;
        blockingAnnotation = scan.getClassInfo(Blocking.class.getCanonicalName());
        assert blockingAnnotation != null : "cannot find " + Blocking.class;
        eventAnnotation = scan.getClassInfo(Event.class.getCanonicalName());
        assert eventAnnotation != null : "cannot find " + Event.class;
        nonEventAnnotation = scan.getClassInfo(NonEvent.class.getCanonicalName());
        assert nonEventAnnotation != null : "cannot find " + NonEvent.class;
        kairos = scan.getClassInfo(Kairos.class.getCanonicalName());
        assert kairos != null : "cannot find " + Kairos.class;
        kronos = scan.getClassInfo(Kronos.class.getCanonicalName());
        assert kronos != null : "cannot find " + Kronos.class;
        allMethodsMarker = scan.getClassInfo(AllMethodsMarker.class.getCanonicalName());
        assert allMethodsMarker != null : "cannot find " + AllMethodsMarker.class;
        transformedAnnotation = scan.getClassInfo(Transformed.class.getCanonicalName());
        assert transformedAnnotation != null : "cannot find " + Transformed.class;
        entities = scan.getClassesWithAnnotation(Entity.class.getCanonicalName());

    }

    @Override
    public void close() throws IOException {
        if (scan != null) {
            scan.close();
        }
    }

    public void generate() {
        entities.filter(new ClassInfoFilter() {

            @Override
            public boolean accept(ClassInfo classInfo) {
                return !classInfo.hasAnnotation(Transformed.class);
            }
        }).forEach(ci -> generateEntity(ci));
    }

    public ClassInfo getBlockingAnnotation() {
        return blockingAnnotation;
    }

    public ClassInfo getEntityAnnotation() {
        return entityAnnotation;
    }

    public ClassInfo getEventAnnotation() {
        return eventAnnotation;
    }

    public ClassInfo getKairos() {
        return kairos;
    }

    public ClassInfo getKronos() {
        return kronos;
    }

    public ClassInfo getNonEventAnnotation() {
        return nonEventAnnotation;
    }

    private void generateEntity(ClassInfo ci) {
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
        System.out.println(ci.getName());
        System.out.println(entIFaces);
        System.out.println(events.stream().map(mi -> '\n' + mi.toString()).toList());
        System.out.println();
        var generator = new EntityGenerator(ci, events);
        generator.renames(null);
    }

}
