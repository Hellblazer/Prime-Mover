/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Derived entity that extends BaseEntity.
 * Tests that derived classes correctly inherit base event methods
 * and add their own without ordinal conflicts.
 *
 * @author hal.hildebrand
 */
@Entity
public class DerivedEntity extends BaseEntity {
    private double derivedValue;

    /**
     * Derived entity's own event method.
     */
    public void derivedEvent(double value) {
        this.derivedValue = value;
    }

    /**
     * Override of base method - should not create duplicate ordinal.
     */
    @Override
    public void baseEvent(String value) {
        super.baseEvent(value + "_derived");
    }

    public double getDerivedValue() {
        return derivedValue;
    }
}
