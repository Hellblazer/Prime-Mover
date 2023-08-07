/**
 * (C) Copyright 2008 Chiral Behaviors, LLC. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chiralbehaviors.janus;

import static com.chiralbehaviors.janus.CompositeClassGenerator.GENERATED_COMPOSITE_SUFFIX;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class CompositeAssembler<T> {
    static class CompositeClassLoader extends ClassLoader {
        public CompositeClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String compositeName, byte[] definition) {
            return defineClass(compositeName, definition, 0, definition.length);
        }
    }

    private static ClassLoader getLoader(Class<?> composite) {
        return composite.getClassLoader();
    }

    protected final Class<T>               generated;
    protected final CompositeClassLoader   loader;
    protected final Map<Class<?>, Integer> mixInMap;

    protected final Class<?>[] mixIns;

    public CompositeAssembler(Class<T> composite) {
        this(composite, getLoader(composite));
    }

    @SuppressWarnings("unchecked")
    public CompositeAssembler(Class<T> composite, final ClassLoader parentLoader) {
        if (!composite.isInterface()) {
            throw new IllegalArgumentException("Supplied composite class is not an interface: " + composite);
        }
        CompositeClassGenerator generator = new CompositeClassGenerator(composite);
        mixInMap = generator.getMixInTypeMapping();
        mixIns = generator.getMixInTypes();
        Class<T> clazz;
        loader = new CompositeClassLoader(parentLoader);

        try {
            clazz = (Class<T>) composite.getClassLoader()
                                        .loadClass(composite.getCanonicalName() + GENERATED_COMPOSITE_SUFFIX);
        } catch (ClassNotFoundException e) {
            clazz = (Class<T>) loader.define(generator.getGeneratedClassName(), generator.generateClassBits());
        }
        generated = clazz;
    }

    public T construct(Object... mixInInstances) {
        if (mixInInstances == null) {
            throw new IllegalArgumentException("supplied mixin instances must not be null");
        }
        if (mixInInstances.length != mixIns.length) {
            throw new IllegalArgumentException("wrong number of arguments supplied");
        }
        Object[] arguments = new Object[mixIns.length];
        for (Object mixIn : mixInInstances) {
            for (Map.Entry<Class<?>, Integer> mapping : mixInMap.entrySet()) {
                if (mapping.getKey().isAssignableFrom(mixIn.getClass())) {
                    arguments[mapping.getValue()] = mixIn;
                }
            }
        }
        T instance = constructInstance(arguments);
        inject(instance, arguments);
        return instance;
    }

    protected T constructInstance(Object[] arguments) {
        Constructor<T> constructor = getConstructor();
        try {
            return constructor.newInstance(arguments);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Illegal arguments in constructing composite", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unexpected error in constructing composite", e.getTargetException());
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot instantiate composite", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access constructor for composite", e);
        }
    }

    protected Constructor<T> getConstructor() {
        Constructor<T> constructor;
        try {
            constructor = generated.getConstructor(mixIns);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find constructor on generated composite class", e);
        }
        return constructor;
    }

    protected void inject(Object value, Field field, Object instance, Class<?> clazz) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Field: " + field + " is not a part of class: " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field: " + field + " on class: " + clazz, e);
        }
    }

    protected void inject(T instance, Object[] facets) {
        for (int i = 0; i < facets.length; i++) {
            Class<?> mixIn = facets[i].getClass();
            Object mixInInstance = facets[i];
            for (Field field : mixIn.getDeclaredFields()) {
                if (!injectFacet(field, facets, mixInInstance, mixIn)) {
                    injectThis(instance, mixIn, mixInInstance, field);
                }
            }
        }
    }

    protected boolean injectFacet(Field field, Object[] facets, Object instance, Class<?> clazz) {
        Facet facetAnnotation = field.getAnnotation(Facet.class);
        if (facetAnnotation != null) {
            for (Object facet : facets) {
                if (field.getType().isAssignableFrom(facet.getClass())) {
                    inject(facet, field, instance, clazz);
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean injectThis(T instance, Class<?> mixIn, Object mixInInstance, Field field) {
        This thisAnnotation = field.getAnnotation(This.class);
        if (thisAnnotation != null) {
            if (field.getType().isAssignableFrom(instance.getClass())) {
                inject(instance, field, mixInInstance, mixIn);
                return true;
            }
        }
        return false;
    }
}
