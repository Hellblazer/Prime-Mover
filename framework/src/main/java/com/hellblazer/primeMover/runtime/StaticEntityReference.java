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

import com.hellblazer.primeMover.api.EntityReference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The implementation of an event which is implemented by a static method call.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public final class StaticEntityReference implements EntityReference {
    private final Method method;

    public StaticEntityReference(Method method) {
        this.method = method;
    }

    @Override
    public Object __invoke(int event, Object[] arguments) throws Throwable {
        try {
            return method.invoke(null, arguments);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @Override
    public String __signatureFor(int event) {
        return method.toString();
    }
}
