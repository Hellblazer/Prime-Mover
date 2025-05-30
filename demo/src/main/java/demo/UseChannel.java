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

package demo;

import com.hellblazer.primeMover.annotations.Entity;
import com.hellblazer.primeMover.api.Kronos;
import com.hellblazer.primeMover.api.SynchronousQueue;

/**
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

@Entity()
public class UseChannel {
    protected final SynchronousQueue<String> channel = Kronos.createChannel(String.class);

    /*
     * (non-Javadoc)
     *
     * @see testClasses.UseChannel#put()
     */
    public void put() {
        System.out.println(Kronos.currentTime() + ": put called");
        channel.put("foo");
        System.out.println(Kronos.currentTime() + ": put continues");
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.UseChannel#take()
     */
    public void take() {
        System.out.println(Kronos.currentTime() + ": take called");
        Object o = channel.take();
        System.out.println(Kronos.currentTime() + ": take continues with object: " + o);
    }

    /*
     * (non-Javadoc)
     *
     * @see testClasses.UseChannel#test()
     */
    public void test() {
        take();
        Kronos.sleep(60000);
        put();
        Kronos.sleep(60000);
        put();
        Kronos.sleep(60000);
        take();
    }

}
