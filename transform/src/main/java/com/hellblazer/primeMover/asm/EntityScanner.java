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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.hellblazer.primeMover.Blocking;
import com.hellblazer.primeMover.Entity;
import com.hellblazer.primeMover.Event;
import com.hellblazer.primeMover.NonEvent;
import com.hellblazer.primeMover.soot.util.OpenAddressingSet.OpenSet;

/**
 * Basic scanning for <code>Enity
 * 
 * @author hal.hildebrand
 */
public class EntityScanner extends ClassVisitor {
    public class EntityAnnotationScanner extends AnnotationVisitor {

        protected EntityAnnotationScanner(int api) {
            super(api);
        }

        @Override
        public void visit(String name, Object value) {
            var type = (Type) value;
            eventInterfaces.add(type);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new EntityAnnotationScanner(Opcodes.ASM9);
        }
    }

    public class EntityMethodScanner extends MethodVisitor {

        private final MethodDescriptor method;

        public EntityMethodScanner(int api, MethodDescriptor method) {
            super(api);
            this.method = method;
        }

        public EntityMethodScanner(int api, MethodVisitor methodVisitor, MethodDescriptor method) {
            super(api, methodVisitor);
            this.method = method;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (!visible) {
                return null;
            }
            final var type = Type.getType(descriptor);

            if (EVENT_ANNOTATION_TYPE.equals(type)) {
                events.add(method);
            } else if (NON_EVENT_ANNOTATION_TYPE.equals(type)) {
                nonEvents.add(method);
            } else if (BLOCKING_ANNOTATION_TYPE.equals(type)) {
                blocking.add(method);
            }
            return null;
        }
    }

    private static final Type BLOCKING_ANNOTATION_TYPE  = Type.getType(Blocking.class);
    private static final Type ENTITY_ANNOTATION_TYPE    = Type.getType(Entity.class);
    private static final Type EVENT_ANNOTATION_TYPE     = Type.getType(Event.class);
    private static final Type NON_EVENT_ANNOTATION_TYPE = Type.getType(NonEvent.class);

    private final Clazz                 base;
    private final Set<MethodDescriptor> blocking        = new OpenSet<>();
    private final Set<Type>             eventInterfaces = new OpenSet<>();
    private final Set<MethodDescriptor> events          = new OpenSet<>();
    private boolean                     isEntity        = false;
    private final Set<MethodDescriptor> nonEvents       = new OpenSet<>();

    public EntityScanner(int api, ClassVisitor classVisitor, Clazz clazz) {
        super(api, classVisitor);
        this.base = clazz;
    }

    public EntityScanner(int api, Clazz clazz) {
        super(api);
        this.base = clazz;
    }

    public Clazz applied() {
        return isEntity ? base.asEntity(eventInterfaces, blocking, events, nonEvents) : base;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (!visible) {
            return null;
        }
        if (ENTITY_ANNOTATION_TYPE.equals(Type.getType(descriptor))) {
            isEntity = true;
            return new EntityAnnotationScanner(Opcodes.ASM9);
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        var method = new MethodDescriptor(access, name, descriptor, exceptions, signature);
        return new EntityMethodScanner(Opcodes.ASM9, method);
    }
}
