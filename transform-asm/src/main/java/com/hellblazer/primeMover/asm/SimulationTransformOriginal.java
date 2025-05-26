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

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.*;
import com.hellblazer.primeMover.asm.OpenAddressingSet.OpenSet;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.*;
import io.github.classgraph.ClassInfoList.ClassInfoFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hal.hildebrand
 */
public class SimulationTransformOriginal implements Closeable {

    private static final String     ALL_METHODS_MARKER = AllMethodsMarker.class.getCanonicalName();
    private final        ScanResult scan;

    private String transformTimestamp = java.time.Instant.now().toString();

    public SimulationTransformOriginal(ClassGraph graph) {
        this(
        graph.enableAllInfo().enableInterClassDependencies().enableExternalClasses().ignoreMethodVisibility().scan());
    }

    public SimulationTransformOriginal(ScanResult scan) {
        this.scan = scan;
    }

    public static Set<ClassInfo> getEntityInterfaces(ClassInfo ci) {
        var arr = (Object[]) ci.getAnnotationInfo(Entity.class).getParameterValues(true).getValue("value");
        if (arr == null) {
            arr = new Object[0];
        }
        Set<ClassInfo> returned = Arrays.stream(arr)
                                        .map(o -> (AnnotationClassRef) o)
                                        .map(acr -> acr.getClassInfo())
                                        .collect(Collectors.toCollection(() -> new OpenSet<ClassInfo>()));
        ci.getSuperclasses()
          .stream()
          .filter(s -> s.getAnnotationInfo(Entity.class) != null)
          .flatMap(s -> Arrays.stream((Object[]) s.getAnnotationInfo(Entity.class)
                                                  .getParameterValues(true)
                                                  .getValue("value")))
          .map(o -> (AnnotationClassRef) o)
          .map(acr -> acr.getClassInfo())
          .forEach(c -> returned.add(c));
        return returned;
    }

    @Override
    public void close() throws IOException {
        if (scan != null) {
            scan.close();
        }
    }

    public EntityGeneratorOriginal generatorOf(String classname) {
        return generatorOf(classname, new ClassInfoFilter() {

            @Override
            public boolean accept(ClassInfo classInfo) {
                return true;
            }

        });
    }

    public EntityGeneratorOriginal generatorOf(String classname, ClassInfoFilter selector) {
        var entities = scan.getClassesWithAnnotation(Entity.class.getCanonicalName()).filter(selector);
        var entity = entities.get(classname);
        if (entity == null) {
            return null;
        }
        return generateEntity(entity);
    }

    public Map<ClassInfo, EntityGeneratorOriginal> generators() {
        return generators(new ClassInfoFilter() {

            @Override
            public boolean accept(ClassInfo classInfo) {
                return true;
            }
        });
    }

    public Map<ClassInfo, EntityGeneratorOriginal> generators(ClassInfoFilter selector) {
        return generators(scan.getClassesWithAnnotation(Entity.class.getCanonicalName()).filter(selector));
    }

    public Map<ClassInfo, EntityGeneratorOriginal> generators(ClassInfoList entities) {
        return entities.filter(new ClassInfoFilter() {
            @Override
            public boolean accept(ClassInfo classInfo) {
                return !classInfo.hasAnnotation(Transformed.class);
            }
        }).stream().collect(Collectors.toMap(ci -> ci, ci -> generateEntity(ci)));
    }

    /**
     * Gets the current transform timestamp being used for @Transformed annotations.
     *
     * @return The current timestamp string
     */
    public String getTransformTimestamp() {
        return transformTimestamp;
    }

    /**
     * Sets the timestamp to be used in @Transformed annotations for all generated entities. This allows for
     * deterministic bytecode generation when the same timestamp is used.
     *
     * @param timestamp The timestamp string to use (typically ISO-8601 format)
     */
    public void setTransformTimestamp(String timestamp) {
        this.transformTimestamp = timestamp;
    }

    public Map<ClassInfo, byte[]> transformed() {
        return transformed(new ClassInfoFilter() {
            @Override
            public boolean accept(ClassInfo classInfo) {
                return true;
            }
        });
    }

