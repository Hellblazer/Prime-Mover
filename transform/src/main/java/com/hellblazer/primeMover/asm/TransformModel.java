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
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import com.hellblazer.primeMover.soot.util.OpenAddressingSet.OpenSet;

/**
 * Provides the base metadata organizational model for the transformation
 * 
 * @author hal.hildebrand
 */
public class TransformModel {
    private final Map<Type, Clazz> classes  = new HashMap<>();
    private final Set<Type>        entities = new OpenSet<>();
    private final URLClassLoader   resources;

    public TransformModel(URLClassLoader resources) {
        this.resources = resources;
    }

    public Clazz get(Type clazz) {
        return classes.computeIfAbsent(clazz, t -> lookup(t));
    }

    void transform(EntityClass clazz) {
        entities.add(clazz.getType());
        classes.put(clazz.getType(), clazz);
    }

    private Clazz lookup(Type t) {
        var url = resources.findResource(t.getInternalName() + ".class");
        try (var fis = url.openStream()) {
            var reader = new ClassReader(fis);
            return new Clazz(this, Type.getObjectType(reader.getSuperName()), t, url);
        } catch (IOException e) {
            throw new IllegalStateException("Class files are always accessible!  Cannot open class: %s stream for: %s".formatted(t,
                                                                                                                                 url.toExternalForm()),
                                            e);
        }
    }
}
