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

package com.hellblazer.primeMover.runtime;

/**
 * Implementation of the blocking sleep.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
class BlockingSleep implements EntityReference {
    final static BlockingSleep BLOCKING_SLEEP_INSTANCE = new BlockingSleep();

    final static int SLEEP_EVENT = 0;

    @Override
    public void __bindTo(Devi controller) {
        // no op
    }

    @Override
    public Object __invoke(int event, Object[] arguments) throws Throwable {
        if (event != SLEEP_EVENT) {
            throw new NoSuchMethodError();
        }
        sleep((Long) arguments[0]);
        return null;
    }

    @Override
    public String __signatureFor(int event) {
        return "<com.hellblazer.primeMover.runtime.BlockingSleepImpl void sleep(org.joda.time.Duration)>";
    }

    public void sleep(long duration) {
        Kairos.sleep(duration);
    }
}
