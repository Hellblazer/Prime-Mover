/*
 * Copyright (C) 2023 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.primeMover.asm;

import com.hellblazer.primeMover.runtime.EntityReference;

/**
 * @author hal.hildebrand
 */

public class MyTestEntity extends MyTest implements EntityReference {

    @Override
    public Object __invoke(int event, Object[] arguments) throws Throwable {
        switch (event) {
        case 0: {
            original_bar();
            return null;
        }
        case 1: {
            return original_myMy();
        }
        case 2: {
            return original_someArgs((String) arguments[0], arguments[1]);
        }
        default:
            throw new IllegalArgumentException("Unknown event: " + event);
        }
    }

    private void original_bar() {
        System.out.println("Hello");
    }

    private String original_myMy() {
        return "bar";
    }

    private String[] original_someArgs(String arg1, Object arg2) throws RuntimeException {
        return new String[] { "hello", "world" };
    }
}
