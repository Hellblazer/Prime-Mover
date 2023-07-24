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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.V1_1;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import io.github.classgraph.ClassGraph;

/**
 * @author hal.hildebrand
 */
public class EntityGeneratorTest {
    @Test
    public void smokin() throws Exception {
        var transform = new SimulationTransform(new ClassGraph().acceptPackages("com.hellblazer.primeMover.asm.testClasses"));

        final var name = "com.hellblazer.primeMover.asm.testClasses.MyTest";
        EntityGenerator generator = transform.generatorOf(name);
        assertNotNull(generator);
        var cw = generator.transformed();
        assertNotNull(cw);
        final var bytes = cw.toByteArray();
        assertNotNull(bytes);

        ClassReader reader = new ClassReader(bytes);

        TraceClassVisitor visitor = new TraceClassVisitor(null, new PrintWriter(System.out, true));
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        CheckClassAdapter.verify(reader, true, new PrintWriter(System.out, true));

        var loader = new ClassLoader(getClass().getClassLoader()) {
            {
                {
                    defineClass(name, ByteBuffer.wrap(bytes), null);
                }
            }
        };
        var clazz = loader.loadClass(name);
        assertNotNull(clazz);
//        clazz.getConstructor().newInstance();
    }

    @Test
    public void tableSwitch() throws Exception {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final var name = "Example";
        cw.visit(V1_1, ACC_PUBLIC, name, null, "java/lang/Object", null);

        Method m = Method.getMethod("void <init> ()");
        GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
        mg.loadThis();
        mg.invokeConstructor(Type.getType(Object.class), m);
        mg.returnValue();
        mg.visitMaxs(1, 1);
        mg.endMethod();

        m = Method.getMethod("void main (String[])");
        mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
        mg.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
        mg.push("Hello world!");
        mg.invokeVirtual(Type.getType(PrintStream.class), Method.getMethod("void println (String)"));
        mg.returnValue();
        mg.visitMaxs(2, 1);
        mg.endMethod();

        m = Method.getMethod("Integer test (int)");
        final var gen = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);

        gen.loadArg(0);
        gen.tableSwitch(new int[] { 0, 1, 2 }, new TableSwitchGenerator() {

            @Override
            public void generateCase(int key, Label end) {
                gen.push(key);
                gen.invokeStatic(Type.getType(Integer.class), Method.getMethod("Integer valueOf (int)"));
                gen.returnValue();
            }

            @Override
            public void generateDefault() {
                gen.push(0);
                gen.invokeStatic(Type.getType(Integer.class), Method.getMethod("Integer valueOf (int)"));
                gen.returnValue();
            }
        });
        gen.visitMaxs(1, 2);
        gen.endMethod();

        cw.visitEnd();

        final var bytes = cw.toByteArray();
        assertNotNull(bytes);

        TraceClassVisitor visitor = new TraceClassVisitor(null, new PrintWriter(System.out, true));
        ClassReader reader = new ClassReader(bytes);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        CheckClassAdapter.verify(reader, true, new PrintWriter(System.out, true));
        var loader = new ClassLoader(getClass().getClassLoader()) {
            {
                {
                    defineClass(name, ByteBuffer.wrap(bytes), null);
                }
            }
        };
        var clazz = loader.loadClass(name);
        assertNotNull(clazz);
        clazz.getConstructor().newInstance();
    }

    @Test
    public void template() throws Exception {

        final var name = "com.hellblazer.primeMover.asm.testClasses.Template";

        ClassReader reader;
        try (var is = getClass().getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class")) {
            reader = new ClassReader(is);
        }

        TraceClassVisitor visitor = new TraceClassVisitor(null, new PrintWriter(System.out, true));
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        CheckClassAdapter.verify(reader, true, new PrintWriter(System.out, true));
    }
}
