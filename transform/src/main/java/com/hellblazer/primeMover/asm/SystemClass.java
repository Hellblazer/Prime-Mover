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

import java.net.URL;
import java.util.Set;

import org.objectweb.asm.Type;

/**
 * @author hal.hildebrand
 */
public class SystemClass extends Clazz {

    public SystemClass(TransformModel model, Type superClass, Type type, URL file) {
        super(model, superClass, type, file);
    }

    @Override
    public Clazz asEntity(Set<Type> eventInterfaces, Set<MethodDescriptor> blocking, Set<MethodDescriptor> events,
                          Set<MethodDescriptor> nonEvents) {
        throw new UnsupportedOperationException("System classes cannot be Entities: %s".formatted(type.getInternalName()
                                                                                                      .replace('/',
                                                                                                               '.')));
    }

    @Override
    public boolean isEntity() {
        return false;
    }

}
