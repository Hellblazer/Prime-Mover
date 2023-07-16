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
import java.net.URL;
import java.util.Set;
import java.util.function.Consumer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

/**
 * Memoization for a class reference
 * 
 * @author hal.hildebrand
 */
public class Clazz {
    protected final URL            file;
    protected final TransformModel model;
    protected final Type           superClass;
    protected final Type           type;

    public Clazz(Clazz clazz) {
        this(clazz.model, clazz.superClass, clazz.type, clazz.file);
    }

    public Clazz(TransformModel model, Type superClass, Type type, URL file) {
        this.superClass = superClass;
        this.type = type;
        this.file = file;
        this.model = model;
    }

    public Clazz asEntity(Set<Type> eventInterfaces, Set<MethodDescriptor> blocking, Set<MethodDescriptor> events,
                          Set<MethodDescriptor> nonEvents) {
        return new EntityClass(this, eventInterfaces, blocking, events, nonEvents);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Clazz other = (Clazz) obj;
        return type.equals(other.type);
    }

    public Type getSuperClass() {
        return superClass;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public boolean isEntity() {
        return false;
    }

    public void process(Consumer<ClassReader> processor) {
        try (var fis = file.openStream()) {
            processor.accept(new ClassReader(fis));
        } catch (IOException e) {
            throw new IllegalStateException("Class files are always accessible!  Cannot open class: %s stream for: %s".formatted(type,
                                                                                                                                 file.toExternalForm()),
                                            e);
        }
    }
}
