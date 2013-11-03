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

/**
 * Represents the blocking continuation of blocking simulated event processing
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class Continuation implements Serializable {
    private static final long serialVersionUID = -4307033871239385970L;
    private EventImpl         caller;
    private Throwable         exception;
    private ContinuationFrame frame;

    private Object            returnValue;

    public Continuation(EventImpl caller) {
        this.caller = caller;
    }

    public Continuation(EventImpl caller, ContinuationFrame frame) {
        this.caller = caller;
        this.frame = frame;
    }

    public EventImpl getCaller() {
        return caller;
    }

    public ContinuationFrame getFrame() {
        return frame;
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
