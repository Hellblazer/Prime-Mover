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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import testClasses.Entity1Impl;

/**
 * @author hal.hildebrand
 */
public class EntityScannerTest {

    @Test
    public void smokin() throws Exception {
        var type = Type.getType(Entity1Impl.class);
        var superType = Type.getType(Entity1Impl.class.getSuperclass());
        var url = Entity1Impl.class.getClassLoader()
                                   .getResource(Entity1Impl.class.getName().replace('.', '/') + ".class");
        var clazz = new Clazz(superType, type, url);
        try (var is = url.openStream()) {
            var reader = new ClassReader(is);
            var scanner = new EntityScanner(Opcodes.ASM9, clazz);
            reader.accept(scanner, ClassReader.SKIP_CODE);
            var applied = (EntityClass) scanner.applied();
            assertNotNull(applied);
            assertEquals(0, applied.events.size());
            assertEquals(0, applied.nonEvents.size());
            assertEquals(1, applied.blocking.size());
            assertEquals(1, applied.eventInterfaces.size());
        }
    }
}
