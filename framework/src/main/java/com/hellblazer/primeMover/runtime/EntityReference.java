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

/**
 * Represents a reference to a simulation Entity
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public interface EntityReference {
    /**
     * Bind the entity reference to a unique controller
     * 
     * @param controller
     */
    void __bindTo(Devi controller);

    /**
     * Invoke the event on the entity
     * 
     * @param event
     * @param arguments
     * @return
     */
    Object __invoke(int event, Object[] arguments) throws Throwable;

    /**
     * Answer the signature matching the event ordinal
     * 
     * @param event
     *            - the ordinal of the event > 0
     * @return the string signature of the event, or null for none
     */
    String __signatureFor(int event);
}
