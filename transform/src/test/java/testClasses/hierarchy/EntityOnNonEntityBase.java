/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Entity class extending non-entity base.
 * Tests that transformation correctly handles inherited non-event methods
 * and only transforms this class's own methods as events.
 *
 * @author hal.hildebrand
 */
@Entity
public class EntityOnNonEntityBase extends NonEntityBase {
    private int entityValue;

    /**
     * This becomes an event method.
     */
    public void entityEvent(int value) {
        this.entityValue = value;
        // Call inherited regular method - should not be transformed
        regularMethod("entity:" + value);
    }

    /**
     * Another entity event.
     */
    public void processEntityData(String data) {
        // Use inherited regular method
        var processed = processString(data);
        regularMethod(processed);
    }

    public int getEntityValue() {
        return entityValue;
    }
}
