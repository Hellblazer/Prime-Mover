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

/**
 * Parameter metadata for method parameters.
 * Replaces io.github.classgraph.MethodParameterInfo.
 *
 * @author hal.hildebrand
 */
public class ParameterMetadata {
    private final int index;
    private final TypeMetadata type;

    public ParameterMetadata(int index, TypeMetadata type) {
        this.index = index;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public TypeMetadata getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
