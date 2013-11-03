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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import junit.framework.TestCase;
import soot.G;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import com.hellblazer.primeMover.runtime.ContinuationFrame;
import com.hellblazer.primeMover.runtime.Framework;

/**
 * Test the transformation which implements the event continuations.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class TestContinuationTransformer extends TestCase {
    private static class MyMockController extends MockController {
        boolean saveFrame = false;
        boolean restoreFrame = false;
        ContinuationFrame frame;

        @Override
        public ContinuationFrame popFrame() {
            ContinuationFrame f = frame;
            frame = null;
            return f;
        }

        @Override
        public void pushFrame(ContinuationFrame frame) {
            this.frame = frame;
        }

        @Override
        public boolean restoreFrame() {
            return restoreFrame;
        }

        @Override
        public boolean saveFrame() {
            return saveFrame;
        }
    }

    public void testContinuations() throws Exception {
        G.reset();
        SimulationTransform.setStandardClassPath();
        SootClass continuationPrototype = getContinuationPrototypeImpl();

        PrintStream out;
        out = System.out;
        out = new PrintStream(new ByteArrayOutputStream());
        PrintWriter pw = new PrintWriter(out);
        // pw.println("Untransformed: ");
        // Printer.v().printTo(continuationPrototype, pw);
        pw.flush();

        ArrayList<SootClass> generated = new ArrayList<SootClass>();
        for (SootMethod method : continuationPrototype.getMethods()) {
            ContinuationTransformer transformer = new ContinuationTransformer(
                                                                              generated,
                                                                              true);
            transformer.transform(method.retrieveActiveBody());
        }

        pw.println();
        pw.println("Transformed: ");
        Printer.v().printTo(continuationPrototype, pw);
        pw.flush();

        generated.add(continuationPrototype);
        Scene.v().loadNecessaryClasses();
        generated.addAll(Scene.v().getApplicationClasses());
        LocalLoader loader = new LocalLoader(generated);
        Thread.currentThread().setContextClassLoader(loader);
        /*
        verifyGenerated(loader,
                        new ByteArrayInputStream(
                                                 loader.classBits.get(ContinuationPrototypeImpl.class.getCanonicalName())));
                                                 */
        Class<?> clazz = loader.loadClass(ContinuationPrototypeImpl.class.getCanonicalName());
        assertNotSame(ContinuationPrototypeImpl.class, clazz);

        ContinuationPrototype prototype = (ContinuationPrototype) clazz.newInstance();

        // Test transformed behavior with no controller present 
        Framework.setController(null);
        assertEquals(2, prototype.zeroth());
        assertEquals("AB", prototype.first());
        assertEquals("B", prototype.second());
        assertEquals("CD", prototype.third("C", "D"));
        assertEquals("ABBB", prototype.nested());

        // simple save of frame
        MyMockController controller = new MyMockController();
        controller.saveFrame = true;
        Framework.setController(controller);
        assertNotNull(Framework.queryController());
        assertNull(prototype.first());
        assertNotNull(controller.frame);
        Class<? extends ContinuationFrame> frameClass = controller.frame.getClass();
        assertEquals(1, frameClass.getDeclaredFields().length);
        Field stringBuilder = frameClass.getDeclaredField("l_0");
        StringBuilder builder = (StringBuilder) stringBuilder.get(controller.frame);
        assertNotNull(builder);

        // continue
        controller.saveFrame = false;
        controller.restoreFrame = true;
        assertEquals("AB", prototype.first());
        assertNull(controller.frame);

        // test for saved argument state
        controller.saveFrame = true;
        controller.restoreFrame = false;
        controller.frame = null;
        assertNull(prototype.arguments(" ", "time ", "around"));
        assertNotNull(controller.frame);
        frameClass = controller.frame.getClass();
        assertEquals(4, frameClass.getDeclaredFields().length);
        // continue
        controller.saveFrame = false;
        controller.restoreFrame = true;
        String result = prototype.arguments("!", "past ", "forgotten");
        assertNotNull(result);
        assertEquals("first time around", result);
    }

    private SootClass getContinuationPrototypeImpl() {
        SootClass continuationPrototypeImpl = Scene.v().loadClassAndSupport(ContinuationPrototypeImpl.class.getCanonicalName());
        for (SootMethod method : continuationPrototypeImpl.getMethods()) {
            method.retrieveActiveBody();
        }
        return continuationPrototypeImpl;
    }
}
