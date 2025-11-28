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

import java.util.Map;

/**
 * Type metadata representing primitive types, reference types, and array types.
 * Replaces io.github.classgraph.BaseTypeSignature and ArrayTypeSignature.
 *
 * @author hal.hildebrand
 */
public class TypeMetadata {

    private static final Map<Character, String> PRIMITIVE_NAMES = Map.of(
        'V', "void",
        'Z', "boolean",
        'B', "byte",
        'C', "char",
        'S', "short",
        'I', "int",
        'J', "long",
        'F', "float",
        'D', "double"
    );

    private final String descriptor;
    private final boolean isPrimitive;
    private final boolean isArray;
    private final boolean isVoid;
    private final char primitiveChar;  // For primitives: I, J, D, F, Z, B, C, S, V
    private final String className;    // For reference types
    private final int arrayDimensions; // For arrays

    private TypeMetadata(String descriptor) {
        this.descriptor = descriptor;

        if (descriptor.isEmpty()) {
            throw new IllegalArgumentException("Empty descriptor");
        }

        char first = descriptor.charAt(0);

        if (first == '[') {
            // Array type
            this.isArray = true;
            this.isPrimitive = false;
            this.isVoid = false;
            this.primitiveChar = 0;

            // Count dimensions
            int dims = 0;
            while (dims < descriptor.length() && descriptor.charAt(dims) == '[') {
                dims++;
            }
            this.arrayDimensions = dims;

            // Get element type
            String elementDesc = descriptor.substring(dims);
            if (elementDesc.startsWith("L") && elementDesc.endsWith(";")) {
                this.className = elementDesc.substring(1, elementDesc.length() - 1).replace('/', '.');
            } else if (elementDesc.length() == 1 && PRIMITIVE_NAMES.containsKey(elementDesc.charAt(0))) {
                this.className = PRIMITIVE_NAMES.get(elementDesc.charAt(0));
            } else {
                this.className = elementDesc;
            }
        } else if (first == 'L' && descriptor.endsWith(";")) {
            // Reference type
            this.isArray = false;
            this.isPrimitive = false;
            this.isVoid = false;
            this.primitiveChar = 0;
            this.arrayDimensions = 0;
            this.className = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
        } else if (PRIMITIVE_NAMES.containsKey(first)) {
            // Primitive type
            this.isArray = false;
            this.isPrimitive = true;
            this.isVoid = (first == 'V');
            this.primitiveChar = first;
            this.arrayDimensions = 0;
            this.className = PRIMITIVE_NAMES.get(first);
        } else {
            throw new IllegalArgumentException("Invalid type descriptor: " + descriptor);
        }
    }

    /**
     * Parse a type from its descriptor string
     */
    public static TypeMetadata fromDescriptor(String descriptor) {
        return new TypeMetadata(descriptor);
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public boolean isReference() {
        return !isPrimitive && !isArray;
    }

    /**
     * Get the primitive type character (I, J, D, F, Z, B, C, S, V)
     * @return primitive char or 0 if not primitive
     */
    public char getPrimitiveChar() {
        return primitiveChar;
    }

    /**
     * Get the class name for reference types, or primitive name for primitives
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get array dimensions (0 if not an array)
     */
    public int getArrayDimensions() {
        return arrayDimensions;
    }

    /**
     * Get the element type for arrays
     */
    public TypeMetadata getElementType() {
        if (!isArray) {
            return this;
        }
        return fromDescriptor(descriptor.substring(1));
    }

    /**
     * Get the base element type for multi-dimensional arrays
     */
    public TypeMetadata getBaseElementType() {
        if (!isArray) {
            return this;
        }
        return fromDescriptor(descriptor.substring(arrayDimensions));
    }

    @Override
    public String toString() {
        if (isArray) {
            var base = getBaseElementType();
            return base.className + "[]".repeat(arrayDimensions);
        }
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeMetadata that)) return false;
        return descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }
}
