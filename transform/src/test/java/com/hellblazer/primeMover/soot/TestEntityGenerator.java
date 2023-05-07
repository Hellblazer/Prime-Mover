/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.soot;

import static com.hellblazer.primeMover.soot.util.Utils.getEntityInterfaces;
import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Framework;

import junit.framework.TestCase;
import soot.G;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import testClasses.Entity2Impl;

/**
 * Test the generation of the proxies which implement the actual mechanics of
 * the event simulation framework.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class TestEntityGenerator extends TestCase {

    private static class MyMockController extends MockController {
        int     event1Ordinal;
        boolean event1Posted = false;
        int     event2Ordinal;
        boolean event2Posted = false;

        @Override
        public Object postContinuingEvent(EntityReference entity, int event, Object... arguments) throws Throwable {
            assertNotNull(arguments);
            assertEquals(1, arguments.length);
            event2Ordinal = event;
            event2Posted = true;
            return null;
        }

        @Override
        public void postEvent(EntityReference entity, int event, Object... arguments) {
            assertNull(arguments);
            event1Ordinal = event;
            event1Posted = true;
        }

    }

    ArrayList<SootClass> generated;
    EntityGenerator      generator;

    public void testCreateClass() {
        assertNotNull(generator.getEntity());
        assertNotNull(generator.getEntity().getFieldByName(EntityGenerator.INITIALIZED_FIELD));
        assertNotNull(generator.getEntity().getFieldByName(EntityGenerator.CONTROLLER_FIELD));
    }

    public void testGenerateClassInitMethod() {
        generator.constructInvokeMap();
        SootMethod init = generator.generateClassInitMethod();
        assertNotNull(init);
    }

    public void testGenerateConstructors() {
        SootMethod initialize = generator.generateInitializeMethod();
        initialize.setDeclaringClass(generator.getBase());
        assertNotNull(initialize);
        List<SootMethod> constructors = generator.generateConstructors(initialize);
        assertNotNull(constructors);
        assertTrue(constructors.size() > 0);
    }

    public void testGenerateEntity() throws Throwable {
        generator.generateEntity();
        assertNotNull(generator.getEntity());
        assertSame(generator.getEntity(), Scene.v().getSootClass(generator.getEntity().toString()));

        PrintStream out;
        out = System.out;
        out = new PrintStream(new ByteArrayOutputStream());
        PrintWriter pw = new PrintWriter(out);
        Printer.v().printTo(generator.getEntity(), pw);
        pw.flush();

        Scene.v().loadNecessaryClasses();

        ClassLoader entityLoader = new LocalLoader(asList(generator.getEntity()));

        Class<?> entityClass = entityLoader.loadClass(generator.getEntity().toString());
        assertNotNull(entityClass);

        MyMockController controller = new MyMockController();
        Framework.setController(controller);

        EntityThroughSuperclass entity = (EntityThroughSuperclass) entityClass.newInstance();
        EntityReference ref = (EntityReference) entity;

        entity.event1();
        assertTrue(controller.event1Posted);
        entity.event2(null);
        assertTrue(controller.event2Posted);

        ref.__invoke(controller.event1Ordinal, new Object[0]);
        assertTrue(entity.invoke1);
        assertNull(entity.field2);
        assertFalse(entity.invoke2);

        ref.__invoke(controller.event2Ordinal, new Object[] { new Entity2Impl() });
        assertTrue(entity.invoke2);

        try {
            ref.__invoke(2, new Object[0]);
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
            // expected
        }
    }

    public void testGenerateEvents() {
        SootMethod event1 = generator.getBase().getSuperclass().getMethod("void event1()");
        SootMethod event2 = generator.getBase().getSuperclass().getMethod("void event2(testClasses.Entity2)");
        generator.constructInvokeMap();
        SootMethod initialize = generator.generateInitializeMethod();
        assertNotNull(initialize);
        initialize.setDeclaringClass(generator.getBase());
        SootMethod generatedEvent1 = generator.generateEvent(event1, initialize);
        assertNotNull(generatedEvent1);
        SootMethod generatedEvent2 = generator.generateEvent(event2, initialize);
        assertNotNull(generatedEvent2);

    }

    public void testGenerateInitializeMethod() {
        SootMethod initialize = generator.generateInitializeMethod();
        assertNotNull(initialize);
    }

    public void testGenerateInvokeMethod() {
        generator.constructInvokeMap();
        SootMethod invoke = generator.generateInvokeMethod();
        assertNotNull(invoke);

    }

    public void testGetEntityInterfaces() {
        Collection<SootClass> interfaces = getEntityInterfaces(generator.getBase());
        assertNotNull(interfaces);
        assertEquals(3, interfaces.size());
        assertTrue(interfaces.contains(Scene.v()
                                            .loadClass(CompositeInterface.class.getCanonicalName(),
                                                       SootClass.SIGNATURES)));
        assertTrue(interfaces.contains(Scene.v().loadClass(Interface1.class.getCanonicalName(), SootClass.SIGNATURES)));
        assertTrue(interfaces.contains(Scene.v().loadClass(Interface2.class.getCanonicalName(), SootClass.SIGNATURES)));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        G.reset();
        SimulationTransform.setStandardClassPath();
        Options.v().set_validate(true);
        Scene.v().loadBasicClasses();
        generated = new ArrayList<SootClass>();
        generator = new EntityGenerator(generated,
                                        Scene.v().loadClassAndSupport(EntityThroughSuperclass.class.getCanonicalName()),
                                        true);
    }

}
