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

package com.hellblazer.primeMover.api;

/**
 * Represents a type-safe reference to a simulation entity. EntityReference is a marker interface
 * implemented by generated proxy classes that enable event-based method dispatch for entities
 * annotated with {@link com.hellblazer.primeMover.annotations.Entity @Entity}.
 *
 * <p><b>Generated Code:</b> When a class is marked with {@code @Entity}, the Prime Mover bytecode
 * transformer generates a corresponding EntityReference implementation. This generated class:
 * <ul>
 *   <li>Implements this interface along with the original entity's public interface</li>
 *   <li>Delegates method calls through the simulation event queue</li>
 *   <li>Maps each method to an ordinal for efficient dispatch</li>
 * </ul>
 *
 * <p><b>Usage:</b> Entity references are obtained by casting entity instances:
 * <pre>{@code
 * @Entity
 * public class Server {
 *     public void processRequest(int id) { ... }
 * }
 *
 * Server server = new Server();
 * EntityReference ref = (EntityReference) server;
 * // Method calls on server are now scheduled as events
 * server.processRequest(42); // Becomes an event in the simulation
 * }</pre>
 *
 * <p><b>Thread Safety:</b> EntityReference implementations are thread-safe. Multiple threads
 * can schedule events through the same reference concurrently.
 *
 * <p><b>Internal Interface:</b> Methods prefixed with {@code __} are internal to the framework
 * and should not be called directly by application code. They are public only for technical
 * reasons related to bytecode generation.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * @see com.hellblazer.primeMover.annotations.Entity
 * @see Controller#postEvent(EntityReference, int, Object...)
 */
public interface EntityReference {

    /**
     * Invokes the method corresponding to the given event ordinal on the underlying entity.
     * This method is part of the internal framework infrastructure and should not be called
     * directly by application code.
     *
     * <p><b>Internal Use Only:</b> This method is invoked by the simulation framework when
     * processing events from the event queue. Application code should call entity methods
     * normally, which the transformed bytecode will route through the event system.
     *
     * <p><b>Ordinal Mapping:</b> Each public method in an {@code @Entity} class is assigned
     * a unique ordinal (positive integer) during bytecode transformation. This ordinal serves
     * as an efficient method identifier for event dispatch.
     *
     * @param event the ordinal identifying which method to invoke (must be positive)
     * @param arguments the arguments to pass to the method (may be empty but not null)
     * @return the return value from the invoked method (may be null)
     * @throws Throwable if the invoked method throws an exception
     * @throws IllegalArgumentException if the event ordinal is invalid
     */
    Object __invoke(int event, Object[] arguments) throws Throwable;

    /**
     * Returns the method signature corresponding to the given event ordinal. This is used
     * for debugging, logging, and introspection purposes.
     *
     * <p><b>Internal Use Only:</b> This method is part of the framework infrastructure.
     * It is called by the simulation controller when creating event metadata.
     *
     * <p>The returned signature typically includes the class name, method name, and parameter
     * types in a format like {@code "com.example.Server.processRequest(int, String)"}.
     *
     * @param event the ordinal of the event (must be positive)
     * @return the string signature of the method, or null if the ordinal is invalid
     */
    String __signatureFor(int event);
}
