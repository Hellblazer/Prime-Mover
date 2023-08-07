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

import org.junit.jupiter.api.Test;

import com.chiralbehaviors.janus.testClasses.Composite1;
import com.chiralbehaviors.janus.testClasses.MixIn1Impl;
import com.chiralbehaviors.janus.testClasses.MixIn2Impl;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class TestCompositeAssembler {

    @Test
    public void testConstruct() {
        CompositeAssembler<Composite1> assembler = new CompositeAssembler<Composite1>(Composite1.class);
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.construct(mixIn2, mixIn1);
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
        CompositeAssembler<Composite1> assembler = new CompositeAssembler<Composite1>(Composite1.class);
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.construct(mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(mixIn2, instance.getFriend1());
        assertSame(mixIn1, instance.getFriend2());
    }

    @Test
    public void testThis() {
        CompositeAssembler<Composite1> assembler = new CompositeAssembler<Composite1>(Composite1.class);
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.construct(mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(instance, instance.getComposite());
    }
}
