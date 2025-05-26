/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
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

package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hellblazer.primeMover.api.Kronos;

import testClasses.DriverImpl;
import testClasses.UseChannelImpl;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class TestAPI {

    @Test
    public void testChannel() throws Exception {
        TrackingController controller = new TrackingController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);

        new UseChannelImpl().test();

        while (controller.send()) {

        }

        assertEquals(5, controller.events.size());
        assertEquals("<testClasses.UseChannelImpl: void test()>", controller.events.get(0));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(1));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(2));
        assertEquals("<testClasses.UseChannelImpl: void put(java.lang.String)>", controller.events.get(3));
        assertEquals("<testClasses.UseChannelImpl: void take()>", controller.events.get(4));

        assertEquals(4, controller.blockingEvents.size());
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(0));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(1));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: void put(java.lang.Object)>",
                     controller.blockingEvents.get(2));
        assertEquals("<com.hellblazer.primeMover.runtime.SynchronousQueueImpl: java.lang.Object take()>",
                     controller.blockingEvents.get(3));

        assertEquals(9, controller.references.size());

    }

    public void testEventContinuationThroughput() throws Exception {
        Demo.eventThroughput();
    }

    public void testEventThroughput() throws Exception {
        Demo.eventContinuationThroughput();
    }

    public void testThreading() throws Exception {
        TrackingController controller = new TrackingController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);

        new DriverImpl().runThreaded();

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
