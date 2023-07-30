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

package com.hellblazer.primeMover.runtime;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

/**
 * Represents the blocking continuation of blocking simulated event processing
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class Continuation implements Serializable {
    private static final Logger logger           = Logger.getLogger(Continuation.class.getCanonicalName());
    private static final long   serialVersionUID = -4307033871239385970L;

    private volatile Throwable exception;
    private volatile Object    returnValue;
    private volatile Thread    thread;

    public boolean isParked() {
        return thread != null;
    }

    public Object park(CompletableFuture<Object> futureSailor) throws Throwable {
        assert thread == null;
        thread = Thread.currentThread();
        futureSailor.complete(null);
        LockSupport.park();
        if (exception != null) {
            throw exception;
        }
        return returnValue;
    }

    public void resume() {
        if (thread != null && thread.isAlive()) {
            logger.finer("Resuming: %s".formatted(thread));
            LockSupport.unpark(thread);
        }
    }

    public Object returnFrom() throws Throwable {
        if (exception != null) {
            throw exception;
        }
        return returnValue;
    }

    public void setReturnState(Object returnValue, Throwable exception) {
        this.returnValue = returnValue;
        this.exception = exception;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
