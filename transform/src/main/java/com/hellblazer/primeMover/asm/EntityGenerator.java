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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.commons.TableSwitchGenerator;

import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.soot.util.OpenAddressingSet.OpenSet;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

/**
 * @author hal.hildebrand
 */
public class EntityGenerator {
    private static final String INVOKE                    = "__invoke";
    private static final String METHOD_REMAP_KEY_TEMPLATE = "%s.%s%s";
    private static final String REMAPPED_TEMPLATE         = "gen$%s";
    private static final String SIGNATURE_FOR             = "__signatureFor";

    private final ClassInfo                clazz;
    private final Map<Integer, MethodInfo> mapped;
    private final Set<MethodInfo>          remapped;

    public EntityGenerator(ClassInfo clazz, Set<MethodInfo> events) {
        this.clazz = clazz;
        mapped = new HashMap<Integer, MethodInfo>();
        remapped = new OpenSet<MethodInfo>();
        var key = 0;
        for (var mi : events.stream().sorted().toList()) {
            mapped.put(key++, mi);
            final boolean declared = clazz.getDeclaredMethodInfo(mi.getName())
                                          .stream()
                                          .filter(m -> mi.equals(m))
                                          .findFirst()
                                          .isPresent();
            if (declared) {
                remapped.add(mi);
            }
        }
    }

    public ClassWriter generate() throws MalformedURLException, IOException {
        try (var is = clazz.getResource().open()) {
            final var classReader = new ClassReader(is);
            ClassWriter cw = new ClassWriter(classReader, 0);
            var renamer = renames(cw);
            classReader.accept(renamer, ClassReader.EXPAND_FRAMES);
            renamer.visit(clazz.getClassfileMajorVersion(), clazz.getModifiers(), clazz.getName().replace('.', '/'),
                          clazz.getTypeSignatureStr(),
                          clazz.getSuperclass() == null ? Object.class.getCanonicalName().replace('.', '/')
                                                        : clazz.getSuperclass().getName().replace('.', '/'),
                          clazz.getInterfaces()
                               .stream()
                               .map(ci -> ci.getName().replace('.', '/'))
                               .toList()
                               .toArray(new String[0]));
            generateInvoke(renamer);
            generateSignatureFor(renamer);
            return cw;
        }
    }

    private TableSwitchGenerator eventSwitch(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
                var mi = mapped.get(key);
                if (mi == null) {
                    throw new IllegalArgumentException("no key: " + key + " in mapped");
                }
                adapter.loadThis();
                int i = 0;
                for (var pi : mi.getParameterInfo()) {
                    adapter.loadArg(1);
                    adapter.push(i);
                    adapter.arrayLoad(Type.getType(Object.class));
                    adapter.checkCast(Type.getObjectType(pi.getTypeDescriptor().toString().replace('.', '/')));
                }
                if (remapped.contains(mi)) {
                    adapter.invokeVirtual(Type.getObjectType(clazz.getName().replace('.', '/')),
                                          new Method(REMAPPED_TEMPLATE.formatted(mi.getName()),
                                                     mi.getTypeDescriptorStr()));
                } else {
                    adapter.invokeVirtual(Type.getType(clazz.getSuperclass().getName().replace('.', '/')),
                                          Method.getMethod(mi.toStringWithSimpleNames()));
                }
                if (mi.getTypeSignature() == null) {
                    adapter.push((String) null);
                }
                adapter.returnValue();
            }

            @Override
            public void generateDefault() {
                adapter.throwException(Type.getType(IllegalArgumentException.class), "unknown event key");
            }
        };
    }

    private void generateInvoke(ClassVisitor cv) {
        Method m;
        java.lang.reflect.Method method;
        try {
            method = EntityReference.class.getMethod(INVOKE, int.class, Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INVOKE), e);
        }
        try {
            m = Method.getMethod(method);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(INVOKE), e);
        }
        GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, m.getDescriptor(),
                                                   Arrays.stream(method.getExceptionTypes())
                                                         .map(t -> Type.getType(t))
                                                         .toList()
                                                         .toArray(new Type[0]),
                                                   cv);
        mg.loadArg(0);
        var keys = mapped.keySet().stream().mapToInt(i -> (int) i).sorted().toArray();
        mg.tableSwitch(keys, eventSwitch(mg));
        mg.endMethod();
    }

    private void generateSignatureFor(ClassVisitor cv) {
        Method m;
        java.lang.reflect.Method method;
        try {
            method = EntityReference.class.getMethod(SIGNATURE_FOR, int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SIGNATURE_FOR), e);
        }
        try {
            m = Method.getMethod(EntityReference.class.getMethod(SIGNATURE_FOR, int.class));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '%s' method".formatted(SIGNATURE_FOR), e);
        }
        GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, m.getDescriptor(),
                                                   Arrays.stream(method.getExceptionTypes())
                                                         .map(t -> Type.getType(t))
                                                         .toList()
                                                         .toArray(new Type[0]),
                                                   cv);
        var keys = mapped.keySet().stream().mapToInt(i -> (int) i).sorted().toArray();
        mg.loadArg(0);
        mg.tableSwitch(keys, signatureSwitch(mg));
        mg.loadThis();
        mg.endMethod();
    }

    private ClassVisitor renames(ClassVisitor cv) {
        var mappings = new HashMap<String, String>();
        for (var mi : remapped) {
            mappings.put(METHOD_REMAP_KEY_TEMPLATE.formatted(clazz.getName().replace('.', '/'), mi.getName(),
                                                             mi.getTypeDescriptorStr()),
                         REMAPPED_TEMPLATE.formatted(mi.getName()));
        }
        return new ClassRemapper(cv, new SimpleRemapper(mappings));
    }

    private TableSwitchGenerator signatureSwitch(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
                var mi = mapped.get(key);
                if (mi == null) {
                    throw new IllegalArgumentException("no key: " + key + " in mapped");
                }
                adapter.push(mi.toStringWithSimpleNames());
                adapter.returnValue();
            }

            @Override
            public void generateDefault() {
                adapter.throwException(Type.getType(IllegalArgumentException.class), "unknown event key");
            }
        };
    }
}
