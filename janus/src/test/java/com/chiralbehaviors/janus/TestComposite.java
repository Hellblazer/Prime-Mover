/**
 * (C) Copyright 2008 Chiral Behaviors, LLC. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.chiralbehaviors.janus;

import com.chiralbehaviors.janus.Composite.CompositeClassLoader;
import com.chiralbehaviors.janus.testClasses.*;
import org.junit.jupiter.api.Test;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class TestComposite {

    @Test
    public void testConstruct() {
        var assembler = Composite.instance();
        var mixIn1 = new MixIn1Impl();
        var mixIn2 = new MixIn2Impl();

        var instance = assembler.assemble(Composite1.class,
                                          new CompositeClassLoader(getClass().getClassLoader()), mixIn2, mixIn1);
        assertNotNull(instance);
        assertEquals("MixIn1-Method1", instance.m11());
        assertEquals("MixIn1-Method2", instance.m12());
        assertEquals("Hello", instance.m13("Goodbye", "Hello", "Not here at the moment"));
        assertEquals("MixIn2-Method1", instance.m21());
        assertEquals("MixIn2-Method2", instance.m22());
        instance.m23("Hello");
        assertEquals("Hello", MixIn2Impl.RESULT);
        assertEquals(0, instance.m24());
    }

    @Test
    public void testFacets() {
        var assembler = Composite.instance();
        var mixIn1 = new MixIn1Impl();
        var mixIn2 = new MixIn2Impl();

        var instance = assembler.assemble(Composite1.class,
                                          new CompositeClassLoader(getClass().getClassLoader()), mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(mixIn2, instance.getFriend1());
        assertSame(mixIn1, instance.getFriend2());
    }

    @Test
    public void testGeneratedBits() {
        var generator = Composite.instance();
        var generatedBits = generator.generateClassBits(Composite1.class);
        assertNotNull(generatedBits);

        // Validate bytecode using ClassFile API (parsing validates structure)
        ClassModel classModel = ClassFile.of().parse(generatedBits);
        assertNotNull(classModel);
        assertTrue(classModel.methods().size() > 0, "Generated class should have methods");

        // Print class info for debugging
        System.out.println("Generated class: " + classModel.thisClass().asInternalName());
        System.out.println("Methods:");
        for (MethodModel method : classModel.methods()) {
            System.out.println("  " + method.methodName().stringValue() + method.methodType().stringValue());
        }
    }

    @Test
    public void testMappedConstruct() {
        var assembler = Composite.instance();
        var mixIn1 = new MixIn1Impl();
        var mixIn2 = new MixIn2Impl();

        var parameters = new HashMap<Class<?>, Object>();
        parameters.put(MixIn1.class, mixIn1);
        parameters.put(MixIn2.class, mixIn2);
        var instance = assembler.assemble(Composite1.class,
                                          new CompositeClassLoader(getClass().getClassLoader()), parameters);
        assertNotNull(instance);
        assertEquals("MixIn1-Method1", instance.m11());
        assertEquals("MixIn1-Method2", instance.m12());
        assertEquals("Hello", instance.m13("Goodbye", "Hello", "Not here at the moment"));
        assertEquals("MixIn2-Method1", instance.m21());
        assertEquals("MixIn2-Method2", instance.m22());
        instance.m23("Hello");
        assertEquals("Hello", MixIn2Impl.RESULT);
        assertEquals(0, instance.m24());
    }

    @Test
    public void testThis() {
        var assembler = Composite.instance();
        var mixIn1 = new MixIn1Impl();
        var mixIn2 = new MixIn2Impl();

        var instance = assembler.assemble(Composite1.class,
                                          new CompositeClassLoader(getClass().getClassLoader()), mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(instance, instance.getComposite());
    }
}
