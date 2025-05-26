/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.classfile;

import com.hellblazer.primeMover.TrackingController;
import com.hellblazer.primeMover.runtime.Kairos;
import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import testClasses.LocalLoader;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */

public class TestAPI {

    @Test
    public void testApi() throws Exception {
        var loader = new LocalLoader(getTransformed());
        testThreading(loader);
        testChannel(loader);
    }

    @SuppressWarnings("unused")
    private void dump(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        TraceClassVisitor visitor = new TraceClassVisitor(null, new PrintWriter(System.out, true));
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        CheckClassAdapter.verify(reader, true, new PrintWriter(System.out, true));
    }

    private Map<String, byte[]> getTransformed() throws Exception {
        try (var transform = new SimulationTransform(
        new ClassGraph().acceptPackages("com.hellblazer.*", "testClasses"))) {
            return transform.transformed(SimulationTransform.EXCLUDE_TRANSFORMED_FILTER).entrySet().stream().collect(
            Collectors.toMap(e -> e.getKey().getName().replace('.', '/'), e -> {
                //                                    dump(e.getValue());
                return e.getValue();
            }));
        }
    }

    private void testChannel(ClassLoader loader) throws Exception {
        TrackingController controller = new TrackingController();
        Kairos.setController(controller);
        controller.setCurrentTime(0);
        Class<?> useChannelImplClass = loader.loadClass("testClasses.UseChannelImpl");
        Object useChannel = useChannelImplClass.getConstructor().newInstance();
        Method test = useChannelImplClass.getDeclaredMethod("test");
        test.invoke(useChannel);

        while (controller.send()) {

        }

        assertEquals(13, controller.events.size(),
                     String.format("events: %s", controller.events.stream().map(s -> "\n" + s).toList()));
        var i = 0;
        assertEquals("<testClasses.UseChannelImpl: void test()>", controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(i++));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(i++));

        assertEquals(4, controller.blockingEvents.size());
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(0));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(1));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(2));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(3));

        assertEquals(17, controller.references.size());

    }

    private void testThreading(ClassLoader loader) throws Exception {
        var controller = new TrackingController();
        Kairos.setController(controller);
        controller.setCurrentTime(0);

        var driverImplClass = loader.loadClass("testClasses.DriverImpl");
        var driverImpl = driverImplClass.getConstructor().newInstance();
        var runThreaded = driverImplClass.getDeclaredMethod("runThreaded");
        runThreaded.invoke(driverImpl);

        while (controller.send())
            ;

        assertEquals(34, controller.events.size(),
                     String.format("events: %s", controller.events.stream().map(s -> '\n' + s).toList()));
        var i = 0;
        assertEquals("<testClasses.DriverImpl: void runThreaded()>", controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));

        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(i++));

        assertEquals(15, controller.blockingEvents.size());
        for (String contEvent : controller.blockingEvents) {
            assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                         contEvent);
        }

        assertEquals(49, controller.references.size());
    }
}