    public Map<ClassInfo, byte[]> transformed(ClassInfoFilter selector) {
        var entities = scan.getClassesWithAnnotation(Entity.class.getCanonicalName()).filter(selector);
        var transformed = new HashMap<ClassInfo, byte[]>();
        var map = new HashMap<String, String>();
        map.put(Kronos.class.getCanonicalName().replace('.', '/'), Kairos.class.getCanonicalName().replace('.', '/'));
        var apiRemapper = new SimpleRemapper(map);
        final var allClasses = scan.getAllClasses();
        var kronos = allClasses.get(Kronos.class.getCanonicalName());
        if (kronos == null) {
            throw new IllegalStateException("Cannot find %s".formatted(Kronos.class.getSimpleName()));
        }
        allClasses.filter(selector)
                  .filter(ci -> !ci.getPackageName().equals(Kairos.class.getPackageName()))
                  .filter(ci -> !ci.getPackageName().equals(Kronos.class.getPackageName()))
                  .filter(ci -> !ci.getPackageName().equals(Entity.class.getPackageName()))
                  .filter(ci -> !ci.getPackageName().equals(EntityGeneratorOriginal.class.getPackageName()))
                  .filter(ci -> !ci.getPackageName().startsWith(ClassVisitor.class.getPackageName()))
                  .filter(ci -> !ci.getPackageName().startsWith("org.junit"))
                  .filter(ci -> !ci.getPackageName().startsWith(ClassInfo.class.getPackageName()))
                  .filter(ci -> !entities.contains(ci))
                  .filter(ci -> !ci.hasAnnotation(Transformed.class))
                  .filter(ci -> ci.getClassDependencies().contains(kronos))
                  .forEach(ci -> {
                      if (ci.getResource() != null) {
                          try (var is = ci.getResource().open()) {
                              final var classReader = new ClassReader(is);
                              ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                              classReader.accept(new ClassRemapper(cw, apiRemapper), ClassReader.EXPAND_FRAMES);
                              final var written = cw.toByteArray();
                              @SuppressWarnings("deprecation")
                              final var b = classReader.b;
                              if (!written.equals(b)) {
                                  transformed.put(ci, written);
                              }
                          } catch (IOException e) {
                              throw new IllegalStateException("Unable to read class bytes: " + ci, e);
                          }
                      }
                  });
        generators(entities).forEach((ci, generator) -> {
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

    private EntityGeneratorOriginal generateEntity(ClassInfo ci) {
        var entIFaces = getEntityInterfaces(ci);
        var allPublic = entIFaces.stream().anyMatch(c -> c.getName().equals(ALL_METHODS_MARKER));
        var interfaces = ci.getInterfaces();
        var implemented = new OpenSet<ClassInfo>();
        entIFaces.forEach(c -> {
            if (interfaces.contains(c)) {
                implemented.add(c);
            }
        });
        if (entIFaces.isEmpty() && !allPublic) {
            throw new IllegalStateException(
            "Apparently was not able to get the AllMethodsMarker annotation class info");
        }
        var events = implemented.stream()
                                .flatMap(intf -> intf.getMethodInfo().stream())
                                .map(mi -> ci.getMethodInfo(mi.getName())
                                             .stream()
                                             .filter(m -> m.getTypeDescriptorStr()
                                                           .equals(mi.getTypeDescriptorStr()))
                                             .findFirst()
                                             .orElseGet(() -> ci.getSuperclasses()
                                                                .stream()
                                                                .flatMap(c -> c.getMethodInfo(mi.getName())
                                                                               .stream())
                                                                .filter(m -> m.getTypeDescriptorStr()
                                                                              .equals(mi.getTypeDescriptorStr()))
                                                                .findFirst()
                                                                .orElse(null)))
                                .filter(mi -> mi != null)
                                .filter(mi -> !mi.hasAnnotation(NonEvent.class))
                                .collect(Collectors.toCollection(() -> new OpenSet<>()));
        ci.getDeclaredMethodInfo()
          .stream()
          .filter(m -> !m.isStatic())
          .filter(m -> !m.hasAnnotation(NonEvent.class))
          .filter(m -> allPublic ? m.isPublic() : m.hasAnnotation(Blocking.class) || m.hasAnnotation(Event.class))
          .forEach(mi -> events.add(mi));
        return new EntityGeneratorOriginal(ci, events, transformTimestamp);
    }
}
