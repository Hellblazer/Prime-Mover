/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Abstract entity base class.
 * Tests that abstract @Entity classes transform correctly and
 * concrete subclasses can use inherited abstract event methods.
 *
 * @author hal.hildebrand
 */
@Entity(EventInterface.class)
public abstract class AbstractEntity implements EventInterface {
    private String lastMessage;

    /**
     * Concrete event method in abstract class.
     */
    public void abstractBaseEvent(String message) {
        this.lastMessage = message;
    }

    /**
     * Abstract method that subclasses must implement.
     * Should become an event in concrete subclasses.
     */
    public abstract void abstractEvent(int value);

    @Override
    public void interfaceEvent(String message) {
        this.lastMessage = "interface:" + message;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
