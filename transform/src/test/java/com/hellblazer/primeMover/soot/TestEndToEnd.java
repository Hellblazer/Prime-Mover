/**
 * Copyright (C) 2010 Hal Hildebrand. All rights reserved.
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

package com.hellblazer.primeMover.soot;

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

import com.hellblazer.primeMover.TrackingController;
import com.hellblazer.primeMover.runtime.Framework;

import soot.G;
import soot.options.Options;
import testClasses.Entity1Impl;

/**
 * Test the end to end behavior of simulation transform.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class TestEndToEnd {
    private static final String TEST_CLASSES = "testClasses";
    File                        outputDir    = new File(OUTPUT_DIR, TEST_CLASSES);
    File                        processedDir = new File(PROCESSED_DIR, TEST_CLASSES);
    File                        sourceDir    = new File(SOURCE_DIR, TEST_CLASSES);

    @SuppressWarnings("deprecation")
    @Test
    public void testTransform() throws Exception {
        G.reset();
        initializeDirectory(processedDir);
        initializeDirectory(outputDir);
        copyDirectory(sourceDir, processedDir);
        String[] argv = { "-w" };

        Options.v().set_process_dir(asList(PROCESSED_DIR.getAbsolutePath()));
        Options.v().set_output_dir(OUTPUT_DIR.getAbsolutePath());
        // Options.v().setPhaseOption("cg", "verbose:true");
        // Options.v().set_verbose(true);
        SimulationTransform.setStandardClassPath();
        SimulationTransform.main(argv);

        ClassLoader loader = new LocalLoader(getTransformed());
        TrackingController controller = new TrackingController();
        // controller.setDebugEvents(true);
        // controller.setEventLogger(Logger.getLogger("Event Logger"));
        Framework.setController(controller);
        Class<?> entity1Clazz = loader.loadClass(Entity1Impl.class.getCanonicalName()
        + EntityGenerator.GENERATED_ENTITY_SUFFIX);

        Object entity;
        entity = entity1Clazz.newInstance();
        Method event = entity1Clazz.getMethod("event1");
        controller.setCurrentTime(0);
        event.invoke(entity);

        while (controller.send()) {

        }

        assertEquals(6, controller.events.size());
        assertEquals(2, controller.blockingEvents.size());
        assertEquals(8, controller.references.size());

        int i = 0;

        assertEquals("<testClasses.Entity1Impl: void event1()>", controller.events.get(i++));
        assertEquals("<testClasses.Entity1Impl: void event2(testClasses.Entity2)>", controller.events.get(i++));
        assertEquals("<testClasses.Entity1Impl: void event1()>", controller.events.get(i++));
        assertEquals("<testClasses.Entity2Impl: void myEvent()>", controller.events.get(i++));
        assertEquals("<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>",
                     controller.events.get(i++));
        assertEquals("<testClasses.Entity1Impl: void event1()>", controller.events.get(i++));
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
}
