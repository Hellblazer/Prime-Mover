/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses.hierarchy;

/**
 * Non-entity base class.
 * Tests that entity transformation works when only some classes
 * in the hierarchy are annotated with @Entity.
 *
 * @author hal.hildebrand
 */
public class NonEntityBase {
    private String utilityValue;

    /**
     * Regular method - not an event since this class is not @Entity.
     */
    public void regularMethod(String value) {
        this.utilityValue = value;
    }

    /**
     * Another regular method.
     */
    public String processString(String input) {
        return input.toUpperCase();
    }

    public String getUtilityValue() {
        return utilityValue;
    }
}
