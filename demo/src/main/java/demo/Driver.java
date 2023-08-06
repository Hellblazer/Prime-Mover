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

import static com.hellblazer.primeMover.Kronos.sleep;

import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.annotations.Entity;

/*
 * A trampoline for tests which lives in the transformed class space of the
 * simulation
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */

@Entity
public class Driver {

    public void runContinuationBenchmark(String mode, Integer nevents, Integer nwarm) {
        ContinuationThroughput benchmark = new ContinuationThroughput(mode, nevents);
        benchmark.go();
    }

    public void runEventBenchmark(String mode, Integer nevents, Integer nwarm) {
        EventThroughput benchmark = new EventThroughput(mode, nevents);
        sleep(nwarm);
        benchmark.start();
        sleep(nevents + 1);
        benchmark.finish();
    }

    public void runThreaded() {
        Threaded threaded = new Threaded();
        System.out.println("Begin: 1 at: " + Kronos.currentTime());
        threaded.process(1);
        System.out.println("Begin: 2 at: " + Kronos.currentTime());
        threaded.process(2);
        System.out.println("Begin: 3 at: " + Kronos.currentTime());
        threaded.process(3);
        System.out.println("Finish at: " + Kronos.currentTime());
    }
}
