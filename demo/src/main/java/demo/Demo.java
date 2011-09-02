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

import static com.hellblazer.primeMover.Kronos.sleep;

import java.util.Map;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.SimulationException;
import com.hellblazer.primeMover.controllers.SimulationController;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class Demo {

    public static void channel() throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);
        new UseChannel().test();
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : "
                               + spectrumEntry.getKey());
        }
    }

    public static void eventContinuationThroughput() throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);

        new ContinuationThroughputImpl("STRING", 10000000, 10).go();
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : "
                               + spectrumEntry.getKey());
        }
    }

    public static void eventThroughput() throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);
        EventThroughput benchmark = new EventThroughput("STRING", 10000000, 100);
        sleep(10);
        benchmark.start();
        sleep(10000000 + 1);
        benchmark.finish();
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : "
                               + spectrumEntry.getKey());
        }
    }

    public static void main(String[] argv) throws Exception {
        try {
            threaded();
            System.out.println();
            System.out.println();
            channel();
            System.out.println();
            System.out.println();
            eventContinuationThroughput();
            System.out.println();
            System.out.println();
            eventThroughput();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void threaded() throws SimulationException {
        SimulationController controller = new SimulationController();
        Kronos.setController(controller);
        controller.setCurrentTime(0);
        Threaded threaded = new Threaded();
        threaded.process(1);
        threaded.process(2);
        threaded.process(3);
        controller.eventLoop();
        System.out.println("Event spectrum:");
        for (Map.Entry<String, Integer> spectrumEntry : controller.getSpectrum().entrySet()) {
            System.out.println("\t" + spectrumEntry.getValue() + "\t\t : "
                               + spectrumEntry.getKey());
        }
    }
}
