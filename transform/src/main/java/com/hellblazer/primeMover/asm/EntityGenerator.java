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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.MethodRemapper;
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
    private static final String METHOD_REMAP_KEY_TEMPLATE = "%s.%s.%s";
    private static final String REMAPPED_TEMPLATE         = "gen$%s";

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

    public void generate(ClassWriter cw) {
        generateInvoke(cw);
    }

    public ClassVisitor renames(ClassVisitor cw) {
        var mappings = new HashMap<String, String>();
        for (var mi : remapped) {
            mappings.put(METHOD_REMAP_KEY_TEMPLATE.formatted(clazz.getName(), mi.getName(), mi.getTypeDescriptorStr()),
                         REMAPPED_TEMPLATE.formatted(mi.getName()));
        }
        return new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                             String[] exceptions) {
                return new MethodRemapper(super.visitMethod(access, name, descriptor, signature, exceptions),
                                          new SimpleRemapper(mappings));
            }
        };
    }

    private TableSwitchGenerator eventSwitch(GeneratorAdapter adapter) {
        return new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
                var mi = mapped.get(key);
                if (mi == null) {
                    throw new IllegalArgumentException("no key: " + key + " in mapped");
                }
                adapter.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
                int i = 0;
                for (var pi : mi.getParameterInfo()) {
                    adapter.loadArg(1);
                    adapter.push(i);
                    adapter.arrayLoad(Type.getType(Object.class));
                    adapter.checkCast(Type.getType(pi.toStringWithSimpleNames()));
                }
                if (remapped.contains(mi)) {
                    adapter.invokeVirtual(Type.getType(clazz.getName()),
                                          new Method(REMAPPED_TEMPLATE.formatted(mi.getName()),
                                                     mi.getTypeDescriptorStr()));
                } else {
                    adapter.invokeVirtual(Type.getType(clazz.getSuperclass().getName()),
                                          Method.getMethod(mi.toString()));
                }
                if ("void".equals(mi.getTypeSignature().getResultType().toString())) {
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

    private void generateInvoke(ClassWriter cw) {
        Method m;
        try {
            m = Method.getMethod(EntityReference.class.getMethod("__invoke", int.class, Object[].class));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Cannot get '__invoke' method", e);
        }
        GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, null);
        var keys = mapped.keySet().stream().mapToInt(i -> (int) i).sorted().toArray();
        mg.tableSwitch(keys, eventSwitch(mg));
        mg.endMethod();
    }
}
