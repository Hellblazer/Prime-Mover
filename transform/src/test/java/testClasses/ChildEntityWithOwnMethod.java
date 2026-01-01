/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Child entity that extends a parent with interfaces and adds its own method.
 * This tests the critical scenario where inherited interface methods must be
 * properly indexed alongside the child's own methods.
 *
 * Bug scenario: Without proper inherited interface handling, the child's
 * event indices would mismatch with the parent's wrapper methods, causing
 * ClassCastExceptions when the parent's wrapper posts an event that gets
 * dispatched by the child's __invoke method.
 */
@Entity
public class ChildEntityWithOwnMethod extends ParentEntityWithInterface {
    private int lastNumber;

    /**
     * Child's own method with different parameter type.
     * Must not conflict with inherited parentMethod(String).
     */
    public void childMethod(Integer number) {
        this.lastNumber = number;
    }

    public int getLastNumber() {
        return lastNumber;
    }
}
