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

import java.io.PrintWriter;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.chiralbehaviors.janus.CompositeAssembler.CompositeClassLoader;
import com.chiralbehaviors.janus.testClasses.Composite1;
import com.chiralbehaviors.janus.testClasses.MixIn1;
import com.chiralbehaviors.janus.testClasses.MixIn2;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class TestCompositeClassGenerator {
    @Test
    public void testGeneratedBits() {
        CompositeClassGenerator generator = new CompositeClassGenerator(Composite1.class);
        byte[] generatedBits = generator.generateClassBits();
        assertNotNull(generatedBits);
        TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
        CheckClassAdapter cv = new CheckClassAdapter(tcv);
        ClassReader reader = new ClassReader(generatedBits);
        reader.accept(cv, 0);
    }

    @Test
    public void testGeneratedClass() {
        CompositeClassGenerator generator = new CompositeClassGenerator(Composite1.class);
        CompositeClassLoader loader = new CompositeClassLoader(Composite1.class.getClassLoader());
        Class<?> generated = loader.define(generator.getGeneratedClassName(), generator.generateClassBits());
        assertNotNull(generated);
    }

    @Test
    public void testInitialization() {
        CompositeClassGenerator generator = new CompositeClassGenerator(Composite1.class);
        Map<Class<?>, Integer> mixInMap = generator.getMixInTypeMapping();
        assertEquals(2, mixInMap.size());
        assertEquals(0, mixInMap.get(MixIn1.class));
        assertEquals(1, mixInMap.get(MixIn2.class));
    }
}
