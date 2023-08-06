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

import static com.hellblazer.primeMover.Kronos.currentTime;
import static com.hellblazer.primeMover.Kronos.sleep;

import com.hellblazer.primeMover.annotations.Blocking;
import com.hellblazer.primeMover.annotations.Entity;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

@Entity
public class ContinuationThroughput {
    private static final byte[] B = new byte[] {};
    /** number of continuation events. */
    protected final int         limit;
    /** benchmark type. */
    protected final String      mode;

    /**
     * Create new continuation event benchmarking entity.
     * 
     * @param mode  benchmark type
     * @param limit number of continuation events
     */
    public ContinuationThroughput(String mode, int limit) {
        this.mode = mode;
        this.limit = limit;
        System.out.println("   type: " + mode);
        System.out.println(" events: " + limit);
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.ContinuationThroughput#go()
     */
    public void go() {
        System.out.println("benchmark BEGIN");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < limit; i++) {
            sleep(1);
            call();
        }
        System.out.println("benchmark END");
        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;
        System.out.println("seconds: " + duration);
        System.out.println(Math.round((limit / duration)) + " " + mode + " continuation events/second");
        System.out.println();
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.ContinuationThroughput#operation_array(byte[])
     */
    @Blocking
    public byte[] operation_array(byte[] b) {
        sleep(1);
        return b;
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.ContinuationThroughput#operation_double(double)
     */
    @Blocking
    public void operation_double(double d) {
        sleep(1);
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.ContinuationThroughput#operation_int(int)
     */
    @Blocking
    public void operation_int(int i) {
        sleep(1);
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.ContinuationThroughput#operation_null()
     */
    @Blocking
    public void operation_null() {
        sleep(1);
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.ContinuationThroughput#operation_show()
     */
    @Blocking
    public void operation_show() {
        System.out.println("operation_show at t=" + currentTime());
        sleep(1);
        // throw new RuntimeException("hi");
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.ContinuationThroughput#operation_string(java.lang.String)
     */
    @Blocking
    public void operation_string(String s) {
        sleep(1);
    }

    /**
     * Perform single continuation call.
     */
    protected int call() {
        if (mode.equals("NULL")) {
            operation_null();
        } else if (mode.equals("INT")) {
            operation_int(1);
        } else if (mode.equals("DOUBLE")) {
            operation_double(1.0);
        } else if (mode.equals("STRING")) {
            operation_string("foo");
        } else if (mode.equals("ARRAY")) {
            operation_array(B);
        } else if (mode.equals("SHOW")) {
            try {
                operation_show();
            } catch (RuntimeException e) {
                System.out.println("caught exception: " + e);
            }
        } else {
            throw new RuntimeException("unrecognized benchmark: " + mode);
        }
        return 1;
    }

}
