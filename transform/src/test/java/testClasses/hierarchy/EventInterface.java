/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

/**
 * Interface for event methods used in entity testing.
 *
 * @author hal.hildebrand
 */
public interface EventInterface {
    /**
     * Interface method that will become an event.
     */
    void interfaceEvent(String message);

    /**
     * Another interface event method.
     */
    void processRequest(int requestId);
}
