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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author hal.hildebrand
 */
public record MethodDescriptor(int access, String name, Type type, Type[] exceptions, String signature) {

    public MethodDescriptor(int access, String name, String descriptor, String[] exceptions, String signature) {
        this(access, name, Type.getMethodType(descriptor),
             exceptions == null ? null : (Type[]) Arrays.stream(exceptions).map(s -> Type.getType(s)).toArray(),
             signature);
    }

    public Type getReturnType() {
        return type.getReturnType();
    }

    public Type[] getArgumentTypes() {
        return type.getArgumentTypes();
    }

    @Override
    public String toString() {
        var arguments = new StringBuilder();
        AtomicBoolean frist = new AtomicBoolean(true);
        Arrays.stream(getArgumentTypes()).map(t -> t.getInternalName()).forEach(c -> {
            if (!frist.get()) {
                arguments.append(", ");
            }
            frist.set(false);
            arguments.append(c.replace('/', '.'));
        });
        return "%s %s %s(%s)".formatted(toAccess(), getReturnType().getInternalName().replace('/', '.'), name,
                                        arguments.toString());
    }

    private String toAccess() {
        var builder = new StringBuilder();
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            builder.append("public");
        } else if ((access & Opcodes.ACC_PRIVATE) != 0) {
            builder.append("private");
        } else if ((access & Opcodes.ACC_PROTECTED) != 0) {
            builder.append("protected");
        } else {
            throw new IllegalStateException("unknown access");
        }

        if ((access & Opcodes.ACC_STATIC) != 0) {
            builder.append(' ');
            builder.append("static");
        }

        if ((access & Opcodes.ACC_FINAL) != 0) {
            builder.append(' ');
            builder.append("final");
        }
        return builder.toString();
    }
}
