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

import java.util.Set;

import org.objectweb.asm.Type;

/**
 * @author hal.hildebrand
 */
public class EntityClass extends Clazz {

    final Set<MethodDescriptor> blocking;
    final Set<Type>             eventInterfaces;
    final Set<MethodDescriptor> events;
    final Set<MethodDescriptor> nonEvents;

    public EntityClass(Clazz clazz, Set<Type> eventInterfaces, Set<MethodDescriptor> blocking,
                       Set<MethodDescriptor> events, Set<MethodDescriptor> nonEvents) {
        super(clazz);
        this.eventInterfaces = eventInterfaces;
        this.blocking = blocking;
        this.events = events;
        this.nonEvents = nonEvents;
        model.transform(this);
    }

    public void addBlocking(MethodDescriptor method) {
        blocking.add(method);
    }

    public void addEvent(MethodDescriptor method) {
        events.add(method);
    }

    public void addEventInterface(Type iFace) {
        eventInterfaces.add(iFace);
    }

    public void addNonEvent(MethodDescriptor method) {
        nonEvents.add(method);
    }

}
