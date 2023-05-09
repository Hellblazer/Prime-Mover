/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover;

import static com.hellblazer.primeMover.soot.Util.OUTPUT_DIR;
import static com.hellblazer.primeMover.soot.Util.PROCESSED_DIR;
import static com.hellblazer.primeMover.soot.Util.SOURCE_DIR;
import static com.hellblazer.utils.Utils.copyDirectory;
import static com.hellblazer.utils.Utils.getBits;
import static com.hellblazer.utils.Utils.initializeDirectory;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.runtime.Framework;
import com.hellblazer.primeMover.soot.EntityGenerator;
import com.hellblazer.primeMover.soot.LocalLoader;
import com.hellblazer.primeMover.soot.SimulationTransform;

import soot.G;
import soot.options.Options;
import testClasses.DriverImpl;
import testClasses.UseChannelImpl;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class TestAPI {
    private static final String TEST_CLASSES = "testClasses";
    File                        outputDir    = new File(OUTPUT_DIR, TEST_CLASSES);
    File                        processedDir = new File(PROCESSED_DIR, TEST_CLASSES);
    File                        sourceDir    = new File(SOURCE_DIR, TEST_CLASSES);

    @Test
    public void testApi() throws Exception {
        G.reset();
        initializeDirectory(processedDir);
        initializeDirectory(outputDir);
        copyDirectory(sourceDir, processedDir);
        String[] argv = { "-w" };

        Options.v().set_process_dir(asList(PROCESSED_DIR.getAbsolutePath()));
        Options.v().set_output_dir(OUTPUT_DIR.getAbsolutePath());
        Options.v().setPhaseOption("cg", "verbose:true");
        Options.v().set_verbose(true);
        SimulationTransform.setStandardClassPath();
        SimulationTransform.main(argv);

        ClassLoader loader = new LocalLoader(getTransformed());
        testChannel(loader);
        testThreading(loader);
    }

    private Map<String, byte[]> getTransformed() throws IOException {
        HashMap<String, byte[]> classBits = new HashMap<String, byte[]>();
        File[] listing = outputDir.listFiles();
        assertNotNull(listing);
        for (File classFile : listing) {
            String name = classFile.getName();
            if (name.endsWith(".class")) {
                String className = TEST_CLASSES + '.' + name.substring(0, name.lastIndexOf('.'));
                classBits.put(className, getBits(classFile));
            }
        }
        return classBits;
    }

    private void testChannel(ClassLoader loader) throws Exception {
        TrackingController controller = new TrackingController();
        Framework.setController(controller);
        controller.setCurrentTime(0);
        Class<?> useChannelImplClass = loader.loadClass(UseChannelImpl.class.getCanonicalName()
        + EntityGenerator.GENERATED_ENTITY_SUFFIX);
        @SuppressWarnings("deprecation")
        Object useChannel = useChannelImplClass.newInstance();
        Method test = useChannelImplClass.getDeclaredMethod("test");
        test.invoke(useChannel);

        while (controller.send()) {

        }

        assertEquals(5, controller.events.size());
        assertEquals("<testClasses.UseChannelImpl: void test()>", controller.events.get(0));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(1));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(2));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(3));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(4));

        assertEquals(8, controller.blockingEvents.size());
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(0));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(1));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(2));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(3));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(4));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(5));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(6));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(7));

        assertEquals(13, controller.references.size());

    }

    private void testThreading(ClassLoader loader) throws Exception {
        TrackingController controller = new TrackingController();
        Framework.setController(controller);
        controller.setCurrentTime(0);

        Class<?> driverImplClass = loader.loadClass(DriverImpl.class.getCanonicalName()
        + EntityGenerator.GENERATED_ENTITY_SUFFIX);
        @SuppressWarnings("deprecation")
        Object driverImpl = driverImplClass.newInstance();
        Method runThreaded = driverImplClass.getDeclaredMethod("runThreaded");
        runThreaded.invoke(driverImpl);

        while (controller.send()) {

        }

        assertEquals(4, controller.events.size());
        assertEquals("<testClasses.DriverImpl: void runThreaded()>", controller.events.get(0));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(1));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(2));
        assertEquals("<testClasses.ThreadedImpl: void process(int)>", controller.events.get(3));

        assertEquals(30, controller.blockingEvents.size());
        for (String contEvent : controller.blockingEvents) {
            assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                         contEvent);
        }

        assertEquals(34, controller.references.size());
    }
}
