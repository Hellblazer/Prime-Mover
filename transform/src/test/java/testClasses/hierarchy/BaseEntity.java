/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Base entity class for testing inheritance transformation.
 * This is the root of the hierarchy with basic event methods.
 *
 * @author hal.hildebrand
 */
@Entity
public class BaseEntity {
    private String baseValue;
    private int baseCounter;

    /**
     * Base event method that sets a value.
     */
    public void baseEvent(String value) {
        this.baseValue = value;
        this.baseCounter++;
    }

    /**
     * Another base event with different signature.
     */
    public void anotherBaseEvent(int count) {
        this.baseCounter += count;
    }

    public String getBaseValue() {
        return baseValue;
    }

    public int getBaseCounter() {
        return baseCounter;
    }
}
