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

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.util.*;

/**
 * Annotation metadata extracted from bytecode.
 * Replaces io.github.classgraph annotation handling.
 *
 * @author hal.hildebrand
 */
public class AnnotationMetadata {
    private final String name;
    private final Annotation annotation;
    private final Map<String, Object> values = new HashMap<>();

    public AnnotationMetadata(String name, Annotation annotation) {
        this.name = name;
        this.annotation = annotation;
        parseValues();
    }

    public String getName() {
        return name;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Get an annotation value by element name
     */
    public Object getValue(String elementName) {
        return values.get(elementName);
    }

    /**
     * Get the "value" element (common default)
     */
    public Object getValue() {
        return values.get("value");
    }

    /**
     * Get class references from the "value" element.
     * Used for @Entity(SomeInterface.class) style annotations.
     * @return List of class names referenced in the value
     */
    public List<String> getClassValues() {
        var value = values.get("value");
        if (value instanceof List<?> list) {
            return list.stream()
                       .filter(String.class::isInstance)
                       .map(String.class::cast)
                       .toList();
        } else if (value instanceof String s) {
            return List.of(s);
        }
        return List.of();
    }

    /**
     * Get a single class reference from the "value" element
     */
    public String getClassValue() {
        var classes = getClassValues();
        return classes.isEmpty() ? null : classes.getFirst();
    }

    public Map<String, Object> getAllValues() {
        return Collections.unmodifiableMap(values);
    }

    private void parseValues() {
        for (AnnotationElement element : annotation.elements()) {
            var elementName = element.name().stringValue();
            var elementValue = parseAnnotationValue(element.value());
            values.put(elementName, elementValue);
        }
    }

    private Object parseAnnotationValue(AnnotationValue av) {
        return switch (av) {
            case AnnotationValue.OfString s -> s.stringValue();
            case AnnotationValue.OfInt i -> i.intValue();
            case AnnotationValue.OfLong l -> l.longValue();
            case AnnotationValue.OfDouble d -> d.doubleValue();
            case AnnotationValue.OfFloat f -> f.floatValue();
            case AnnotationValue.OfBoolean b -> b.booleanValue();
            case AnnotationValue.OfByte b -> b.byteValue();
            case AnnotationValue.OfChar c -> c.charValue();
            case AnnotationValue.OfShort s -> s.shortValue();
            case AnnotationValue.OfClass c -> {
                // Convert Lcom/example/Foo; to com.example.Foo
                var desc = c.classSymbol().descriptorString();
                if (desc.startsWith("L") && desc.endsWith(";")) {
                    yield desc.substring(1, desc.length() - 1).replace('/', '.');
                }
                yield desc;
            }
            case AnnotationValue.OfEnum e -> e.constantName().stringValue();
            case AnnotationValue.OfAnnotation a -> new AnnotationMetadata(
                a.annotation().className().stringValue(), a.annotation());
            case AnnotationValue.OfArray arr -> {
                var list = new ArrayList<>();
                for (var v : arr.values()) {
                    list.add(parseAnnotationValue(v));
                }
                yield list;
            }
        };
    }

    @Override
    public String toString() {
        return "@" + name + values;
    }
}
