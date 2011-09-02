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

package testClasses;

import static com.hellblazer.primeMover.Kronos.sleep;

import com.hellblazer.primeMover.Entity;

/*
 * A trampoline for tests which lives in the transformed class space of the
 * simulation
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */

@Entity(Driver.class)
public class DriverImpl implements Driver {

    /* (non-Javadoc)
     * @see testClasses.Driver#runContinuationBenchmark(java.lang.String, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public void runContinuationBenchmark(String mode, Integer nevents,
                                         Integer nwarm) {
        ContinuationThroughput benchmark = new ContinuationThroughputImpl(
                                                                          mode,
                                                                          nevents,
                                                                          nwarm);
        benchmark.go();
    }

    /* (non-Javadoc)
     * @see testClasses.Driver#runEventBenchmark(java.lang.String, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public void runEventBenchmark(String mode, Integer nevents, Integer nwarm) {
        EventThroughputImpl benchmark = new EventThroughputImpl(mode, nevents,
                                                                nwarm);
        sleep(nwarm);
        benchmark.start();
        sleep(nevents + 1);
        benchmark.finish();
    }

    /* (non-Javadoc)
     * @see testClasses.Driver#runThreaded()
     */
    @Override
    public void runThreaded() {
        Threaded threaded = new ThreadedImpl();
        threaded.process(1);
        threaded.process(2);
        threaded.process(3);
    }
}
