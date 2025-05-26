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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class that mirrors TestComposite but uses the ClassFile API implementation
 * instead of the ASM implementation.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */
public class TestCompositeClassFileAPI {

    @Test
    public void testClassFileAPIAssemblyEquivalence() {
        // Compare behavior of ClassFile API vs ASM implementation
        var classFileAssembler = new ClassFileAPIComposite();
        var asmAssembler = Composite.instance();

        MixIn1Impl mixIn1_cf = new MixIn1Impl();
        MixIn2Impl mixIn2_cf = new MixIn2Impl();
        MixIn1Impl mixIn1_asm = new MixIn1Impl();
        MixIn2Impl mixIn2_asm = new MixIn2Impl();

        Composite1 cfInstance = classFileAssembler.assemble(Composite1.class,
                                                            new CompositeClassLoader(getClass().getClassLoader()),
                                                            mixIn2_cf, mixIn1_cf);

        Composite1 asmInstance = asmAssembler.assemble(Composite1.class,
                                                       new CompositeClassLoader(getClass().getClassLoader()),
                                                       mixIn2_asm, mixIn1_asm);

        // Both should behave identically
        assertEquals(asmInstance.m11(), cfInstance.m11());
        assertEquals(asmInstance.m12(), cfInstance.m12());
        assertEquals(asmInstance.m21(), cfInstance.m21());
        assertEquals(asmInstance.m22(), cfInstance.m22());
        assertEquals(asmInstance.m24(), cfInstance.m24());

        // Test method with parameters
        String testResult1 = asmInstance.m13("A", "B", "C");
        String testResult2 = cfInstance.m13("A", "B", "C");
        assertEquals(testResult1, testResult2);

        // Both should have same class structure
        assertEquals(asmInstance.getClass().getDeclaredFields().length,
                     cfInstance.getClass().getDeclaredFields().length);
        assertEquals(asmInstance.getClass().getDeclaredMethods().length,
                     cfInstance.getClass().getDeclaredMethods().length);
    }

    @Test
    public void testClassFileAPISpecific() {
        // Test that specifically validates ClassFile API implementation details
        var generator = new CompositeClassFileAPI() {
        };
        byte[] generatedBits = generator.generateClassBits(Composite1.class);
        assertNotNull(generatedBits);

        // Verify the bytecode is valid and has expected class version (Java 5 = 49)
        ClassReader reader = new ClassReader(generatedBits);
        assertEquals(49, reader.readShort(6), "Class file should use Java 5 version (49)");

        // Verify class name follows expected pattern
        String className = reader.getClassName();
        assertEquals("com/chiralbehaviors/janus/testClasses/Composite1$composite", className);

        // Verify it implements the expected interface
        String[] interfaces = reader.getInterfaces();
        assertEquals(1, interfaces.length);
        assertEquals("com/chiralbehaviors/janus/testClasses/Composite1", interfaces[0]);
    }

    @Test
    public void testConstruct() {
        var assembler = new ClassFileAPIComposite();
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
        var assembler = new ClassFileAPIComposite();
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
        var generator = new ClassFileAPIComposite();
        byte[] generatedBits = generator.generateClassBits(Composite1.class);
        assertNotNull(generatedBits);

        // Verify the generated bytecode can be analyzed with ASM tools
        TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
        CheckClassAdapter cv = new CheckClassAdapter(tcv);
        ClassReader reader = new ClassReader(generatedBits);
        reader.accept(cv, 0);
    }

    @Test
    public void testMappedConstruct() {
        var assembler = new ClassFileAPIComposite();
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
        var assembler = new ClassFileAPIComposite();
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.assemble(Composite1.class,
                                                 new CompositeClassLoader(getClass().getClassLoader()), mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(instance, instance.getComposite());
    }

    /**
     * Create a composite implementation that combines the functionality of the ClassFile API
     * generator with the assembly capabilities of the original Composite interface.
     */
    private static class ClassFileAPIComposite implements Composite {
        private final CompositeClassFileAPI generator = new CompositeClassFileAPI() {
        };

        @Override
        public byte[] generateClassBits(Class<?> composite) {
            return generator.generateClassBits(composite);
        }
    }
}
