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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.chiralbehaviors.janus.testClasses.Composite1;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class TestCompositeClassGenerator {
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
}
