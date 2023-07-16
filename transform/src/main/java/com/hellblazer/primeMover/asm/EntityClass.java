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

import com.hellblazer.primeMover.soot.util.OpenAddressingSet.OpenSet;

/**
 * @author hal.hildebrand
 */
public class EntityClass extends Clazz {

    private final Set<Type> blocking = new OpenSet<>();
    private final Set<Type> events   = new OpenSet<>();

    public EntityClass(Type superClass, Type type, URL file) {
        super(superClass, type, file);
    }

    public void addBlocking(Type method) {
        blocking.add(method);
    }

    public void addEvent(Type method) {
        events.add(method);
    }

}
