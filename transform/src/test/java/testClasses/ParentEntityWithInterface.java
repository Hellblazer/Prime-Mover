/**
 * Copyright (C) 2008-2025 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 */
package testClasses;

import com.hellblazer.primeMover.annotations.Entity;

/**
 * Parent entity that implements an interface.
 * Tests that child entities correctly inherit the interface method events.
 */
@Entity(ParentInterface.class)
public class ParentEntityWithInterface implements ParentInterface {
    private String lastArg;

    @Override
    public void parentMethod(String arg) {
        this.lastArg = arg;
    }

    public String getLastArg() {
        return lastArg;
    }
}
