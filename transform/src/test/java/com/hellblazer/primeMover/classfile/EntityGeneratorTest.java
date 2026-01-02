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
import org.junit.jupiter.api.Test;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hal.hildebrand
 */
public class EntityGeneratorTest {
    @Test
    public void smokin() throws Exception {
        var scanner = new ClassScanner()
            .addClasspathEntry(Path.of("target/test-classes"))
            .addClasspathEntry(Path.of("target/classes"))
            .scan();
        var transform = new SimulationTransform(scanner);

        final var name = "com.hellblazer.primeMover.classfile.testClasses.MyTest";
        var generator = transform.generatorOf(name);
        assertNotNull(generator);
        var cw = generator.generate();
        assertNotNull(cw);
        final var bytes = cw;
        assertNotNull(bytes);

        // Validate bytecode using ClassFile API (parsing validates structure)
        ClassModel classModel = ClassFile.of().parse(bytes);
        assertNotNull(classModel);
        assertEquals(name.replace('.', '/'), classModel.thisClass().asInternalName());
        assertTrue(classModel.methods().size() > 0, "Generated class should have methods");

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
    public void template() throws Exception {
        final var name = "com.hellblazer.primeMover.classfile.testClasses.Template";

        // Read and validate class file using ClassFile API
        try (var is = getClass().getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class")) {
            assertNotNull(is, "Template class should exist");
            byte[] bytes = is.readAllBytes();
            ClassModel classModel = ClassFile.of().parse(bytes);
            assertNotNull(classModel);
            assertEquals(name.replace('.', '/'), classModel.thisClass().asInternalName());
        }
    }
}
