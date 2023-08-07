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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.PrintWriter;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.chiralbehaviors.janus.Composite.CompositeClassLoader;
import com.chiralbehaviors.janus.testClasses.Composite1;
import com.chiralbehaviors.janus.testClasses.MixIn1;
import com.chiralbehaviors.janus.testClasses.MixIn1Impl;
import com.chiralbehaviors.janus.testClasses.MixIn2;
import com.chiralbehaviors.janus.testClasses.MixIn2Impl;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class TestComposite {

    @Test
    public void testConstruct() {
        var assembler = Composite.instance();
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.assemble(Composite1.class,
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
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.assemble(Composite1.class,
                                                 new CompositeClassLoader(getClass().getClassLoader()), mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(mixIn2, instance.getFriend1());
        assertSame(mixIn1, instance.getFriend2());
    }

    @Test
    public void testGeneratedBits() {
        var generator = Composite.instance();
        byte[] generatedBits = generator.generateClassBits(Composite1.class);
        assertNotNull(generatedBits);
        TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
        CheckClassAdapter cv = new CheckClassAdapter(tcv);
        ClassReader reader = new ClassReader(generatedBits);
        reader.accept(cv, 0);
    }

    @Test
    public void testMappedConstruct() {
        var assembler = Composite.instance();
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        var parameters = new HashMap<Class<?>, Object>();
        parameters.put(MixIn1.class, mixIn1);
        parameters.put(MixIn2.class, mixIn2);
        Composite1 instance = assembler.assemble(Composite1.class,
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
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.assemble(Composite1.class,
                                                 new CompositeClassLoader(getClass().getClassLoader()), mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(instance, instance.getComposite());
    }
}
