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

import com.hellblazer.primeMover.ControllerImpl;
import com.hellblazer.primeMover.classfile.testClasses.Foo;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author hal.hildebrand
 */
public class EntityGeneratorTest {
    @Test
    public void smokin() throws Exception {
        var transform = new SimulationTransform(
        new ClassGraph().acceptPackages("com.hellblazer"));

        //        final var name = "testClasses.ContinuationThroughputImpl";
        //        final var name = "testClasses.HelloWorld";
        //        final var name = "testClasses.SubEntity";
        final var name = "com.hellblazer.primeMover.classfile.testClasses.MyTest";
        var generator = transform.generatorOf(name);
        assertNotNull(generator);
        var cw = generator.generate();
        assertNotNull(cw);
        final var bytes = cw;
        assertNotNull(bytes);

        ClassReader reader = new ClassReader(bytes);
        final var out = new PrintStream(new ByteArrayOutputStream()); // System.out;
        final PrintWriter printWriter = new PrintWriter(out, true);
        TraceClassVisitor visitor = new TraceClassVisitor(null, printWriter);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        CheckClassAdapter.verify(reader, printWriter != null, printWriter);

        var loader = new ClassLoader(getClass().getClassLoader()) {
            {
                {
                    defineClass(name, ByteBuffer.wrap(bytes), null);
                }
            }
        };
        var clazz = loader.loadClass(name);
        assertNotNull(clazz);
        final var controller = new ControllerImpl();
        Kairos.setController(controller);
        final var constructor = clazz.getConstructor();
        assertNotNull(constructor);
        var entity = constructor.newInstance();
        assertNotNull(entity);
        assertTrue(entity instanceof Foo);
        var foo = (Foo) entity;
        foo.bar();
        assertEquals(1, controller.eventQueue.size());
        while (controller.send())
            ;
        assertEquals(0, controller.eventQueue.size());
    }

    @Test
    public void tableSwitch() throws Exception {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final var name = "Example";
        cw.visit(Opcodes.V17, ACC_PUBLIC, name, null, "java/lang/Object", null);

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
        mg.visitMaxs(0, 0);
        mg.endMethod();

        m = Method.getMethod("Integer test (int)");
        final var gen = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);

        gen.loadArg(0);
        gen.tableSwitch(new int[] { 0, 1, 2 }, new TableSwitchGenerator() {

            @Override
            public void generateCase(int key, Label end) {
                gen.visitFrame(Opcodes.F_NEW, 2, new Object[] { Type.getObjectType("Example").getInternalName(),
                                                                Opcodes.INTEGER }, 0, new Object[] {});
                gen.push("foo");
                gen.visitFrame(Opcodes.F_NEW, 2, new Object[] { Type.getObjectType("Example").getInternalName(),
                                                                Opcodes.INTEGER }, 1, new Object[] { Type.getType(
                String.class).getInternalName() });
                gen.push(key);
                gen.visitFrame(Opcodes.F_NEW, 2, new Object[] { Type.getObjectType("Example").getInternalName(),
                                                                Opcodes.INTEGER }, 2, new Object[] { Type.getType(
                String.class).getInternalName(), Opcodes.INTEGER });
                gen.invokeStatic(Type.getType(Integer.class), Method.getMethod("Integer parseInt (String, int)"));
                gen.returnValue();
            }

            @Override
            public void generateDefault() {
                gen.visitFrame(Opcodes.F_NEW, 2, new Object[] { Type.getObjectType("Example").getInternalName(),
                                                                Opcodes.INTEGER }, 0, new Object[] {});
                gen.push(0);
                gen.visitFrame(Opcodes.F_NEW, 2, new Object[] { Type.getObjectType("Example").getInternalName(),
                                                                Opcodes.INTEGER }, 1, new Object[] { Opcodes.INTEGER });
                gen.invokeStatic(Type.getType(Integer.class), Method.getMethod("Integer valueOf (int)"));
                gen.returnValue();
            }
        });
        gen.visitMaxs(0, 0);
        gen.endMethod();

        cw.visitEnd();

        final var bytes = cw.toByteArray();
        assertNotNull(bytes);

        final var out = new PrintStream(new ByteArrayOutputStream()); // System.out;
        final PrintWriter printWriter = new PrintWriter(out, true);
        TraceClassVisitor visitor = new TraceClassVisitor(null, printWriter);
        ClassReader reader = new ClassReader(bytes);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        CheckClassAdapter.verify(reader, printWriter != null, printWriter);
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

        final var name = "com.hellblazer.primeMover.classfile.testClasses.Template";

        ClassReader reader;
        try (var is = getClass().getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class")) {
            reader = new ClassReader(is);
        }

        TraceClassVisitor visitor = new TraceClassVisitor(null, null);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        CheckClassAdapter.verify(reader, false, new PrintWriter(System.out, false));
    }
}
