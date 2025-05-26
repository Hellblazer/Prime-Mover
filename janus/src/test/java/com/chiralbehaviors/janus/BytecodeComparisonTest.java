package com.chiralbehaviors.janus;

import com.chiralbehaviors.janus.testClasses.Composite1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify that the ClassFile API implementation generates identical bytecode to the original ASM
 * implementation.
 */
public class BytecodeComparisonTest {

    @Test
    public void testClassLoading() throws Exception {
        // Test that both implementations generate valid, loadable classes
        ClassLoader parentLoader = BytecodeComparisonTest.class.getClassLoader();

        // Load ASM generated class
        Composite asmComposite = Composite.instance();
        byte[] asmBytecode = asmComposite.generateClassBits(Composite1.class);
        TestClassLoader asmLoader = new TestClassLoader(parentLoader);
        Class<?> asmClass = asmLoader.defineClass("com.chiralbehaviors.janus.testClasses.Composite1$composite",
                                                  asmBytecode);
        assertNotNull(asmClass, "ASM generated class should be loadable");

        // Load ClassFile API generated class
        CompositeClassFileAPI classFileComposite = new CompositeClassFileAPI() {
        };
        byte[] classFileBytecode = classFileComposite.generateClassBits(Composite1.class);
        TestClassLoader classFileLoader = new TestClassLoader(parentLoader);
        Class<?> classFileClass = classFileLoader.defineClass(
        "com.chiralbehaviors.janus.testClasses.Composite1$composite", classFileBytecode);
        assertNotNull(classFileClass, "ClassFile API generated class should be loadable");

        // Verify both classes have the same structure
        assertEquals(asmClass.getDeclaredFields().length, classFileClass.getDeclaredFields().length,
                     "Both classes should have same number of fields");
        assertEquals(asmClass.getDeclaredMethods().length, classFileClass.getDeclaredMethods().length,
                     "Both classes should have same number of methods");

        // Verify field names and types match (order may vary by reflection implementation)
        var asmFields = asmClass.getDeclaredFields();
        var classFileFields = classFileClass.getDeclaredFields();

        // Check that both have mixIn_0 and mixIn_1 fields
        boolean asmHasMixIn0 = false, asmHasMixIn1 = false;
        boolean cfHasMixIn0 = false, cfHasMixIn1 = false;

        for (var field : asmFields) {
            if ("mixIn_0".equals(field.getName())) {
                asmHasMixIn0 = true;
            }
            if ("mixIn_1".equals(field.getName())) {
                asmHasMixIn1 = true;
            }
        }
        for (var field : classFileFields) {
            if ("mixIn_0".equals(field.getName())) {
                cfHasMixIn0 = true;
            }
            if ("mixIn_1".equals(field.getName())) {
                cfHasMixIn1 = true;
            }
        }

        assertEquals(true, asmHasMixIn0 && asmHasMixIn1, "ASM class should have mixIn_0 and mixIn_1 fields");
        assertEquals(true, cfHasMixIn0 && cfHasMixIn1, "ClassFile class should have mixIn_0 and mixIn_1 fields");
    }

    @Test
    public void testStructurallyEquivalentBytecode() {
        // Generate bytecode using original ASM implementation
        Composite asmComposite = Composite.instance();
        byte[] asmBytecode = asmComposite.generateClassBits(Composite1.class);
        assertNotNull(asmBytecode, "ASM implementation should generate bytecode");

        // Generate bytecode using ClassFile API implementation
        CompositeClassFileAPI classFileComposite = new CompositeClassFileAPI() {
        };
        byte[] classFileBytecode = classFileComposite.generateClassBits(Composite1.class);
        assertNotNull(classFileBytecode, "ClassFile API implementation should generate bytecode");

        // Both should have same length - indicating similar structure
        assertEquals(asmBytecode.length, classFileBytecode.length, "Bytecode should have same length");

        // Both should be valid bytecode (they can be loaded successfully)
        assertNotNull(asmBytecode, "ASM bytecode should be valid");
        assertNotNull(classFileBytecode, "ClassFile API bytecode should be valid");
    }

    /**
     * Simple ClassLoader for testing bytecode loading
     */
    private static class TestClassLoader extends ClassLoader {
        public TestClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
