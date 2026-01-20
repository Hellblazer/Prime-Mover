/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Concrete entity at the bottom of a three-level hierarchy.
 * Tests deep inheritance chains with multiple levels of @Entity classes.
 *
 * @author hal.hildebrand
 */
@Entity
public class ConcreteEntity extends DerivedEntity {
    private boolean concreteFlag;

    /**
     * Concrete entity's own event method.
     */
    public void concreteEvent(boolean flag) {
        this.concreteFlag = flag;
    }

    /**
     * Another concrete method with complex parameter type.
     */
    public void processData(String key, int value, double factor) {
        baseEvent(key);
        anotherBaseEvent(value);
        derivedEvent(factor);
    }

    public boolean isConcreteFlag() {
        return concreteFlag;
    }
}
