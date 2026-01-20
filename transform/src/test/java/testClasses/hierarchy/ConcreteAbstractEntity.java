/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Concrete implementation of AbstractEntity.
 * Tests that concrete classes extending abstract @Entity classes
 * correctly inherit and implement event methods.
 *
 * @author hal.hildebrand
 */
@Entity
public class ConcreteAbstractEntity extends AbstractEntity {
    private int abstractValue;
    private int requestCount;

    @Override
    public void abstractEvent(int value) {
        this.abstractValue = value;
    }

    @Override
    public void processRequest(int requestId) {
        this.requestCount++;
        abstractEvent(requestId);
    }

    /**
     * Concrete entity's own event.
     */
    public void concreteSpecificEvent(String data) {
        abstractBaseEvent(data);
    }

    public int getAbstractValue() {
        return abstractValue;
    }

    public int getRequestCount() {
        return requestCount;
    }
}
