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

package demo;

import static com.hellblazer.primeMover.Kronos.endSimulation;
import static com.hellblazer.primeMover.Kronos.sleep;

import com.hellblazer.primeMover.Entity;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

@Entity()
public class EventThroughput {
    /** benchmark end time. */
    private long endTime;
    /** benchmark type. */
    private final String mode;
    /** total number of events. */
    /** number of warm-up events. */
    private final int nevents;
    /** benchmark start time. */
    private long startTime;

    /**
     * Create new event throughput benchmark entity.
     * 
     * @param mode
     *            benchmark type
     * @param nevents
     *            number of benchmark events
     * @param nwarm
     *            number of warm-up events
     */
    public EventThroughput(String mode, int nevents, int nwarm) {
        this.mode = mode;
        this.nevents = nevents;

        if ("NULL".equals(mode)) {
            nullOperation();
        } else if ("INT".equals(mode)) {
            intOperation(1);
        } else if ("DOUBLE".equals(mode)) {
            doubleOperation(1);
        } else if ("STRING".equals(mode)) {
            stringOperation("prime mover");
        } else {
            throw new RuntimeException("unrecognized mode: " + mode);
        }
        System.out.println(" events: " + nevents);
        System.out.println(" warmup: " + nwarm);
    }

    /* (non-Javadoc)
     * @see testClasses.EventThroughput#doubleOperation(double)
     */
    public void doubleOperation(double d) {
        sleep(1);
        doubleOperation(d);
    }

    /* (non-Javadoc)
     * @see testClasses.EventThroughput#finish()
     */
    public void finish() {
        System.out.println("benchmark END");
        endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1000000000.0;
        System.out.println("seconds: " + duration);
        System.out.println(Math.round((nevents / duration)) + " " + mode
                           + " events/second");
        System.out.println();
        endSimulation();
    }

    /* (non-Javadoc)
     * @see testClasses.EventThroughput#intOperation(int)
     */
    public void intOperation(int i) {
        sleep(1);
        intOperation(i);
    }

    /* (non-Javadoc)
     * @see testClasses.EventThroughput#nullOperation()
     */
    public void nullOperation() {
        sleep(1);
        nullOperation();
    }

    /* (non-Javadoc)
     * @see testClasses.EventThroughput#start()
     */
    public void start() {
        System.out.println("benchmark BEGIN");
        System.gc();
        startTime = System.nanoTime();
    }

    /* (non-Javadoc)
     * @see testClasses.EventThroughput#stringOperation(java.lang.String)
     */
    public void stringOperation(String s) {
        sleep(1);
        stringOperation(s);
    }

}
